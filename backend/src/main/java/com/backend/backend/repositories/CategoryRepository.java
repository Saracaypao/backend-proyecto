package com.backend.backend.repositories;

import com.backend.backend.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {
    boolean existsByName(String name);
}
