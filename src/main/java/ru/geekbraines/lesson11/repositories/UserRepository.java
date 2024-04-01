package ru.geekbraines.lesson11.repositories;



import java.util.Optional;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,Long> {

   // Optional<ru.geekbraines.lesson11.entities.User> findByUsername(String username);

}
