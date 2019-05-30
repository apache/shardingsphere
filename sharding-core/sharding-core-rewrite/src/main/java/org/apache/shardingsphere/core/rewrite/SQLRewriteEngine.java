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

package org.apache.shardingsphere.core.rewrite;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.token.SQLToken;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.rewriter.BaseSQLRewriter;
import org.apache.shardingsphere.core.rewrite.rewriter.EncryptSQLRewriter;
import org.apache.shardingsphere.core.rewrite.rewriter.SQLRewriter;
import org.apache.shardingsphere.core.rewrite.rewriter.ShardingSQLRewriter;
import org.apache.shardingsphere.core.rewrite.token.MasterSlaveTokenGenerateEngine;
import org.apache.shardingsphere.core.rewrite.token.ShardingTokenGenerateEngine;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.rule.BindingTableRule;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL rewrite engine.
 * 
 * @author panjuan
 * @author zhangliang
 */
public final class SQLRewriteEngine {
    
    private final BaseRule baseRule;
    
    private final SQLStatement sqlStatement;
    
    private final List<SQLToken> sqlTokens;
    
    private final SQLBuilder sqlBuilder;
    
    private final ParameterBuilder parameterBuilder;
    
    private final BaseSQLRewriter baseSQLRewriter;
    
    private final List<SQLRewriter> sqlRewriters;
    
    public SQLRewriteEngine(final ShardingRule shardingRule, final DatabaseType databaseType, final SQLRouteResult sqlRouteResult, final List<Object> parameters) {
        baseRule = shardingRule;
        sqlStatement = sqlRouteResult.getSqlStatement();
        sqlTokens = new ShardingTokenGenerateEngine().generateSQLTokens(sqlStatement, shardingRule);
        sqlBuilder = new SQLBuilder();
        parameterBuilder = new ParameterBuilder(parameters);
        baseSQLRewriter = new BaseSQLRewriter(sqlStatement, sqlTokens);
        sqlRewriters = Arrays.asList(new ShardingSQLRewriter(shardingRule, databaseType, sqlRouteResult),
                new EncryptSQLRewriter(shardingRule.getShardingEncryptorEngine(), sqlStatement, sqlRouteResult.getOptimizeResult()));
        pattern();
    }
    
    public SQLRewriteEngine(final EncryptRule encryptRule, final SQLStatement sqlStatement, final OptimizeResult optimizeResult, final List<Object> parameters) {
        baseRule = encryptRule;
        this.sqlStatement = sqlStatement;
        sqlTokens = sqlStatement.getSQLTokens();
        sqlBuilder = new SQLBuilder();
        parameterBuilder = new ParameterBuilder(parameters);
        baseSQLRewriter = new BaseSQLRewriter(sqlStatement, sqlTokens);
        sqlRewriters = Collections.<SQLRewriter>singletonList(new EncryptSQLRewriter(encryptRule.getEncryptorEngine(), sqlStatement, optimizeResult));
        pattern();
    }
    
    public SQLRewriteEngine(final SQLStatement sqlStatement) {
        baseRule = null;
        this.sqlStatement = sqlStatement;
        sqlTokens = new MasterSlaveTokenGenerateEngine().generateSQLTokens(sqlStatement, null);
        sqlBuilder = new SQLBuilder();
        parameterBuilder = new ParameterBuilder(Collections.emptyList());
        baseSQLRewriter = new BaseSQLRewriter(sqlStatement, sqlTokens);
        sqlRewriters = Collections.emptyList();
        pattern();
    }
    
    private void pattern() {
        if (sqlTokens.isEmpty()) {
            baseSQLRewriter.rewrite(sqlBuilder);
        } else {
            rewrite();
        }
    }
    
    private void rewrite() {
        baseSQLRewriter.rewriteInitialLiteral(sqlBuilder);
        for (SQLToken each : sqlTokens) {
            for (SQLRewriter sqlRewriter : sqlRewriters) {
                sqlRewriter.rewrite(sqlBuilder, parameterBuilder, each);
            }
            baseSQLRewriter.rewrite(sqlBuilder, parameterBuilder, each);
        }
    }
    
    /**
     * Generate SQL.
     * 
     * @return sql unit
     */
    public SQLUnit generateSQL() {
        return new SQLUnit(sqlBuilder.toSQL(), parameterBuilder.getParameters());
    }
    
    /**
     * Generate SQL.
     * 
     * @param routingUnit routing unit
     * @return sql unit
     */
    public SQLUnit generateSQL(final RoutingUnit routingUnit) {
        return new SQLUnit(sqlBuilder.toSQL(routingUnit, getTableTokens(routingUnit)), parameterBuilder.getParameters(routingUnit));
    }
   
    private Map<String, String> getTableTokens(final RoutingUnit routingUnit) {
        Map<String, String> result = new HashMap<>();
        for (TableUnit each : routingUnit.getTableUnits()) {
            String logicTableName = each.getLogicTableName().toLowerCase();
            result.put(logicTableName, each.getActualTableName());
            Optional<BindingTableRule> bindingTableRule = ((ShardingRule) baseRule).findBindingTableRule(logicTableName);
            if (bindingTableRule.isPresent()) {
                result.putAll(getBindingTableTokens(routingUnit.getMasterSlaveLogicDataSourceName(), each, bindingTableRule.get()));
            }
        }
        return result;
    }
    
    private Map<String, String> getBindingTableTokens(final String dataSourceName, final TableUnit tableUnit, final BindingTableRule bindingTableRule) {
        Map<String, String> result = new HashMap<>();
        for (String each : sqlStatement.getTables().getTableNames()) {
            String tableName = each.toLowerCase();
            if (!tableName.equals(tableUnit.getLogicTableName().toLowerCase()) && bindingTableRule.hasLogicTable(tableName)) {
                result.put(tableName, bindingTableRule.getBindingActualTable(dataSourceName, tableName, tableUnit.getActualTableName()));
            }
        }
        return result;
    }
}
