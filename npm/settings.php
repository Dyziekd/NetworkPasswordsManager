<?php
    
    require_once 'user.php';

    $user = new User();

    // get params
    if(isset($_POST['idUser']))
        $idUser = $_POST['idUser'];
    if(isset($_POST['newValue']))
        $newValue = $_POST['newValue'];
    if(isset($_POST['password']))
        $password = $_POST['password'];
    
    
    if($_POST['function'] == "changeEmail")
    {
         $json = $user->changeEmail($idUser, $newValue, $password);
         echo json_encode($json);
    }

    if($_POST['function'] == "changePassword")
    {
         $json = $user->changePassword($idUser, $newValue, $password);
         echo json_encode($json);
    }

    if($_POST['function'] == "changeSL1Password")
    {
        $json = $user->changeSL1Password($idUser, $newValue, $password);
        echo json_encode($json);
    }
    
    if($_POST['function'] == "changeSL2Password")
    {    
        $json = $user->changeSL2Password($idUser, $newValue, $password);
        echo json_encode($json);
    }

    if($_POST['function']=="changeAutoLogout")
    {    
        $json = $user->changeAutoLogout($idUser, $newValue, $password);
        echo json_encode($json);
    }
    
?>