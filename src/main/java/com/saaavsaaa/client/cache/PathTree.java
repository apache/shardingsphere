package com.saaavsaaa.client.cache;

/**
 * Created by aaa on 18-4-26.
 */
public class PathTree {
    private final PathNode rootNode;
    
    public PathTree(String root) {
        this.rootNode = new PathNode(root);
    }
}
