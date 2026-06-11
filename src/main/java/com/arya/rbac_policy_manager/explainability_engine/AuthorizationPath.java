package com.arya.rbac_policy_manager.explainability_engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AuthorizationPath {
    private final List<PathNode> nodes = new ArrayList<>();

    public AuthorizationPath() {
    }

    public AuthorizationPath(List<PathNode> nodes) {
        this.nodes.addAll(nodes);
    }

    public void addNode(PathNode node) {
        nodes.add(node);
    }

    public void removeLastNode() {
        if (!nodes.isEmpty()) {
            nodes.remove(nodes.size() - 1);
        }
    }

    public PathNode getLastNode() {
        if (nodes.isEmpty()) {
            return null;
        }

        return nodes.get(nodes.size() - 1);
    }

    public int length() {
        return nodes.size();
    }

    public List<PathNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public AuthorizationPath copy() {
        return new AuthorizationPath(nodes);
    }

    public String toDisplayString() {
        return nodes.stream()
                .map(PathNode::toString)
                .collect(Collectors.joining(" -> "));
    }

    @Override
    public String toString() {
        return toDisplayString();
    }

    public static AuthorizationPath of(List<PathNode> nodes) {
        return new AuthorizationPath(nodes);
    }
}