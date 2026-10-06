package com.example.praticalTestBE.domain.entity;

import com.example.praticalTestBE.domain.enums.ConditionAction;
import com.example.praticalTestBE.domain.enums.ConditionOperator;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Objects;

/**
 * Entity representing dynamic conditional logic rules driving component behavior.
 */
@Entity
@Table(
    name = "tbl_form_conditions",
    indexes = {
        @Index(name = "idx_condition_version", columnList = "form_version_id"),
        @Index(name = "idx_condition_trigger", columnList = "trigger_component_id"),
        @Index(name = "idx_condition_target", columnList = "target_component_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FormCondition extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_version_id", nullable = false, foreignKey = @ForeignKey(name = "fk_condition_version"))
    @JsonBackReference("version-conditions")
    private FormVersion formVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trigger_component_id", nullable = false, foreignKey = @ForeignKey(name = "fk_condition_trigger"))
    private FormComponent triggerComponent;

    @NotNull(message = "Condition operator is mandatory")
    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false, length = 50)
    private ConditionOperator operator;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "logic_operator", length = 10)
    private com.example.praticalTestBE.domain.enums.LogicOperator logicOperator = com.example.praticalTestBE.domain.enums.LogicOperator.AND;

    @Column(name = "trigger_value", columnDefinition = "TEXT")
    private String triggerValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_component_id", nullable = false, foreignKey = @ForeignKey(name = "fk_condition_target"))
    private FormComponent targetComponent;

    @NotNull(message = "Condition action is mandatory")
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private ConditionAction action;

    @Column(name = "action_value", columnDefinition = "TEXT")
    private String actionValue;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormCondition that = (FormCondition) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
