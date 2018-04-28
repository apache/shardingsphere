package com.saaavsaaa.client.cache;

import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.Client;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.common.PathUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by aaa on 18-4-26.
 */
public final class PathTree {
    private final Map<String, String> currentNodes = new ConcurrentHashMap<>();
    private PathNode rootNode;
    private PathStatus Status;
    
    public PathTree(final String root) {
        this.rootNode = new PathNode(root);
        this.Status = PathStatus.RELEASE;
    }
    
    public void loading(final Client client) throws KeeperException, InterruptedException {
        this.setStatus(PathStatus.CHANGING);
        
        PathNode newRoot = new PathNode(rootNode.getKey());
        List<String> children = client.getChildren(rootNode.getKey());
        children.remove(PathUtil.getRealPath(rootNode.getKey(), Constants.CHANGING_KEY));
        this.attechIntoNode(children, newRoot, client);
        rootNode = newRoot;
        
        this.setStatus(PathStatus.RELEASE);
    }
    
    private void attechIntoNode(final List<String> children, final PathNode pathNode, final Client client) throws KeeperException, InterruptedException {
        if (children.isEmpty()){
            return;
        }
        for (String child : children) {
            PathNode current = new PathNode(PathUtil.getRealPath(pathNode.getKey(), child), client.getData(child));
            pathNode.attechChild(current);
            List<String> subs = client.getChildren(child);
            this.attechIntoNode(subs, current, client);
        }
    }
    
    public PathStatus getStatus() {
        return Status;
    }
    
    public void setStatus(final PathStatus status) {
        if (PathStatus.RELEASE == status){
            currentNodes.clear();
        }
        Status = status;
    }
    
    public PathNode getRootNode() {
        return rootNode;
    }
    
    public byte[] getValue(final String path){
        if (currentNodes.containsKey(path)){
            return currentNodes.get(path).getBytes(Constants.UTF_8);
        }
        PathNode node = get(path);
        return null == node ? null : node.getValue();
    }
    
    private PathNode get(final String path){
        PathUtils.validatePath(path);
        return rootNode.get(1, path);
    }
    
    public List<String> getChildren(String path) {
        PathNode node = get(path);
        List<String> result = new ArrayList<>();
        if (node == null){
            return result;
        }
        if (node.getChildren().isEmpty()) {
            return result;
        }
        Iterator<PathNode> children = node.getChildren().values().iterator();
        while (children.hasNext()){
            // children keys don't needn't currentNodes
            result.add(new String(children.next().getValue()));
        }
        return result;
    }
    
    public void put(String path, String value) {
        PathUtils.validatePath(path);
        currentNodes.put(path, value);
    }
    
    public void delete(String path) {
        PathUtils.validatePath(path);
        currentNodes.remove(path);
        String prxpath = path.substring(0, path.lastIndexOf(Constants.PATH_SEPARATOR));
        PathNode node = get(prxpath);
        node.getChildren().remove(path);
    }
}
