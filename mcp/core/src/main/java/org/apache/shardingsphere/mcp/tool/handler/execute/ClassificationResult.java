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

package org.apache.shardingsphere.mcp.tool.handler.execute;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.capability.SupportedMCPStatement;

import java.util.Optional;

/**
 * Statement classification result.
 */
@RequiredArgsConstructor
@Getter
public final class ClassificationResult {
    
    private final SupportedMCPStatement statementClass;
    
    private final String statementType;
    
    private final String normalizedSql;
    
    private final String targetObjectName;
    
    private final String savepointName;
    
    /**
     * Get the target object name when one exists.
     *
     * @return optional target object name
     */
    public Optional<String> getTargetObjectName() {
        return targetObjectName.isEmpty() ? Optional.empty() : Optional.of(targetObjectName);
    }
    
    /**
     * Get the savepoint name when one exists.
     *
     * @return optional savepoint name
     */
    public Optional<String> getSavepointName() {
        return savepointName.isEmpty() ? Optional.empty() : Optional.of(savepointName);
    }
    
    String getAuditMarker() {
        return SupportedMCPStatement.TRANSACTION_CONTROL == statementClass || SupportedMCPStatement.SAVEPOINT == statementClass ? statementType : statementClass.name();
    }
}
