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

package org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter;

import lombok.Setter;
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptConditionsAware;
import org.apache.shardingsphere.encrypt.rewrite.aware.SchemaNameAware;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptCondition;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Predicate parameter rewriter for encrypt.
 */
@Setter
public final class EncryptPredicateParameterRewriter implements ParameterRewriter<SQLStatementContext<?>>, EncryptRuleAware, EncryptConditionsAware, SchemaNameAware {
    
    private EncryptRule encryptRule;
    
    private Collection<EncryptCondition> encryptConditions;
    
    private String schemaName;
    
    @Override
    public boolean isNeedRewrite(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof WhereAvailable && !((WhereAvailable) sqlStatementContext).getWhereSegments().isEmpty();
    }
    
    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final SQLStatementContext<?> sqlStatementContext, final List<Object> parameters) {
        for (EncryptCondition each : encryptConditions) {
            boolean queryWithCipherColumn = encryptRule.isQueryWithCipherColumn(each.getTableName());
            if (queryWithCipherColumn) {
                encryptParameters(parameterBuilder, each.getPositionIndexMap(), getEncryptedValues(schemaName, each, each.getValues(parameters)));
            }
        }
    }
    
    private List<Object> getEncryptedValues(final String schemaName, final EncryptCondition encryptCondition, final List<Object> originalValues) {
        String tableName = encryptCondition.getTableName();
        String columnName = encryptCondition.getColumnName();
        return encryptRule.findAssistedQueryColumn(tableName, columnName).isPresent()
                ? encryptRule.getEncryptAssistedQueryValues(schemaName, tableName, columnName, originalValues) 
                        : encryptRule.getEncryptValues(schemaName, tableName, columnName, originalValues);
    }
    
    private void encryptParameters(final ParameterBuilder parameterBuilder, final Map<Integer, Integer> positionIndexes, final List<Object> encryptValues) {
        if (!positionIndexes.isEmpty()) {
            for (Entry<Integer, Integer> entry : positionIndexes.entrySet()) {
                ((StandardParameterBuilder) parameterBuilder).addReplacedParameters(entry.getValue(), encryptValues.get(entry.getKey()));
            }
        }
    }
}
