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

package org.apache.shardingsphere.encrypt.rewrite.parameter.impl;

import lombok.Setter;
import org.apache.shardingsphere.encrypt.rewrite.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptCondition;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptConditionEngine;
import org.apache.shardingsphere.encrypt.rewrite.parameter.EncryptParameterRewriter;
import org.apache.shardingsphere.infra.metadata.schema.model.schema.SchemaMetaData;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Predicate parameter rewriter for encrypt.
 */
@Setter
public final class EncryptPredicateParameterRewriter extends EncryptParameterRewriter<SQLStatementContext> implements SchemaMetaDataAware, QueryWithCipherColumnAware {
    
    private SchemaMetaData schemaMetaData;
    
    private boolean queryWithCipherColumn;
    
    @Override
    protected boolean isNeedRewriteForEncrypt(final SQLStatementContext sqlStatementContext) {
        return true;
    }
    
    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final SQLStatementContext sqlStatementContext, final List<Object> parameters) {
        List<EncryptCondition> encryptConditions = new EncryptConditionEngine(getEncryptRule(), schemaMetaData).createEncryptConditions(sqlStatementContext);
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
        return getEncryptRule().findAssistedQueryColumn(tableName, columnName).isPresent()
                ? getEncryptRule().getEncryptAssistedQueryValues(tableName, columnName, originalValues) : getEncryptRule().getEncryptValues(tableName, columnName, originalValues);
    }
    
    private void encryptParameters(final ParameterBuilder parameterBuilder, final Map<Integer, Integer> positionIndexes, final List<Object> encryptValues) {
        if (!positionIndexes.isEmpty()) {
            for (Entry<Integer, Integer> entry : positionIndexes.entrySet()) {
                ((StandardParameterBuilder) parameterBuilder).addReplacedParameters(entry.getValue(), encryptValues.get(entry.getKey()));
            }
        }
    }
}
