<?php

    require_once 'user.php';

    $user = new User();
    
    if(isset($_POST['idUser']))
        $idUser = $_POST['idUser'];

    if($_POST['function'] == "getLoginTime")
    {
        $json = $user->getLoginTime($idUser);
        echo json_encode($json);
    }

?>    


