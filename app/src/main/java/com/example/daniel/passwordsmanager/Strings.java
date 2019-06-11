package com.example.daniel.passwordsmanager;

public class Strings
{
    // server urls
    public final static String basicURL = "http://192.168.1.2/npm/";
    public final static String loginURL = basicURL + "login.php";
    public final static String registerURL = basicURL + "register.php";
    public final static String logoutURL = basicURL + "logout.php";
    public final static String securityLevelURL = basicURL + "change-sl.php";
    public final static String settingsURL = basicURL + "settings.php";
    public final static String passwordsURL = basicURL + "passwords.php";
    public final static String groupsURL = basicURL + "groups.php";

    // network requests tag
    public static final String LOGIN_REQUEST_TAG = "LoginRequest";
    public static final String REGISTER_REQUEST_TAG = "RegisterRequest";
    public static final String LOGOUT_REQUEST_TAG = "LogoutRequest";
    public static final String SETTINGS_REQUEST_TAG = "SettingsRequest";
    public static final String CHANGE_SL_REQUEST_TAG = "ChangeSLRequest";
    public static final String PASSWORDS_LIST_REQUEST_TAG = "PasswordsListRequest";
    public static final String GROUPS_LIST_REQUEST_TAG = "GroupsListRequest";
    public static final String PASSWORDS_REQUEST_TAG = "PasswordsRequest";
    public static final String GROUPS_REQUEST_TAG = "GroupsRequest";

    // network request functions
    public static final String CHANGE_EMAIL_REQUEST_FUNCTION = "changeEmail";
    public static final String CHANGE_PASSWORD_REQUEST_FUNCTION = "changePassword";
    public static final String CHANGE_SL1_PASSWORD_REQUEST_FUNCTION = "changeSL1Password";
    public static final String CHANGE_SL2_PASSWORD_REQUEST_FUNCTION = "changeSL2Password";
    public static final String CHANGE_AUTO_LOGOUT_SETTINGS_REQUEST_FUNCTION = "changeAutoLogout";
    public static final String LOGOUT_TO_SL0_REQUEST_FUNCTION = "sl0";
    public static final String LOGIN_TO_SL1_REQUEST_FUNCTION = "sl1";
    public static final String LOGIN_TO_SL2_REQUEST_FUNCTION = "sl2";
    public static final String ADD_REQUEST_FUNCTION = "add";
    public static final String EDIT_REQUEST_FUNCTION = "edit";
    public static final String DELETE_REQUEST_FUNCTION = "delete";
    public static final String GET_PASSWORDS_REQUEST_FUNCTION = "getPasswords";
    public static final String GET_GROUPS_REQUEST_FUNCTION = "getGroups";


    // shared preference file path
    public final static String preferencesFile = "com.example.daniel.passwordsmanager.preferences";

    // cryptography
    public static final String CRYPTOGRAPHY_ALGORITHM = "AES/CBC/PKCS5PADDING";
}
