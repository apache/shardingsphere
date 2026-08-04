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

package org.apache.shardingsphere.mcp.core.protocol.exception;

import org.apache.shardingsphere.mcp.api.exception.MCPUnsupportedException;

/**
 * Exception for locking reads rejected by the MCP read-only SQL contract.
 */
public final class MCPLockingReadStatementException extends MCPUnsupportedException {
    
    private static final long serialVersionUID = 7156903819043022869L;
    
    public MCPLockingReadStatementException() {
        super("Locking read statements such as SELECT ... FOR UPDATE are not supported by the MCP read-only contract.");
    }
}
