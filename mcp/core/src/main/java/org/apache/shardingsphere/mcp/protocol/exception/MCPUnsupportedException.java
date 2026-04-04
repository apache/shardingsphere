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

package org.apache.shardingsphere.mcp.protocol.exception;

import org.apache.shardingsphere.mcp.protocol.MCPError.MCPErrorCode;

/**
 * Exception for unsupported MCP operations.
 */
public class MCPUnsupportedException extends MCPProtocolException {
    
    private static final long serialVersionUID = 7488172893235310016L;
    
    public MCPUnsupportedException(final String message) {
        super(MCPErrorCode.UNSUPPORTED, message);
    }
    
    public MCPUnsupportedException(final String message, final Throwable cause) {
        super(MCPErrorCode.UNSUPPORTED, message, cause);
    }
}
