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

package io.shardingsphere.orchestration.reg.newzk.client.cache;

import io.shardingsphere.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zookeeper node cache.
 *
 * @author lidongbo
 */
@Getter
@Setter
public final class PathNode {
    
    private final Map<String, PathNode> children = new ConcurrentHashMap<>();
    
    private final String nodeKey;
    
    private String path;
    
    private byte[] value;
    
    PathNode(final String key) {
        this(key, ZookeeperConstants.RELEASE_VALUE);
    }
    
    PathNode(final String key, final byte[] value) {
        this.nodeKey = key;
        this.value = value;
        this.path = key;
    }
    
    void attachChild(final PathNode node) {
        children.put(node.nodeKey, node);
        node.setPath(PathUtil.getRealPath(path, node.getNodeKey()));
    }
    
    PathNode set(final PathResolve pathResolve, final String value) {
        if (pathResolve.isEnd()) {
            setValue(value.getBytes(ZookeeperConstants.UTF_8));
            return this;
        }
        pathResolve.next();
        if (children.containsKey(pathResolve.getCurrent())) {
            return children.get(pathResolve.getCurrent()).set(pathResolve, value);
        }
        PathNode result = new PathNode(pathResolve.getCurrent(), ZookeeperConstants.NOTHING_DATA);
        this.attachChild(result);
        result.set(pathResolve, value);
        return result;
    }
    
    PathNode get(final PathResolve pathResolve) {
        pathResolve.next();
        if (children.containsKey(pathResolve.getCurrent())) {
            if (pathResolve.isEnd()) {
                return children.get(pathResolve.getCurrent());
            }
            return children.get(pathResolve.getCurrent()).get(pathResolve);
        }
        return null;
    }
    
    void delete(final PathResolve pathResolve) {
        pathResolve.next();
        if (children.containsKey(pathResolve.getCurrent())) {
            if (pathResolve.isEnd()) {
                children.remove(pathResolve.getCurrent());
            } else {
                children.get(pathResolve.getCurrent()).delete(pathResolve);
            }
        }
    }
}
