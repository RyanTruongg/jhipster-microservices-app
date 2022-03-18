package com.alert.service.mapper;

import com.alert.domain.Alert;
import com.alert.service.dto.AlertDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Alert} and its DTO {@link AlertDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface AlertMapper extends EntityMapper<AlertDTO, Alert> {}
