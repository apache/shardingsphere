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

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.Assist;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Zookeeper node cache.
 *
 * @author lidongbo
 */
@Slf4j
public final class PathNode {
    
    private final Map<String, PathNode> children = new ConcurrentHashMap<>();

    private final String nodeKey;
    
    @Getter(value = AccessLevel.PACKAGE)
    @Setter(value = AccessLevel.PACKAGE)
    private String path;

    @Getter(value = AccessLevel.PACKAGE)
    @Setter(value = AccessLevel.PACKAGE)
    private byte[] value;
    
    PathNode(final String key) {
        this(key, ZookeeperConstants.RELEASE_VALUE);
    }
    
    PathNode(final String key, final byte[] value) {
        this.nodeKey = key;
        this.value = value;
        this.path = key;
    }
    
    /**
     * Get children.
     *
     * @return children
     */
    public Map<String, PathNode> getChildren() {
        return children;
    }
    
    /**
     * Get key.
     *
     * @return node key
     */
    public String getKey() {
        return this.nodeKey;
    }
    
    /**
     * Attach child node.
     *
     * @param node node
     */
    public void attachChild(final PathNode node) {
        this.children.put(node.nodeKey, node);
        node.setPath(PathUtil.getRealPath(path, node.getKey()));
    }
    
    PathNode set(final Iterator<String> iterator, final String value) {
        String key = iterator.next();
        log.debug("PathNode set:{},value:{}", key, value);
        PathNode result = children.get(key);
        if (result == null) {
            log.debug("set children haven't:{}", key);
            result = new PathNode(key);
            children.put(key, result);
        }
        if (iterator.hasNext()) {
            result.set(iterator, value);
        } else {
            result.setValue(value.getBytes(ZookeeperConstants.UTF_8));
        }
        return result;
    }
    
    PathNode get(final Iterator<String> iterator) {
        String key = iterator.next();
        log.debug("get:{}", key);
        PathNode result = children.get(key);
        if (result == null) {
            log.debug("get children haven't:{}", key);
            return null;
        }
        if (iterator.hasNext()) {
            return result.get(iterator);
        }
        return result;
    }
    
    void delete(final String path, final LexerEngine lexerEngine) {
        if (lexerEngine.getCurrentToken().getType().equals(Assist.END)) {
            children.remove(path);
        }
        if (children.containsKey(path)) {
            PathNode node = children.get(path);
            lexerEngine.nextToken();
            lexerEngine.skipIfEqual(Symbol.SLASH);
            node.delete(lexerEngine.getCurrentToken().getLiterals(), lexerEngine);
        }
    }
}
