<?php

    require_once 'group.php';

    $groupObject = new Group();

    if($_POST['function'] == "add")
    {
        // get params
        $name = $_POST['name'];
        $security_level = $_POST['security_level'];
        $id_owner = $_POST['id_owner'];
        $id_super_group = $_POST['id_super_group'];

        // set json response
        $json = $groupObject->addGroup($name, $security_level, $id_owner, $id_super_group);
        echo json_encode($json);
    }

    if($_POST['function'] == "edit")
    {
        // get params
        $id_group = $_POST['id_group'];
        $name = $_POST['name'];
        $security_level = $_POST['security_level'];
        $id_owner = $_POST['id_owner'];
        $id_super_group = $_POST['id_super_group'];

        // set json response
        $json = $groupObject->editGroup($id_group, $name, $security_level, $id_owner, $id_super_group);
        echo json_encode($json);
    }

    if($_POST['function'] == "delete")
    {
        // get params
        $id_group = $_POST['id_group'];

        // set json response
        $json = $groupObject->deleteGroup($id_group);
        echo json_encode($json);
    }

    if($_POST['function'] == "getGroups")
    {
        // get params 
        $security_level = $_POST['security_level'];
        $id_owner = $_POST['id_owner'];
        $id_super_group = $_POST['id_super_group'];

        // set json response
        $json = $groupObject->getGroups($security_level, $id_owner, $id_super_group);
        echo json_encode($json);
    }

?>