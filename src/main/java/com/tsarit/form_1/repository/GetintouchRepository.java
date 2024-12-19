package com.tsarit.form_1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tsarit.form_1.model.Getintouch;

@Repository
public interface GetintouchRepository extends JpaRepository<Getintouch, Integer>{

}
