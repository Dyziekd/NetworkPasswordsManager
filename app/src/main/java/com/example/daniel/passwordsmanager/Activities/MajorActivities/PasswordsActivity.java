package com.example.daniel.passwordsmanager.Activities.MajorActivities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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
import com.example.daniel.passwordsmanager.Activities.MinorActivities.AddEditGroupActivity;
import com.example.daniel.passwordsmanager.Activities.MinorActivities.AddEditPasswordActivity;
import com.example.daniel.passwordsmanager.Activities.MinorActivities.SecurityLevelActivity;
import com.example.daniel.passwordsmanager.Adapters.GroupsAdapter;
import com.example.daniel.passwordsmanager.Adapters.PasswordsAdapter;
import com.example.daniel.passwordsmanager.DataModels.Group;
import com.example.daniel.passwordsmanager.DataModels.Password;
import com.example.daniel.passwordsmanager.PublicFunctions;
import com.example.daniel.passwordsmanager.R;
import com.example.daniel.passwordsmanager.Strings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PasswordsActivity extends AppCompatActivity
{
    // resources
    private ArrayList<Integer> groupPath;
    private ArrayList<String> groupNames;
    private GroupsAdapter groupsAdapter;
    private ListView passwordsListView, groupsListView;
    private PasswordsAdapter passwordsAdapter;
    private RequestQueue requestQueue;
    private SharedPreferences userDetails;
    private TextView currentGroupLabel, currentSecurityLevelLabel;

    // current group id
    private Integer idCurrentGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwords);

        // init shared preferences
        userDetails = getApplicationContext().getSharedPreferences(Strings.preferencesFile, Context.MODE_PRIVATE);

        // init resources
        initResources();
    }

    // sets security level label on resume and updates lists
    @Override
    protected void onResume()
    {
        super.onResume();

        // set security level label
        setSecurityLevelLabel();

        // update lists
        updatePasswordsList();
        updateGroupsList();
    }

    // cancel all requests on stop
    @Override
    protected void onStop()
    {
        super.onStop();
        if(requestQueue != null)
        {
            requestQueue.cancelAll(Strings.PASSWORDS_LIST_REQUEST_TAG);
            requestQueue.cancelAll(Strings.GROUPS_LIST_REQUEST_TAG);
        }
    }

    // reset logout timer on user interaction
    @Override
    public void onUserInteraction()
    {
        super.onUserInteraction();

        if(MainActivity.logoutTimer != null)
            MainActivity.logoutTimer.reset();
    }

    // back to previous group when clicking back (or leave this activity if current group is root group)
    @Override
    public void onBackPressed()
    {
        // finish this activity if current group is root group or back to previous group
        if(groupPath.size() == 1)
            finish();
        else
        {
            // update group name
            groupNames.remove(groupNames.size() - 1);   // delete last group name
            if(groupNames.isEmpty())   // update current group name
                currentGroupLabel.setText("");
            else
                currentGroupLabel.setText(groupNames.get(groupNames.size() - 1));

            // update group id
            groupPath.remove(groupPath.size() - 1); // delete last group id
            idCurrentGroup = groupPath.get(groupPath.size() - 1);   // update current group id

            // update current group SL (used for password encryption/decryption)
            int currentGroupSL = 0;
            if(idCurrentGroup != null)
                currentGroupSL = groupsAdapter.getGroupSecurityLevel(idCurrentGroup);
            SharedPreferences.Editor spEditor = userDetails.edit();
            spEditor.putInt("currentGroupSL", currentGroupSL);
            spEditor.apply();

            // update lists
            updatePasswordsList();
            updateGroupsList();
        }
    }

    // inflates toolbar into activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // inflate toolbar into activity
        getMenuInflater().inflate(R.menu.action_bar, menu);

        return super.onCreateOptionsMenu(menu);
    }

    // inflates context menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        if(v.equals(passwordsListView)) // inflate passwords context menu
            getMenuInflater().inflate(R.menu.password_context_menu, menu);
        else if(v.equals(groupsListView))    // inflate groups context menu
            getMenuInflater().inflate(R.menu.groups_context_menu, menu);
    }

    // sets reactions for clicking icons on toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            // add password
            case R.id.action_bar__add_password_button:
            {
                Intent addPasswordActivity = new Intent(getApplicationContext(), AddEditPasswordActivity.class);
                addPasswordActivity.putExtra("editMode", false);
                addPasswordActivity.putExtra("idPasswordsGroup", idCurrentGroup);
                startActivity(addPasswordActivity);
                break;
            }

            // add group
            case R.id.action_bar__add_group_button:
            {
                Intent addGroupActivity = new Intent(getApplicationContext(), AddEditGroupActivity.class);
                addGroupActivity.putExtra("editMode", false);
                addGroupActivity.putExtra("idSuperGroup", idCurrentGroup);
                startActivity(addGroupActivity);
                break;
            }

            // logout
            case R.id.action_bar__logout_button:
            {
                new AlertDialog.Builder(PasswordsActivity.this).
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

    // sets reactions for clicking options on context menu
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info;
        final Password passwordData;
        final Group groupData;

        switch (item.getItemId())
        {
                                            // passwords context menu

            // copy password to clipboard
            case R.id.passwords_context_menu__copy_button:
            {
                // get password
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                passwordData = passwordsAdapter.getItem(info.position);
                String password = passwordData.getPassword();

                // insert password into clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("copy", password);
                clipboard.setPrimaryClip(clip);

                // show toast
                Toast.makeText(getApplicationContext(), R.string.copy_password_toast, Toast.LENGTH_SHORT).show();

                return true;
            }

            // go to the website
            case R.id.passwords_context_menu__go_to_website_button:
            {
                // get website url
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                passwordData = passwordsAdapter.getItem(info.position);
                String website = passwordData.getServiceUrl();

                // do not open browser if url is empty
                if(TextUtils.isEmpty(website))
                {
                    Toast.makeText(getApplicationContext(), R.string.go_to_website_error ,Toast.LENGTH_SHORT).show();
                    return false;
                }
                else if(website.startsWith("www.")) // correct url
                    website = "http://" + website;
                else if(!website.startsWith("http://www.") || !website.startsWith("https://www."))
                    website = "http://www." + website;

                // open browser
                Intent openWebsite = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
                startActivity(openWebsite);

                return true;
            }

            // edit password
            case R.id.passwords_context_menu__edit_button:
            {
                // get clicked item data
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                passwordData = passwordsAdapter.getItem(info.position);

                // start new activity
                Intent editPasswordActivity = new Intent(getApplicationContext(), AddEditPasswordActivity.class);
                editPasswordActivity.putExtra("editMode", true);
                editPasswordActivity.putExtra("idPassword", passwordData.getId());
                editPasswordActivity.putExtra("password", passwordData.getPassword());
                editPasswordActivity.putExtra("serviceName", passwordData.getServiceName());
                editPasswordActivity.putExtra("serviceUrl", passwordData.getServiceUrl());
                editPasswordActivity.putExtra("changeReminder", passwordData.isChangeReminder());
                editPasswordActivity.putExtra("passwordLifetime", passwordData.getPasswordLifetime());
                editPasswordActivity.putExtra("idPasswordsGroup", idCurrentGroup);
                startActivity(editPasswordActivity);

                return true;
            }

            // delete password
            case R.id.passwords_context_menu__delete_button:
            {
                // show loading indicator
                final ProgressBar loadingIndicator = (ProgressBar)findViewById(R.id.passwords__passwords_loading_indicator);
                loadingIndicator.setVisibility(View.VISIBLE);

                // get clicked item data
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                passwordData = passwordsAdapter.getItem(info.position);

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

                                    // remove group from list on success
                                    if(jsonResponse.getInt("success") == 1)
                                        passwordsAdapter.remove(passwordData);
                                }
                                catch (JSONException e)
                                {
                                    Toast.makeText(getApplicationContext(), R.string.delete_password_json_extract_error, Toast.LENGTH_LONG).show();
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
                        String function = Strings.DELETE_REQUEST_FUNCTION;
                        String idPassword = String.valueOf(passwordData.getId());

                        // put params
                        params.put("function", function);
                        params.put("id_password", idPassword);

                        return params;
                    }
                };
                request.setTag(Strings.GROUPS_LIST_REQUEST_TAG);

                // send request to the server
                requestQueue.add(request);

                return true;
            }


                                                 // groups context menu

            // edit group
            case R.id.groups_context_menu__edit_button:
            {
                // get clicked item data
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                groupData = groupsAdapter.getItem(info.position);

                // start new activity
                Intent editGroupActivity = new Intent(getApplicationContext(), AddEditGroupActivity.class);
                editGroupActivity.putExtra("editMode", true);
                editGroupActivity.putExtra("idGroup", groupData.getId());
                editGroupActivity.putExtra("name", groupData.getName());
                editGroupActivity.putExtra("securityLevel", groupData.getSecurityLevel());
                editGroupActivity.putExtra("idSuperGroup", idCurrentGroup);
                startActivity(editGroupActivity);

                return true;
            }

            // delete group
            case R.id.groups_context_menu__delete_button:
            {
                // show loading indicator
                final ProgressBar loadingIndicator = (ProgressBar)findViewById(R.id.passwords__groups_loading_indicator);
                loadingIndicator.setVisibility(View.VISIBLE);

                // get clicked item data
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                groupData = groupsAdapter.getItem(info.position);

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

                                    // remove group from list on success
                                    if(jsonResponse.getInt("success") == 1)
                                        groupsAdapter.remove(groupData);
                                }
                                catch (JSONException e)
                                {
                                    Toast.makeText(getApplicationContext(), R.string.delete_group_json_extract_error, Toast.LENGTH_LONG).show();
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
                        String function = Strings.DELETE_REQUEST_FUNCTION;
                        String idGroup = String.valueOf(groupData.getId());

                        // put params
                        params.put("function", function);
                        params.put("id_group", idGroup);

                        return params;
                    }
                };
                request.setTag(Strings.GROUPS_LIST_REQUEST_TAG);

                // send request to the server
                requestQueue.add(request);

                return true;
            }
        }

        return super.onContextItemSelected(item);
    }

    // initiates resources
    private void initResources()
    {
        // init toolbar
        initToolbar();

        // init memory for storing the current group path
        initGroupPathMemory();

        // init request queue
        requestQueue = Volley.newRequestQueue(this);

        // init lists
        initPasswordsList();
        initGroupsList();
    }

    // initiates toolbar
    private void initToolbar()
    {
        // init toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // init current security level label and current group label
        currentSecurityLevelLabel = (TextView)findViewById(R.id.toolbar__current_security_level);
        currentGroupLabel = (TextView)findViewById(R.id.passwords__current_group_label);
    }

    // initiates variables for memorizing group path
    private void initGroupPathMemory()
    {
        idCurrentGroup = null;
        groupNames = new ArrayList<String>();
        groupPath = new ArrayList<Integer>();
        groupPath.add(idCurrentGroup);
    }

    // initiates array list, adapter and listview for groups
    private void initPasswordsList()
      {
        // init passwords adapter
        passwordsAdapter = new PasswordsAdapter(this, R.layout.activity_main, new ArrayList<Password>());

        // init passwords listview
        passwordsListView = (ListView)findViewById(R.id.passwords__passwords_list);
        passwordsListView.setAdapter(passwordsAdapter);
        passwordsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get password data
                Password passwordData = passwordsAdapter.getItem(position);
                TextView passwordField = (TextView) view.findViewById(R.id.passwords_list__password);

                // show / hide password on click
                if (passwordField.getText().toString().equals("********"))
                {
                    passwordField.setText(passwordData.getPassword());

                    // show toast if password is expired
                    if (passwordData.isPassswordExpired())
                        Toast.makeText(getApplicationContext(), R.string.password_expired_toast, Toast.LENGTH_SHORT).show();
                }
                else
                    passwordField.setText("********");
            }
        });
        registerForContextMenu(passwordsListView);
    }

    // initiates array list, adapter and listview for groups
    private void initGroupsList()
    {
        // init groups adapter
        groupsAdapter = new GroupsAdapter(this, R.layout.activity_main, new ArrayList<Group>());

        // init groups listview
        groupsListView = (ListView)findViewById(R.id.passwords__groups_list);
        groupsListView.setAdapter(groupsAdapter);
        groupsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override   // open group
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // update group name
                String name = groupsAdapter.getItem(position).getName();
                currentGroupLabel.setText(name);
                groupNames.add(name);

                // update group id
                idCurrentGroup = groupsAdapter.getItem(position).getId();
                groupPath.add(idCurrentGroup);

                // update current group SL (used for password encryption/decryption)
                int currentGroupSL = groupsAdapter.getItem(position).getSecurityLevel();
                SharedPreferences.Editor spEditor = userDetails.edit();
                spEditor.putInt("currentGroupSL", currentGroupSL);
                spEditor.apply();

                // update lists
                updatePasswordsList();
                updateGroupsList();
            }
        });
        registerForContextMenu(groupsListView);
    }

    // sets current security level label text
    private void setSecurityLevelLabel()
    {
        // get current security level
        int currentSL = userDetails.getInt("currentSL", 0);

        // set current security level label text
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

    // updates passwords list with newest server data
    private void updatePasswordsList()
    {
        // show loading indicator
        final ProgressBar loadingIndicator = (ProgressBar)findViewById(R.id.passwords__passwords_loading_indicator);
        loadingIndicator.setVisibility(View.VISIBLE);

        // clear current passwords list
        passwordsAdapter.clear();

        // create request
        StringRequest request = new StringRequest(Request.Method.POST, Strings.passwordsURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try
                        {
                            // get passwords array from json response
                            JSONArray passwordsArray = new JSONArray(response);

                            // add passwords to list
                            for(int i=0; i<passwordsArray.length(); i++)
                            {
                                // get password data object
                                JSONObject passwordData = passwordsArray.getJSONObject(i);

                                // prepare values for password decryption
                                String login = userDetails.getString("login", "defLogin");
                                String pass =  passwordData.getString("password");
                                String keyName = "sl" + passwordData.getString("security_level") + "cipherKey";
                                String key = userDetails.getString(keyName, "defKey");
                                String initVector = userDetails.getString("login", "defIv");

                                // get password data
                                int idPassword = passwordData.getInt("id_password");
                                pass = PublicFunctions.aesDecrypt(pass, key, initVector);
                                String serviceName = passwordData.getString("service_name");
                                String serviceUrl = passwordData.getString("service_url_address");
                                if(serviceUrl.equals("null"))
                                    serviceUrl = null;
                                boolean changeReminder = false;
                                Integer passwordLifetime = null;
                                String expirationDate = null;
                                if(passwordData.getInt("change_reminder") == 1)
                                {
                                    changeReminder = true;
                                    passwordLifetime = passwordData.getInt("password_lifetime");
                                    expirationDate = passwordData.getString("expiration_date");
                                }

                                // create password object
                                Password password = new Password(idPassword, pass, serviceName, serviceUrl, changeReminder, passwordLifetime, expirationDate);

                                // add password to the list
                                passwordsAdapter.add(password);
                            }

                        }
                        catch (JSONException e)
                        {
                            Toast.makeText(getApplicationContext(), R.string.get_passwords_json_extract_error, Toast.LENGTH_LONG).show();
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

                        // get params
                        String function = Strings.GET_PASSWORDS_REQUEST_FUNCTION;
                        String idPasswordsGroup = String.valueOf(idCurrentGroup);
                        String idOwner = String.valueOf(userDetails.getInt("idUser", 0));

                        // put params
                        params.put("function", function);
                        params.put("id_passwords_group", idPasswordsGroup);
                        params.put("id_owner", idOwner);

                        return params;
                    }
                };
        request.setTag(Strings.PASSWORDS_LIST_REQUEST_TAG);

        // send request to the server
        requestQueue.add(request);

        // notify adapter about data change
        if(passwordsAdapter != null)
            passwordsAdapter.notifyDataSetChanged();

    }

    // updates groups list with newest server data
    private void updateGroupsList()
    {
        // show loading indicator
        final ProgressBar loadingIndicator = (ProgressBar)findViewById(R.id.passwords__groups_loading_indicator);
        loadingIndicator.setVisibility(View.VISIBLE);

        // clear current groups list
        groupsAdapter.clear();

        // create request
        StringRequest request = new StringRequest(Request.Method.POST, Strings.groupsURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try
                        {
                            // get groups array from json response
                            JSONArray groupsArray = new JSONArray(response);

                            // add groups to list
                            for(int i=0; i<groupsArray.length(); i++)
                            {
                                // get group data
                                JSONObject groupData = groupsArray.getJSONObject(i);

                                int idGroup = groupData.getInt("id_group");
                                String name = groupData.getString("name");
                                int securityLevel = groupData.getInt("security_level");

                                // create group object
                                Group group = new Group(idGroup, name, securityLevel);

                                // add group to the list
                                groupsAdapter.add(group);
                            }

                        }
                        catch (JSONException e)
                        {
                            Toast.makeText(getApplicationContext(), R.string.get_groups_json_extract_error, Toast.LENGTH_LONG).show();
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

                // get params
                String function = Strings.GET_GROUPS_REQUEST_FUNCTION;
                String securityLevel = String.valueOf(userDetails.getInt("currentSL", 0));
                String idSuperGroup = String.valueOf(idCurrentGroup);
                String idOwner = String.valueOf(userDetails.getInt("idUser", 0));

                // put params
                params.put("function", function);
                params.put("security_level", securityLevel);
                params.put("id_super_group", idSuperGroup);
                params.put("id_owner", idOwner);

                return params;
            }
        };
        request.setTag(Strings.GROUPS_LIST_REQUEST_TAG);

        // send request to the server
        requestQueue.add(request);

        // notify adapter about data change
        if(groupsAdapter != null)
            groupsAdapter.notifyDataSetChanged();
    }




                                                       // ON BUTTONS CLICK

    // starts SecurityLevelActivity
    public void startSecurityLevelActivity(View view)
    {
        Intent securityLevelActivity = new Intent(getApplicationContext(), SecurityLevelActivity.class);
        startActivity(securityLevelActivity);
    }
}
