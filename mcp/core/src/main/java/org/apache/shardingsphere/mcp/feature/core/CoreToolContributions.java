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

package org.apache.shardingsphere.mcp.feature.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.feature.spi.MCPContribution;
import org.apache.shardingsphere.mcp.feature.spi.MCPDirectToolContribution;
import org.apache.shardingsphere.mcp.tool.handler.execute.ExecuteSQLToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.metadata.SearchMetadataToolHandler;

import java.util.Collection;
import java.util.List;

/**
 * Core tool contributions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class CoreToolContributions {
    
    static Collection<MCPContribution> createContributions() {
        SearchMetadataToolHandler searchMetadataToolHandler = new SearchMetadataToolHandler();
        ExecuteSQLToolHandler executeSQLToolHandler = new ExecuteSQLToolHandler();
        return List.of(
                new MCPDirectToolContribution(searchMetadataToolHandler.getToolDescriptor(), searchMetadataToolHandler::handle),
                new MCPDirectToolContribution(executeSQLToolHandler.getToolDescriptor(), executeSQLToolHandler::handle));
    }
}
