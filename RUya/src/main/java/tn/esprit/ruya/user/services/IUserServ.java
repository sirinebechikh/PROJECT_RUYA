package tn.esprit.ruya.user.services;

import tn.esprit.ruya.models.User;

import java.util.List;
import java.util.Optional;

public interface IUserServ {
     List<User> getAllUsers();
      User updateUser(Long id, User updatedUser);
      void deleteUser(Long id) ;
      User createUser(User user);
      Optional<User> getUserById(Long id);
     Optional<User> findByUsernameOrEmail(String input) ;
     boolean existsByUsername(String username);
     boolean existsByEmail(String email);
     User updateUserStatus(Long id, boolean active);

}
