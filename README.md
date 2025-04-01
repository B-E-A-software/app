BEASoftware is an application that allows users to store lists of todos. They could be private public or shared with a list of other users.

# Contributors

Barbu Eduard - Gr. 344
Florescu Bogdan Ilie - Gr. 343
Matei Alexandru Cristian - Gr. 343

# Functionalities

User CRUD:
--- create user when registering
--- be able to see(read) all users
--- update user details through PUT or PATCH
--- delete user from database

Authentication:
--- each guest is required to register or log in in order to use functionalities
--- passwords are encrypted
--- the user receives a session token when they are logged in
--- an user cannot see or modify another user's TODO lists or profiles

Authorization:
---roles: USER, ADMIN
--- admins have full control and can use every exposed endpoint, while users can only access the main app

TODOlists:
--- each user can create their own TODOlist, which represents a list of activities they want to do, with a description
--- an user can share their TODOlist with other users that request access to that TODOlists
--- when accepted, an user can see the other person's TODOlist, but only the owner can modify it




