package com.petlove.weblove.modules.file.repository;

import com.petlove.weblove.modules.file.entity.FileObject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileObjectRepository extends JpaRepository<FileObject, Long> {
}
