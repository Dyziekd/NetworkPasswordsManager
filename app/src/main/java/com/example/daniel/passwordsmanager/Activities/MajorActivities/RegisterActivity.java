package com.example.daniel.passwordsmanager.Activities.MajorActivities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
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
import com.example.daniel.passwordsmanager.PublicFunctions;
import com.example.daniel.passwordsmanager.R;
import com.example.daniel.passwordsmanager.Strings;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity
{
    // resources
    private EditText loginField, emailField, passwordField, confirmPasswordField, sl1passwordField, sl2passwordField;
    private TextView passwordPowerBarLabel, sl1passwordPowerBarLabel, sl2passwordPowerBarLabel;
    private ProgressBar passwordPowerBar, sl1passwordPowerBar, sl2passwordPowerBar;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // init resources
        initResources();
    }

    // cancel all requests on stop
    @Override
    protected void onStop()
    {
        super.onStop();
        if(requestQueue != null)
            requestQueue.cancelAll(Strings.REGISTER_REQUEST_TAG);
    }

    // initiates resources
    private void initResources()
    {
        // init password power bars labels
        passwordPowerBarLabel = (TextView)findViewById(R.id.register__password_power_bar_label);
        sl1passwordPowerBarLabel = (TextView)findViewById(R.id.register__SL1password_power_bar_label);
        sl2passwordPowerBarLabel = (TextView)findViewById(R.id.register__SL2password_power_bar_label);

        // init input fields
        loginField = (EditText)findViewById(R.id.generator__password_field);
        emailField = (EditText)findViewById(R.id.register__email_field);
        passwordField = (EditText)findViewById(R.id.register__password_field);
        confirmPasswordField = (EditText) findViewById(R.id.register__confirm_password_field);
        sl1passwordField = (EditText)findViewById(R.id.register__SL1password_field);
        sl2passwordField = (EditText) findViewById(R.id.register__SL2password_field);

        // init password power bars
        passwordPowerBar = (ProgressBar)findViewById(R.id.register__password_power_bar);
        sl1passwordPowerBar = (ProgressBar)findViewById(R.id.register__SL1password_power_bar);
        sl2passwordPowerBar = (ProgressBar)findViewById(R.id.register__SL2password_power_bar);

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

        // change SL1 password power bar when typing SL1 password
        sl1passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if(TextUtils.isEmpty(sl1passwordField.getText().toString()))   // set default label if password field is empty
                    sl1passwordPowerBarLabel.setText(R.string.sl1_password_power_bar_label);
                else
                {
                    int passwordPower = PublicFunctions.checkPasswordPower(sl1passwordField.getText().toString());
                    PublicFunctions.setPasswordPowerBarProgress(sl1passwordPowerBar, sl1passwordPowerBarLabel, passwordPower);
                }
            }
        });

        // change SL2 password power bar when typing SL2 password
        sl2passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if(TextUtils.isEmpty(sl2passwordField.getText().toString()))   // set default label if password field is empty
                    sl2passwordPowerBarLabel.setText(R.string.sl2_password_power_bar_label);
                else
                {
                    int passwordPower = PublicFunctions.checkPasswordPower(sl2passwordField.getText().toString());
                    PublicFunctions.setPasswordPowerBarProgress(sl2passwordPowerBar, sl2passwordPowerBarLabel, passwordPower);
                }
            }
        });

        // init request queue
        requestQueue = Volley.newRequestQueue(this);
    }

    // validates register form inputs
    private boolean validateForm()
    {
        boolean isCorrect = true;

        // get inputs
        String login = loginField.getText().toString();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();
        String sl1password = sl1passwordField.getText().toString();
        String sl2password = sl2passwordField.getText().toString();

        // validate login
        if(TextUtils.isEmpty(login))
        {
            loginField.setError(getString(R.string.empty_field_error));
            isCorrect = false;
        }

        // validate email
        if(TextUtils.isEmpty(email))
        {
            emailField.setError(getString(R.string.empty_field_error));
            isCorrect = false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            emailField.setError(getString(R.string.incorrect_email_error));
            isCorrect = false;
        }

        // validate password
        if(TextUtils.isEmpty(password))
        {
            passwordField.setError(getString(R.string.empty_field_error));
            isCorrect = false;
        }

        // validate confirm password
        if(TextUtils.isEmpty(confirmPassword))
        {
            confirmPasswordField.setError(getString(R.string.empty_field_error));
            isCorrect = false;
        }

        // validate sl1password
        if(TextUtils.isEmpty(sl1password))
        {
            sl1passwordField.setError(getString(R.string.empty_field_error));
            isCorrect = false;
        }

        // validate sl2password
        if(TextUtils.isEmpty(sl2password))
        {
            sl2passwordField.setError(getString(R.string.empty_field_error));
            isCorrect = false;
        }

        // check if password is the same as confirmed password
        if(!password.equals(confirmPassword))
        {
            confirmPasswordField.setError(getString(R.string.incorrect_confirm_password_error));
            isCorrect = false;
        }

        return isCorrect;
    }

                                            // ON BUTTONS CLICK

    // attempts register on button click
    public void attemptRegister(View view)
    {
        if(validateForm())
        {
            // create and show loading dialog
            final ProgressDialog registerDialog =  new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
            registerDialog.setCancelable(false);
            registerDialog.setMessage(getString(R.string.register_dialog_message));
            registerDialog.show();

            // create request
            StringRequest request = new StringRequest(Request.Method.POST, Strings.registerURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try
                            {
                                // get json response
                                JSONObject jsonResponse = new JSONObject(response);

                                // show message
                                String message = jsonResponse.getString("message");
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                                // finish this activity if registration was successful
                                int success = jsonResponse.getInt("success");
                                if(success == 1)
                                    finish();
                            }
                            catch (JSONException e)
                            {
                                Toast.makeText(getApplicationContext(), R.string.register_json_extract_error, Toast.LENGTH_LONG).show();
                            }

                            // hide loading dialog
                            registerDialog.hide();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            registerDialog.hide();
                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                        }
                    })
                    {
                        @Override   // add params to the request
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();

                            // get inputs
                            String login = loginField.getText().toString();
                            String email = emailField.getText().toString();
                            String password = passwordField.getText().toString();
                            String sl1password = sl1passwordField.getText().toString();
                            String sl2password = sl2passwordField.getText().toString();

                            // put params
                            params.put("login", login);
                            params.put("email", email);
                            params.put("password", PublicFunctions.sha512(password, login));
                            params.put("level1_password", PublicFunctions.sha512(sl1password, login));
                            params.put("level2_password", PublicFunctions.sha512(sl2password, login));

                            return params;
                        }
                    };
            request.setTag(Strings.REGISTER_REQUEST_TAG);

            // send request to the server
            requestQueue.add(request);
        }
    }

    // starts LoginActivity on hyperlink click
    public void startLoginActivity(View view)
    {
        finish();
    }

}
