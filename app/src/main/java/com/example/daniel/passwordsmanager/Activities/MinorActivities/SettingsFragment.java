package com.example.daniel.passwordsmanager.Activities.MinorActivities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.daniel.passwordsmanager.PublicFunctions;
import com.example.daniel.passwordsmanager.R;

public class SettingsFragment extends Fragment
{
    // settings modes
    private final static int CHANGE_EMAIL_MODE = 0;
    private final static int CHANGE_PASSWORD_MODE = 1;
    private final static int CHANGE_SL1_PASSWORD_MODE = 2;
    private final static int CHANGE_SL2_PASSWORD_MODE = 3;
    private final static int CHANGE_AUTO_LOGOUT_SETTINGS_MODE = 4;
    private int CURRENT_MODE;

    // resources
    private EditText newValueField;
    private TextView settingsTitle, passwordPowerBarLabel;
    private ProgressBar passwordPowerBar;

    public SettingsFragment() {}

    // set mode on create
    public SettingsFragment(int MODE)
    {
        CURRENT_MODE = MODE;
    }

    // inflate basic view
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View fragmentView = inflater.inflate(R.layout.fragment_settings, container, false);

        return fragmentView;
    }

    // set view for specified mode
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        // init resources
        initResources();

        // set texts and fields depends on mode
        switch(CURRENT_MODE)
        {
            case CHANGE_EMAIL_MODE: // change email mode
                settingsTitle.setText(R.string.change_email_fragment_title);
                newValueField.setHint(R.string.new_email_field_hint);
                break;

            case CHANGE_PASSWORD_MODE: // change password mode
                settingsTitle.setText(R.string.change_password_fragment_title);
                newValueField.setHint(R.string.new_password_field_hint);
                newValueField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                initPasswordPowerBar();
                break;

            case CHANGE_SL1_PASSWORD_MODE: // change security level 1 mode
                settingsTitle.setText(R.string.change_SL1_password_fragment_title);
                newValueField.setHint(R.string.new_sl1_password_field_hint);
                newValueField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                initPasswordPowerBar();
                break;

            case CHANGE_SL2_PASSWORD_MODE: // change security level 2 mode
                settingsTitle.setText(R.string.change_SL2_password_fragment_title);
                newValueField.setHint(R.string.new_sl2_password_field_hint);
                newValueField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                initPasswordPowerBar();
                break;

            case CHANGE_AUTO_LOGOUT_SETTINGS_MODE: // change auto logout settings mode
                settingsTitle.setText(R.string.change_auto_logout_settings_fragment_title);
                newValueField.setHint(R.string.auto_logout_field_hint);
                newValueField.setInputType(InputType.TYPE_CLASS_NUMBER);
                CheckBox autoLogoutCheckbox = (CheckBox)getView().findViewById(R.id.settings_fragment__auto_logout_checkbox);
                autoLogoutCheckbox.setVisibility(View.VISIBLE);
                autoLogoutCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked)
                        {
                            newValueField.setEnabled(true);
                            newValueField.setText("");
                            newValueField.setHint(getString(R.string.auto_logout_field_hint));
                        }
                        else if(!isChecked)
                        {
                            newValueField.setEnabled(false);
                            newValueField.setText("");
                            newValueField.setHint(R.string.auto_logout_disabled_field_hint);
                        }
                    }
                });
                break;
        }
    }

    // initiates resources
    private void initResources()
    {
        settingsTitle = (TextView) getView().findViewById(R.id.settings_fragment__title);
        newValueField = (EditText)getView().findViewById(R.id.settings_fragment__new_value_field);
    }

    // initates password power bar
    private void initPasswordPowerBar()
    {
        // init resources
        passwordPowerBar = (ProgressBar)getView().findViewById(R.id.settings_fragment__password_power_bar);
        passwordPowerBarLabel = (TextView)getView().findViewById(R.id.settings_fragment__password_power_bar_label);

        // show power bar
        passwordPowerBar.setVisibility(View.VISIBLE);
        passwordPowerBarLabel.setVisibility(View.VISIBLE);

        // add listener
        newValueField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(TextUtils.isEmpty(newValueField.getText().toString()))   // set default label if password field is empty
                    passwordPowerBarLabel.setText(R.string.password_power_bar_label);
                else
                {
                    int passwordPower = PublicFunctions.checkPasswordPower(newValueField.getText().toString());
                    PublicFunctions.setPasswordPowerBarProgress(passwordPowerBar, passwordPowerBarLabel, passwordPower);
                }
            }
        });
    }
}
