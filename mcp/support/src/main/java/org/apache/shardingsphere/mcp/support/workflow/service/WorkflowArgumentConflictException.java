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

package org.apache.shardingsphere.mcp.support.workflow.service;

import lombok.Getter;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;

import java.util.List;

/**
 * Exception for conflicting public workflow arguments.
 */
@Getter
public final class WorkflowArgumentConflictException extends MCPInvalidRequestException {
    
    private static final long serialVersionUID = -2772609954982385217L;
    
    private final List<String> conflictingArguments;
    
    public WorkflowArgumentConflictException(final List<String> conflictingArguments) {
        super(String.format("Conflicting workflow arguments: %s.", String.join(", ", conflictingArguments)));
        this.conflictingArguments = List.copyOf(conflictingArguments);
    }
}
