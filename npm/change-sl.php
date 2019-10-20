<?php

    require_once 'user.php';
    
    $user = new User();
    
    // get POST params
    if(isset($_POST['idUser']))
        $idUser = $_POST['idUser'];
    
    // logout to security level 0
    if($_POST['function'] == "sl0")
    {    
        $json = $user->logoutToZeroSL($idUser);
        echo json_encode($json);
    }

    // login to security level 1
    if($_POST['function']=="sl1")
    {
        $level1_password = $_POST['level1_password'];
        
        $json = $user->loginFirstSL($idUser, $level1_password);
        echo json_encode($json);   
    }

    // login to security level 2
    if($_POST['function']=="sl2")
    {  
        $level1_password = $_POST['level1_password'];
        $level2_password = $_POST['level2_password'];

        $json = $user->loginSecondSL($idUser, $level1_password, $level2_password);
        echo json_encode($json);
}

?>
    