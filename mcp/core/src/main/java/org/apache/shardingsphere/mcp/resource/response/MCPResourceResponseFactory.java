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

package org.apache.shardingsphere.mcp.resource.response;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.metadata.query.MetadataQueryResult;
import org.apache.shardingsphere.mcp.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;

/**
 * Factory for MCP resource responses.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPResourceResponseFactory {
    
    /**
     * Create a resource response from one metadata query result.
     *
     * @param metadataQueryResult metadata query result
     * @return resource response
     */
    public static MCPResponse fromMetadataQueryResult(final MetadataQueryResult metadataQueryResult) {
        return metadataQueryResult.isSuccessful() ? new MCPMetadataResponse(metadataQueryResult.getMetadataObjects()) : new MCPErrorResponse(metadataQueryResult.getError());
    }
}
