package com.example.daniel.passwordsmanager.Activities.MajorActivities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.passwordsmanager.PublicFunctions;
import com.example.daniel.passwordsmanager.R;

import java.util.ArrayList;
import java.util.Random;

public class PasswordGeneratorActivity extends AppCompatActivity
{
    // resources
    private CheckBox smallLettersCheckbox, bigLettersCheckbox, digitsCheckbox, specialCharsCheckbox;
    private EditText passwordField;
    private ProgressBar passwordPowerBar;
    private RadioGroup firstCharRadioButtons;
    private SeekBar lengthSlider;
    private TextView passwordPowerBarLabel, lengthLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_generator);

        // init resources
        initResources();
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
        // init labels
        passwordPowerBarLabel = (TextView)findViewById(R.id.generator__password_power_bar_label);
        lengthLabel = (TextView)findViewById(R.id.generator__length_label);

        // init input fields
        passwordField = (EditText)findViewById(R.id.generator__password_field);

        // init power bar
        passwordPowerBar = (ProgressBar)findViewById(R.id.generator__password_power_bar);

        // init checkboxes
        smallLettersCheckbox = (CheckBox)findViewById(R.id.generator__small_letters_checkbox);
        bigLettersCheckbox = (CheckBox)findViewById(R.id.generator__big_letters_checkbox);
        digitsCheckbox = (CheckBox)findViewById(R.id.generator__digits_checkbox);
        specialCharsCheckbox = (CheckBox)findViewById(R.id.generator__special_chars_checkbox);

        // init radio buttons group
        firstCharRadioButtons = (RadioGroup)findViewById(R.id.generator__first_char_radio_buttons);

        // init slider
        lengthSlider = (SeekBar) findViewById(R.id.generator__password_length_slider);
        lengthSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lengthLabel.setText(getString(R.string.password_length_label) + ": " + String.valueOf(lengthSlider.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        lengthLabel.setText(getString(R.string.password_length_label) + ": " + String.valueOf(lengthSlider.getProgress()));

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
    }

    // validates checkboxes
    private boolean validCheckboxes()
    {
        // check if atleast one checkbox is checked
        if(!smallLettersCheckbox.isChecked() && !bigLettersCheckbox.isChecked() && !digitsCheckbox.isChecked() && !specialCharsCheckbox.isChecked())
        {
            Toast.makeText(getApplicationContext(), R.string.password_generator_checkboxes_unchecked_toast, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }



                                                    // ON BUTTONS CLICK

    // sets password field with new generated password (using user configuration)
    public void generatePassword(View view)
    {
        // do not generate password if no checkboxes are checked
        if(!validCheckboxes())
            return;

        // get password generator configuration from inputs
        int length = lengthSlider.getProgress();
        boolean useSmallLetters = smallLettersCheckbox.isChecked();
        boolean useBigLetters = bigLettersCheckbox.isChecked();
        boolean useDigits = digitsCheckbox.isChecked();
        boolean useSpecialChars = specialCharsCheckbox.isChecked();

        // get id of radio button specifying first char category
        Integer firstCharRadioButtonId = firstCharRadioButtons.getCheckedRadioButtonId();

        // generate password
        PasswordGenerator passwordGenerator = new PasswordGenerator(length, useSmallLetters, useBigLetters, useDigits, useSpecialChars, firstCharRadioButtonId);
        String generatedPassword = passwordGenerator.generatePassword();
        passwordField.setText(generatedPassword);
    }

    // sets password field with new generated password (using random configuration)
    public void random(View view)
    {
        // set password generator random configuration
        Random random = new Random();
        int length = random.nextInt(32) + 4;
        boolean useSmallLetters = random.nextBoolean();
        boolean useBigLetters = random.nextBoolean();
        boolean useDigits = random.nextBoolean();
        boolean useSpecialChars = random.nextBoolean();

        // fill ui
        lengthSlider.setProgress(length);
        smallLettersCheckbox.setChecked(useSmallLetters);
        bigLettersCheckbox.setChecked(useBigLetters);
        digitsCheckbox.setChecked(useDigits);
        specialCharsCheckbox.setChecked(useSpecialChars);
        firstCharRadioButtons.clearCheck();

        // generate password
        PasswordGenerator passwordGenerator = new PasswordGenerator(length, useSmallLetters, useBigLetters, useDigits, useSpecialChars, null);
        String generatedPassword = passwordGenerator.generatePassword();
        passwordField.setText(generatedPassword);
    }

    // copies password to clipboard
    public void copyPassword(View view)
    {
        // get password from password field
        String textToCopy = passwordField.getText().toString();

        // do not copy if password field is empty
        if(TextUtils.isEmpty(textToCopy))
            return;

        // put password into clipboard
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("copy", textToCopy);
        clipboard.setPrimaryClip(clip);

        // show toast
        Toast.makeText(getApplicationContext(), R.string.copy_password_toast, Toast.LENGTH_SHORT).show();
    }



                                                        // SUBCLASSES

    // class used to generate password
    private static class PasswordGenerator
    {
        // chars used to generate password
        private static final String SMALL_LETTERS = "abcdefghijklmnopqrstuvwxyz";
        private static final String BIG_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private static final String DIGITS = "0123456789";
        private static final String SPECIAL_CHARS = "!@#$%&*()_+-=[]|,./?><";

        private static int length;
        private static boolean useSmallLetters, useBigLetters, useDigits, useSpecialChars;
        private static Integer firstCharRadioButtonId;

        public PasswordGenerator(int length, boolean useSmallLetters, boolean useBigLetters, boolean useDigits, boolean useSpecialChars, Integer firstCharRadioButtonId)
        {
            PasswordGenerator.length = length;
            PasswordGenerator.useSmallLetters = useSmallLetters;
            PasswordGenerator.useBigLetters = useBigLetters;
            PasswordGenerator.useDigits = useDigits;
            PasswordGenerator.useSpecialChars = useSpecialChars;
            PasswordGenerator.firstCharRadioButtonId = firstCharRadioButtonId;
        }

        public String generatePassword()
        {
            // create password builder and random object
            StringBuilder passwordBuilder = new StringBuilder(length);
            Random random = new Random(System.nanoTime());

            // create categories list
            ArrayList<String> charCategories = createCategoriesList(useSmallLetters, useBigLetters, useDigits, useSpecialChars);

            // return empty string if categories list is empty
            if(charCategories.isEmpty())
                return "";

            // build password
            for(int i = 0; i < length; i++)
            {
                String charCategory = charCategories.get(random.nextInt(charCategories.size()));
                int position = random.nextInt(charCategory.length());
                passwordBuilder.append(charCategory.charAt(position));
            }

            // replace first char if it is specified
            if(firstCharRadioButtonId != null)
            {
                char firstChar;

                switch (firstCharRadioButtonId)
                {
                    case R.id.generator__first_char_small_letter_radio_button:
                        firstChar = SMALL_LETTERS.charAt(random.nextInt(SMALL_LETTERS.length()));
                        break;

                    case R.id.generator__first_char_big_letter_radio_button:
                        firstChar = BIG_LETTERS.charAt(random.nextInt(BIG_LETTERS.length()));
                        break;

                    case R.id.generator__first_char_digit_radio_button:
                        firstChar = DIGITS.charAt(random.nextInt(DIGITS.length()));
                        break;

                    case R.id.generator__first_char_special_char_radio_button:
                        firstChar = SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length()));
                        break;

                    default:
                        firstChar = 'x';
                }


                passwordBuilder.replace(0, 1, String.valueOf(firstChar));
            }

            // return password
            String password = passwordBuilder.toString();
            return password;
        }

        private ArrayList<String> createCategoriesList(boolean useSmallLetters, boolean useBigLetters, boolean useDigits, boolean useSpecialChars)
        {
            // create list containg categories of char used to generate password (small letters / big letters / digits / special chars)
            ArrayList<String> charCategoriesList = new ArrayList<String>(4);

            if(useSmallLetters)
                charCategoriesList.add(SMALL_LETTERS);
            if(useBigLetters)
                charCategoriesList.add(BIG_LETTERS);
            if(useDigits)
                charCategoriesList.add(DIGITS);
            if(useSpecialChars)
                charCategoriesList.add(SPECIAL_CHARS);

            return charCategoriesList;
        }
    }
}
