package com.SafeNet.Backend.domain.file.repository;

import com.SafeNet.Backend.domain.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
