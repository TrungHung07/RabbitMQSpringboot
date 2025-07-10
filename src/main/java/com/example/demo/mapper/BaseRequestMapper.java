package com.example.demo.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

/**
 * @param <D>> DTO Request type
 * @param <E> Entity type
 */
public interface BaseRequestMapper<D, E> {
    E toEntity(D dto);

    List<E> toListEntity(List<D> dtoList);

    // Partial Update
    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget E entity, D dto);

 
}