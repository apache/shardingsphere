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

package org.apache.shardingsphere.core.rewrite.feature.encrypt.token.generator.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.preprocessor.statement.SQLStatementContext;
import org.apache.shardingsphere.core.preprocessor.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.token.pojo.AssistQueryAndPlainInsertColumnsToken;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.LinkedList;
import java.util.List;

/**
 * Assist query and plain insert columns token generator.
 *
 * @author panjuan
 * @author zhangliang
 */
@Setter
public final class AssistQueryAndPlainInsertColumnsTokenGenerator implements OptionalSQLTokenGenerator, EncryptRuleAware {
    
    private EncryptRule encryptRule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertSQLStatementContext && sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class).isPresent()
                && !((InsertStatement) sqlStatementContext.getSqlStatement()).useDefaultColumns()
                && !encryptRule.getAssistedQueryAndPlainColumns(sqlStatementContext.getTablesContext().getSingleTableName()).isEmpty();
    }
    
    @Override
    public AssistQueryAndPlainInsertColumnsToken generateSQLToken(final SQLStatementContext sqlStatementContext) {
        Optional<InsertColumnsSegment> sqlSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        Preconditions.checkState(sqlSegment.isPresent());
        return new AssistQueryAndPlainInsertColumnsToken(
                sqlSegment.get().getStopIndex(), getDerivedColumnNames((InsertSQLStatementContext) sqlStatementContext));
    }
    
    private List<String> getDerivedColumnNames(final InsertSQLStatementContext sqlStatementContext) {
        List<String> result = new LinkedList<>();
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        for (String each : sqlStatementContext.getColumnNames()) {
            Optional<String> assistedQueryColumn = encryptRule.findAssistedQueryColumn(tableName, each);
            if (assistedQueryColumn.isPresent()) {
                result.add(assistedQueryColumn.get());
            }
            Optional<String> plainColumn = encryptRule.findPlainColumn(tableName, each);
            if (plainColumn.isPresent()) {
                result.add(plainColumn.get());
            }
        }
        return result;
    }
}
