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


![dodaj haslo_Easy-Resize com](https://user-images.githubusercontent.com/48474276/72255924-3e3c5100-3608-11ea-86ae-1722f7d814f2.jpg)

