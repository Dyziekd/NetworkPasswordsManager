package com.example.daniel.passwordsmanager.Activities.MinorActivities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.daniel.passwordsmanager.Activities.MajorActivities.MainActivity;
import com.example.daniel.passwordsmanager.R;
import com.example.daniel.passwordsmanager.Strings;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddEditGroupActivity extends AppCompatActivity
{
    // resources
    private EditText nameField, slField;
    private RequestQueue requestQueue;
    private SharedPreferences userDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_group);

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
            requestQueue.cancelAll(Strings.GROUPS_REQUEST_TAG);
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
        nameField = (EditText) findViewById(R.id.add_edit_group__name_field);
        slField = (EditText) findViewById(R.id.add_edit_group__SL_field);

        // init request queue
        requestQueue = Volley.newRequestQueue(this);

        // check if group is being edited
        boolean isEditMode = getIntent().getExtras().getBoolean("editMode");

        // edit UI if group is being edited
        if(isEditMode)
        {
            // show edit button and hide add button
            Button addButton = (Button)findViewById(R.id.add_edit_group__add_button);
            Button editButton = (Button)findViewById(R.id.add_edit_group__edit_button);
            addButton.setVisibility(View.GONE);
            editButton.setVisibility(View.VISIBLE);

            // set title
            setTitle(getString(R.string.edit_group_activity_title));

            // fill fields
            fillFields();
        }
    }

    // fills all fields (edit mode)
    private void fillFields()
    {
        // get intent data
        Bundle receivedData = getIntent().getExtras();

        // get folder data
        String groupName = receivedData.getString("name");
        int securityLevel = receivedData.getInt("securityLevel");

        // set fields texts
        nameField.setText(groupName);
        slField.setText(String.valueOf(securityLevel));
    }

    // validates form
    private boolean validateForm()
    {
        boolean isCorrect = true;

        // get form inputs
        String groupName = nameField.getText().toString();
        String sl = slField.getText().toString();

        // validate group name
        if(TextUtils.isEmpty(groupName))
        {
            nameField.setError(getString(R.string.empty_field_error));
            isCorrect = false;
        }

        // validate security level
        if(TextUtils.isEmpty(sl))
        {
            slField.setError(getString(R.string.empty_field_error));
            isCorrect = false;
        }
        else if(!TextUtils.isDigitsOnly(sl))
        {
            slField.setError(getString(R.string.no_numbers_error));
            isCorrect = false;
        }
        else
        {
            int securityLevel = Integer.valueOf(sl);
            if(securityLevel < 0 || securityLevel > 2)
            {
                slField.setError(getString(R.string.incorrect_SL_error));
                isCorrect = false;
            }
        }

        return isCorrect;
    }


                                                    // ON BUTTONS CLICK

    // tries to add group to database
    public void addGroup(View view)
    {
        if(validateForm())
        {
            // show loading indicator
            final ProgressBar loadingIndicator = (ProgressBar)findViewById(R.id.add_edit_group__loading_indicator);
            loadingIndicator.setVisibility(View.VISIBLE);

            // create request
            StringRequest request = new StringRequest(Request.Method.POST, Strings.groupsURL,
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

                                // finish this activity if group was added successful
                                int success = jsonResponse.getInt("success");
                                if(success == 1)
                                    finish();
                            }
                            catch (JSONException e)
                            {
                                Toast.makeText(getApplicationContext(), R.string.add_group_json_extract_error, Toast.LENGTH_LONG).show();
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

                            // get inputs
                            String function = Strings.ADD_REQUEST_FUNCTION;
                            String name = nameField.getText().toString();
                            String securityLevel = slField.getText().toString();
                            String idOwner = String.valueOf(userDetails.getInt("idUser", 0));
                            String idSuperGroup = String.valueOf(getIntent().getExtras().getInt("idSuperGroup"));

                            // put params
                            params.put("function", function);
                            params.put("name", name);
                            params.put("security_level", securityLevel);
                            params.put("id_super_group", idSuperGroup);
                            params.put("id_owner", idOwner);

                            return params;
                        }
                    };
            request.setTag(Strings.GROUPS_REQUEST_TAG);

            // send request to the server
            requestQueue.add(request);
        }
    }

    // tries to edit an existing group in database
    public void editGroup(View view)
    {
        if(validateForm())
        {
            // show loading indicator
            final ProgressBar loadingIndicator = (ProgressBar)findViewById(R.id.add_edit_group__loading_indicator);
            loadingIndicator.setVisibility(View.VISIBLE);

            // create request
            StringRequest request = new StringRequest(Request.Method.POST, Strings.groupsURL,
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

                                // finish this activity if group was edited successful
                                int success = jsonResponse.getInt("success");
                                if(success == 1)
                                    finish();
                            }
                            catch (JSONException e)
                            {
                                Toast.makeText(getApplicationContext(), R.string.edit_group_json_extract_error, Toast.LENGTH_LONG).show();
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

                    // get inputs
                    String function = Strings.EDIT_REQUEST_FUNCTION;
                    String idGroup = String.valueOf(getIntent().getExtras().getInt("idGroup"));
                    String name = nameField.getText().toString();
                    String securityLevel = slField.getText().toString();
                    String idOwner = String.valueOf(userDetails.getInt("idUser", 0));
                    String idSuperGroup = String.valueOf(getIntent().getExtras().getInt("idSuperGroup"));

                    // put params
                    params.put("function", function);
                    params.put("id_group", idGroup);
                    params.put("name", name);
                    params.put("security_level", securityLevel);
                    params.put("id_super_group", idSuperGroup);
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
