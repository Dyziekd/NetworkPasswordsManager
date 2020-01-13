# NetworkPasswordsManager

Network Passwords Manager used to storing passwords. 

NPM features:
- creating new accounts
- storing passwords
- grouping passwords (e.g. Social Media, Games), groups can be nested
- 3 level access to passwords group:
  - Security Level 0 available after login
  - Security Level 1 available after entering SL 1 password
  - Security Level 2 available after entering SL 1 and SL 2 passwords
- reminder to change your password
- auto logout after inactivity 
- blocking the account for 30 minutes after entering incorrect password 3x in a row 
- algorithm assessing the password power 
- passwords generator 
------------------------------------------------------------------------------------
- uses SQL database 
- needs internet connection 
- account password and security level passwords are hashed (using SHA-512) 
- passwords stored in database are encrypted (using AES-256) 
- hashed passwords are encryption keys for stored passwords


# Screenshots

![Rejestracja](https://user-images.githubusercontent.com/48474276/72255510-35974b00-3607-11ea-89db-c838461f851a.png)
![Menu](https://user-images.githubusercontent.com/48474276/72255671-a0e11d00-3607-11ea-8024-c73db5693cce.png)
![Lista haseł](https://user-images.githubusercontent.com/48474276/72255507-34661e00-3607-11ea-94d2-781576aa6750.png)
![Dodawanie hasła](https://user-images.githubusercontent.com/48474276/72255500-2fa16a00-3607-11ea-9b70-f29ccd4cbe5e.png)
![Generator haseł](https://user-images.githubusercontent.com/48474276/72255506-3334f100-3607-11ea-8b06-a2721c173cef.png)
