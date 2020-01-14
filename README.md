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
- uses Volley library to communicate with database (REST API)
- needs internet connection 
- account password and security level passwords are hashed (using SHA-512) 
- passwords stored in database are encrypted (using AES-256) 
- hashed passwords are encryption keys for stored passwords


# Screenshots

![Rejestracja](https://user-images.githubusercontent.com/48474276/72256239-fbc74400-3608-11ea-8841-92e23c8884e2.jpg)
![Menu](https://user-images.githubusercontent.com/48474276/72256238-fbc74400-3608-11ea-9e79-dfa5d74ad6bc.jpg)
![Lista haseł](https://user-images.githubusercontent.com/48474276/72256237-fbc74400-3608-11ea-8623-4e0027ad3dda.jpg)
![Generator haseł](https://user-images.githubusercontent.com/48474276/72256236-fbc74400-3608-11ea-905c-34d0ad2e7706.jpg)
![Dodawanie hasła](https://user-images.githubusercontent.com/48474276/72256235-fbc74400-3608-11ea-8618-fdebeaeb1ddf.jpg)
![Dodawanie grupy haseł](https://user-images.githubusercontent.com/48474276/72257021-e05d3880-360a-11ea-9409-4e289ced5b0a.jpg)
