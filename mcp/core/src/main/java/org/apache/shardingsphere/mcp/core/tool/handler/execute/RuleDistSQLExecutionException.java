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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import lombok.Getter;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;

import java.sql.SQLException;

/**
 * Exception for rule DistSQL execution failures that need workflow-aware recovery.
 */
@Getter
public final class RuleDistSQLExecutionException extends MCPInvalidRequestException {
    
    private static final long serialVersionUID = -5256057044313402728L;
    
    private final String database;
    
    private final ClassificationResult classificationResult;
    
    public RuleDistSQLExecutionException(final String database, final ClassificationResult classificationResult, final SQLException cause) {
        super(String.format("Rule DistSQL execution failed for database `%s`; check MCP runtime capability and workflow guidance before asking for corrected SQL.", database), cause);
        this.database = database;
        this.classificationResult = classificationResult;
    }
}
