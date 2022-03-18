package com.alert.service;

import com.alert.service.dto.AlertDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.alert.domain.Alert}.
 */
public interface AlertService {
    /**
     * Save a alert.
     *
     * @param alertDTO the entity to save.
     * @return the persisted entity.
     */
    AlertDTO save(AlertDTO alertDTO);

    /**
     * Partially updates a alert.
     *
     * @param alertDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AlertDTO> partialUpdate(AlertDTO alertDTO);

    /**
     * Get all the alerts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AlertDTO> findAll(Pageable pageable);

    /**
     * Get the "id" alert.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AlertDTO> findOne(Long id);

    /**
     * Delete the "id" alert.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
