package app.dao;

import app.exceptions.EntityNotFoundException;
import app.model.Role;
import app.model.User;

import java.util.List;

public interface ISecurityDAO {
    User createUser(String username, String password, String email, Integer phoneNumber);
    Role createRole(String role);
    User addRoleToUser(String username, String role);
    //User create();
    User update(User user);
    User UpdateUser(String name, String password);
    List<User> getAlleUser();
    User getUserById(int id);
    void deleteUser(int id);
    User verifyUserForReset(String email, String password) throws EntityNotFoundException;
    User UpdatePassword(User user, String newPassword);
}
