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

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PostgreSQL portal context.
 */
public final class PortalContext {
    
    private final Map<String, Portal> portals = new LinkedHashMap<>();
    
    /**
     * Add portal.
     *
     * @param portal portal name
     * @throws SQLException SQL exception
     */
    public void add(final Portal portal) throws SQLException {
        boolean isNamedPortal = !portal.getName().isEmpty();
        Preconditions.checkState(!isNamedPortal || !portals.containsKey(portal.getName()), "Named portal `%s` must be explicitly closed", portal.getName());
        Portal previousPortal = portals.put(portal.getName(), portal);
        if (null != previousPortal) {
            previousPortal.close();
        }
    }
    
    /**
     * Get portal.
     *
     * @param portalName portal name
     * @return portal
     */
    public Portal get(final String portalName) {
        return portals.get(portalName);
    }
    
    /**
     * Close portal.
     *
     * @param portalName portal name
     * @throws SQLException SQL exception
     */
    public void close(final String portalName) throws SQLException {
        Portal result = portals.remove(portalName);
        if (null != result) {
            result.close();
        }
    }
    
    /**
     * Close all portals.
     *
     * @throws SQLException SQL exception
     */
    public void closeAll() throws SQLException {
        for (Portal portal : portals.values()) {
            portal.close();
        }
        portals.clear();
    }
}
