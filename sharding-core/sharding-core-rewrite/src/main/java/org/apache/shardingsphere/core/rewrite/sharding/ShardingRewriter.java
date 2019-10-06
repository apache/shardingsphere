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

package org.apache.shardingsphere.core.rewrite.sharding;

import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.rewriter.encrypt.EncryptParameterBuilderFactory;
import org.apache.shardingsphere.core.rewrite.parameter.rewriter.sharding.ShardingParameterBuilderFactory;
import org.apache.shardingsphere.core.rewrite.sql.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.sql.token.SQLTokenGenerators;
import org.apache.shardingsphere.core.rewrite.sql.token.builder.BaseTokenGeneratorBuilder;
import org.apache.shardingsphere.core.rewrite.sql.token.builder.EncryptTokenGenerateBuilder;
import org.apache.shardingsphere.core.rewrite.sql.token.builder.ShardingTokenGenerateBuilder;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.List;
import java.util.Map;

/**
 * Rewriter for sharding.
 * 
 * @author panjuan
 * @author zhangliang
 */
public final class ShardingRewriter {
    
    private final ShardingRule shardingRule;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final List<SQLToken> sqlTokens;
    
    private final SQLBuilder sqlBuilder;
    
    private final ParameterBuilder parameterBuilder;
    
    public ShardingRewriter(final ShardingRule shardingRule, final TableMetas tableMetas,
                            final SQLRouteResult sqlRouteResult, final String sql, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        this.shardingRule = shardingRule;
        sqlStatementContext = sqlRouteResult.getSqlStatementContext();
        parameterBuilder = createParameterBuilder(tableMetas, sqlRouteResult, parameters, isQueryWithCipherColumn);
        sqlTokens = createSQLTokens(tableMetas, parameters, sqlRouteResult, isQueryWithCipherColumn);
        sqlBuilder = new SQLBuilder(sql, sqlTokens);
    }
    
    private ParameterBuilder createParameterBuilder(final TableMetas tableMetas, final SQLRouteResult sqlRouteResult, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        ParameterBuilder result = sqlRouteResult.getSqlStatementContext() instanceof InsertSQLStatementContext
                ? new GroupedParameterBuilder(((InsertSQLStatementContext) sqlRouteResult.getSqlStatementContext()).getGroupedParameters(), sqlRouteResult.getShardingConditions())
                : new StandardParameterBuilder(parameters);
        ShardingParameterBuilderFactory.build(result, shardingRule, tableMetas, sqlRouteResult, parameters);
        EncryptParameterBuilderFactory.build(result, shardingRule.getEncryptRule(), tableMetas, sqlRouteResult.getSqlStatementContext(), parameters, isQueryWithCipherColumn);
        return result;
    }
    
    private List<SQLToken> createSQLTokens(final TableMetas tableMetas, final List<Object> parameters, final SQLRouteResult sqlRouteResult, final boolean isQueryWithCipherColumn) {
        SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
        sqlTokenGenerators.addAll(new BaseTokenGeneratorBuilder().getSQLTokenGenerators());
        sqlTokenGenerators.addAll(new ShardingTokenGenerateBuilder(shardingRule, sqlRouteResult).getSQLTokenGenerators());
        sqlTokenGenerators.addAll(new EncryptTokenGenerateBuilder(shardingRule.getEncryptRule(), isQueryWithCipherColumn).getSQLTokenGenerators());
        return sqlTokenGenerators.generateSQLTokens(sqlStatementContext, parameters, tableMetas, sqlRouteResult.getRoutingResult().isSingleRouting());
    }
    
    /**
     * Generate SQL.
     * 
     * @param routingUnit routing unit
     * @param logicAndActualTables logic and actual tables
     * @return SQL unit
     */
    public SQLUnit generateSQL(final RoutingUnit routingUnit, final Map<String, String> logicAndActualTables) {
        return new SQLUnit(sqlBuilder.toSQL(routingUnit, logicAndActualTables), parameterBuilder.getParameters(routingUnit));
    }
}
