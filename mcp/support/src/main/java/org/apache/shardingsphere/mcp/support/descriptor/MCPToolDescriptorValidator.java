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

package org.apache.shardingsphere.mcp.support.descriptor;

import org.apache.shardingsphere.infra.spi.ShardingSphereSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;

/**
 * MCP tool descriptor validator.
 */
@SingletonSPI
public interface MCPToolDescriptorValidator extends ShardingSphereSPI {
    
    /**
     * Judge whether this validator supports the tool descriptor.
     *
     * @param toolDescriptor tool descriptor
     * @return whether this validator supports the tool descriptor
     */
    boolean supports(MCPToolDescriptor toolDescriptor);
    
    /**
     * Validate the tool descriptor.
     *
     * @param toolDescriptor tool descriptor
     */
    void validate(MCPToolDescriptor toolDescriptor);
}
