<?php

    require_once 'user.php';
   
    $user = new User();
    
    // get POST params
    if(isset($_POST['login']))
        $login = $_POST['login'];   
    if(isset($_POST['email']))
        $email = $_POST['email'];
    if(isset($_POST['password']))
        $password = $_POST['password'];
     if(isset($_POST['level1_password']))
        $level1_password = $_POST['level1_password'];
     if(isset($_POST['level2_password']))     
        $level2_password = $_POST['level2_password'];
     
    // try to create new user if all params are passed
    if(!empty($login) && !empty($email) && !empty($password) && !empty($level1_password) && !empty($level2_password))
    {        
        $json = $user->registerUser($login, $email, $password, $level1_password, $level2_password);   
        echo json_encode($json);
    }
    
?>