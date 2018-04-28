package com.saaavsaaa.client.cache;

import com.saaavsaaa.client.utility.constant.Constants;
import org.apache.zookeeper.common.PathUtils;

import java.util.TreeMap;

/**
 * Created by aaa on 18-4-26.
 */
public class PathTree {
    private final PathNode rootNode;
    private PathStatus Status;
    
    public PathTree(final String root) {
        this.rootNode = new PathNode(root, Constants.RELEASE_VALUE);
        Status = PathStatus.RELEASE;
    }
    
    public PathStatus getStatus() {
        return Status;
    }
    
    public void setStatus(final PathStatus status) {
        Status = status;
    }
    
    public PathNode getRootNode() {
        return rootNode;
    }
    
    public byte[] getValue(final String path){
        PathNode node = get(path);
        return null == node ? null : node.getValue();
    }
    
    private PathNode get(final String path){
        PathUtils.validatePath(path);
        return rootNode.get(1, path);
    }
}
