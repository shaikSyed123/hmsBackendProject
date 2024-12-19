package com.tsarit.form_1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tsarit.form_1.model.SupportTicket;

@Repository
public interface SupportticketRepository extends JpaRepository<SupportTicket, Integer>{

}



