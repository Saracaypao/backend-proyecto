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

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody CategoryDTO dto) {
        try {
            categoryService.createCategory(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAll() {
        try {
            return ResponseEntity.ok(categoryService.findAll());
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
}
