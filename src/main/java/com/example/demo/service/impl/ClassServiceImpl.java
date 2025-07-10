package com.example.demo.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.response.ClassResponse;
import com.example.demo.entity.ClassEntity;
import com.example.demo.mapper.response.ClassResponseMapper;
import com.example.demo.repository.ClassRepository;
import com.example.demo.service.ClassService;
import com.example.demo.service.RedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassServiceImpl implements ClassService {
    private final ClassRepository classRepository;
    private final ClassResponseMapper responseMapper;
    private final RedisService redisService; 
    @Override
    public List<ClassResponse> getAllClasses() {
        return responseMapper.toListDto(classRepository.findAll());
    }

    @Override
    public ClassResponse getClassById(Integer id) {
        String cacheKey = "class:" + id;
        ClassResponse cachedClass = redisService.get(cacheKey, ClassResponse.class);
        if  (cachedClass != null) {
            log.info("Cache hit for class with id: {}", id);
            return cachedClass;
        }

        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + id));
        redisService.set(cacheKey, responseMapper.toDto(classEntity));
        return responseMapper.toDto(classEntity);
    }
    
    
}
