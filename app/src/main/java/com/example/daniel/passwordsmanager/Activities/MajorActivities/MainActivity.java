package com.example.daniel.passwordsmanager.Activities.MajorActivities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.daniel.passwordsmanager.Activities.MinorActivities.SecurityLevelActivity;
import com.example.daniel.passwordsmanager.PublicFunctions;
import com.example.daniel.passwordsmanager.R;
import com.example.daniel.passwordsmanager.Strings;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    // resources
    public static LogoutTimer logoutTimer;
    private RequestQueue requestQueue;
    private SharedPreferences userDetails;
    private String idUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init shared preferences
        userDetails = getApplicationContext().getSharedPreferences(Strings.preferencesFile, Context.MODE_PRIVATE);

        // init resources
        initResources();
    }

    // sets security lavel label on resume
    @Override
    protected void onResume()
    {
        super.onResume();
        setSecurityLevelLabel();
    }

    // reset logout timer on user interaction
    @Override
    public void onUserInteraction()
    {
        super.onUserInteraction();

        if(logoutTimer != null)
            logoutTimer.reset();
    }

    // inflates toolbar into activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // inflate toolbar into activity
        getMenuInflater().inflate(R.menu.action_bar, menu);

        // hide add password and add folder buttons
        MenuItem addPasswordButton = menu.findItem(R.id.action_bar__add_password_button);
        MenuItem addFolderButton = menu.findItem(R.id.action_bar__add_group_button);
        addPasswordButton.setVisible(false);
        addFolderButton.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    // sets reactions for clicking icons on toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            // logout
            case R.id.action_bar__logout_button:
            {
                new AlertDialog.Builder(MainActivity.this).
                        setTitle(R.string.logout_window_title).
                        setMessage(R.string.logout_window_message).
                        setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                               logout();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    // initiates resources
    private void initResources()
    {
        // init toolbar
        initToolbar();

        // init logout timer
        initLogoutTimer();

        // set dates of last succesful login and last failed login
        setLastLoginDetails();

        // init request queue
        requestQueue = Volley.newRequestQueue(this);
    }

    // initiates toolbar
    private void initToolbar()
    {
        // set toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    // inititates logout timer
    private void initLogoutTimer()
    {
        // get login time
        int loginTime = userDetails.getInt("loginTime", 0);

        // init logout timer
        if(userDetails.getInt("loginTime", 0) != 0)
        {
            logoutTimer = new LogoutTimer(loginTime * 1000 * 60, 1000);
            logoutTimer.start();
        }
    }

    // sets last successful and failed login dates
    private void setLastLoginDetails()
    {
        // create date format
        SimpleDateFormat myDataFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm");

        // get last succesful login date (current date)
        Date currentDate = PublicFunctions.getCurrentDate(myDataFormat);
        String lastLoginSuccess = myDataFormat.format(currentDate);

        // get last failed login date
        String lastLoginFail = userDetails.getString("lastLoginFail", "Unknown date");

        // set last successful and failed login dates
        TextView lastLoginSuccessLabel = (TextView)findViewById(R.id.last_login_success);
        TextView lastLoginFailLabel = (TextView)findViewById(R.id.last_login_fail);
        lastLoginSuccessLabel.setText("Last successful login date: " + lastLoginSuccess);
        lastLoginFailLabel.setText("Last failed login date: " + lastLoginFail);
    }

    // sets current security level label text
    private void setSecurityLevelLabel()
    {
        // get current security level
        int currentSL = userDetails.getInt("currentSL", 0);

        // set current security level label text
        TextView currentSecurityLevelLabel = (TextView)findViewById(R.id.toolbar__current_security_level);
        currentSecurityLevelLabel.setText("Security level: " + String.valueOf(currentSL));
    }

    // logout
    private void logout()
    {
        // create request
        StringRequest request = new StringRequest(Request.Method.POST, Strings.logoutURL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try
                        {
                            // get json response
                            JSONObject jsonResponse = new JSONObject(response);

                            // show message
                            String message = jsonResponse.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        }
                        catch (JSONException e)
                        {
                            Toast.makeText(getApplicationContext(), R.string.logout_json_extract_error, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                })
        {
            // get user details
            String idUser = String.valueOf(userDetails.getInt("idUser", 0));

            @Override   // add params to the request
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();

                // put params
                params.put("id_user", idUser);

                return params;
            }
        };
        request.setTag(Strings.LOGOUT_REQUEST_TAG);

        // send request to the server
        requestQueue.add(request);

        // clear user data
        userDetails.edit().clear().apply();

        // start login activity
        Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
        loginActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginActivity);
}



                                            // ON BUTTONS CLICK

    // starts SecurityLevelActivity
    public void startSecurityLevelActivity(View view)
    {
        Intent securityLevelActivity = new Intent(getApplicationContext(), SecurityLevelActivity.class);
        startActivity(securityLevelActivity);
    }

    // starts PasswordsActivity
    public void startPasswordsActivity(View view)
    {
        Intent passwordsActivity = new Intent(getApplicationContext(), PasswordsActivity.class);
        startActivity(passwordsActivity);
    }

    // starts PasswordGeneratorActivity
    public void startPasswordGeneratorActivity(View view)
    {
        Intent passwordGeneratorActivity = new Intent(getApplicationContext(), PasswordGeneratorActivity.class);
        startActivity(passwordGeneratorActivity);
    }

    // starts SettingsActivity
    public void startAccountSettingsActivity(View view)
    {
        Intent accountSettingsActivity = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(accountSettingsActivity);
    }

                                            // SUBCLASSES

    // logout timer
    public class LogoutTimer extends CountDownTimer
    {
        private LogoutTimer(long millisInFuture, long countDownInterval)
        {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished)
        {
        }

        // logout on finish
        @Override
        public void onFinish()
        {
            logout();
            Toast.makeText(getApplicationContext(), R.string.logout_inactivity_toast, Toast.LENGTH_LONG).show();
        }

        // update auto logout time
        public void updateLogoutTimer(int newLoginTime)
        {
            logoutTimer.cancel();
            logoutTimer = new LogoutTimer(newLoginTime * 1000 * 60, 1000);
            logoutTimer.start();
        }

        // reset timer
        public void reset()
        {
            this.cancel();
            this.start();
        }

        // destroy timer
        public void destroy()
        {
            logoutTimer.cancel();
            logoutTimer = null;
        }
    }
}
