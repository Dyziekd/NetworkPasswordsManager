<?php

    include_once 'db-connect.php';

    class Group
    {

        private $db;        
        private $groups_table = "passwords_groups";
        private $passwords_table = "passwords";

        public function __construct()
        {
            $this->db = new DbConnect();
        }

        public function addGroup($name, $security_level, $id_owner, $id_super_group)
        {
            // make proper query to check if that group already exists 
            if(is_null($id_super_group) || $id_super_group == 0)
            {
                $query = "SELECT id_group FROM ".$this->groups_table." WHERE id_owner = '$id_owner' AND name = '$name' AND id_super_group IS NULL";
                $id_super_group = 'NULL';
            }
            else
                $query = "SELECT id_group FROM ".$this->groups_table." WHERE id_owner = '$id_owner' AND name = '$name' AND id_super_group = '$id_super_group'";

            $result = mysqli_query($this->db->getDb(), $query);

            // that group does not exists 
            if(mysqli_num_rows($result) == 0)
            {
                // get parent-group security level
                $parentGroupSL = $this->getGroupSL($id_super_group);

                // add group if current SL is equal or higher than parent group
                if($security_level >= $parentGroupSL)
                {
                    mysqli_query($this->db->getDb(),"INSERT INTO ".$this->groups_table." (name, security_level, id_owner, id_super_group, created_at) VALUES ('$name', '$security_level', '$id_owner',$id_super_group, NOW())");

                    $json['success'] = 1;
                    $json['message'] = "You have successfully added a new passwords group.";
                }
                // do not add group if current SL is lower than parent group
                else
                {
                    $json['success'] = 0;
                    $json['message'] = "Security level of created group has to be higher or equal to security level of super group.";  
                }
            }
            // that group already exists
            else
            {
                $json['success'] = 0;
                $json['message'] = "This group has been existing in this passwords group."; 
            }

            return $json;
        }

        public function editGroup($id_group, $name, $security_level, $id_owner, $id_super_group)
        {
            // make proper query to check if group with new data already exists 
            if(is_null($id_super_group) || $id_super_group == 0)
            {
                $query = "SELECT id_group FROM ".$this->groups_table." WHERE id_group != '$id_group' AND id_owner = '$id_owner' AND name = '$name' AND id_super_group IS NULL";
                $id_super_group = 'NULL';
            }
            else
                $query = "SELECT id_group FROM ".$this->groups_table." WHERE id_group != '$id_group' AND id_owner = '$id_owner' AND name = '$name' AND id_super_group = '$id_super_group'";

            $result = mysqli_query($this->db->getDb(), $query);

            // that group does not exists 
            if(mysqli_num_rows($result) == 0)
            {
                // get parent-group security level
                $parentGroupSL = $this->getGroupSL($id_super_group);

                // edit this group if current SL is equal or higher than parent group
                if($security_level >= $parentGroupSL)
                {
                    $query = "UPDATE ".$this->groups_table." SET name = '$name', security_level = '$security_level', id_owner = '$id_owner', id_super_group = $id_super_group, updated_at = NOW() WHERE id_group = '$id_group'";
                    mysqli_query($this->db->getDb(),$query);

                    $json['success'] = 1;
                    $json['message'] = "You have successfully edited a passwords group.";

                }
                // do not this edit group if current SL is lower than parent group
                else
                {    
                    $json['success'] = 0;
                    $json['message'] = "Security level of edited group has to be higher or equal to security level of super group.";   
                }
            }
            // that group already exists (do not edit this group)
            else
            {
                 $json['success'] = 0;
                 $json['message'] = "This group name has been existing in this passwords group.";   
            }

            return $json;
        }

        public function deleteGroup($id_group)
        {
            // delete passwords in this group
            $query = "DELETE FROM ".$this->passwords_table." WHERE id_passwords_group = '$id_group'";
            mysqli_query($this->db->getDb(), $query);

            // delete groups in this group
            $query = "DELETE from ".$this->groups_table." WHERE id_super_group = '$id_group'";
            mysqli_query($this->db->getDb(), $query);

            // delete this group
            $query = "DELETE FROM ".$this->groups_table." WHERE id_group = '$id_group'";
            mysqli_query($this->db->getDb(), $query);

            $json['success'] = 1;
            $json['message'] = "You have successfully deleted a passwords group.";

            return $json;
        }

        // returns all groups (to display in app)
        public function getGroups($security_level, $id_owner, $id_super_group)
        {
            $json = array();

            // make proper query to get groups
            if(is_null($id_super_group) || $id_super_group == 0)
            {
                $id_super_group = 'NULL';
                $query = "SELECT id_group, name, security_level FROM ".$this->groups_table." WHERE security_level <= '$security_level' AND id_owner = '$id_owner' AND id_super_group IS NULL";
            }
            else
                $query = "SELECT id_group, name, security_level FROM ".$this->groups_table." WHERE security_level <= '$security_level' AND id_owner = '$id_owner' AND id_super_group = '$id_super_group'";

            // get groups
            $result = mysqli_query($this->db->getDb(), $query);
            $numRows = mysqli_num_rows($result);

            // push each group to json response
            for($i = 0; $i < $numRows; $i++)
            {
                $group = mysqli_fetch_assoc($result);
                array_push($json, $group);
            }

            return $json;
         }

        // returns security level of specified group
        private function getGroupSL($id_group)
        {
            $query = "SELECT security_level FROM ".$this->groups_table." WHERE id_group = $id_group";
            $result2 = mysqli_query($this->db->getDb(), $query);        
            $groupSL = mysqli_fetch_row($result2)['0'];

            return $groupSL;
        }

    }

?>