package com.example.demo.mapper;

import java.util.List;

public interface BaseResponseMapper<D, E> {
    D toDto(E entity);
    List<D> toListDto(List<E> entityList);
}
