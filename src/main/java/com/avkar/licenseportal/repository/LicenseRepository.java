package com.avkar.licenseportal.repository;

import com.avkar.licenseportal.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicenseRepository extends JpaRepository<License, Long> {
}
