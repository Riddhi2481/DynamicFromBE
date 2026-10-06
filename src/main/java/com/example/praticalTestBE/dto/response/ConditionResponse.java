package com.example.praticalTestBE.dto.response;

import com.example.praticalTestBE.domain.enums.ConditionAction;
import com.example.praticalTestBE.domain.enums.ConditionOperator;
import com.example.praticalTestBE.domain.enums.LogicOperator;

/**
 * Response payload representing a dynamic conditional rule.
 */
public record ConditionResponse(
    Long id,
    String triggerFieldCode,
    ConditionOperator operator,
    LogicOperator logicOperator,
    String triggerValue,
    String targetFieldCode,
    ConditionAction action,
    String actionValue
) {}
