package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.response.ClassResponse;

public interface ClassService {
    // Read operations
    List<ClassResponse> getAllClasses();
    ClassResponse getClassById(Integer id);

}
