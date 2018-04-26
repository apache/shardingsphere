package com.saaavsaaa.client.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by aaa on 18-4-18.
 */
public class PathNode {
    private final String nodeKey;
    private Map<String, PathNode> children;
    
    public PathNode(String key) {
        this.nodeKey = key;
    }
    
    public Map<String, PathNode> getChildren() {
        return children;
    }
    
    public void setChildren(Map<String, PathNode> children) {
        this.children = children;
    }
}
