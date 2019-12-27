package com.example.daniel.passwordsmanager.DataModels;

import com.example.daniel.passwordsmanager.PublicFunctions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Password
{
    private int id;
    private Integer passwordLifetime;
    private String password, serviceName, serviceUrl, expirationDate;
    private boolean changeReminder;

    public Password(int id, String password, String serviceName, String serviceUrl,  boolean changeReminder, Integer passwordLifetime, String expirationDate)
    {
        this.id = id;
        this.password = password;
        this.serviceName = serviceName;
        this.serviceUrl = serviceUrl;
        this.changeReminder = changeReminder;
        this.passwordLifetime = passwordLifetime;
        this.expirationDate = expirationDate;
    }

    public int getId() {
        return id;
    }

    public Integer getPasswordLifetime() {
        return passwordLifetime;
    }

    public String getPassword() {
        return password;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public boolean isChangeReminder() {
        return changeReminder;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPasswordLifetime(Integer passwordLifetime) {
        this.passwordLifetime = passwordLifetime;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setChangeReminder(boolean changeReminder) {
        this.changeReminder = changeReminder;
    }

    public boolean isPassswordExpired()
    {
        // return false if password reminder is off
        if(!this.isChangeReminder())
            return false;

        try
        {
            // create date format
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            // get dates
            Date expirationDate = dateFormat.parse(this.getExpirationDate());
            Date currentDate = PublicFunctions.getCurrentDate(new SimpleDateFormat());

            // return true if password is expired
            if (expirationDate.before(currentDate))
                return true;
        }
        catch (ParseException e)
        {}

        return false;
    }
}
