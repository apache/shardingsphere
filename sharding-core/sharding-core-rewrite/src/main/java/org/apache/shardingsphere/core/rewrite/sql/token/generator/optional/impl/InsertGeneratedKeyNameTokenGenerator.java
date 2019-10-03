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

package org.apache.shardingsphere.core.rewrite.sql.token.generator.optional.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.SQLRouteResultAware;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.ShardingRuleAware;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.optional.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.impl.InsertGeneratedKeyNameToken;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Insert generated key name token generator.
 *
 * @author panjuan
 */
@Setter
public final class InsertGeneratedKeyNameTokenGenerator implements OptionalSQLTokenGenerator, ShardingRuleAware, SQLRouteResultAware {
    
    private ShardingRule shardingRule;
    
    private SQLRouteResult sqlRouteResult;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        Optional<InsertColumnsSegment> insertColumnsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        return insertColumnsSegment.isPresent() && !insertColumnsSegment.get().getColumns().isEmpty()
                && sqlRouteResult.getGeneratedKey().isPresent() && sqlRouteResult.getGeneratedKey().get().isGenerated();
    }
    
    @Override
    public InsertGeneratedKeyNameToken generateSQLToken(final SQLStatementContext sqlStatementContext) {
        Optional<InsertColumnsSegment> insertColumnsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        Preconditions.checkState(insertColumnsSegment.isPresent());
        InsertColumnsSegment segment = insertColumnsSegment.get();
        Preconditions.checkState(sqlRouteResult.getGeneratedKey().isPresent());
        return new InsertGeneratedKeyNameToken(segment.getStopIndex(), 
                sqlRouteResult.getGeneratedKey().get().getColumnName(), isToAddCloseParenthesis(sqlStatementContext.getTablesContext().getSingleTableName(), segment));
    }
    
    private boolean isToAddCloseParenthesis(final String tableName, final InsertColumnsSegment segment) {
        return segment.getColumns().isEmpty() && shardingRule.getEncryptRule().getAssistedQueryAndPlainColumns(tableName).isEmpty();
    }
}
