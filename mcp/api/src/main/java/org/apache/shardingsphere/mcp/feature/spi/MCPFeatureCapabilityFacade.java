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

package org.apache.shardingsphere.mcp.feature.spi;

import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseProfile;

import java.util.List;
import java.util.Optional;

/**
 * MCP feature capability facade.
 */
public interface MCPFeatureCapabilityFacade {
    
    /**
     * Provide database capability.
     *
     * @param databaseName database name
     * @return database capability
     */
    Optional<MCPDatabaseCapability> provide(String databaseName);
    
    /**
     * Find runtime database profile.
     *
     * @param databaseName database name
     * @return runtime database profile
     */
    Optional<RuntimeDatabaseProfile> findDatabaseProfile(String databaseName);
    
    /**
     * Get runtime database profiles.
     *
     * @return runtime database profiles
     */
    List<RuntimeDatabaseProfile> getDatabaseProfiles();
}
