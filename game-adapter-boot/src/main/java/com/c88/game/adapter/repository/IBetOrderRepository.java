package com.c88.game.adapter.repository;

import java.util.List;

public interface IBetOrderRepository<T> {

    <S extends T> S save(S entity);

    <S extends T> void saveAll(List<S> entities);

}