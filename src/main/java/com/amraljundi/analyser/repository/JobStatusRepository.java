package com.amraljundi.analyser.repository;

import com.amraljundi.analyser.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobStatusRepository extends JpaRepository<JobStatus, String> {
}
