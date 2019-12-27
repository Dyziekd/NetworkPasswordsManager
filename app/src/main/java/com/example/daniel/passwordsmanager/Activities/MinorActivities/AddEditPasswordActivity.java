package com.example.daniel.passwordsmanager.Activities.MinorActivities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.daniel.passwordsmanager.Activities.MajorActivities.MainActivity;
import com.example.daniel.passwordsmanager.PublicFunctions;
import com.example.daniel.passwordsmanager.R;
import com.example.daniel.passwordsmanager.Strings;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddEditPasswordActivity extends AppCompatActivity
{
    // resources
    private CheckBox passwordReminderCheckbox;
    private EditText serviceNameField, serviceUrlField, passwordField, passwordLifetimeField;
    private TextView passwordPowerBarLabel;
    private ProgressBar passwordPowerBar;
    private RequestQueue requestQueue;
    private SharedPreferences userDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_password);

        // init shared preferences
        userDetails = getApplicationContext().getSharedPreferences(Strings.preferencesFile, Context.MODE_PRIVATE);

        // init resources
        initResources();
    }

    // cancel all requests on stop
    @Override
    protected void onStop()
    {
        super.onStop();
        if(requestQueue != null)
            requestQueue.cancelAll(Strings.PASSWORDS_REQUEST_TAG);
    }

    // reset logout timer on user interaction
    @Override
    public void onUserInteraction()
    {
        super.onUserInteraction();

        if(MainActivity.logoutTimer != null)
            MainActivity.logoutTimer.reset();
    }

    // initiates resources
    private void initResources()
    {
        // init input fields
        serviceNameField = (EditText)findViewById(R.id.add_edit_password__service_name_field);
        serviceUrlField = (EditText)findViewById(R.id.add_edit_password__service_url_field);
        passwordField = (EditText)findViewById(R.id.add_edit_password__password_field);
        passwordLifetimeField = (EditText)findViewById(R.id.add_edit_password__password_lifetime_field);

        // init password power bar
        passwordPowerBar = (ProgressBar)findViewById(R.id.add_edit_password__password_power_bar);

        // init password power bar labels
        passwordPowerBarLabel = (TextView)findViewById(R.id.add_edit_password__password_power_bar_label);

        // init checkbox
        passwordReminderCheckbox = (CheckBox) findViewById(R.id.add_edit_password__password_reminder_checkbox);

        // change password power bar when typing password
        passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if(TextUtils.isEmpty(passwordField.getText().toString()))   // set default label if password field is empty
                    passwordPowerBarLabel.setText(R.string.password_power_bar_label);
                else
                {
                    int passwordPower = PublicFunctions.checkPasswordPower(passwordField.getText().toString());
                    PublicFunctions.setPasswordPowerBarProgress(passwordPowerBar, passwordPowerBarLabel, passwordPower);
                }
            }
        });

        // enable/disable password lifetime field when password reminder checkbox is checked/unchecked
        passwordReminderCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                    passwordLifetimeField.setEnabled(true);
                else if(!isChecked)
                {
                    passwordLifetimeField.setEnabled(false);
                    passwordLifetimeField.setText(null);
                }
            }
        });

        // init request queue
        requestQueue = Volley.newRequestQueue(this);

        // check if password is being edited
        boolean isEditMode = getIntent().getExtras().getBoolean("editMode");

        // edit UI if password is being edited
        if(isEditMode)
        {
            // show edit button and hide add button
            Button addButton = (Button)findViewById(R.id.add_edit_password__add_button);
            Button editButton = (Button)findViewById(R.id.add_edit_password__edit_button);
            addButton.setVisibility(View.GONE);
            editButton.setVisibility(View.VISIBLE);

            // set title
            setTitle(getString(R.string.edit_password_activity_title));

            // fill fields
            fillFields();
        }
    }

    // fills all fields (edit mode)
    private void fillFields()
    {
        // get intent data
        Bundle receivedData = getIntent().getExtras();

        // get password data
        String serviceName = receivedData.getString("serviceName");
        String serviceUrl = receivedData.getString("serviceUrl");
        String password = receivedData.getString("password");
        boolean changeReminder = receivedData.getBoolean("changeReminder");
        Integer passwordLifetime = receivedData.getInt("passwordLifetime");

        // set fields texts
        serviceNameField.setText(serviceName);
        serviceUrlField.setText(serviceUrl);
        passwordField.setText(password);
        passwordReminderCheckbox.setChecked(changeReminder);
        if(changeReminder)
            passwordLifetimeField.setText(String.valueOf(passwordLifetime));
        else
            passwordLifetimeField.setText(null);
    }

    // validates form
    private boolean validateForm()
    {
        boolean isCorrect = true;

        // get inputs
        String serviceName = serviceNameField.getText().toString();
        String url = serviceUrlField.getText().toString();
        String password = passwordField.getText().toString();

        // validate service name
        if(TextUtils.isEmpty(serviceName))
        {
            serviceNameField.setError(getString(R.string.empty_field_error));
            isCorrect = false;
        }

        // validate url if url field is not empty
        if(!TextUtils.isEmpty(url))
            if(!Patterns.WEB_URL.matcher(url).matches())
            {
                serviceUrlField.setError(getString(R.string.incorrect_url_error));
                isCorrect = false;
            }

        // validate password
        if(TextUtils.isEmpty(password))
        {
            passwordField.setError(getString(R.string.empty_field_error));
            isCorrect = false;
        }

        // validate password reminder time if password reminder checkbox is checked
        if(passwordReminderCheckbox.isChecked())
        {
            String passwordLifetime = passwordLifetimeField.getText().toString();

            if(!validatePasswordLifetime(passwordLifetime))
                isCorrect = false;
        }

        return isCorrect;
    }

    // validates password reminder lifetime field
    private boolean validatePasswordLifetime(String passwordLifetime)
    {
        if (TextUtils.isEmpty(passwordLifetime))  // check if field is not empty
        {
            passwordLifetimeField.setError(getString(R.string.empty_field_error));
            return false;
        }
        else if(!TextUtils.isDigitsOnly(passwordLifetime))    // check if field contains only numbers
        {
            passwordLifetimeField.setError(getString(R.string.no_numbers_error));
            return false;
        }
        else if(Integer.valueOf(passwordLifetime) <= 0)   // check if field value is more than 0
        {
            passwordLifetimeField.setError(getString(R.string.incorrect_auto_logout_time_error));
            return false;
        }

        return true;
    }



                                                        // ON BUTTONS CLICK

    // tries to add password to database
    public void addPassword(View view)
    {
        if(validateForm())
        {
            // show loading indicator
            final ProgressBar loadingIndicator = (ProgressBar)findViewById(R.id.add_edit_password__loading_indicator);
            loadingIndicator.setVisibility(View.VISIBLE);

            // create request
            StringRequest request = new StringRequest(Request.Method.POST, Strings.passwordsURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response)
                        {
                            try
                            {
                                // get json response
                                JSONObject jsonResponse = new JSONObject(response);

                                // show message
                                String message = jsonResponse.getString("message");
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                                // finish this activity if password was added successful
                                int success = jsonResponse.getInt("success");
                                if(success == 1)
                                    finish();
                            }
                            catch (JSONException e)
                            {
                                Toast.makeText(getApplicationContext(), R.string.add_password_json_extract_error, Toast.LENGTH_LONG).show();
                            }

                            // hide loading indicator
                            loadingIndicator.setVisibility(View.GONE);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            loadingIndicator.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                        }
                    })
            {
                @Override   // add params to the request
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();

                    // prepare values for password encryption
                    String password = passwordField.getText().toString();
                    String keyName = "sl" + String.valueOf(userDetails.getInt("currentGroupSL", 0)) + "cipherKey";
                    String key = userDetails.getString(keyName, "defKey");
                    String initVector = userDetails.getString("login", "defIv");

                    // get inputs
                    String function = Strings.ADD_REQUEST_FUNCTION;
                    password = PublicFunctions.aesEncrypt(password, key, initVector);
                    String serviceName = serviceNameField.getText().toString();
                    String serviceUrl = serviceUrlField.getText().toString();
                    String changeReminder, passwordLifetime, expirationDate;
                    if(passwordReminderCheckbox.isChecked())
                    {
                        changeReminder = "1";
                        passwordLifetime = passwordLifetimeField.getText().toString();
                        expirationDate = PublicFunctions.calculateExpirationDate(Integer.valueOf(passwordLifetime));
                    }
                    else
                    {
                        changeReminder = "0";
                        passwordLifetime = "";
                        expirationDate = "";
                    }
                    String idOwner = String.valueOf(userDetails.getInt("idUser", 0));
                    String idPasswordsGroup = String.valueOf(getIntent().getExtras().getInt("idPasswordsGroup"));

                    // put params
                    params.put("function", function);
                    params.put("password", password);
                    params.put("service_name", serviceName);
                    params.put("service_url_address", serviceUrl);
                    params.put("change_reminder", changeReminder);
                    params.put("password_lifetime", passwordLifetime);
                    params.put("expiration_date", expirationDate);
                    params.put("id_passwords_group", idPasswordsGroup);
                    params.put("id_owner", idOwner);

                    return params;
                }
            };
            request.setTag(Strings.GROUPS_REQUEST_TAG);

            // send request to the server
            requestQueue.add(request);
        }
    }

    // tries to edit an existing password in database
    public void editPassword(View view)
    {
        if(validateForm())
        {
            // show loading indicator
            final ProgressBar loadingIndicator = (ProgressBar)findViewById(R.id.add_edit_password__loading_indicator);
            loadingIndicator.setVisibility(View.VISIBLE);

            // create request
            StringRequest request = new StringRequest(Request.Method.POST, Strings.passwordsURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response)
                        {
                            try
                            {
                                // get json response
                                JSONObject jsonResponse = new JSONObject(response);

                                // show message
                                String message = jsonResponse.getString("message");
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                                // finish this activity if password was edited successful
                                int success = jsonResponse.getInt("success");
                                if(success == 1)
                                    finish();
                            }
                            catch (JSONException e)
                            {
                                Toast.makeText(getApplicationContext(), R.string.edit_password_json_extract_error, Toast.LENGTH_LONG).show();
                            }

                            // hide loading indicator
                            loadingIndicator.setVisibility(View.GONE);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            loadingIndicator.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                        }
                    })
            {
                @Override   // add params to the request
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();

                    // prepare values for password encryption
                    String password = passwordField.getText().toString();
                    String keyName = "sl" + String.valueOf(userDetails.getInt("currentGroupSL", 0)) + "cipherKey";
                    String key = userDetails.getString(keyName, "defKey");
                    String initVector = userDetails.getString("login", "defIv");

                    // get inputs
                    String function = Strings.EDIT_REQUEST_FUNCTION;
                    String idPassword = String.valueOf(getIntent().getExtras().getInt("idPassword"));
                    password = PublicFunctions.aesEncrypt(password, key, initVector);
                    String serviceName = serviceNameField.getText().toString();
                    String serviceUrl = serviceUrlField.getText().toString();
                    String changeReminder, passwordLifetime, expirationDate;
                    if(passwordReminderCheckbox.isChecked())
                    {
                        changeReminder = "1";
                        passwordLifetime = passwordLifetimeField.getText().toString();
                        expirationDate = PublicFunctions.calculateExpirationDate(Integer.valueOf(passwordLifetime));
                    }
                    else
                    {
                        changeReminder = "0";
                        passwordLifetime = "";
                        expirationDate = "";
                    }
                    String idOwner = String.valueOf(userDetails.getInt("idUser", 0));
                    String idPasswordsGroup = String.valueOf(getIntent().getExtras().getInt("idPasswordsGroup"));

                    // put params
                    params.put("function", function);
                    params.put("id_password", idPassword);
                    params.put("password", password);
                    params.put("service_name", serviceName);
                    params.put("service_url_address", serviceUrl);
                    params.put("change_reminder", changeReminder);
                    params.put("password_lifetime", passwordLifetime);
                    params.put("expiration_date", expirationDate);
                    params.put("id_passwords_group", idPasswordsGroup);
                    params.put("id_owner", idOwner);

                    return params;
                }
            };
            request.setTag(Strings.GROUPS_REQUEST_TAG);

            // send request to the server
            requestQueue.add(request);
        }
    }

    // finishes activity without any result
    public void cancel(View view)
    {
        finish();
    }

}
