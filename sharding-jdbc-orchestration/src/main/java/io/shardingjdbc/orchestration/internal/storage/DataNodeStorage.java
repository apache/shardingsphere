/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.orchestration.internal.storage;

import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

/**
 * Data node storage.
 * 
 * @author caohao
 */
public final class DataNodeStorage {
    
    private final String name;
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final DataNodePath nodePath;
    
    public DataNodeStorage(final String name, final CoordinatorRegistryCenter regCenter) {
        this.name = name;
        this.regCenter = regCenter;
        nodePath = new DataNodePath(name);
    }
    
    /**
     * Justify if node path exists.
     * 
     * @param node node 
     * @return is node path exists
     */
    public boolean isNodeExisted(final String node) {
        return regCenter.isExisted(nodePath.getFullPath(node));
    }
    
    /**
     * Get node data.
     * 
     * @param node node name
     * @return node data
     */
    public String getNodeData(final String node) {
        return regCenter.get(nodePath.getFullPath(node));
    }
    
    /**
     * Fill node data.
     *
     * @param node node name
     * @param value node value
     */
    public void fillNode(final String node, final Object value) {
        regCenter.persist(nodePath.getFullPath(node), value.toString());
    }
    
    /**
     * Fill ephemeral node data.
     * 
     * @param node node name
     * @param value node value
     */
    public void fillEphemeralNode(final String node, final Object value) {
        regCenter.persistEphemeral(nodePath.getFullPath(node), value.toString());
    }
    
    /**
     * Add data listener.
     * 
     * @param listener data listener
     */
    public void addDataListener(final TreeCacheListener listener) {
        TreeCache cache = (TreeCache) regCenter.getRawCache("/" + name);
        cache.getListenable().addListener(listener);
    }
}
