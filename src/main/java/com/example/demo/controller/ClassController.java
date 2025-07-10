package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.APIResponse;
import com.example.demo.dto.response.ClassResponse;
import com.example.demo.service.ClassService;

import io.micrometer.common.lang.NonNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/classes")
public class ClassController {
    private final ClassService classService;
    
    @GetMapping("")
    public ResponseEntity<APIResponse> GetList() {
        List<ClassResponse> classList = classService.getAllClasses();
        APIResponse response = APIResponse.builder()
                                          .statusCode(200)
                                          .message("List of classes retrieved successfully")
                                          .data(classList)
                                          .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse> GetById(@PathVariable("id") @NonNull Integer id) {
        ClassResponse classResponse = classService.getClassById(id);
        APIResponse response = APIResponse.builder()
                                          .statusCode(200)
                                          .message("Class retrieved successfully")
                                          .data(classResponse)
                                          .build();
        return ResponseEntity.ok(response);
    }
}
