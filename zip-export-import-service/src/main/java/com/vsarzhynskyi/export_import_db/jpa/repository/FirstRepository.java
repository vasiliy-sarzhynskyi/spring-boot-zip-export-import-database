package com.vsarzhynskyi.export_import_db.jpa.repository;

import com.vsarzhynskyi.export_import_db.jpa.entity.FirstEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FirstRepository extends JpaRepository<FirstEntity, Long> {
}
