package com.alert.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.alert.IntegrationTest;
import com.alert.domain.Alert;
import com.alert.repository.AlertRepository;
import com.alert.service.criteria.AlertCriteria;
import com.alert.service.dto.AlertDTO;
import com.alert.service.mapper.AlertMapper;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AlertResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AlertResourceIT {

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/alerts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private AlertMapper alertMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAlertMockMvc;

    private Alert alert;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Alert createEntity(EntityManager em) {
        Alert alert = new Alert().content(DEFAULT_CONTENT);
        return alert;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Alert createUpdatedEntity(EntityManager em) {
        Alert alert = new Alert().content(UPDATED_CONTENT);
        return alert;
    }

    @BeforeEach
    public void initTest() {
        alert = createEntity(em);
    }

    @Test
    @Transactional
    void createAlert() throws Exception {
        int databaseSizeBeforeCreate = alertRepository.findAll().size();
        // Create the Alert
        AlertDTO alertDTO = alertMapper.toDto(alert);
        restAlertMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(alertDTO)))
            .andExpect(status().isCreated());

        // Validate the Alert in the database
        List<Alert> alertList = alertRepository.findAll();
        assertThat(alertList).hasSize(databaseSizeBeforeCreate + 1);
        Alert testAlert = alertList.get(alertList.size() - 1);
        assertThat(testAlert.getContent()).isEqualTo(DEFAULT_CONTENT);
    }

    @Test
    @Transactional
    void createAlertWithExistingId() throws Exception {
        // Create the Alert with an existing ID
        alert.setId(1L);
        AlertDTO alertDTO = alertMapper.toDto(alert);

        int databaseSizeBeforeCreate = alertRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAlertMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(alertDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alert in the database
        List<Alert> alertList = alertRepository.findAll();
        assertThat(alertList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllAlerts() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);

        // Get all the alertList
        restAlertMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alert.getId().intValue())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)));
    }

    @Test
    @Transactional
    void getAlert() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);

        // Get the alert
        restAlertMockMvc
            .perform(get(ENTITY_API_URL_ID, alert.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(alert.getId().intValue()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT));
    }

    @Test
    @Transactional
    void getAlertsByIdFiltering() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);

        Long id = alert.getId();

        defaultAlertShouldBeFound("id.equals=" + id);
        defaultAlertShouldNotBeFound("id.notEquals=" + id);

        defaultAlertShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultAlertShouldNotBeFound("id.greaterThan=" + id);

        defaultAlertShouldBeFound("id.lessThanOrEqual=" + id);
        defaultAlertShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllAlertsByContentIsEqualToSomething() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);

        // Get all the alertList where content equals to DEFAULT_CONTENT
        defaultAlertShouldBeFound("content.equals=" + DEFAULT_CONTENT);

        // Get all the alertList where content equals to UPDATED_CONTENT
        defaultAlertShouldNotBeFound("content.equals=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllAlertsByContentIsNotEqualToSomething() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);

        // Get all the alertList where content not equals to DEFAULT_CONTENT
        defaultAlertShouldNotBeFound("content.notEquals=" + DEFAULT_CONTENT);

        // Get all the alertList where content not equals to UPDATED_CONTENT
        defaultAlertShouldBeFound("content.notEquals=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllAlertsByContentIsInShouldWork() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);

        // Get all the alertList where content in DEFAULT_CONTENT or UPDATED_CONTENT
        defaultAlertShouldBeFound("content.in=" + DEFAULT_CONTENT + "," + UPDATED_CONTENT);

        // Get all the alertList where content equals to UPDATED_CONTENT
        defaultAlertShouldNotBeFound("content.in=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllAlertsByContentIsNullOrNotNull() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);

        // Get all the alertList where content is not null
        defaultAlertShouldBeFound("content.specified=true");

        // Get all the alertList where content is null
        defaultAlertShouldNotBeFound("content.specified=false");
    }

    @Test
    @Transactional
    void getAllAlertsByContentContainsSomething() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);

        // Get all the alertList where content contains DEFAULT_CONTENT
        defaultAlertShouldBeFound("content.contains=" + DEFAULT_CONTENT);

        // Get all the alertList where content contains UPDATED_CONTENT
        defaultAlertShouldNotBeFound("content.contains=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllAlertsByContentNotContainsSomething() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);

        // Get all the alertList where content does not contain DEFAULT_CONTENT
        defaultAlertShouldNotBeFound("content.doesNotContain=" + DEFAULT_CONTENT);

        // Get all the alertList where content does not contain UPDATED_CONTENT
        defaultAlertShouldBeFound("content.doesNotContain=" + UPDATED_CONTENT);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAlertShouldBeFound(String filter) throws Exception {
        restAlertMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alert.getId().intValue())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)));

        // Check, that the count call also returns 1
        restAlertMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAlertShouldNotBeFound(String filter) throws Exception {
        restAlertMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAlertMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAlert() throws Exception {
        // Get the alert
        restAlertMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewAlert() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);

        int databaseSizeBeforeUpdate = alertRepository.findAll().size();

        // Update the alert
        Alert updatedAlert = alertRepository.findById(alert.getId()).get();
        // Disconnect from session so that the updates on updatedAlert are not directly saved in db
        em.detach(updatedAlert);
        updatedAlert.content(UPDATED_CONTENT);
        AlertDTO alertDTO = alertMapper.toDto(updatedAlert);

        restAlertMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alertDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(alertDTO))
            )
            .andExpect(status().isOk());

        // Validate the Alert in the database
        List<Alert> alertList = alertRepository.findAll();
        assertThat(alertList).hasSize(databaseSizeBeforeUpdate);
        Alert testAlert = alertList.get(alertList.size() - 1);
        assertThat(testAlert.getContent()).isEqualTo(UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void putNonExistingAlert() throws Exception {
        int databaseSizeBeforeUpdate = alertRepository.findAll().size();
        alert.setId(count.incrementAndGet());

        // Create the Alert
        AlertDTO alertDTO = alertMapper.toDto(alert);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlertMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alertDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(alertDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Alert in the database
        List<Alert> alertList = alertRepository.findAll();
        assertThat(alertList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAlert() throws Exception {
        int databaseSizeBeforeUpdate = alertRepository.findAll().size();
        alert.setId(count.incrementAndGet());

        // Create the Alert
        AlertDTO alertDTO = alertMapper.toDto(alert);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlertMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(alertDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Alert in the database
        List<Alert> alertList = alertRepository.findAll();
        assertThat(alertList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAlert() throws Exception {
        int databaseSizeBeforeUpdate = alertRepository.findAll().size();
        alert.setId(count.incrementAndGet());

        // Create the Alert
        AlertDTO alertDTO = alertMapper.toDto(alert);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlertMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(alertDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Alert in the database
        List<Alert> alertList = alertRepository.findAll();
        assertThat(alertList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAlertWithPatch() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);

        int databaseSizeBeforeUpdate = alertRepository.findAll().size();

        // Update the alert using partial update
        Alert partialUpdatedAlert = new Alert();
        partialUpdatedAlert.setId(alert.getId());

        partialUpdatedAlert.content(UPDATED_CONTENT);

        restAlertMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlert.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAlert))
            )
            .andExpect(status().isOk());

        // Validate the Alert in the database
        List<Alert> alertList = alertRepository.findAll();
        assertThat(alertList).hasSize(databaseSizeBeforeUpdate);
        Alert testAlert = alertList.get(alertList.size() - 1);
        assertThat(testAlert.getContent()).isEqualTo(UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void fullUpdateAlertWithPatch() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);

        int databaseSizeBeforeUpdate = alertRepository.findAll().size();

        // Update the alert using partial update
        Alert partialUpdatedAlert = new Alert();
        partialUpdatedAlert.setId(alert.getId());

        partialUpdatedAlert.content(UPDATED_CONTENT);

        restAlertMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlert.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAlert))
            )
            .andExpect(status().isOk());

        // Validate the Alert in the database
        List<Alert> alertList = alertRepository.findAll();
        assertThat(alertList).hasSize(databaseSizeBeforeUpdate);
        Alert testAlert = alertList.get(alertList.size() - 1);
        assertThat(testAlert.getContent()).isEqualTo(UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void patchNonExistingAlert() throws Exception {
        int databaseSizeBeforeUpdate = alertRepository.findAll().size();
        alert.setId(count.incrementAndGet());

        // Create the Alert
        AlertDTO alertDTO = alertMapper.toDto(alert);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlertMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, alertDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(alertDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Alert in the database
        List<Alert> alertList = alertRepository.findAll();
        assertThat(alertList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAlert() throws Exception {
        int databaseSizeBeforeUpdate = alertRepository.findAll().size();
        alert.setId(count.incrementAndGet());

        // Create the Alert
        AlertDTO alertDTO = alertMapper.toDto(alert);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlertMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(alertDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Alert in the database
        List<Alert> alertList = alertRepository.findAll();
        assertThat(alertList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAlert() throws Exception {
        int databaseSizeBeforeUpdate = alertRepository.findAll().size();
        alert.setId(count.incrementAndGet());

        // Create the Alert
        AlertDTO alertDTO = alertMapper.toDto(alert);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlertMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(alertDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Alert in the database
        List<Alert> alertList = alertRepository.findAll();
        assertThat(alertList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAlert() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);

        int databaseSizeBeforeDelete = alertRepository.findAll().size();

        // Delete the alert
        restAlertMockMvc
            .perform(delete(ENTITY_API_URL_ID, alert.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Alert> alertList = alertRepository.findAll();
        assertThat(alertList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
