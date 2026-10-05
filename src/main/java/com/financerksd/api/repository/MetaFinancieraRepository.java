package com.financerksd.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financerksd.api.model.MetaFinanciera;

public interface MetaFinancieraRepository extends JpaRepository<MetaFinanciera, Integer> {
}
