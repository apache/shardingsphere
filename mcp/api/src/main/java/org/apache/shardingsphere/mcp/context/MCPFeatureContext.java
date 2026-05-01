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

package org.apache.shardingsphere.mcp.context;

import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureCapabilityFacade;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;

/**
 * MCP feature request context.
 */
public interface MCPFeatureContext extends AutoCloseable {
    
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
    
    @Override
    default void close() {
    }
}
