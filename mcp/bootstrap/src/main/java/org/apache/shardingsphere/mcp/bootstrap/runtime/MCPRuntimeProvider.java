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

package org.apache.shardingsphere.mcp.bootstrap.runtime;

import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeTopologyConfiguration;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;

import java.util.Map;
import java.util.Properties;

/**
 * Load one MCP runtime projection from JDBC runtime properties.
 */
public final class MCPRuntimeProvider {
    
    private final DatabaseRuntimeFactory databaseRuntimeFactory = new DatabaseRuntimeFactory();
    
    private final JdbcMetadataLoader metadataLoader = new JdbcMetadataLoader();
    
    /**
     * Load one runtime projection from JDBC runtime properties.
     *
     * @param props runtime properties
     * @return loaded runtime projection
     */
    public LoadedRuntime load(final Properties props) {
        Map<String, DatabaseConnectionConfiguration> connectionConfigurations = databaseRuntimeFactory.createConnectionConfigurations(props);
        return load(connectionConfigurations);
    }
    
    /**
     * Load one runtime projection from runtime topology configuration.
     *
     * @param runtimeTopologyConfiguration runtime topology configuration
     * @return loaded runtime projection
     */
    public LoadedRuntime load(final RuntimeTopologyConfiguration runtimeTopologyConfiguration) {
        Map<String, DatabaseConnectionConfiguration> connectionConfigurations = databaseRuntimeFactory.createConnectionConfigurations(runtimeTopologyConfiguration);
        return load(connectionConfigurations);
    }
    
    private LoadedRuntime load(final Map<String, DatabaseConnectionConfiguration> connectionConfigurations) {
        MetadataCatalog metadataCatalog = metadataLoader.load(connectionConfigurations);
        return new LoadedRuntime(metadataCatalog, databaseRuntimeFactory.createDatabaseRuntime(connectionConfigurations, metadataCatalog, metadataLoader));
    }
}
