package com.hanghae.naegahama.repository;

import com.hanghae.naegahama.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File,Long> {
}