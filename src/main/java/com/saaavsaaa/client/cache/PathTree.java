package com.saaavsaaa.client.cache;

/**
 * Created by aaa on 18-4-26.
 */
public class PathTree {
    private final PathNode rootNode;
    private PathStatus Status;
    
    public PathTree(String root) {
        this.rootNode = new PathNode(root);
    }
    
    public PathStatus getStatus() {
        return Status;
    }
    
    public void setStatus(PathStatus status) {
        Status = status;
    }
}
