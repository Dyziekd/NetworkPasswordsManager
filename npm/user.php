<?php
    
    include_once 'db-connect.php';
    
    class User
    {
        private $db;   
        private $db_table = "users";
        
        public function __construct(){
            $this->db = new DbConnect();
        }
        
        // add new user to dataabse
        public function registerUser($login, $email, $password, $level1_password, $level2_password)
        {
            // check if email is valid
            if($this->isValidEmail($email))
            {
                // check if account with that login or email exists
                if($this->isLoginEmailExist($login, $email))
                {
                    // login or email already taken
                    $json['success'] = 0;
                    $json['message'] = "This login or email has been associated with another user";
                } 
                else
                {
                    // add user to database
                    $query = "INSERT INTO ".$this->db_table." (login, email, password, level1_password, level2_password, login_fails, available, created_at) VALUES ('$login', '$email', '$password', '$level1_password', '$level2_password', 0, 1, NOW())";            
                    $inserted = mysqli_query($this->db->getDb(), $query);
                    
                    if($inserted == 1)
                    {
                        // registration successful
                        $json['success'] = 1;
                        $json['message'] = "You are successfully registered";
                    }
                    else
                    {
                        // registration failed
                        $json['success'] = 0;
                        $json['message'] = "Registration failed";
                    }             
                }
            }
            else
            {
                // invalid email
                $json['success'] = 0;
                $json['message'] = "Email address is not valid";
            }
     
            return $json;
        }
        
        // login user 
        public function loginUser($login, $password)
        {
            $json = array();
            
            // check if account with that login exists
            if($this->isLoginExist($login))              
            {
                // unblock account if it is unavailable for more than 30 minutes
                if($this->checkAccountAvailability($login))
                    $this->unblockAccount ($login);
                            
                // check if account is available
                if($this->isAccountAvailable($login))
                {
                    // check if password is correct (if user with that login and password exists)
                    if($this->isUserExist($login, $password)) 
                    {
                        $this->updateOnLoginSuccess($login);
                        
                        $json['success'] = 1;
                        $json['message'] = "You are successfully logged in.";
                        
                        // get last login fail date
                        $query = "SELECT last_login_fail FROM ".$this->db_table." WHERE login = '$login' ";
                        $result = mysqli_query($this->db->getDb(), $query);
                        $last_login_fail = mysqli_fetch_row($result);
                        if(is_null($last_login_fail['0']))
                            $json['last_login_fail'] = "never";
                        else
                            $json['last_login_fail'] = date("d.m.Y H:i", strtotime($last_login_fail['0']));
 
                        // get user id
                        $query = "SELECT id_user FROM ".$this->db_table." WHERE login = '$login'";
                        $result = mysqli_query($this->db->getDb(), $query);
                        $id_user = mysqli_fetch_row($result);
                        $json['id_user'] = $id_user['0'];
                        
                        // get login time
                        $query = "SELECT login_time FROM ".$this->db_table." WHERE login = '$login'";
                        $result = mysqli_query($this->db->getDb(), $query);
                        $login_time = mysqli_fetch_row($result);
                        if(is_null($login_time['0']))
                            $json['login_time']= '0';
                        else
                            $json['login_time']= $login_time['0'];
                        
                        // get cipher keys
                        $query = "SELECT login, password, level1_password, level2_password FROM ".$this->db_table." WHERE login = '$login'";
                        $result = mysqli_query($this->db->getDb(), $query);
                        $rows = mysqli_fetch_assoc($result);
                        $json['login'] = $rows['login'];
                        $json['sl0_cipher_key'] = $rows['password'];
                        $json['sl1_cipher_key'] = $rows['level1_password'];
                        $json['sl2_cipher_key'] = $rows['level2_password'];    
                    }
                    else
                    {
                        $this->updateOnLoginFail($login);
                        
                        // block account if user failed to login 3 times in a row
                        if($this->isLoginFailedThirdTime($login))
                        {
                            // block account
                            $this->blockAccount($login);
                            
                            $json['success'] = 0;
                            $json['message'] = "Your account is being blocked. Try again 30 minutes later.";
                        }
                        else
                        {
                            // incorrect password
                            $json['success'] = 0;
                            $json['message'] = "Incorrect password. Try again.";
                        }
                        
                    }
                }
                else   
                {
                    // account is unavailable
                    $json['success'] = 0;
                    $json['message'] = "This account is not available";
                }
            }
            else
            {
                // account with that login does not existst
                $json['success'] = 0;
                $json['message'] = "This user does not exist";
            }
            
            return $json;
        }
        
        public function logout($id_user)
        {
            $query = "UPDATE ".$this->db_table." SET status = '0', current_security_level = NULL WHERE id_user = '$id_user'";
            mysqli_query($this->db->getDb(), $query);
            
            $json['success'] = 1;
            $json['message'] = "You have been successfully logged out";
            
            return $json;
        }
        
        public function logoutToZeroSL($idUser)  
        {
            $query = "UPDATE ".$this->db_table." SET current_security_level = '0' WHERE id_user = '$idUser'";
            mysqli_query($this->db->getDb(), $query);
            
            $json['success'] = 1;
            $json['message'] = "You have just logged out to 0 security level!";
            
            return $json;
        }

        public function loginFirstSL($idUser, $level1_password)
        {
            // check if password is correct
            $query = "SELECT level1_password FROM ".$this->db_table." WHERE id_user = '$idUser' AND level1_password = '$level1_password'";
            $result = mysqli_query($this->db->getDb(), $query);
            
            if(mysqli_num_rows($result) > 0)
            { 
                // login to SL1
                $query = "UPDATE ".$this->db_table." SET current_security_level = '1' WHERE id_user = '$idUser'";
                mysqli_query($this->db->getDb(), $query);
                
                $json['success'] = 1;
                $json['message'] = "You have just been logged into 1 security level!";    
            }
            else
            {
                // incorrect SL1 password
                $json['success'] = 0;
                $json['message'] = "Incorrect password";
            }
            
            return $json;
        }
        
        public function loginSecondSL($idUser, $level1_password, $level2_password)
        {
            // check if password is correct
            $query = "SELECT level1_password, level2_password FROM ".$this->db_table." WHERE id_user = '$idUser' AND level1_password = '$level1_password' AND level2_password = '$level2_password'";
            $result = mysqli_query($this->db->getDb(), $query);
            
            if(mysqli_num_rows($result) > 0)
            {
                // login to SL2
                $query = "UPDATE ".$this->db_table." SET current_security_level = '2' WHERE id_user = '$idUser'";
                mysqli_query($this->db->getDb(), $query);
                
                $json['success'] = 1;
                $json['message'] = "You have just been logged into 2 security level!";
            }
            else
            {
                // incorrect password(s)
                $json['success'] = 0;
                $json['message'] = "Incorrect password";
            }
            
            return $json;
        }
        
        public function changeEmail($idUser, $newValue, $password)
        {
            if($this->isPasswordCorrect($idUser, $password))
            {
                // check if new email is valid
                if($this->isValidEmail($newValue))
                {
                    // check if new email is already taken
                    $query = "SELECT email FROM ".$this->db_table." WHERE email = '$newValue' AND id_user != '$idUser'";
                    $result = mysqli_query($this->db->getDb(), $query);
                    if(mysqli_num_rows($result) == 0)
                    {
                        // update email 
                        $query = "UPDATE ".$this->db_table." SET email = '$newValue', updated_at = NOW() WHERE id_user = '$idUser'";
                        mysqli_query($this->db->getDb(), $query);
                
                        $json['success'] = 1;
                        $json['message'] = "Your email has been updated.";
                    }
                    else
                    {
                        // email already taken
                        $json['success'] = 0;
                        $json['message'] = "This email has been associated with another user.";
                    }
                } 
                else
                {
                    // invalid email
                   $json['success'] = 0;
                   $json['message'] = "Invalid email.";
                }
            }
            else
            {
                // incorrect password
                $json['success'] = 0;
                $json['message'] = "Incorrect password."; 
            }
            return $json;
        }
        
        public function changePassword($idUser, $newValue, $password)
        { 
            if($this->isPasswordCorrect($idUser, $password))
            {
                // update password 
                $query = "UPDATE ".$this->db_table." SET password = '$newValue', updated_at = NOW() WHERE id_user = '$idUser'";
                mysqli_query($this->db->getDb(), $query);
                
                $json['success'] = 1;
                $json['message'] = "Your password has been updated.";
            } 
            else
            {
                // incorrect password
                $json['success'] = 0;
                $json['message'] = "Incorrect current password."; 
            }
            
            return $json;
        }
        
        public function changeSL1Password($idUser, $newValue, $password)
        {
            if($this->isPasswordCorrect($idUser, $password))
            {
                // update security level 1 password
                $query = "UPDATE ".$this->db_table." SET level1_password = '$newValue', updated_at = NOW() WHERE id_user = '$idUser'";
                mysqli_query($this->db->getDb(), $query);
                
                $json['success'] = 1;
                $json['message'] = "Your password for Security Level 1 has been updated.";
            } 
            else
            {
                // incorrect password
                $json['success'] = 0;
                $json['message'] = "Incorrect current password."; 
            }
            
            return $json;
        }
        
        public function changeSL2Password($idUser, $newValue, $password)
        {
            if($this->isPasswordCorrect($idUser, $password))
            {
                // update security level 2 password
                $query = "UPDATE ".$this->db_table." SET level2_password = '$newValue', updated_at = NOW() WHERE id_user = '$idUser'";
                mysqli_query($this->db->getDb(), $query);
                
                $json['success'] = 1;
                $json['message'] = "Your password for Security Level 2 has been updated.";
            } 
            else
            {
                // incorrect password
                $json['success'] = 0;
                $json['message'] = "Incorrect current password."; 
            }
            
            return $json;
        }
        
        public function changeAutoLogout($idUser, $newValue, $password)
        {
            if($this->isPasswordCorrect($idUser, $password))
            {
                if($newValue)
                {
                    // update auto logout time
                    $query = "UPDATE ".$this->db_table." SET login_time = '$newValue', updated_at = NOW() WHERE id_user = '$idUser'";
                    mysqli_query($this->db->getDb(), $query);
                
                    $json['success'] = 1;
                    $json['message'] = "Your auto logout time has been changed.";
                }
                else
                {
                    // turn off auto logout
                    $query = "UPDATE ".$this->db_table." SET login_time = NULL, updated_at = NOW() WHERE id_user = '$idUser'";
                    mysqli_query($this->db->getDb(), $query);    
                    
                    $json['success'] = 1;
                    $json['message'] = "Auto logout time has been changed. You haven't got any auto logout time.";  
                }
            } 
            else
            {
                // incorrect password
                $json['success'] = 0;
                $json['message'] = "Incorrect password."; 
            }
            
            return $json;
        }
        
        // update fields in database when successfully logging in
        public function updateOnLoginSuccess($login)
        {
            $query = "UPDATE ".$this->db_table." SET current_security_level = '0', status = '1', login_fails = '0', last_login_success = NOW() WHERE login = '$login'";
            mysqli_query($this->db->getDb(), $query); 
        }
        
        // update login fails counter and date of last login fail 
        public function updateOnLoginFail($login)  
        {
            $query = "UPDATE ".$this->db_table." SET login_fails = login_fails + 1, last_login_fail = NOW() WHERE login = '$login'"; 
            mysqli_query($this->db->getDb(), $query);
        }
        
        // check if user failed to login 3x in a row
        public function isLoginFailedThirdTime($login) 
        {
            $query = "SELECT login_fails FROM ".$this->db_table." WHERE login = '$login'";
            $result = mysqli_query($this->db->getDb(), $query);
            $row = mysqli_fetch_row($result);

            // login fail 3x in a row
            if($row[0] == '3')
                return true;
            
            return false;
        }
        
        // return true if account is available 
        public function isAccountAvailable($login)
        {
            $query = "SELECT available FROM ".$this->db_table." WHERE login = '$login'";
            $result = mysqli_query($this->db->getDb(), $query);
            $row = mysqli_fetch_row($result);

            return ($row[0] == '1');
        }
        
        // return true if account is unavailable for more than 30 minutes
        public function checkAccountAvailability($login)
        {            
            $query = "SELECT last_login_fail from ".$this->db_table." WHERE login = '$login' ";
            $result = mysqli_query($this->db->getDb(), $query);
            $last_login_fail = mysqli_fetch_row($result);

            // get last failed login date and current date
            $t1 = $last_login_fail['0'];
            $t2 = date("Y-m-d h:i:s", strtotime('+12 hours'));

            // calculate time of account unavailability
            $a1 = strtotime($t1);
            $a2 = strtotime($t2);
            $diff = $a2 - $a1;	

            // return true if account is unavailable for more than 30 minutes, otherwise return false
            return $diff > 43230;
        }    
        
        // block user account for 30 minutes (used after 3x login fail in a row)
        public function blockAccount($login)                            
        {
            $query = "UPDATE ".$this->db_table." SET login_fails = 0, available = '0' WHERE login = '$login'";
            mysqli_query($this->db->getDb(), $query);
        }
        
         // unblock user account
        public function unblockAccount($login)                            
        {
            $query = "UPDATE ".$this->db_table." SET available ='1' WHERE login = '$login'";
            $result = mysqli_query($this->db->getDb(), $query);
        }
        
        // return true if password is correct 
        public function isPasswordCorrect($idUser, $password)
        {
            $query = "SELECT password FROM ".$this->db_table." WHERE id_user = '$idUser'";
            $result = mysqli_query($this->db->getDb(), $query);
            $row = mysqli_fetch_row($result);
            
            return ($row['0'] == "$password");
        }
        
        // return true if account with that login and password exists
        public function isUserExist($login, $password)
        {
            $query = "SELECT * FROM ".$this->db_table." WHERE login = '$login' AND password = '$password' LIMIT 1";
            $result = mysqli_query($this->db->getDb(), $query);
            
            return (mysqli_num_rows($result) > 0);
        }
        
        // return true if account with that login or email exists
        public function isLoginEmailExist($login, $email)
        {              
            $query = "SELECT * FROM ".$this->db_table." WHERE login = '$login' OR email = '$email'";
            $result = mysqli_query($this->db->getDb(), $query);
            
            return (mysqli_num_rows($result) > 0);
        }
        
        // return true if account with that login exists
        public function isLoginExist($login) 
        {
            $query = "SELECT * from ".$this->db_table." WHERE login = '$login' LIMIT 1";
            $result = mysqli_query($this->db->getDb(), $query);
            
            return (mysqli_num_rows($result) > 0);
        }
        
        // check if email is valid
        public function isValidEmail($email)
        {    
            return filter_var($email, FILTER_VALIDATE_EMAIL);
        }
    }

?>