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

package org.apache.shardingsphere.mcp.support.database;

import org.apache.shardingsphere.mcp.api.MCPRequestContext;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureCapabilityFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;

import java.util.Optional;

/**
 * Database-aware MCP request context.
 */
public interface MCPDatabaseRequestContext extends MCPRequestContext {
    
    /**
     * Get metadata query facade.
     *
     * @return metadata query facade
     */
    MCPMetadataQueryFacade getMetadataQueryFacade();
    
    /**
     * Get SQL execution facade.
     *
     * @return SQL execution facade
     */
    MCPFeatureExecutionFacade getExecutionFacade();
    
    /**
     * Get direct query facade.
     *
     * @return direct query facade
     */
    MCPFeatureQueryFacade getQueryFacade();
    
    /**
     * Get capability facade.
     *
     * @return capability facade
     */
    MCPFeatureCapabilityFacade getCapabilityFacade();
    
    /**
     * Find runtime database configuration.
     *
     * @param databaseName database name
     * @return runtime database configuration
     */
    Optional<RuntimeDatabaseConfiguration> findRuntimeDatabaseConfiguration(String databaseName);
}
