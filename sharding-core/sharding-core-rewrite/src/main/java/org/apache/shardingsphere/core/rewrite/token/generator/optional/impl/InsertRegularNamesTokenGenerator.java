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

package org.apache.shardingsphere.core.rewrite.token.generator.optional.impl;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.rewrite.builder.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.statement.InsertRewriteStatement;
import org.apache.shardingsphere.core.rewrite.statement.RewriteStatement;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertRegularNamesToken;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Insert regular column names token generator.
 *
 * @author panjuan
 */
public final class InsertRegularNamesTokenGenerator implements OptionalSQLTokenGenerator<BaseRule> {
    
    @Override
    public Optional<InsertRegularNamesToken> generateSQLToken(final RewriteStatement rewriteStatement,
                                                              final ParameterBuilder parameterBuilder, final BaseRule baseRule, final boolean isQueryWithCipherColumn) {
        if (!isNeedToGenerateSQLToken(rewriteStatement.getSqlStatementContext(), baseRule)) {
            return Optional.absent();
        }
        return createInsertColumnsToken(rewriteStatement, baseRule);
    }
    
    private boolean isNeedToGenerateSQLToken(final SQLStatementContext sqlStatementContext, final BaseRule baseRule) {
        Optional<InsertColumnsSegment> insertColumnsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        return !(baseRule instanceof MasterSlaveRule) 
                && sqlStatementContext instanceof InsertSQLStatementContext && insertColumnsSegment.isPresent() && insertColumnsSegment.get().getColumns().isEmpty();
    }
    
    private Optional<InsertRegularNamesToken> createInsertColumnsToken(final RewriteStatement rewriteStatement, final BaseRule baseRule) {
        Optional<InsertColumnsSegment> insertColumnsSegment = rewriteStatement.getSqlStatementContext().getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        return insertColumnsSegment.isPresent()
                ? Optional.of(new InsertRegularNamesToken(insertColumnsSegment.get().getStopIndex(), 
                getActualInsertColumns((InsertRewriteStatement) rewriteStatement, baseRule), !isNeedToAppendColumns((InsertSQLStatementContext) rewriteStatement.getSqlStatementContext(), baseRule)))
                : Optional.<InsertRegularNamesToken>absent();
    }
    
    private Collection<String> getActualInsertColumns(final InsertRewriteStatement rewriteStatement, final BaseRule baseRule) {
        Collection<String> result = new LinkedList<>();
        Map<String, String> logicAndCipherColumns = getEncryptRule(baseRule).getLogicAndCipherColumns(rewriteStatement.getSqlStatementContext().getTablesContext().getSingleTableName());
        boolean isGeneratedKey = rewriteStatement.getSqlStatementContext() instanceof InsertSQLStatementContext 
                && (rewriteStatement.getGeneratedKey().isPresent() && (rewriteStatement.getGeneratedKey().get().isGenerated()));
        for (String each : ((InsertSQLStatementContext) rewriteStatement.getSqlStatementContext()).getColumnNames()) {
            if (!isGeneratedKey || !each.equalsIgnoreCase(rewriteStatement.getGeneratedKey().get().getColumnName())) {
                result.add(logicAndCipherColumns.keySet().contains(each) ? logicAndCipherColumns.get(each) : each);
            }
        }
        if (isGeneratedKey) {
            result.add(rewriteStatement.getGeneratedKey().get().getColumnName());
        }
        return result;
    }
    
    private EncryptRule getEncryptRule(final BaseRule baseRule) {
        return baseRule instanceof ShardingRule ? ((ShardingRule) baseRule).getEncryptRule() : (EncryptRule) baseRule;
    }
    
    private boolean isNeedToAppendColumns(final InsertSQLStatementContext insertSQLStatementContext, final BaseRule baseRule) {
        return baseRule instanceof ShardingRule
                ? isNeedToAppendColumns(insertSQLStatementContext, (ShardingRule) baseRule) : isNeedToAppendAssistedQueryAndPlainColumns(insertSQLStatementContext, (EncryptRule) baseRule);
    }
    
    private boolean isNeedToAppendColumns(final InsertSQLStatementContext insertSQLStatementContext, final ShardingRule shardingRule) {
        return isNeedToAppendAssistedQueryAndPlainColumns(insertSQLStatementContext, shardingRule.getEncryptRule());
    }
    
    private boolean isNeedToAppendAssistedQueryAndPlainColumns(final SQLStatementContext sqlStatementContext, final EncryptRule encryptRule) {
        return !encryptRule.getAssistedQueryAndPlainColumns(sqlStatementContext.getTablesContext().getSingleTableName()).isEmpty();
    }
}
