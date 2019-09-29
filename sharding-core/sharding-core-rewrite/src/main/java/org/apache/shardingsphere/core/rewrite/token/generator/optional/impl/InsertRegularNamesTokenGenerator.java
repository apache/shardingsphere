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
import org.apache.shardingsphere.core.route.router.sharding.keygen.GeneratedKey;
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
                ? Optional.of(createInsertColumnsToken((InsertRewriteStatement) rewriteStatement)) : Optional.<InsertRegularNamesToken>absent();
    }
    
    private boolean isNeedToGenerateSQLToken(final SQLStatement sqlStatement) {
        return sqlStatement instanceof InsertStatement && ((InsertStatement) sqlStatement).useDefaultColumns() && sqlStatement.findSQLSegment(InsertColumnsSegment.class).isPresent();
    }
    
    private InsertRegularNamesToken createInsertColumnsToken(final InsertRewriteStatement rewriteStatement) {
        Optional<InsertColumnsSegment> insertColumnsSegment = rewriteStatement.getSqlStatementContext().getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        Preconditions.checkState(insertColumnsSegment.isPresent());
        int startIndex = insertColumnsSegment.get().getStopIndex();
        boolean hasMoreDerivedColumns = !encryptRule.getAssistedQueryAndPlainColumns(rewriteStatement.getSqlStatementContext().getTablesContext().getSingleTableName()).isEmpty();
        if (rewriteStatement.getGeneratedKey().isPresent()) {
            return new InsertRegularNamesToken(
                    startIndex, getActualInsertColumns((InsertSQLStatementContext) rewriteStatement.getSqlStatementContext(), rewriteStatement.getGeneratedKey().get()), !hasMoreDerivedColumns);
        }
        return new InsertRegularNamesToken(startIndex, getActualInsertColumns((InsertSQLStatementContext) rewriteStatement.getSqlStatementContext()), !hasMoreDerivedColumns);
    }
    
    private Collection<String> getActualInsertColumns(final InsertSQLStatementContext insertSQLStatementContext, final GeneratedKey generatedKey) {
        Collection<String> result = new LinkedList<>();
        Map<String, String> logicAndCipherColumns = encryptRule.getLogicAndCipherColumns(insertSQLStatementContext.getTablesContext().getSingleTableName());
        for (String each : insertSQLStatementContext.getColumnNames()) {
            if (!generatedKey.isGenerated() || !each.equalsIgnoreCase(generatedKey.getColumnName())) {
                result.add(logicAndCipherColumns.keySet().contains(each) ? logicAndCipherColumns.get(each) : each);
            }
        }
        if (generatedKey.isGenerated()) {
            result.add(generatedKey.getColumnName());
        }
        return result;
    }
    
    private Collection<String> getActualInsertColumns(final InsertSQLStatementContext insertSQLStatementContext) {
        Collection<String> result = new LinkedList<>();
        Map<String, String> logicAndCipherColumns = encryptRule.getLogicAndCipherColumns(insertSQLStatementContext.getTablesContext().getSingleTableName());
        for (String each : insertSQLStatementContext.getColumnNames()) {
            result.add(logicAndCipherColumns.keySet().contains(each) ? logicAndCipherColumns.get(each) : each);
        }
        return result;
    }
}
