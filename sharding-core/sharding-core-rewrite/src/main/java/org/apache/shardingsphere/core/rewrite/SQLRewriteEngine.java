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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.token.SQLToken;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.rewriter.BaseSQLRewriter;
import org.apache.shardingsphere.core.rewrite.rewriter.EncryptSQLRewriter;
import org.apache.shardingsphere.core.rewrite.rewriter.ShardingSQLRewriter;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.rule.BindingTableRule;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.encrypt.ShardingEncryptorEngine;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL rewrite engine.
 * 
 * <p>Rewrite logic SQL to actual SQL.</p>
 *
 * @author panjuan
 */
@RequiredArgsConstructor
public final class SQLRewriteEngine {
    
    private final BaseRule baseRule;
    
    private final DatabaseType databaseType;
    
    private final SQLRouteResult sqlRouteResult;
    
    private final SQLStatement sqlStatement;
    
    private final SQLBuilder sqlBuilder;
    
    private final ParameterBuilder parameterBuilder;
    
    public SQLRewriteEngine(final ShardingRule shardingRule, final DatabaseType databaseType, final SQLRouteResult sqlRouteResult, final List<Object> parameters) {
        this(shardingRule, databaseType, sqlRouteResult, sqlRouteResult.getSqlStatement(), new SQLBuilder(), new ParameterBuilder(parameters));
        pattern(sqlRouteResult.getOptimizeResult());
    }
    
    public SQLRewriteEngine(final EncryptRule encryptRule, final DatabaseType databaseType, final SQLStatement sqlStatement, final OptimizeResult optimizeResult, final List<Object> parameters) {
        this(encryptRule, databaseType, null, sqlStatement, new SQLBuilder(), new ParameterBuilder(parameters));
        pattern(optimizeResult);
    }
    
    public SQLRewriteEngine(final SQLStatement sqlStatement) {
        this(null, null, null, sqlStatement, new SQLBuilder(), new ParameterBuilder(Collections.emptyList()));
        pattern(null);
    }
    
    private void pattern(final OptimizeResult optimizeResult) {
        BaseSQLRewriter baseSQLRewriter = new BaseSQLRewriter(sqlStatement);
        if (baseSQLRewriter.isToRewriteSQLTokens()) {
            rewrite(baseSQLRewriter, new ShardingSQLRewriter(getShardingRule(), sqlStatement.getLogicSQL(), databaseType, sqlStatement, sqlRouteResult), 
                    new EncryptSQLRewriter(getShardingEncryptorEngine(), sqlStatement, optimizeResult));
        } else {
            baseSQLRewriter.rewrite(sqlBuilder);
        }
    }
    
    private ShardingEncryptorEngine getShardingEncryptorEngine() {
        if (null == baseRule) {
            return null;
        }
        return baseRule instanceof ShardingRule ? ((ShardingRule) baseRule).getShardingEncryptorEngine() : ((EncryptRule) baseRule).getEncryptorEngine();
    }
    
    private ShardingRule getShardingRule() {
        return baseRule instanceof ShardingRule ? (ShardingRule) baseRule : null;
    }
    
    private void rewrite(final BaseSQLRewriter baseSQLRewriter, final ShardingSQLRewriter shardingSQLRewriter, final EncryptSQLRewriter encryptSQLRewriter) {
        baseSQLRewriter.rewriteInitialLiteral(sqlBuilder);
        for (SQLToken each : sqlStatement.getSQLTokens()) {
            shardingSQLRewriter.rewrite(sqlBuilder, parameterBuilder, each);
            encryptSQLRewriter.rewrite(sqlBuilder, parameterBuilder, each);
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
