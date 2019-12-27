package com.example.daniel.passwordsmanager.Activities.MajorActivities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity
{
    // resources
    private EditText loginField, passwordField;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // set activity name
        setTitle(getString(R.string.login_activity_title));

        // init resources
        initResources();

        // checks if internet connection is active
        PublicFunctions.checkInternetConnection(LoginActivity.this);
    }

    // cancel all requests on stop
    @Override
    protected void onStop()
    {
        super.onStop();
        if(requestQueue != null)
            requestQueue.cancelAll(Strings.LOGIN_REQUEST_TAG);
    }

    // initiates resources
    private void initResources()
    {
        // init input fields
        loginField = (EditText)findViewById(R.id.login__login_field);
        passwordField = (EditText)findViewById(R.id.login__password_field);

        // init request queue
        requestQueue = Volley.newRequestQueue(this);
    }

    // validates login form inputs
    private boolean validateForm()
    {
        boolean isCorrect = true;

        // get inputs
        String login = loginField.getText().toString();
        String password = passwordField.getText().toString();

        // validate login
        if(TextUtils.isEmpty(login))
        {
            loginField.setError(getString(R.string.empty_field_error));
            isCorrect = false;
        }

        // validate password
        if(TextUtils.isEmpty(password))
        {
            passwordField.setError(getString(R.string.empty_field_error));
            isCorrect = false;
        }

        return isCorrect;
    }



                                                            // ON BUTTONS CLICK

    // attempts login on button click
    public void attemptLogin(View view)
    {
        if(validateForm())
        {
            // create and show loading dialog
            final ProgressDialog loginDialog =  new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
            loginDialog.setCancelable(false);
            loginDialog.setMessage(getString(R.string.login_dialog_message));
            loginDialog.show();

            // create request
            StringRequest request = new StringRequest(Request.Method.POST, Strings.loginURL,
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

                                // sign in if login was successful
                                int success = jsonResponse.getInt("success");
                                if(success == 1)
                                {
                                    // get user details
                                    int idUser = jsonResponse.getInt("id_user");
                                    int currentSL = 0;
                                    int loginTime = jsonResponse.getInt("login_time");
                                    String login = jsonResponse.getString("login");
                                    String lastLoginFail = jsonResponse.getString("last_login_fail");

                                    // save user details to shared preferences
                                    SharedPreferences userDetails = getApplicationContext().getSharedPreferences(Strings.preferencesFile, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor spEditor = userDetails.edit();
                                    spEditor.putInt("idUser", idUser);
                                    spEditor.putInt("currentSL", currentSL);
                                    spEditor.putInt("currentGroupSL", 0);
                                    spEditor.putInt("loginTime", loginTime);
                                    spEditor.putString("login", login);
                                    spEditor.putString("sl0cipherKey", passwordField.getText().toString());
                                    spEditor.putString("lastLoginFail", lastLoginFail);
                                    spEditor.apply();

                                    // start main activity
                                    Intent mainActivity = new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(mainActivity);
                                    finish();
                                }
                            }
                            catch (JSONException e)
                            {
                                Toast.makeText(getApplicationContext(), R.string.login_json_extract_error, Toast.LENGTH_LONG).show();
                            }

                            // hide loading dialog
                            loginDialog.hide();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            loginDialog.hide();
                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                        }
                    })
                    {
                        @Override   // add params to the request
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();

                            // get inputs
                            String login = loginField.getText().toString();
                            String password = passwordField.getText().toString();

                            // put params
                            params.put("login", login);
                            params.put("password", PublicFunctions.sha512(password, login));

                            return params;
                        }
                    };
            request.setTag(Strings.LOGIN_REQUEST_TAG);

            // send request to the server
            requestQueue.add(request);
        }
    }

    // starts RegisterActivity on hyperlink click
    public void startRegisterActivity(View view)
    {
        Intent registerActivity = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(registerActivity);
    }


}
