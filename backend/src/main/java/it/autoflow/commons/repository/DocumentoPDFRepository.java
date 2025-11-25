package it.autoflow.commons.repository;

import it.autoflow.commons.entity.DocumentoPDF;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoPDFRepository extends JpaRepository<DocumentoPDF, Long> {
}