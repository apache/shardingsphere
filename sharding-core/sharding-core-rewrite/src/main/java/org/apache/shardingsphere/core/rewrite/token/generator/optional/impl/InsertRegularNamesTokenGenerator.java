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
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rewrite.builder.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.statement.InsertRewriteStatement;
import org.apache.shardingsphere.core.rewrite.statement.RewriteStatement;
import org.apache.shardingsphere.core.rewrite.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertRegularNamesToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Insert regular column names token generator.
 *
 * @author panjuan
 */
@Setter
public final class InsertRegularNamesTokenGenerator implements OptionalSQLTokenGenerator, EncryptRuleAware {
    
    private EncryptRule encryptRule;
    
    @Override
    public Optional<InsertRegularNamesToken> generateSQLToken(final RewriteStatement rewriteStatement, final ParameterBuilder parameterBuilder) {
        return isNeedToGenerateSQLToken(rewriteStatement.getSqlStatementContext().getSqlStatement())
                ? Optional.of(createInsertColumnsToken(rewriteStatement)) : Optional.<InsertRegularNamesToken>absent();
    }
    
    private boolean isNeedToGenerateSQLToken(final SQLStatement sqlStatement) {
        return sqlStatement instanceof InsertStatement && ((InsertStatement) sqlStatement).useDefaultColumns() && sqlStatement.findSQLSegment(InsertColumnsSegment.class).isPresent();
    }
    
    private InsertRegularNamesToken createInsertColumnsToken(final RewriteStatement rewriteStatement) {
        Optional<InsertColumnsSegment> insertColumnsSegment = rewriteStatement.getSqlStatementContext().getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        Preconditions.checkState(insertColumnsSegment.isPresent());
        return new InsertRegularNamesToken(insertColumnsSegment.get().getStopIndex(),
                getActualInsertColumns((InsertRewriteStatement) rewriteStatement), !hasMoreDerivedColumns(rewriteStatement.getSqlStatementContext().getTablesContext().getSingleTableName()));
    }
    
    private Collection<String> getActualInsertColumns(final InsertRewriteStatement rewriteStatement) {
        Collection<String> result = new LinkedList<>();
        Map<String, String> logicAndCipherColumns = encryptRule.getLogicAndCipherColumns(rewriteStatement.getSqlStatementContext().getTablesContext().getSingleTableName());
        boolean isGeneratedKey = rewriteStatement.getGeneratedKey().isPresent() && (rewriteStatement.getGeneratedKey().get().isGenerated());
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
    
    private boolean hasMoreDerivedColumns(final String tableName) {
        return !encryptRule.getAssistedQueryAndPlainColumns(tableName).isEmpty();
    }
    
}
