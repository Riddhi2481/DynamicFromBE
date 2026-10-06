package com.example.praticalTestBE.dto.request;

import com.example.praticalTestBE.domain.enums.ConditionAction;
import com.example.praticalTestBE.domain.enums.ConditionOperator;
import com.example.praticalTestBE.domain.enums.LogicOperator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for configuring a dynamic conditional rule.
 */
public record ConditionRequest(
    @NotBlank(message = "Trigger field code is mandatory")
    @Size(max = 100)
    String triggerFieldCode,

    @NotNull(message = "Condition operator is mandatory")
    ConditionOperator operator,

    LogicOperator logicOperator,

    String triggerValue,

    @NotBlank(message = "Target field code is mandatory")
    @Size(max = 100)
    String targetFieldCode,

    @NotNull(message = "Condition action is mandatory")
    ConditionAction action,

    String actionValue
) {
    public ConditionRequest {
        if (logicOperator == null) logicOperator = LogicOperator.AND;
    }
}
