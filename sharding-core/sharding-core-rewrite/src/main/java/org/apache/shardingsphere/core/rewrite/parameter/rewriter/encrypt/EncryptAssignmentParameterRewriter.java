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
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.core.rewrite.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collections;
import java.util.List;

/**
 * Update assignment parameter rewriter for encrypt.
 *
 * @author zhangliang
 */
@Setter
public final class EncryptAssignmentParameterRewriter implements ParameterRewriter, EncryptRuleAware {
    
    private EncryptRule encryptRule;
    
    @Override
    public void rewrite(final SQLStatementContext sqlStatementContext, final List<Object> parameters, final ParameterBuilder parameterBuilder) {
        if (sqlStatementContext.getSqlStatement() instanceof UpdateStatement) {
            String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
            for (AssignmentSegment each : ((UpdateStatement) sqlStatementContext.getSqlStatement()).getSetAssignment().getAssignments()) {
                if (each.getValue() instanceof ParameterMarkerExpressionSegment && encryptRule.findShardingEncryptor(tableName, each.getColumn().getName()).isPresent()) {
                    encryptParameters(tableName, each, parameters, parameterBuilder);
                }
            }
        }
    }
    
    private void encryptParameters(final String tableName, final AssignmentSegment assignmentSegment, final List<Object> parameters, final ParameterBuilder parameterBuilder) {
        String columnName = assignmentSegment.getColumn().getName();
        int parameterMarkerIndex = ((ParameterMarkerExpressionSegment) assignmentSegment.getValue()).getParameterMarkerIndex();
        Object originalValue = parameters.get(parameterMarkerIndex);
        Object cipherValue = encryptRule.getEncryptValues(tableName, columnName, Collections.singletonList(originalValue)).iterator().next();
        if (encryptRule.findPlainColumn(tableName, columnName).isPresent()) {
            ((StandardParameterBuilder) parameterBuilder).getAddedIndexAndParameters().put(parameterMarkerIndex + 1, cipherValue);
        } else {
            ((StandardParameterBuilder) parameterBuilder).getReplacedIndexAndParameters().put(parameterMarkerIndex, cipherValue);
        }
        if (encryptRule.findAssistedQueryColumn(tableName, columnName).isPresent()) {
            Object assistedQueryValue = encryptRule.getEncryptAssistedQueryValues(tableName, columnName, Collections.singletonList(originalValue)).iterator().next();
            ((StandardParameterBuilder) parameterBuilder).getAddedIndexAndParameters().put(parameterMarkerIndex + 2, assistedQueryValue);
        }
    }
}
