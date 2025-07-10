package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.ClassEntity;

public interface ClassRepository extends JpaRepository<ClassEntity, Integer> {
    // Additional query methods can be defined here if needed
    Optional<ClassEntity> findByName(String name);
}
