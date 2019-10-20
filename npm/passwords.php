<?php

    require_once 'password.php';

    $passwordObject = new Password();

    if($_POST['function'] == "add")
    {
        // get params
        $password = $_POST['password'];
        $service_name = $_POST['service_name'];
        $service_url_address = $_POST['service_url_address'];
        $change_reminder = $_POST['change_reminder'];
        $password_lifetime = $_POST['password_lifetime'];
        $expiration_date = $_POST['expiration_date'];
        $id_owner = $_POST['id_owner'];
        $id_passwords_group = $_POST['id_passwords_group'];

        // set json response
        $json = $passwordObject->addPassword($password, $service_name, $service_url_address, $change_reminder, $password_lifetime, $expiration_date, $id_owner, $id_passwords_group);
        echo json_encode($json);
    }

    if($_POST['function'] == "edit")
    {
        // get params
        $id_password = $_POST['id_password'];
        $password = $_POST['password'];
        $service_name = $_POST['service_name'];
        $service_url_address = $_POST['service_url_address'];
        $change_reminder = $_POST['change_reminder'];
        $password_lifetime = $_POST['password_lifetime'];
        $expiration_date = $_POST['expiration_date'];
        $id_owner = $_POST['id_owner'];
        $id_passwords_group = $_POST['id_passwords_group'];

        // set json response
        $json = $passwordObject->editPassword($id_password, $password, $service_name, $service_url_address, $change_reminder, $password_lifetime, $expiration_date, $id_owner, $id_passwords_group);
        echo json_encode($json);
    }
    
    if($_POST['function'] == "delete")
    {
        //get params
        $id_password = $_POST['id_password'];

        //set json response
        $json = $passwordObject->deletePassword($id_password);
        echo json_encode($json);
    }

    if($_POST['function'] == "getPasswords")
    {
        // get params 
        $id_owner = $_POST['id_owner'];
        $id_passwords_group = $_POST['id_passwords_group'];

        // set json response
        $json = $passwordObject->getPasswords($id_owner, $id_passwords_group);
        echo json_encode($json);
    }

?>