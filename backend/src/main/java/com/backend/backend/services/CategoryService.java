package com.backend.backend.services;

import com.backend.backend.dto.CategoryDTO;
import com.backend.backend.dto.CategoryResponseDTO;
import com.backend.backend.entities.Category;
import com.backend.backend.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Logica para crear una nueva categoria
    public void createCategory(CategoryDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new RuntimeException("A category with that name already exists");
        }

        Category category = Category.builder()
                .name(dto.getName())
                .build();

        categoryRepository.save(category);
    }

    // Logica que devuelve todas las categorias
    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAll().stream().map(c ->
                CategoryResponseDTO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build()
        ).collect(Collectors.toList());
    }

    // Logica que devuelve una categoria por ID
    public CategoryResponseDTO findById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    // Logica para eliminar una categoria por ID
    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        categoryRepository.delete(category);
    }
}