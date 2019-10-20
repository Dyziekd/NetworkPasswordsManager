<?php

    require_once 'user.php';
    
    $user = new User();
    
    // get POST params
    if(isset($_POST['id_user']))
        $id_user = $_POST['id_user'];
    
    $json = $user->logout($id_user);
    echo json_encode($json);

 ?>