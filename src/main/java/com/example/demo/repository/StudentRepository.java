package com.example.demo.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.StudentEntity;


public interface StudentRepository extends JpaRepository<StudentEntity, Integer> {
    // Additional query methods can be defined here if needed
    Optional<StudentEntity> findByName(String name);
}
