package com.alert.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.alert.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AlertDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AlertDTO.class);
        AlertDTO alertDTO1 = new AlertDTO();
        alertDTO1.setId(1L);
        AlertDTO alertDTO2 = new AlertDTO();
        assertThat(alertDTO1).isNotEqualTo(alertDTO2);
        alertDTO2.setId(alertDTO1.getId());
        assertThat(alertDTO1).isEqualTo(alertDTO2);
        alertDTO2.setId(2L);
        assertThat(alertDTO1).isNotEqualTo(alertDTO2);
        alertDTO1.setId(null);
        assertThat(alertDTO1).isNotEqualTo(alertDTO2);
    }
}
