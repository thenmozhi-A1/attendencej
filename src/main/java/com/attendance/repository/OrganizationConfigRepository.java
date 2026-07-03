package com.attendance.repository;

import com.attendance.entity.OrganizationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationConfigRepository extends JpaRepository<OrganizationConfig, Long> {
    Optional<OrganizationConfig> findByConfigKey(String configKey);
}
