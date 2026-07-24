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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator;

import io.modelcontextprotocol.server.transport.ServerTransportSecurityException;
import lombok.Getter;
import org.apache.shardingsphere.mcp.support.diagnostic.MCPDiagnosticCategory;

/**
 * MCP transport security exception with a stable diagnostic category.
 */
@Getter
public final class MCPTransportSecurityException extends ServerTransportSecurityException {
    
    public static final String CATEGORY_ORIGIN_NOT_ALLOWED = MCPDiagnosticCategory.ORIGIN_NOT_ALLOWED;
    
    public static final String CATEGORY_SESSION_ATTRIBUTION_MISMATCH = MCPDiagnosticCategory.SESSION_ATTRIBUTION_MISMATCH;
    
    private static final long serialVersionUID = -8481052901882403850L;
    
    private final String category;
    
    public MCPTransportSecurityException(final int statusCode, final String message, final String category) {
        super(statusCode, message);
        this.category = category;
    }
}
