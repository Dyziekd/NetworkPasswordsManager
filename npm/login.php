<?php
    
    require_once 'user.php';
    
    $user = new User();  
    
    // get POST params
    if(isset($_POST['login']))
        $login = $_POST['login'];
    if(isset($_POST['password']))
        $password = $_POST['password'];
    
    
    if(!empty($login) && !empty($password))
    {     
        $json = $user->loginUser($login, $password);   
        echo json_encode($json);  
    }
    
 ?>