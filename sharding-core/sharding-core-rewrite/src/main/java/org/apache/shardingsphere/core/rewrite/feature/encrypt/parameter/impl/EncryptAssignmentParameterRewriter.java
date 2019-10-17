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

package org.apache.shardingsphere.core.rewrite.feature.encrypt.parameter.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.preprocessor.statement.SQLStatementContext;
import org.apache.shardingsphere.core.preprocessor.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Assignment parameter rewriter for encrypt.
 *
 * @author zhangliang
 */
@Setter
public final class EncryptAssignmentParameterRewriter implements ParameterRewriter, EncryptRuleAware {
    
    private EncryptRule encryptRule;
    
    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final SQLStatementContext sqlStatementContext, final List<Object> parameters) {
        if (isSetAssignmentStatement(sqlStatementContext)) {
            String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
            int count = 0;
            Collection<String> assistedQueryColumns = encryptRule.getAssistedQueryColumns(tableName);
            for (AssignmentSegment each : getSetAssignmentsSegment(sqlStatementContext.getSqlStatement()).getAssignments()) {
                if (each.getValue() instanceof ParameterMarkerExpressionSegment && encryptRule.findShardingEncryptor(tableName, each.getColumn().getName()).isPresent()) {
                    StandardParameterBuilder standardParameterBuilder = parameterBuilder instanceof StandardParameterBuilder
                            ? (StandardParameterBuilder) parameterBuilder : ((GroupedParameterBuilder) parameterBuilder).getParameterBuilders().get(0);  
                    encryptParameters(standardParameterBuilder, tableName, each, parameters, assistedQueryColumns, count);
                    count++;
                }
            }
        }
    }
    
    private boolean isSetAssignmentStatement(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof UpdateStatement
                || sqlStatementContext instanceof InsertSQLStatementContext && sqlStatementContext.getSqlStatement().findSQLSegment(SetAssignmentsSegment.class).isPresent();
    }
    
    private SetAssignmentsSegment getSetAssignmentsSegment(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            Optional<SetAssignmentsSegment> result = ((InsertStatement) sqlStatement).getSetAssignment();
            Preconditions.checkState(result.isPresent());
            return result.get();
        }
        return ((UpdateStatement) sqlStatement).getSetAssignment();
    }
    
    private void encryptParameters(final StandardParameterBuilder parameterBuilder, final String tableName, final AssignmentSegment assignmentSegment, final List<Object> parameters,
                                   final Collection<String> assistedQueryColumns, final int count) {
        String columnName = assignmentSegment.getColumn().getName();
        int parameterMarkerIndex = ((ParameterMarkerExpressionSegment) assignmentSegment.getValue()).getParameterMarkerIndex();
        Object originalValue = parameters.get(parameterMarkerIndex);
        Object cipherValue = encryptRule.getEncryptValues(tableName, columnName, Collections.singletonList(originalValue)).iterator().next();
        parameterBuilder.addReplacedParameters(parameterMarkerIndex, cipherValue);
        int addedPlainParameterMarkerIndex = parameterMarkerIndex + count + 1;
        if (!assistedQueryColumns.isEmpty()) {
            addedPlainParameterMarkerIndex = parameterMarkerIndex + count + 2;
        }
        if (encryptRule.findAssistedQueryColumn(tableName, columnName).isPresent()) {
            List<Object> assistedQueryValues = encryptRule.getEncryptAssistedQueryValues(tableName, columnName, Collections.singletonList(originalValue));
            int addedAssistedParameterMakerIndex = parameterMarkerIndex + count + 1;
            parameterBuilder.addAddedParameters(addedAssistedParameterMakerIndex, assistedQueryValues);
        }
        if (encryptRule.findPlainColumn(tableName, columnName).isPresent()) {
            parameterBuilder.addAddedParameters(addedPlainParameterMarkerIndex, Arrays.asList(originalValue));
        }
    }
}
