package com.example.daniel.passwordsmanager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PublicFunctions
{

                                                        // CRYPTOGRAPHY

    // returns hashed text using SHA512 algorithm
    public static String sha512(String text, String salt)
    {
        String result;

        try
        {
            // get bytes to encryption
            byte textBytes[] = text.getBytes();
            byte saltBytes[] = salt.getBytes();
            byte bytesToEncrypt[] = new byte[textBytes.length + saltBytes.length];
            System.arraycopy(textBytes, 0, bytesToEncrypt, 0, textBytes.length);
            System.arraycopy(saltBytes, 0, bytesToEncrypt, textBytes.length, saltBytes.length);

            // create digest object with SHA-512 algorithm
            MessageDigest myDigest = MessageDigest.getInstance("SHA-512");
            myDigest.update(bytesToEncrypt);

            // encrypt bytes
            byte encryptedBytes[] = myDigest.digest();

            // convert hex to string
            StringBuilder hexString = new StringBuilder();
            for(int i = 0; i < encryptedBytes.length; i++)
                hexString.append(String.format("%02X", encryptedBytes[i]));

            // return hash in lower case
            result = hexString.toString().toLowerCase();
        }
        catch(Exception e)
        {
            result = "error";
        }

        return result;
    }

    // encrypts text using aes algorithm (get byte s -> encrypt -> encode -> to String)
    public static String aesEncrypt(String text, String key, String initVector)
    {
        String result;

        try
        {
            // fix key and init vector
            key = fixLength(key, 32);
            initVector = fixLength(initVector, 16);

            // init encryption key and init vector
            SecretKeySpec myKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            IvParameterSpec myInitVector = new IvParameterSpec(initVector.getBytes("UTF-8"));

            // init aes encryption cipher
            Cipher cipher = Cipher.getInstance(Strings.CRYPTOGRAPHY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, myKey, myInitVector);

            // encrypt bytes (get bytes -> encrypt)
            byte[] encryptedBytes = cipher.doFinal(text.getBytes("UTF-8"));

            // convert encoded bytes to string
            result = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        }
        catch (Exception e)
        {
            result = "error";
        }

        return result;
    }

    // decrypts text using aes algorithm(get bytes -> decode -> decrypt -> to String)
    public static String aesDecrypt(String text, String key, String initVector)
    {
        String result;

        try
        {
            // fix key and init vector
            key = fixLength(key, 32);
            initVector = fixLength(initVector, 16);

            // init decryption key and init vector
            SecretKeySpec myKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES"); // create new KEY in utf-8
            IvParameterSpec myInitVector = new IvParameterSpec(initVector.getBytes("UTF-8"));

            // init aes decryption cipher
            Cipher cipher = Cipher.getInstance(Strings.CRYPTOGRAPHY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, myKey, myInitVector);

            // decrypt bytes (get bytes -> decode -> decrypt)
            byte[] decryptedBytes = cipher.doFinal(Base64.decode(text, Base64.DEFAULT));

            // convert decrypted bytes to String
            result = new String(decryptedBytes);
        }
        catch (Exception e)
        {
            result = "error";
        }

        return result;
    }

    // fixes word length
    private static String fixLength(String word, int length)
    {
        // pad 0 for words with size less than passed length
        if (word.length() < length)
        {
            int numPad = length - word.length();

            for (int i = 0; i < numPad; i++)
                word += "0"; // add 0
        }
        // get first bytes for words with size greater than passed length
        else if (word.length() > length)
            word = word.substring(0, length);

        return word;
    }

                                                      // NETWORK

    // checks internet connection
    public static void checkInternetConnection(final Activity activity)
    {
        // get information about network
        ConnectivityManager myConnectivityManager = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo myNetworkInfo = myConnectivityManager.getActiveNetworkInfo();

        // check connection
        if(myNetworkInfo!= null && myNetworkInfo.isConnectedOrConnecting()) // internet connection is active
            return;
        else   // show dialog window if internet connection is not active
        {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.connection_error_window_title)
                    .setMessage(R.string.connection_error_window_message)
                    .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {    // try to connect again
                            checkInternetConnection(activity);
                        }
                    })
                    .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {   // exit
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.finish();
                        }
                    }).show();
        }
    }

                                                     // DATA (PASSWORDS/GROUPS)

    // returns password power
    public static int checkPasswordPower(String password)
    {
        int passwordPower = 0;

        if(password.length() >= 6)
            passwordPower += 25;
        if(PublicFunctions.hasNumber(password))
            passwordPower += 25;
        if(PublicFunctions.hasUpperAndLower(password))
            passwordPower += 25;
        if(PublicFunctions.hasSpecialChar(password))
            passwordPower +=25;

        return passwordPower;
    }

        // checks if passed string contains number
        public static boolean hasNumber(String myString)
        {
            for(int i=0; i<myString.length(); i++)
                if(Character.isDigit(myString.charAt(i)))
                    return true;

            return false;
        }

        // checks if passed string contains upper and lower letter
        public static boolean hasUpperAndLower(String myString)
        {
            for(int i=0; i<myString.length(); i++)
                if(Character.isUpperCase(myString.charAt(i)))
                {
                    for(int j=0; j<myString.length(); j++)
                        if(Character.isLowerCase(myString.charAt(j)))
                            return true;
                }

            return false;
        }

        // checks if passed string contains special char
        public static boolean hasSpecialChar(String myString)
        {
            for(int i=0; i<myString.length(); i++)
                if(!Character.isLetterOrDigit(myString.charAt(i)))
                    return true;

            return false;
        }

    // sets password power bar progress, color and label
    public static void setPasswordPowerBarProgress(ProgressBar passwordPowerBar, TextView passwordBarLabel, int passwordPower)
    {
        // set progress
        passwordPowerBar.setProgress(passwordPower);

        // set color and label
        switch (passwordPower)
        {
            case 0: // very weak password
                passwordPowerBar.getProgressDrawable().setColorFilter(Color.parseColor(passwordPowerBar.getContext().getResources().getString(R.color.password_power_bar_progress0_color)), PorterDuff.Mode.SRC_IN);
                passwordBarLabel.setText(R.string.password_power_very_weak);
                break;
            case 25:    // weak password
                passwordPowerBar.getProgressDrawable().setColorFilter(Color.parseColor(passwordPowerBar.getContext().getResources().getString(R.color.password_power_bar_progress25_color)), PorterDuff.Mode.SRC_IN);
                passwordBarLabel.setText(R.string.password_power_weak);
                break;
            case 50:    // good password
                passwordPowerBar.getProgressDrawable().setColorFilter(Color.parseColor(passwordPowerBar.getContext().getResources().getString(R.color.password_power_bar_progress50_color)), PorterDuff.Mode.SRC_IN);
                passwordBarLabel.setText(R.string.password_power_good);
                break;
            case 75:    // strong password
                passwordPowerBar.getProgressDrawable().setColorFilter(Color.parseColor(passwordPowerBar.getContext().getResources().getString(R.color.password_power_bar_progress75_color)), PorterDuff.Mode.SRC_IN);
                passwordBarLabel.setText(R.string.password_power_strong);
                break;
            case 100:   // very strong password
                passwordPowerBar.getProgressDrawable().setColorFilter(Color.parseColor(passwordPowerBar.getContext().getResources().getString(R.color.password_power_bar_progress100_color)), PorterDuff.Mode.SRC_IN);
                passwordBarLabel.setText(R.string.password_power_very_strong);
                break;
        }
    }

    // returns current date
    public static Date getCurrentDate(SimpleDateFormat dataFormat)
    {
        // set data format
        dataFormat.setTimeZone(TimeZone.getTimeZone("GMT+1"));

        // return current date
        return Calendar.getInstance().getTime();
    }

    // calculates password expiration date (expiration date = current date + password lifetime)
    public static String calculateExpirationDate(int passwordLifetime)
    {
        // create date format
        SimpleDateFormat myDataFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

        // get current date in proper format
        Date currentDate = getCurrentDate(myDataFormat);

        // get password lifetime and current date in ms
        long passwordLifetimeInMs = (long)passwordLifetime * 24 * 60 * 60 * 1000;
        long currentDateInMs = currentDate.getTime();

        // calculate expiration date (expiration date = current date + password lifetime)
        long expirationDateInMs = currentDateInMs + passwordLifetimeInMs;

        // convert expiration date of long type to date object
        Date expirationDate = new Date(expirationDateInMs);

        return myDataFormat.format(expirationDate);
    }
}
