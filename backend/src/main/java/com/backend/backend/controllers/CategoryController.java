package com.backend.backend.controllers;

import com.backend.backend.dto.CategoryDTO;
import com.backend.backend.dto.CategoryResponseDTO;
import com.backend.backend.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(
        origins = "https://pnc-proyecto-final-frontend-grupo-0-delta.vercel.app",
        allowedHeaders = "*",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // Maneja peticion crear una catergoria
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody CategoryDTO dto) {
        try {
            categoryService.createCategory(dto);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Category created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
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

    // Maneja peticion para eliminar una categoria por ID
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<?> deleteCategory(@PathVariable String categoryId) {
        try {
            categoryService.deleteCategory(categoryId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Category deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
