<?php

    include_once 'db-connect.php';

    class Password
    {

       private $db;
       private $passwords_table= "passwords";
       private $groups_table = "passwords_groups";

       public function __construct()
        {
            $this->db = new DbConnect();
        }

        public function addPassword($password, $service_name, $service_url_address, $change_reminder, $password_lifetime, $expiration_date, $id_owner, $id_passwords_group)
        {
            // make proper query to check if that password already exists
            if(is_null($id_passwords_group) || $id_passwords_group == 0)
            {
                $query = "SELECT password FROM ".$this->passwords_table." WHERE id_owner = '$id_owner' AND password = '$password' AND service_name = '$service_name' AND id_passwords_group IS NULL";
                $id_passwords_group = 'NULL';
            }
            else
            {  
                $query = "SELECT password FROM ".$this->passwords_table." WHERE id_owner = '$id_owner' AND password = '$password' AND service_name = '$service_name' AND id_passwords_group = '$id_passwords_group'";
                $id_passwords_group = "'".$id_passwords_group."'";
            }

            $result = mysqli_query($this->db->getDb(), $query);

            // that password does not exists 
            if(mysqli_num_rows($result) == 0)
            {       
                // set reminder settings
                if($change_reminder == 0)
                {
                    $password_lifetime = 'NULL'; 
                    $expiration_date = 'NULL';
                }
                else
                {
                    $password_lifetime = "'".$password_lifetime."'"; 
                    $expiration_date = "'".$expiration_date."'";
                }

                // set url address
                if(empty($service_url_address))
                    $service_url = 'NULL';
                else
                    $service_url = "'".$service_url_address."'";

                // add new password
                $query = "INSERT INTO ".$this->passwords_table." (password, service_name, service_url_address, change_reminder, password_lifetime, expiration_date, id_owner, id_passwords_group, created_at) VALUES ('$password', '$service_name', $service_url, '$change_reminder', $password_lifetime, $expiration_date, '$id_owner', $id_passwords_group, NOW())";
                mysqli_query($this->db->getDb(), $query);  

                $json['success'] = 1;
                $json['message'] = "Your new password is added.";
            }
            // that group already exists
            else
            {
                $json['success'] = 0;
                $json['message'] = "This password has been existing in this group.";
            }

            return $json;
        }

        public function editPassword($id_password, $password, $service_name, $service_url_address, $change_reminder, $password_lifetime, $expiration_date, $id_owner, $id_passwords_group)
        {
            // make proper query to check if password with new data already exists
            if(is_null($id_passwords_group) || $id_passwords_group == 0)
            {
                $query = "SELECT password FROM ".$this->passwords_table." WHERE id_password != '$id_password' AND id_owner = '$id_owner' AND password = '$password' AND service_name = '$service_name' AND id_passwords_group IS NULL";
                $id_passwords_group = 'NULL';
            }
            else
            {
                $query = "SELECT password FROM ".$this->passwords_table." WHERE id_password != '$id_password' AND id_owner = '$id_owner' AND password = '$password' AND service_name = '$service_name' AND id_passwords_group = '$id_passwords_group'";
                $id_passwords_group = "'".$id_passwords_group."'";
            }

            $result = mysqli_query($this->db->getDb(), $query);

            // that password does not exists 
            if(mysqli_num_rows($result) == 0)
            {    
                // set reminder settings
                if($change_reminder == 0)
                {
                    $password_lifetime = 'NULL'; 
                    $expiration_date = 'NULL';
                }
                else
                {
                    $password_lifetime = "'".$password_lifetime."'"; 
                    $expiration_date = "'".$expiration_date."'";
                }

                // set url address
                if(empty($service_url_address))
                    $service_url = 'NULL';
                else
                    $service_url = "'".$service_url_address."'";

                // edit password
                $query = "UPDATE ".$this->passwords_table." SET password = '$password', service_name = '$service_name', service_url_address = $service_url, change_reminder = '$change_reminder', password_lifetime = $password_lifetime, expiration_date = $expiration_date, id_owner = '$id_owner', id_passwords_group = $id_passwords_group, updated_at = NOW() WHERE id_password = '$id_password'";
                mysqli_query($this->db->getDB(), $query); 

                $json['success'] = 1;
                $json['message'] = "Your password is edited.";
            }
            else
            {
                $json['success'] = 0;
                $json['message'] = "This password has been existing in this group.";
            }

            return $json;
        }

        public function deletePassword($id_password)
        {
           $query = "DELETE FROM ".$this->passwords_table." WHERE id_password = '$id_password'";
           mysqli_query($this->db->getDb(), $query);

           $json['success'] = 1;
           $json['message'] = "Your password is deleted.";

           return $json;
        }

        // returns passwords from specified group (to display in app)
        public function getPasswords($id_owner, $id_passwords_group)
        {
            $json = array();

            // make proper query to get passwords
            if(is_null($id_passwords_group) || $id_passwords_group == 0)
            {
                $query = "SELECT * FROM ".$this->passwords_table." WHERE id_owner = '$id_owner' AND id_passwords_group IS NULL";
                $id_passwords_group = 'NULL';
            }
            else
                $query = "SELECT * FROM ".$this->passwords_table." WHERE id_owner = '$id_owner' AND id_passwords_group = '$id_passwords_group'";

            // get passwords
            $result = mysqli_query($this->db->getDb(), $query);
            $numRows = mysqli_num_rows($result);

            // push each password to json response
            for($i=0; $i<$numRows; $i++)
            {
                $password = mysqli_fetch_assoc($result);
              
                // set proper security level (its part of cipher key used for password encryption)
                if($id_passwords_group == 'NULL')
                    $password['security_level'] = '0';
                else
                {
                    // get parent group security level
                    $query2 = "SELECT security_level FROM ".$this->groups_table." WHERE id_group = '$id_passwords_group'";
                    $result2 = mysqli_query($this->db->getDb(), $query2);
                    $securityLevel = mysqli_fetch_row($result2)['0'];
                    
                    $password['security_level'] = $securityLevel;
                }

                array_push($json, $password);
            }

            return $json;
        }
    }

?>