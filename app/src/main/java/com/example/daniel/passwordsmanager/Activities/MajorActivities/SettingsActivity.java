package com.example.daniel.passwordsmanager.Activities.MajorActivities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
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
import com.example.daniel.passwordsmanager.Activities.MinorActivities.SettingsFragment;
import com.example.daniel.passwordsmanager.Strings;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity
{
    // settings modes
    private final static int CHANGE_EMAIL_MODE = 0;
    private final static int CHANGE_PASSWORD_MODE = 1;
    private final static int CHANGE_SL1_PASSWORD_MODE = 2;
    private final static int CHANGE_SL2_PASSWORD_MODE = 3;
    private final static int CHANGE_AUTO_LOGOUT_SETTINGS_MODE = 4;
    private int CURRENT_MODE;

    // resources
    private EditText newValueField, passwordField;
    private Fragment settingsFragment;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        // init resources
        initResources();
    }

    // cancel all requests on stop
    @Override
    protected void onStop()
    {
        super.onStop();
        if(requestQueue != null)
            requestQueue.cancelAll(Strings.SETTINGS_REQUEST_TAG);
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
        // create settings titles
        final String[] settings = {"Change email", "Change password", "Change security level 1 password", "Change security level 2 password", "Change auto logout settings"};

        // create adapter
        ArrayAdapter myAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, settings);

        // create settings list
        ListView settingsList = (ListView)findViewById(R.id.settings_list);
        settingsList.setAdapter(myAdapter);
        settingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // set current settings mode
                switch(position)
                {
                    case 0: // set change email settings mode
                        CURRENT_MODE = CHANGE_EMAIL_MODE;
                        break;

                    case 1: // set change password settings mode
                        CURRENT_MODE = CHANGE_PASSWORD_MODE;
                        break;

                    case 2: // set change security level 1 password settings mode
                        CURRENT_MODE = CHANGE_SL1_PASSWORD_MODE;
                        break;

                    case 3: // set change security level 2 password settings mode
                        CURRENT_MODE = CHANGE_SL2_PASSWORD_MODE;
                        break;

                    case 4: // set change auto logout settings mode
                        CURRENT_MODE = CHANGE_AUTO_LOGOUT_SETTINGS_MODE;
                        break;
                }

                // update UI
                updateFragment();
            }
        });

        // init request queue
        requestQueue = Volley.newRequestQueue(this);
    }

    // shows fragment
    private void updateFragment()
    {
        settingsFragment = new SettingsFragment(CURRENT_MODE);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.settings_fragment, settingsFragment);
        fragmentTransaction.commit();
    }

    // validates fragment form inputs
    private boolean validForm()
    {
        boolean isCorrect = true;

        // get inputs
        newValueField = (EditText)findViewById(R.id.settings_fragment__new_value_field);
        passwordField = (EditText)findViewById(R.id.settings_fragment__password_field);
        String newValue = newValueField.getText().toString();
        String password = passwordField.getText().toString();

        // valid new value field
        if(TextUtils.isEmpty(newValue))
        {
            // do not valid this field if mode is auto logout settings mode and auto logout checkbox is not checked
            if(CURRENT_MODE == CHANGE_AUTO_LOGOUT_SETTINGS_MODE && !((CheckBox)findViewById(R.id.settings_fragment__auto_logout_checkbox)).isChecked())
            {}
            else
            {
                newValueField.setError(getString(R.string.empty_field_error));
                isCorrect = false;
            }
        }
        else if(CURRENT_MODE == CHANGE_EMAIL_MODE)        // additional valid for email mode
            if(!Patterns.EMAIL_ADDRESS.matcher(newValue).matches())
            {
                newValueField.setError(getString(R.string.incorrect_email_error));
                isCorrect = false;
            }

        // valid password field
        if(TextUtils.isEmpty(password))
        {
            passwordField.setError(getString(R.string.empty_field_error));
            isCorrect = false;
        }

        return isCorrect;
    }


                                                // ON BUTTONS CLICK

    // tries to apply changes
    public void attemptApplyChanges(View view)
    {
        if(validForm())
        {
            // create and show loading dialog
            final ProgressDialog changeSettingsDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
            changeSettingsDialog.setCancelable(true);
            changeSettingsDialog.setMessage(getString(R.string.change_settings_dialog_message));
            changeSettingsDialog.show();

            // create request
            StringRequest request = new StringRequest(Request.Method.POST, Strings.settingsURL,
                    new Response.Listener<String>()
                    {
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

                                // change auto logout time if auto logout settings was sucessfully changed
                                if(CURRENT_MODE == CHANGE_AUTO_LOGOUT_SETTINGS_MODE && jsonResponse.getInt("success") == 1)
                                {
                                    // init auto logout checkbox
                                    CheckBox autoLogoutCheckbox = ((CheckBox)findViewById(R.id.settings_fragment__auto_logout_checkbox));

                                    // update login time if checkbox is checked
                                    if(autoLogoutCheckbox.isChecked())
                                    {
                                        // get new login time
                                        int newLoginTime = Integer.valueOf(newValueField.getText().toString());

                                        // save login time to shared preferences
                                        SharedPreferences userDetails = getApplicationContext().getSharedPreferences(Strings.preferencesFile, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor spEditor = userDetails.edit();
                                        spEditor.putInt("loginTime", newLoginTime);
                                        spEditor.apply();

                                        // update logout timer
                                        if(MainActivity.logoutTimer != null)
                                            MainActivity.logoutTimer.updateLogoutTimer(newLoginTime);
                                        else
                                            Toast.makeText(getApplicationContext(), R.string.enable_auto_logout_toast, Toast.LENGTH_LONG).show();
                                    }
                                    else    // disable logout timer
                                    {
                                        // remove login time from shared preferences
                                        SharedPreferences userDetails = getApplicationContext().getSharedPreferences(Strings.preferencesFile, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor spEditor = userDetails.edit();
                                        spEditor.remove("loginTime");
                                        spEditor.apply();

                                        // disable logout timer
                                        if(MainActivity.logoutTimer != null)
                                            MainActivity.logoutTimer.destroy();
                                    }
                                }
                            }
                            catch (JSONException e)
                            {
                                Toast.makeText(getApplicationContext(), R.string.change_settings_json_extract_error, Toast.LENGTH_LONG).show();
                            }

                            // hide loading dialog
                            changeSettingsDialog.hide();
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            changeSettingsDialog.hide();
                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                        }
                    })
                    {
                        @Override   // add params to the request
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();

                            // get inputs
                            SharedPreferences userDetails = getApplicationContext().getSharedPreferences(Strings.preferencesFile, Context.MODE_PRIVATE);
                            int idUser = userDetails.getInt("idUser", 0);
                            String newValue = newValueField.getText().toString();
                            String password = passwordField.getText().toString();

                            // put params
                            params.put("idUser", String.valueOf(idUser));
                            params.put("password", PublicFunctions.sha512(password, userDetails.getString("login", "salt")));

                            switch(CURRENT_MODE)
                            {
                                case CHANGE_EMAIL_MODE:
                                    params.put("function", Strings.CHANGE_EMAIL_REQUEST_FUNCTION);
                                    params.put("newValue", newValue);
                                    break;

                                case CHANGE_PASSWORD_MODE:
                                    params.put("function", Strings.CHANGE_PASSWORD_REQUEST_FUNCTION);
                                    params.put("newValue", PublicFunctions.sha512(newValue, userDetails.getString("login", "salt")));
                                    break;

                                case CHANGE_SL1_PASSWORD_MODE:
                                    params.put("function", Strings.CHANGE_SL1_PASSWORD_REQUEST_FUNCTION);
                                    params.put("newValue", PublicFunctions.sha512(newValue, userDetails.getString("login", "salt")));
                                    break;

                                case CHANGE_SL2_PASSWORD_MODE:
                                    params.put("function", Strings.CHANGE_SL2_PASSWORD_REQUEST_FUNCTION);
                                    params.put("newValue", PublicFunctions.sha512(newValue, userDetails.getString("login", "salt")));
                                    break;

                                case CHANGE_AUTO_LOGOUT_SETTINGS_MODE:
                                    params.put("function", Strings.CHANGE_AUTO_LOGOUT_SETTINGS_REQUEST_FUNCTION);
                                    params.put("newValue", newValue);
                                    break;
                            }

                            return params;
                        }
                    };
            request.setTag(Strings.SETTINGS_REQUEST_TAG);

            // send request to the server
            requestQueue.add(request);
        }
    }

    // hides fragment
    public void hideFragment(View view)
    {
        if(settingsFragment.isAdded())
        {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(settingsFragment);
            fragmentTransaction.commit();
        }
    }
}
