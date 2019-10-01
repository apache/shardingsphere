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

package org.apache.shardingsphere.core.rewrite.parameter.rewriter.encrypt;

import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.rewrite.encrypt.EncryptCondition;
import org.apache.shardingsphere.core.rewrite.encrypt.EncryptConditionEngine;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.builder.standard.StandardParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.core.rewrite.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.token.generator.QueryWithCipherColumnAware;
import org.apache.shardingsphere.core.rewrite.token.generator.TableMetasAware;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Where parameter rewriter for encrypt.
 *
 * @author zhangliang
 */
@Setter
public final class EncryptPredicateParameterRewriter implements ParameterRewriter, TableMetasAware, EncryptRuleAware, QueryWithCipherColumnAware {
    
    private TableMetas tableMetas;
    
    private EncryptRule encryptRule;
    
    private boolean queryWithCipherColumn;
    
    @Override
    public void rewrite(final SQLStatementContext sqlStatementContext, final List<Object> parameters, final ParameterBuilder parameterBuilder) {
        List<EncryptCondition> encryptConditions = new EncryptConditionEngine(encryptRule, tableMetas).createEncryptConditions(sqlStatementContext);
        if (encryptConditions.isEmpty()) {
            return;
        }
        for (EncryptCondition each : encryptConditions) {
            if (queryWithCipherColumn) {
                encryptParameters(parameterBuilder, each.getPositionIndexMap(), getEncryptedValues(each, each.getValues(parameters)));
            }
        }
    }
    
    private List<Object> getEncryptedValues(final EncryptCondition encryptCondition, final List<Object> originalValues) {
        String tableName = encryptCondition.getTableName();
        String columnName = encryptCondition.getColumnName();
        return encryptRule.findAssistedQueryColumn(tableName, columnName).isPresent()
                ? encryptRule.getEncryptAssistedQueryValues(tableName, columnName, originalValues) : encryptRule.getEncryptValues(tableName, columnName, originalValues);
    }
    
    private void encryptParameters(final ParameterBuilder parameterBuilder, final Map<Integer, Integer> positionIndexes, final List<Object> encryptValues) {
        if (!positionIndexes.isEmpty()) {
            for (Entry<Integer, Integer> entry : positionIndexes.entrySet()) {
                ((StandardParameterBuilder) parameterBuilder).getReplacedIndexAndParameters().put(entry.getValue(), encryptValues.get(entry.getKey()));
            }
        }
    }
}
