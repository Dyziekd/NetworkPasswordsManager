package com.example.daniel.passwordsmanager.DataModels;

public class Group
{
    private int id, securityLevel;
    private String name;

    public Group(int id, String name, int securityLevel)
    {
        this.id = id;
        this.name = name;
        this.securityLevel = securityLevel;
    }

    public int getId()
    {
        return id;
    }

    public int getSecurityLevel() {
        return securityLevel;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }

    public void setName(String name) {
        this.name = name;
    }
}
