package com.SafeNet.Backend.domain.files.repository;

import com.SafeNet.Backend.domain.files.domain.Files;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<Files, Long> {
}
