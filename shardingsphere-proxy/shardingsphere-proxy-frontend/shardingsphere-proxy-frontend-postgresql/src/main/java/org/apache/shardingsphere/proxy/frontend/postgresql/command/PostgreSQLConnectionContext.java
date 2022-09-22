/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.frontend.postgresql.command;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.Portal;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PostgreSQL connection context.
 */
public final class PostgreSQLConnectionContext {
    
    private final Map<String, Portal<?>> portals = new LinkedHashMap<>();
    
    /**
     * Create a portal.
     *
     * @param portal portal name
     */
    public void addPortal(final Portal<?> portal) {
        boolean isNamedPortal = !portal.getName().isEmpty();
        Preconditions.checkState(!isNamedPortal || !portals.containsKey(portal.getName()), "Named portal `%s` must be explicitly closed", portal.getName());
        Portal<?> previousPortal = portals.put(portal.getName(), portal);
        if (null != previousPortal) {
            previousPortal.close();
        }
    }
    
    /**
     * Get portal.
     *
     * @param <T> type of portal
     * @param portal portal name
     * @return portal
     */
    public <T extends Portal<?>> T getPortal(final String portal) {
        return (T) portals.get(portal);
    }
    
    /**
     * Close portal.
     *
     * @param portal portal name
     */
    public void closePortal(final String portal) {
        Portal<?> result = portals.remove(portal);
        if (null != result) {
            result.close();
        }
    }
    
    /**
     * Close all portals.
     */
    public void closeAllPortals() {
        for (Portal<?> each : portals.values()) {
            each.close();
        }
        portals.clear();
    }
}
