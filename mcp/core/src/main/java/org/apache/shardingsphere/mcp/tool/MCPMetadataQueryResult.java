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

package org.apache.shardingsphere.mcp.tool;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * MCP metadata query result.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class MCPMetadataQueryResult {
    
    private final List<MetadataObject> metadataObjects;
    
    private final boolean successful;
    
    private final MCPErrorCode errorCode;
    
    private final String message;
    
    /**
     * Create a successful metadata result.
     *
     * @param metadataObjects loaded metadata objects
     * @return successful metadata result
     */
    public static MCPMetadataQueryResult success(final List<MetadataObject> metadataObjects) {
        return new MCPMetadataQueryResult(metadataObjects, true, MCPErrorCode.INVALID_REQUEST, "");
    }
    
    /**
     * Create an error metadata result.
     *
     * @param errorCode unified error code
     * @param message error message
     * @return failed metadata result
     */
    public static MCPMetadataQueryResult error(final MCPErrorCode errorCode, final String message) {
        return new MCPMetadataQueryResult(Collections.emptyList(), false, errorCode, message);
    }
    
    /**
     * Get the error code when one exists.
     *
     * @return optional error code
     */
    public Optional<MCPErrorCode> getErrorCode() {
        return successful ? Optional.empty() : Optional.of(errorCode);
    }
}
