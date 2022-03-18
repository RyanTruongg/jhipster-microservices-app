package com.alert.web.rest;

import com.alert.repository.AlertRepository;
import com.alert.service.AlertQueryService;
import com.alert.service.AlertService;
import com.alert.service.criteria.AlertCriteria;
import com.alert.service.dto.AlertDTO;
import com.alert.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.alert.domain.Alert}.
 */
@RestController
@RequestMapping("/api")
public class AlertResource {

    private final Logger log = LoggerFactory.getLogger(AlertResource.class);

    private static final String ENTITY_NAME = "alertAlert";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AlertService alertService;

    private final AlertRepository alertRepository;

    private final AlertQueryService alertQueryService;

    public AlertResource(AlertService alertService, AlertRepository alertRepository, AlertQueryService alertQueryService) {
        this.alertService = alertService;
        this.alertRepository = alertRepository;
        this.alertQueryService = alertQueryService;
    }

    /**
     * {@code POST  /alerts} : Create a new alert.
     *
     * @param alertDTO the alertDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new alertDTO, or with status {@code 400 (Bad Request)} if the alert has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/alerts")
    public ResponseEntity<AlertDTO> createAlert(@RequestBody AlertDTO alertDTO) throws URISyntaxException {
        log.debug("REST request to save Alert : {}", alertDTO);
        if (alertDTO.getId() != null) {
            throw new BadRequestAlertException("A new alert cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AlertDTO result = alertService.save(alertDTO);
        return ResponseEntity
            .created(new URI("/api/alerts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /alerts/:id} : Updates an existing alert.
     *
     * @param id the id of the alertDTO to save.
     * @param alertDTO the alertDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alertDTO,
     * or with status {@code 400 (Bad Request)} if the alertDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the alertDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/alerts/{id}")
    public ResponseEntity<AlertDTO> updateAlert(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody AlertDTO alertDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Alert : {}, {}", id, alertDTO);
        if (alertDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alertDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alertRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        AlertDTO result = alertService.save(alertDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, alertDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /alerts/:id} : Partial updates given fields of an existing alert, field will ignore if it is null
     *
     * @param id the id of the alertDTO to save.
     * @param alertDTO the alertDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alertDTO,
     * or with status {@code 400 (Bad Request)} if the alertDTO is not valid,
     * or with status {@code 404 (Not Found)} if the alertDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the alertDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/alerts/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AlertDTO> partialUpdateAlert(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody AlertDTO alertDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Alert partially : {}, {}", id, alertDTO);
        if (alertDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alertDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alertRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AlertDTO> result = alertService.partialUpdate(alertDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, alertDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /alerts} : get all the alerts.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of alerts in body.
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<AlertDTO>> getAllAlerts(
        AlertCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get Alerts by criteria: {}", criteria);
        Page<AlertDTO> page = alertQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /alerts/count} : count all the alerts.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/alerts/count")
    public ResponseEntity<Long> countAlerts(AlertCriteria criteria) {
        log.debug("REST request to count Alerts by criteria: {}", criteria);
        return ResponseEntity.ok().body(alertQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /alerts/:id} : get the "id" alert.
     *
     * @param id the id of the alertDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the alertDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/alerts/{id}")
    public ResponseEntity<AlertDTO> getAlert(@PathVariable Long id) {
        log.debug("REST request to get Alert : {}", id);
        Optional<AlertDTO> alertDTO = alertService.findOne(id);
        return ResponseUtil.wrapOrNotFound(alertDTO);
    }

    /**
     * {@code DELETE  /alerts/:id} : delete the "id" alert.
     *
     * @param id the id of the alertDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/alerts/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        log.debug("REST request to delete Alert : {}", id);
        alertService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
