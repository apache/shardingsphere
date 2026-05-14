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
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exception for SQL sent to the wrong MCP execution tool.
 */
@Getter
public final class SQLToolMismatchException extends MCPUnsupportedException {
    
    private static final long serialVersionUID = 4446140739545147946L;
    
    private final String sourceTool;
    
    private final String targetTool;
    
    private final ClassificationResult classificationResult;
    
    private final Map<String, Object> suggestedArguments;
    
    /**
     * Create SQL tool mismatch exception.
     *
     * @param message error message
     * @param sourceTool source tool name
     * @param targetTool target tool name
     * @param classificationResult SQL classification result
     * @param suggestedArguments safe arguments for retrying with the target tool
     */
    public SQLToolMismatchException(final String message, final String sourceTool, final String targetTool,
                                    final ClassificationResult classificationResult, final Map<String, Object> suggestedArguments) {
        super(message);
        this.sourceTool = sourceTool;
        this.targetTool = targetTool;
        this.classificationResult = classificationResult;
        this.suggestedArguments = Collections.unmodifiableMap(new LinkedHashMap<>(suggestedArguments));
    }
}
