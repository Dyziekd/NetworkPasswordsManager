package com.example.daniel.passwordsmanager.Activities.MinorActivities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class SecurityLevelActivity extends AppCompatActivity
{
    private EditText sl1passwordField, sl2passwordField;
    private RequestQueue requestQueue;
    private SharedPreferences userDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_level);

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
            requestQueue.cancelAll(Strings.CHANGE_SL_REQUEST_TAG);
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
        // init buttons
        Button logoutToSL0Button = (Button)findViewById(R.id.changeSL_logout_to_SL0_button);
        Button loginToSL1Button = (Button)findViewById(R.id.changeSL_login_to_SL1_button);
        Button loginToSL2Button = (Button)findViewById(R.id.changeSL_login_to_SL2_button);

        // init input fields
        sl1passwordField = (EditText)findViewById(R.id.changeSL__SL1_password_field);
        sl2passwordField = (EditText)findViewById(R.id.changeSL__SL2_password_field);

        // hide some views depends on current secuity level
        int currentSL = userDetails.getInt("currentSL", 0);
        switch(currentSL)
        {
            case 0: // hide logout to security level 0 button
                logoutToSL0Button.setVisibility(View.GONE);
                break;

            case 1: // hide login to security level 1 button and field for this password
                loginToSL1Button.setVisibility(View.GONE);
                break;

            case 2: // hide login to security level 1 and 2 buttons and fields for these passwords
                loginToSL1Button.setVisibility(View.GONE);
                loginToSL2Button.setVisibility(View.GONE);
                sl1passwordField.setVisibility(View.GONE);
                sl2passwordField.setVisibility(View.GONE);
                findViewById(R.id.changeSL__SL1_password_field_toggle).setVisibility(View.GONE);
                findViewById(R.id.changeSL__SL2_password_field_toggle).setVisibility(View.GONE);
                break;
        }

        // init request queue
        requestQueue = Volley.newRequestQueue(this);
    }

    // creates progress dialog (shown when trying to change security level)
    private ProgressDialog createSLProgressDialog()
    {
        ProgressDialog progressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.sl_dialog_message));

        return progressDialog;
    }


                                                // ON BUTTONS CLICK

    // logout to SL0
    public void logoutToSL0(View view)
    {
        // create and show loading dialog
        final ProgressDialog slProgressDialog = createSLProgressDialog();
        slProgressDialog.show();

        // create request
        StringRequest request = new StringRequest(Request.Method.POST, Strings.securityLevelURL,
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

                            // change security level if login was successful
                            int success = jsonResponse.getInt("success");
                            if(success == 1)
                            {
                                // change security level
                                SharedPreferences.Editor spEditor = userDetails.edit();
                                spEditor.putInt("currentSL", 0);
                                spEditor.apply();
                                finish();
                            }
                        }
                        catch (JSONException e)
                        {
                            Toast.makeText(getApplicationContext(), R.string.sl_json_extract_error, Toast.LENGTH_LONG).show();
                        }

                        // hide loading dialog
                        slProgressDialog.hide();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        slProgressDialog.hide();
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                })
                {
                    @Override   // add params to the request
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();

                        // get params
                        String function = Strings.LOGOUT_TO_SL0_REQUEST_FUNCTION;
                        String idUser = String.valueOf(userDetails.getInt("idUser", 0));

                        // put params
                        params.put("function", function);
                        params.put("idUser", idUser);

                        return params;
                    }
                };
        request.setTag(Strings.CHANGE_SL_REQUEST_TAG);

        // send request to the server
        requestQueue.add(request);
    }

    // logins to SL1
    public void loginToSL1(View view)
    {
        boolean isCorrect = true;
        final String sl1password = sl1passwordField.getText().toString();

        // validate SL1 password field
        if(TextUtils.isEmpty(sl1password))
        {
            sl1passwordField.setError(getString(R.string.empty_field_error));
            isCorrect = false;
        }

        // send request to the server
        if(isCorrect)
        {
            // create and show loading dialog
            final ProgressDialog slProgressDialog = createSLProgressDialog();
            slProgressDialog.show();

            // create request
            StringRequest request = new StringRequest(Request.Method.POST, Strings.securityLevelURL,
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

                                // change security level if login was successful
                                int success = jsonResponse.getInt("success");
                                if(success == 1)
                                {
                                    // change security level
                                    SharedPreferences.Editor spEditor = userDetails.edit();
                                    spEditor.putInt("currentSL", 1);
                                    spEditor.putString("sl1cipherKey", sl1password);
                                    spEditor.apply();
                                    finish();
                                }
                            }
                            catch (JSONException e)
                            {
                                Toast.makeText(getApplicationContext(), R.string.sl_json_extract_error, Toast.LENGTH_LONG).show();
                            }

                            // hide loading dialog
                            slProgressDialog.hide();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            slProgressDialog.hide();
                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                        }
                    })
                    {
                        @Override   // add params to the request
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();

                            // get params
                            String function = Strings.LOGIN_TO_SL1_REQUEST_FUNCTION;
                            String idUser = String.valueOf(userDetails.getInt("idUser", 0));
                            String sl1Passsword = sl1passwordField.getText().toString();

                            // put params
                            params.put("function", function);
                            params.put("idUser", idUser);
                            params.put("level1_password", PublicFunctions.sha512(sl1Passsword, userDetails.getString("login", "salt")));

                            return params;
                        }
                    };
            request.setTag(Strings.CHANGE_SL_REQUEST_TAG);

            // send request to the server
            requestQueue.add(request);
        }
    }

    // login to SL2
    public void loginToSL2(View view)
    {
        boolean isCorrect = true;
        final String sl1password = sl1passwordField.getText().toString();
        final String sl2password = sl2passwordField.getText().toString();

        // validate SL1 passwords fields
        if(TextUtils.isEmpty(sl1password))
        {
            sl1passwordField.setError(getString(R.string.empty_field_error));
            isCorrect = false;
        }

        // validate SL2 password field
        if(TextUtils.isEmpty(sl2password))
        {
            sl2passwordField.setError(getString(R.string.empty_field_error));
            isCorrect = false;
        }

        // send request to the server
        if(isCorrect)
        {
            // create and show loading dialog
            final ProgressDialog slProgressDialog = createSLProgressDialog();
            slProgressDialog.show();

            // create request
            StringRequest request = new StringRequest(Request.Method.POST, Strings.securityLevelURL,
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

                                // change security level if login was successful
                                int success = jsonResponse.getInt("success");
                                if(success == 1)
                                {
                                    // change security level
                                    SharedPreferences.Editor spEditor = userDetails.edit();
                                    spEditor.putInt("currentSL", 2);
                                    spEditor.putString("sl1cipherKey", sl1password);
                                    spEditor.putString("sl2cipherKey", sl2password);
                                    spEditor.apply();
                                    finish();
                                }
                            }
                            catch (JSONException e)
                            {
                                Toast.makeText(getApplicationContext(), R.string.sl_json_extract_error, Toast.LENGTH_LONG).show();
                            }

                            // hide loading dialog
                            slProgressDialog.hide();
                        }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                slProgressDialog.hide();
                                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                            }
                        })
                        {
                            @Override   // add params to the request
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();

                                // get params
                                String function = Strings.LOGIN_TO_SL2_REQUEST_FUNCTION;
                                String idUser = String.valueOf(userDetails.getInt("idUser", 0));
                                String sl1Passsword = sl1passwordField.getText().toString();
                                String sl2Passsword = sl2passwordField.getText().toString();

                                // put params
                                params.put("function", function);
                                params.put("idUser", String.valueOf(idUser));
                                params.put("level1_password", PublicFunctions.sha512(sl1Passsword, userDetails.getString("login", "salt")));
                                params.put("level2_password", PublicFunctions.sha512(sl2Passsword, userDetails.getString("login", "salt")));

                                return params;
                            }
                        };
            request.setTag(Strings.CHANGE_SL_REQUEST_TAG);

            // send request to the server
            requestQueue.add(request);
        }
    }
}
