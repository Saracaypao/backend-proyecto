package com.backend.backend.controllers;

import com.backend.backend.dto.CategoryDTO;
import com.backend.backend.dto.CategoryResponseDTO;
import com.backend.backend.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // Maneja peticion crear una catergoria
    @PostMapping
    public ResponseEntity<Void> createCategory(@RequestBody CategoryDTO dto) {
        try {
            categoryService.createCategory(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Maneja peticion para obtener todas las categorias
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAll() {
        try {
            return ResponseEntity.ok(categoryService.findAll());
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    // Maneja peticion para obtener una categoria por ID
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable String categoryId) {
        try {
            CategoryResponseDTO category = categoryService.findById(categoryId);
            return ResponseEntity.ok(category);
        } catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }
}
