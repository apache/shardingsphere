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

package org.apache.shardingsphere.core.rewrite.statement;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.rewrite.encrypt.EncryptCondition;
import org.apache.shardingsphere.core.rewrite.encrypt.EncryptConditions;
import org.apache.shardingsphere.core.rewrite.encrypt.WhereClauseEncryptConditionEngine;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingCondition;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collections;

/**
 * Rewrite statement factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RewriteStatementFactory {
    
    /**
     * Create new instance of rewrite statement.
     * 
     * @param shardingRule sharding rule
     * @param tableMetas table metas
     * @param sqlRouteResult sql route result
     * @return rewrite statement
     */
    public static RewriteStatement newInstance(final ShardingRule shardingRule, final TableMetas tableMetas, final SQLRouteResult sqlRouteResult) {
        EncryptConditions encryptConditions = sqlRouteResult.getSqlStatementContext().getSqlStatement() instanceof DMLStatement
                ? createEncryptConditions(shardingRule.getEncryptRule(), tableMetas, (DMLStatement) sqlRouteResult.getSqlStatementContext().getSqlStatement())
                : new EncryptConditions(Collections.<EncryptCondition>emptyList());
        return sqlRouteResult.getSqlStatementContext() instanceof InsertSQLStatementContext
                ? new InsertRewriteStatement(
                        (InsertSQLStatementContext) sqlRouteResult.getSqlStatementContext(), sqlRouteResult.getShardingConditions(), encryptConditions, 
                        sqlRouteResult.getGeneratedKey().orNull(), shardingRule.getEncryptRule())
                : new RewriteStatement(sqlRouteResult.getSqlStatementContext(), sqlRouteResult.getShardingConditions(), encryptConditions);
    }
    
    /**
     * Create new instance of rewrite statement.
     * 
     * @param encryptRule encrypt rule
     * @param tableMetas table metas
     * @param sqlStatementContext SQL statement context
     * @return rewrite statement
     */
    public static RewriteStatement newInstance(final EncryptRule encryptRule, final TableMetas tableMetas, final SQLStatementContext sqlStatementContext) {
        ShardingConditions shardingConditions = new ShardingConditions(Collections.<ShardingCondition>emptyList());
        EncryptConditions encryptConditions = sqlStatementContext.getSqlStatement() instanceof DMLStatement
                ? createEncryptConditions(encryptRule, tableMetas, (DMLStatement) sqlStatementContext.getSqlStatement()) : new EncryptConditions(Collections.<EncryptCondition>emptyList());
        return sqlStatementContext instanceof InsertSQLStatementContext
                ? new InsertRewriteStatement((InsertSQLStatementContext) sqlStatementContext, shardingConditions, encryptConditions, null, encryptRule)
                : new RewriteStatement(sqlStatementContext, shardingConditions, encryptConditions);
    }
    
    private static EncryptConditions createEncryptConditions(final EncryptRule encryptRule, final TableMetas tableMetas, final DMLStatement sqlStatement) {
        return new EncryptConditions(new WhereClauseEncryptConditionEngine(encryptRule, tableMetas).createEncryptConditions(sqlStatement));
    }
}
