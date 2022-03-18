package com.alert.repository;

import com.alert.domain.Alert;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Alert entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long>, JpaSpecificationExecutor<Alert> {}
