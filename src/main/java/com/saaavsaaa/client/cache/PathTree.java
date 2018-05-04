package com.saaavsaaa.client.cache;

import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.Provider;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.common.PathUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by aaa
 */
public final class PathTree {
    private PathNode rootNode;
    private PathStatus Status;
    
    public PathTree(final String root) {
        this.rootNode = new PathNode(root);
        this.Status = PathStatus.RELEASE;
    }
    
    public void loading(final Provider provider) throws KeeperException, InterruptedException {
        this.setStatus(PathStatus.CHANGING);
        
        PathNode newRoot = new PathNode(rootNode.getKey());
        List<String> children = provider.getChildren(rootNode.getKey());
        children.remove(provider.getRealPath(Constants.CHANGING_KEY));
        this.attechIntoNode(children, newRoot, provider);
        rootNode = newRoot;
        
        this.setStatus(PathStatus.RELEASE);
    }
    
    private void attechIntoNode(final List<String> children, final PathNode pathNode, final Provider provider) throws KeeperException, InterruptedException {
        if (children.isEmpty()){
            return;
        }
        for (String child : children) {
            String childPath = PathUtil.getRealPath(pathNode.getKey(), child);
            PathNode current = new PathNode(PathUtil.checkPath(child), provider.getData(childPath));
            pathNode.attachChild(current);
            List<String> subs = provider.getChildren(childPath);
            this.attechIntoNode(subs, current, provider);
        }
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
        return rootNode.get(keyIterator(path)); //rootNode.get(1, path);
    }
    
    private Iterator<String> keyIterator(final String path){
        List<String> nodes = PathUtil.getShortPathNodes(path);
        Iterator<String> iterator = nodes.iterator();
        iterator.next(); // root
        return iterator;
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
            result.add(new String(children.next().getValue()));
        }
        return result;
    }
    
    public void put(final String path, final String value) {
        PathUtils.validatePath(path);
        if (Status == Status.RELEASE){
            rootNode.set(keyIterator(path), value);
        } else {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                System.out.println("cache put status not release");
            }
            put(path, value);
        }
    }
    
    public void delete(String path) {
        PathUtils.validatePath(path);
        String prxpath = path.substring(0, path.lastIndexOf(Constants.PATH_SEPARATOR));
        PathNode node = get(prxpath);
        node.getChildren().remove(path);
    }
}
