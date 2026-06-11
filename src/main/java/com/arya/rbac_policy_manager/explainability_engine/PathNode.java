package com.arya.rbac_policy_manager.explainability_engine;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class PathNode {
    private String name;
    private UUID id;
    private NodeType type;

    @Override
    public String toString() {
        return type + ":" + name;
    }
}
