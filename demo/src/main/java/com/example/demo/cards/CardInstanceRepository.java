package com.example.demo.cards;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardInstanceRepository extends CrudRepository<CardInstance, Long> {
}
