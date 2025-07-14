package com.example.demo.mapper.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.demo.dto.request.ClassRequest;
import com.example.demo.entity.ClassEntity;
import com.example.demo.mapper.BaseRequestMapper;

@Mapper(componentModel = "spring")
public interface ClassRequestMapper extends BaseRequestMapper<ClassRequest, ClassEntity> {
    
    @Override
    @Mapping(target = "id", ignore = true) // Never map ID from request - let it be auto-generated
    ClassEntity toEntity(ClassRequest dto);
    
    @Override
    @Mapping(target = "id", ignore = true) // Never update ID during updates
    void update(@org.mapstruct.MappingTarget ClassEntity entity, ClassRequest dto);
}
