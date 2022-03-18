package com.alert.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.alert.domain.Alert} entity.
 */
public class AlertDTO implements Serializable {

    private Long id;

    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlertDTO)) {
            return false;
        }

        AlertDTO alertDTO = (AlertDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, alertDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlertDTO{" +
            "id=" + getId() +
            ", content='" + getContent() + "'" +
            "}";
    }
}
