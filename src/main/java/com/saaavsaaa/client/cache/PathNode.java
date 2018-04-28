package com.saaavsaaa.client.cache;

import com.saaavsaaa.client.utility.constant.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by aaa on 18-4-18.
 * todo nodeKey can use current node short path: get children.containsKey(path) should be change -> /root/aaa/aaa
 */
public class PathNode {
    private final String nodeKey;
    private byte[] value;
    private Map<String, PathNode> children = new ConcurrentHashMap<>();
    
    public PathNode(final String key, final byte[] value) {
        this.nodeKey = key;
        this.value = value;
    }
    
    public Map<String, PathNode> getChildren() {
        return children;
    }
    
    public String getKey(){
        return this.nodeKey;
    }
    
    public void attechChild(final PathNode node) {
        this.children.put(node.nodeKey, node);
    }
    
    PathNode get(final int index, final String path) {
        if (children.isEmpty()){
            return null;
        }
        if (children.containsKey(path)){
            return children.get(path);
        }
        int nextSeparate = path.indexOf(Constants.PATH_SEPARATOR, index);
        if (nextSeparate == -1){
            nextSeparate = path.length() - 1;
        }
        
        return children.get(path.substring(0, nextSeparate)).get(nextSeparate + 1, path);
    }
    
    public byte[] getValue() {
        return value;
    }
}
