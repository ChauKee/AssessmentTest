package com.vanguard.assessment.repository;

import com.vanguard.assessment.entity.CsvImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CsvImportRepository extends JpaRepository<CsvImport, Integer> {
}
