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
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Statement classification result.
 */
@Getter
public final class ClassificationResult {
    
    private static final Collection<String> RULE_DIST_SQL_PREFIXES = List.of(
            "CREATE SHARDING ", "ALTER SHARDING ", "DROP SHARDING ",
            "CREATE DEFAULT SHARDING ", "ALTER DEFAULT SHARDING ", "DROP DEFAULT SHARDING ",
            "CREATE BROADCAST ", "DROP BROADCAST ",
            "CREATE ENCRYPT ", "ALTER ENCRYPT ", "DROP ENCRYPT ",
            "CREATE MASK ", "ALTER MASK ", "DROP MASK ",
            "CREATE SHADOW ", "ALTER SHADOW ", "DROP SHADOW ",
            "CREATE DEFAULT SHADOW ", "ALTER DEFAULT SHADOW ", "DROP DEFAULT SHADOW ",
            "CREATE READWRITE_SPLITTING ", "ALTER READWRITE_SPLITTING ", "DROP READWRITE_SPLITTING ");
    
    private final SupportedMCPStatement statementClass;
    
    private final String statementType;
    
    private final String normalizedSql;
    
    private final String targetObjectName;
    
    private final Collection<String> referencedObjectNames;
    
    private final String savepointName;
    
    private final Optional<SupportedMCPStatement> explainedStatementClass;
    
    public ClassificationResult(final SupportedMCPStatement statementClass, final String statementType, final String normalizedSql, final String targetObjectName, final String savepointName) {
        this(statementClass, statementType, normalizedSql, targetObjectName, savepointName, null, targetObjectName.isEmpty() ? List.of() : List.of(targetObjectName));
    }
    
    ClassificationResult(final SupportedMCPStatement statementClass, final String statementType, final String normalizedSql, final String targetObjectName, final String savepointName,
                         final SupportedMCPStatement explainedStatementClass, final Collection<String> referencedObjectNames) {
        this.statementClass = statementClass;
        this.statementType = statementType;
        this.normalizedSql = normalizedSql;
        this.targetObjectName = targetObjectName;
        this.referencedObjectNames = referencedObjectNames;
        this.savepointName = savepointName;
        this.explainedStatementClass = Optional.ofNullable(explainedStatementClass);
    }
    
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
    
    /**
     * Determine whether this statement mutates ShardingSphere rule metadata through DistSQL.
     *
     * @return true when the statement is a recognized rule DistSQL statement
     */
    public boolean isRuleDistSQL() {
        if (SupportedMCPStatement.DDL != statementClass) {
            return false;
        }
        String upperSql = normalizedSql.toUpperCase(Locale.ENGLISH);
        for (String each : RULE_DIST_SQL_PREFIXES) {
            if (upperSql.startsWith(each)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get model-facing side-effect scope.
     *
     * @return side-effect scope
     */
    public String getSideEffectScope() {
        if (isRuleDistSQL()) {
            return "rule-metadata";
        }
        return switch (explainedStatementClass.orElse(statementClass)) {
            case DML -> "physical-data";
            case DDL -> "physical-structure";
            case DCL -> "privilege-metadata";
            case TRANSACTION_CONTROL, SAVEPOINT -> "transaction-state";
            default -> "unknown-side-effect";
        };
    }
    
    String getTraceStatementMarker() {
        return SupportedMCPStatement.TRANSACTION_CONTROL == statementClass || SupportedMCPStatement.SAVEPOINT == statementClass ? statementType : statementClass.name();
    }
}
