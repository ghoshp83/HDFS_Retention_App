package com.pralay.apps.offlineRetention;

import java.util.ArrayList;
import java.util.List;

public class DirTree {
    private final String node;
    private List<DirTree> children = new ArrayList<>();

    public DirTree(String node) {
        this.node = node;
    }

    public DirTree addChild(DirTree tree) {
        children.add(tree);
        return this;
    }

    public DirTree addChildren(List<DirTree> trees) {
        children.addAll(trees);
        return this;
    }

    public List<DirTree> getChildren() {
        return children;
    }

    public String getNode() {
        return node;
    }
}
