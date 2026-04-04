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

package org.apache.shardingsphere.mcp.metadata.query;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;
import org.apache.shardingsphere.mcp.protocol.MCPError;
import org.apache.shardingsphere.mcp.protocol.MCPError.MCPErrorCode;

import java.util.Collections;
import java.util.List;

/**
 * Result for one metadata query.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class MetadataQueryResult {
    
    private final List<MetadataObject> metadataObjects;
    
    private final boolean successful;
    
    private final MCPError error;
    
    /**
     * Create a successful metadata query result.
     *
     * @param metadataObjects loaded metadata objects
     * @return successful metadata query result
     */
    public static MetadataQueryResult success(final List<MetadataObject> metadataObjects) {
        return new MetadataQueryResult(metadataObjects, true, null);
    }
    
    /**
     * Create a failed metadata query result.
     *
     * @param errorCode unified error code
     * @param message error message
     * @return failed metadata query result
     */
    public static MetadataQueryResult error(final MCPErrorCode errorCode, final String message) {
        return new MetadataQueryResult(Collections.emptyList(), false, new MCPError(errorCode, message));
    }
    
    /**
     * Get error.
     *
     * @return error
     */
    public MCPError getError() {
        return successful ? new MCPError(MCPErrorCode.INVALID_REQUEST, "") : error;
    }
}
