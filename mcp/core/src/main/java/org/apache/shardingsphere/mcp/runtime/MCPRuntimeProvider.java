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

package org.apache.shardingsphere.mcp.runtime;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.util.Map;

/**
 * SPI for one MCP runtime provider.
 */
public interface MCPRuntimeProvider extends TypedSPI {
    
    /**
     * Create runtime context.
     *
     * @param sessionManager session manager
     * @param runtimeConfiguration runtime configuration
     * @return runtime context
     */
    MCPRuntimeContext createRuntimeContext(MCPSessionManager sessionManager, Object runtimeConfiguration);
    
    /**
     * Swap YAML runtime configuration to runtime configuration object.
     *
     * @param yamlRuntimeConfiguration YAML runtime configuration
     * @return runtime configuration object
     */
    Object swapToObject(Map<String, Map<String, Object>> yamlRuntimeConfiguration);
    
    /**
     * Swap runtime configuration object to YAML runtime configuration.
     *
     * @param runtimeConfiguration runtime configuration
     * @return YAML runtime configuration
     */
    Map<String, Map<String, Object>> swapToYamlConfiguration(Object runtimeConfiguration);
}
