/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.jdbc.orchestration.reg.new_zk.client.cache;

import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.utility.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * zookeeper node cache
 *
 * @author lidongbo
 */
public class PathNode {
    private static final Logger logger = LoggerFactory.getLogger(PathNode.class);
    private final Map<String, PathNode> children = new ConcurrentHashMap<>();
    private final String nodeKey;
    private byte[] value;
    
    PathNode(final String key) {
        this(key, Constants.RELEASE_VALUE);
    }
    
    PathNode(final String key, final byte[] value) {
        this.nodeKey = key;
        this.value = value;
    }
    
    public Map<String, PathNode> getChildren() {
        return children;
    }
    
    public String getKey(){
        return this.nodeKey;
    }
    
    public void attachChild(final PathNode node) {
        this.children.put(node.nodeKey, node);
    }
    
    
    PathNode set(final Iterator<String> iterator, final String value){
        String key = iterator.next();
        logger.debug("PathNode set:{},value:{}", key, value);
        PathNode node = children.get(key);
        if (node == null){
            logger.debug("set children haven't:{}", key);
            node = new PathNode(key);
            children.put(key, node);
        }
        if (iterator.hasNext()){
            node.set(iterator, value);
        } else {
            node.setValue(value.getBytes(Constants.UTF_8));
        }
        return node;
    }
    
    PathNode get(final Iterator<String> iterator){
        String key = iterator.next();
        logger.debug("get:{}", key);
        PathNode node = children.get(key);
        if (node == null){
            logger.debug("get children haven't:{}", key);
            return null;
        }
        if (iterator.hasNext()){
            return node.get(iterator);
        }
        return node;
    }
    
    public byte[] getValue() {
        return value;
    }
    
    public void setValue(byte[] value) {
        this.value = value;
    }
    
    @Deprecated
    PathNode get(final int index, final String path) {
        if (children.isEmpty()){
            logger.debug("get children haven't:{},index:{}", path, index);
            return null;
        }
        if (children.containsKey(path)){
            return children.get(path);
        }
        int nextSeparate = path.indexOf(Constants.PATH_SEPARATOR, index);
        logger.debug("get nextSeparate:{}", nextSeparate);
        if (nextSeparate == -1){
            nextSeparate = path.length() - 1;
        }
        
        return children.get(path.substring(0, nextSeparate)).get(nextSeparate + 1, path);
    }
}
