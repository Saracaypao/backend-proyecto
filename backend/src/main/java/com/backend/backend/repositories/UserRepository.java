package com.backend.backend.repositories;

import com.backend.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>{
    Optional<User> findByEmailIgnoreCase(String email);

}
