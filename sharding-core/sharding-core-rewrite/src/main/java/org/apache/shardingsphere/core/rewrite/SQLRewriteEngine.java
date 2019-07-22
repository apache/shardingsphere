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
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.InsertOptimizeResultUnit;
import org.apache.shardingsphere.core.rewrite.builder.BaseParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.InsertParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.token.BaseTokenGenerateEngine;
import org.apache.shardingsphere.core.rewrite.token.EncryptTokenGenerateEngine;
import org.apache.shardingsphere.core.rewrite.token.ShardingTokenGenerateEngine;
import org.apache.shardingsphere.core.rewrite.token.pojo.SQLToken;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptEngine;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.encrypt.ShardingQueryAssistedEncryptor;

import java.util.Collections;
import java.util.LinkedList;
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
    
    private final OptimizedStatement optimizedStatement;
    
    private final List<SQLToken> sqlTokens;
    
    private final SQLBuilder sqlBuilder;
    
    private final ParameterBuilder parameterBuilder;
    
    public SQLRewriteEngine(final ShardingRule shardingRule, 
                            final SQLRouteResult sqlRouteResult, final String sql, final List<Object> parameters, final boolean isSingleRoute, final boolean isQueryWithCipherColumn) {
        baseRule = shardingRule;
        this.optimizedStatement = getEncryptedOptimizedStatement(shardingRule.getEncryptRule().getEncryptEngine(), sqlRouteResult.getOptimizedStatement());
        parameterBuilder = createParameterBuilder(parameters, sqlRouteResult);
        sqlTokens = createSQLTokens(isSingleRoute, isQueryWithCipherColumn);
        sqlBuilder = new SQLBuilder(sql, sqlTokens);
    }
    
    public SQLRewriteEngine(final EncryptRule encryptRule, final OptimizedStatement optimizedStatement, final String sql, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        baseRule = encryptRule;
        this.optimizedStatement = getEncryptedOptimizedStatement(encryptRule.getEncryptEngine(), optimizedStatement);
        parameterBuilder = createParameterBuilder(parameters);
        sqlTokens = createSQLTokens(false, isQueryWithCipherColumn);
        sqlBuilder = new SQLBuilder(sql, sqlTokens);
    }
    
    public SQLRewriteEngine(final MasterSlaveRule masterSlaveRule, final OptimizedStatement optimizedStatement, final String sql) {
        baseRule = masterSlaveRule;
        this.optimizedStatement = optimizedStatement;
        parameterBuilder = createParameterBuilder(Collections.emptyList());
        sqlTokens = createSQLTokens(false, false);
        sqlBuilder = new SQLBuilder(sql, sqlTokens);
    }
    
    private OptimizedStatement getEncryptedOptimizedStatement(final EncryptEngine encryptEngine, final OptimizedStatement optimizedStatement) {
        if (isNeededToEncrypt(encryptEngine, optimizedStatement)) {
            encryptInsertOptimizeResultUnit(encryptEngine, optimizedStatement);
        }
        return optimizedStatement;
    }
    
    private boolean isNeededToEncrypt(final EncryptEngine encryptEngine, final OptimizedStatement optimizedStatement) {
        return optimizedStatement instanceof InsertOptimizedStatement && !encryptEngine.getEncryptTableNames().isEmpty();
    }
    
    private void encryptInsertOptimizeResultUnit(final EncryptEngine encryptEngine, final OptimizedStatement optimizedStatement) {
        for (InsertOptimizeResultUnit unit : ((InsertOptimizedStatement) optimizedStatement).getUnits()) {
            for (String each : ((InsertOptimizedStatement) optimizedStatement).getInsertColumns().getRegularColumnNames()) {
                encryptInsertOptimizeResult(encryptEngine, unit, optimizedStatement.getTables().getSingleTableName(), each);
            }
        }
    }
    
    private void encryptInsertOptimizeResult(final EncryptEngine encryptEngine, final InsertOptimizeResultUnit unit, final String tableName, final String columnName) {
        Optional<ShardingEncryptor> shardingEncryptor = encryptEngine.getShardingEncryptor(tableName, columnName);
        if (!shardingEncryptor.isPresent()) {
            return;
        }
        if (shardingEncryptor.get() instanceof ShardingQueryAssistedEncryptor) {
            Optional<String> assistedColumnName = encryptEngine.getAssistedQueryColumn(tableName, columnName);
            Preconditions.checkArgument(assistedColumnName.isPresent(), "Can not find assisted query Column Name");
            unit.setColumnValue(assistedColumnName.get(), ((ShardingQueryAssistedEncryptor) shardingEncryptor.get()).queryAssistedEncrypt(unit.getColumnValue(columnName).toString()));
        }
        unit.setColumnValue(columnName, shardingEncryptor.get().encrypt(unit.getColumnValue(columnName)));
    }
    
    private ParameterBuilder createParameterBuilder(final List<Object> parameters, final SQLRouteResult sqlRouteResult) {
        if (optimizedStatement instanceof InsertOptimizedStatement) {
            return new InsertParameterBuilder(parameters, (InsertOptimizedStatement) optimizedStatement);
        }
        return new BaseParameterBuilder(parameters, sqlRouteResult);
    }
    
    private ParameterBuilder createParameterBuilder(final List<Object> parameters) {
        if (optimizedStatement instanceof InsertOptimizedStatement) {
            return new InsertParameterBuilder(parameters, (InsertOptimizedStatement) optimizedStatement);
        }
        return new BaseParameterBuilder(parameters);
    }
    
    private List<SQLToken> createSQLTokens(final boolean isSingleRoute, final boolean isQueryWithCipherColumn) {
        List<SQLToken> result = new LinkedList<>();
        result.addAll(new BaseTokenGenerateEngine().generateSQLTokens(optimizedStatement, parameterBuilder, baseRule, isSingleRoute, isQueryWithCipherColumn));
        if (baseRule instanceof ShardingRule) {
            ShardingRule shardingRule = (ShardingRule) baseRule;
            result.addAll(new ShardingTokenGenerateEngine().generateSQLTokens(optimizedStatement, parameterBuilder, shardingRule, isSingleRoute, isQueryWithCipherColumn));
            result.addAll(new EncryptTokenGenerateEngine().generateSQLTokens(optimizedStatement, parameterBuilder, shardingRule.getEncryptRule(), isSingleRoute, isQueryWithCipherColumn));
        } else if (baseRule instanceof EncryptRule) {
            result.addAll(new EncryptTokenGenerateEngine().generateSQLTokens(optimizedStatement, parameterBuilder, (EncryptRule) baseRule, isSingleRoute, isQueryWithCipherColumn));
        }
        Collections.sort(result);
        return result;
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
     * @param logicAndActualTables logic and actual tables
     * @return sql unit
     */
    public SQLUnit generateSQL(final RoutingUnit routingUnit, final Map<String, String> logicAndActualTables) {
        return new SQLUnit(sqlBuilder.toSQL(routingUnit, logicAndActualTables), parameterBuilder.getParameters(routingUnit));
    }
}
