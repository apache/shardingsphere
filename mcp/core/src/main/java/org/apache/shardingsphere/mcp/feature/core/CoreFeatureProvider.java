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

import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureProvider;
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandler;
import org.apache.shardingsphere.mcp.resource.handler.capability.DatabaseCapabilitiesHandler;
import org.apache.shardingsphere.mcp.resource.handler.capability.ServiceCapabilitiesHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.DatabaseHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.DatabasesHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.IndexHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.IndexesHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.SchemaHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.SchemasHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.SequenceHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.SequencesHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.TableColumnHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.TableColumnsHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.TableHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.TablesHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.ViewColumnHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.ViewColumnsHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.ViewHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.ViewsHandler;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.execute.ExecuteSQLToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.metadata.SearchMetadataToolHandler;

import java.util.Collection;
import java.util.List;

/**
 * Core MCP feature provider.
 */
public final class CoreFeatureProvider implements MCPFeatureProvider {
    
    @Override
    public Collection<ToolHandler> getToolHandlers() {
        return List.of(new SearchMetadataToolHandler(), new ExecuteSQLToolHandler());
    }
    
    @Override
    public Collection<ResourceHandler> getResourceHandlers() {
        return List.of(
                new ServiceCapabilitiesHandler(),
                new DatabaseCapabilitiesHandler(),
                new DatabasesHandler(),
                new DatabaseHandler(),
                new SchemasHandler(),
                new SchemaHandler(),
                new SequencesHandler(),
                new SequenceHandler(),
                new TablesHandler(),
                new ViewsHandler(),
                new TableHandler(),
                new TableColumnsHandler(),
                new TableColumnHandler(),
                new ViewHandler(),
                new ViewColumnsHandler(),
                new ViewColumnHandler(),
                new IndexesHandler(),
                new IndexHandler());
    }
}
