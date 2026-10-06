package com.example.praticalTestBE.service;

import com.example.praticalTestBE.domain.entity.FormCondition;
import com.example.praticalTestBE.domain.enums.ConditionAction;
import com.example.praticalTestBE.domain.enums.ConditionOperator;
import com.example.praticalTestBE.domain.enums.LogicOperator;
import com.example.praticalTestBE.exception.CircularDependencyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Generic configuration-driven conditional logic engine.
 * Evaluates dynamic visibility and mandatory rule actions based on submitted values.
 * Supports AND/OR condition evaluation and performs circular dependency graph checks.
 */
@Service
public class ConditionalLogicService {

    /**
     * Evaluates condition rules against submitted data map and returns active target action state map.
     * Output Map: Key = target fieldCode, Value = Set of applied ConditionActions.
     */
    public Map<String, Set<ConditionAction>> evaluateConditions(List<FormCondition> conditions, Map<String, Object> submittedData) {
        Map<String, Set<ConditionAction>> activeActions = new HashMap<>();

        if (conditions == null || conditions.isEmpty()) {
            return activeActions;
        }

        // Group conditions by Target Field + Action
        Map<String, Map<ConditionAction, List<FormCondition>>> groupedConditions = new HashMap<>();
        for (FormCondition cond : conditions) {
            if (cond.getTriggerComponent() != null && cond.getTargetComponent() != null) {
                String targetCode = cond.getTargetComponent().getFieldCode();
                groupedConditions.computeIfAbsent(targetCode, k -> new HashMap<>())
                    .computeIfAbsent(cond.getAction(), k -> new ArrayList<>())
                    .add(cond);
            }
        }

        // Evaluate grouped conditions with AND/OR logic
        for (var targetEntry : groupedConditions.entrySet()) {
            String targetFieldCode = targetEntry.getKey();
            for (var actionEntry : targetEntry.getValue().entrySet()) {
                ConditionAction action = actionEntry.getKey();
                List<FormCondition> condList = actionEntry.getValue();

                boolean actionTriggered = evaluateGroupedConditions(condList, submittedData);
                if (actionTriggered) {
                    activeActions.computeIfAbsent(targetFieldCode, k -> new HashSet<>()).add(action);
                }
            }
        }

        return activeActions;
    }

    private boolean evaluateGroupedConditions(List<FormCondition> condList, Map<String, Object> submittedData) {
        if (condList == null || condList.isEmpty()) return false;

        LogicOperator groupOperator = condList.get(0).getLogicOperator() != null ? condList.get(0).getLogicOperator() : LogicOperator.AND;

        if (groupOperator == LogicOperator.OR) {
            // OR Logic: Action triggers if ANY condition evaluates to true
            for (FormCondition cond : condList) {
                String triggerCode = cond.getTriggerComponent().getFieldCode();
                Object actualVal = submittedData.get(triggerCode);
                if (evaluateSingleCondition(cond.getOperator(), cond.getTriggerValue(), actualVal)) {
                    return true;
                }
            }
            return false;
        } else {
            // AND Logic: Action triggers only if ALL conditions evaluate to true
            for (FormCondition cond : condList) {
                String triggerCode = cond.getTriggerComponent().getFieldCode();
                Object actualVal = submittedData.get(triggerCode);
                if (!evaluateSingleCondition(cond.getOperator(), cond.getTriggerValue(), actualVal)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Evaluates a single condition operator against submitted value.
     */
    public boolean evaluateSingleCondition(ConditionOperator operator, String expectedValue, Object actualValue) {
        String actualStr = actualValue != null ? actualValue.toString().trim() : "";
        String expectedStr = expectedValue != null ? expectedValue.trim() : "";

        return switch (operator) {
            case EQUALS -> actualStr.equalsIgnoreCase(expectedStr);
            case NOT_EQUALS -> !actualStr.equalsIgnoreCase(expectedStr);
            case CONTAINS -> actualStr.toLowerCase().contains(expectedStr.toLowerCase());
            case NOT_CONTAINS -> !actualStr.toLowerCase().contains(expectedStr.toLowerCase());
            case IS_EMPTY -> actualValue == null || actualStr.isEmpty();
            case IS_NOT_EMPTY -> actualValue != null && !actualStr.isEmpty();
            case IN -> {
                if (expectedStr.isEmpty()) yield false;
                List<String> options = Arrays.stream(expectedStr.split(","))
                    .map(str -> str.trim())
                    .toList();
                yield options.contains(actualStr);
            }
            case NOT_IN -> {
                if (expectedStr.isEmpty()) yield true;
                List<String> options = Arrays.stream(expectedStr.split(","))
                    .map(str -> str.trim())
                    .toList();
                yield !options.contains(actualStr);
            }
            case GREATER_THAN -> compareNumbers(actualStr, expectedStr) > 0;
            case LESS_THAN -> compareNumbers(actualStr, expectedStr) < 0;
            case GREATER_THAN_OR_EQUAL -> compareNumbers(actualStr, expectedStr) >= 0;
            case LESS_THAN_OR_EQUAL -> compareNumbers(actualStr, expectedStr) <= 0;
        };
    }

    private int compareNumbers(String val1, String val2) {
        try {
            BigDecimal n1 = new BigDecimal(val1);
            BigDecimal n2 = new BigDecimal(val2);
            return n1.compareTo(n2);
        } catch (Exception e) {
            return val1.compareToIgnoreCase(val2);
        }
    }

    /**
     * Detects circular dependencies in conditional rules using Directed Graph Cycle Detection (DFS).
     * Throws CircularDependencyException if a cycle is detected.
     */
    public void detectCircularDependencies(List<FormCondition> conditions) {
        if (conditions == null || conditions.isEmpty()) return;

        Map<String, List<String>> adjacencyList = new HashMap<>();
        for (FormCondition cond : conditions) {
            if (cond.getTriggerComponent() != null && cond.getTargetComponent() != null) {
                String u = cond.getTriggerComponent().getFieldCode();
                String v = cond.getTargetComponent().getFieldCode();
                adjacencyList.computeIfAbsent(u, k -> new ArrayList<>()).add(v);
            }
        }

        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();

        for (String node : adjacencyList.keySet()) {
            if (hasCycleDFS(node, adjacencyList, visited, recStack)) {
                throw new CircularDependencyException(
                    "Circular dependency detected in dynamic conditional rules starting at field: " + node
                );
            }
        }
    }

    private boolean hasCycleDFS(String node, Map<String, List<String>> adj, Set<String> visited, Set<String> recStack) {
        if (recStack.contains(node)) return true;
        if (visited.contains(node)) return false;

        visited.add(node);
        recStack.add(node);

        List<String> neighbors = adj.getOrDefault(node, List.of());
        for (String neighbor : neighbors) {
            if (hasCycleDFS(neighbor, adj, visited, recStack)) {
                return true;
            }
        }

        recStack.remove(node);
        return false;
    }
}
