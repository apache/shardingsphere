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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.cache;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.Constants;
import lombok.Getter;
import lombok.Setter;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(PathNode.class);

    private final Map<String, PathNode> children = new ConcurrentHashMap<>();

    private final String nodeKey;

    @Getter
    @Setter
    private byte[] value;
    
    PathNode(final String key) {
        this(key, Constants.RELEASE_VALUE);
    }
    
    PathNode(final String key, final byte[] value) {
        this.nodeKey = key;
        this.value = value;
    }
    
    /**
     * get children.
     *
     * @return children
     */
    public Map<String, PathNode> getChildren() {
        return children;
    }
    
    /**
     * get key.
     *
     * @return node key
     */
    public String getKey() {
        return this.nodeKey;
    }
    
    /**
     * attach child node.
     *
     * @param node node
     */
    public void attachChild(final PathNode node) {
        this.children.put(node.nodeKey, node);
    }
    
    PathNode set(final Iterator<String> iterator, final String value) {
        String key = iterator.next();
        LOGGER.debug("PathNode set:{},value:{}", key, value);
        PathNode node = children.get(key);
        if (node == null) {
            LOGGER.debug("set children haven't:{}", key);
            node = new PathNode(key);
            children.put(key, node);
        }
        if (iterator.hasNext()) {
            node.set(iterator, value);
        } else {
            node.setValue(value.getBytes(Constants.UTF_8));
        }
        return node;
    }
    
    PathNode get(final Iterator<String> iterator) {
        String key = iterator.next();
        LOGGER.debug("get:{}", key);
        PathNode node = children.get(key);
        if (node == null) {
            LOGGER.debug("get children haven't:{}", key);
            return null;
        }
        if (iterator.hasNext()) {
            return node.get(iterator);
        }
        return node;
    }
}
