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
import org.apache.shardingsphere.core.optimize.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.insert.InsertOptimizeResultUnit;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.insert.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.rewriter.sql.BaseSQLRewriter;
import org.apache.shardingsphere.core.rewrite.rewriter.sql.EncryptSQLRewriter;
import org.apache.shardingsphere.core.rewrite.rewriter.sql.SQLRewriter;
import org.apache.shardingsphere.core.rewrite.rewriter.sql.ShardingSQLRewriter;
import org.apache.shardingsphere.core.rewrite.token.BaseTokenGenerateEngine;
import org.apache.shardingsphere.core.rewrite.token.EncryptTokenGenerateEngine;
import org.apache.shardingsphere.core.rewrite.token.ShardingTokenGenerateEngine;
import org.apache.shardingsphere.core.rewrite.token.pojo.SQLToken;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.rule.BindingTableRule;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.encrypt.ShardingEncryptorEngine;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.encrypt.ShardingQueryAssistedEncryptor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
    
    private final Collection<SQLRewriter> sqlRewriters;
    
    public SQLRewriteEngine(final ShardingRule shardingRule, final SQLRouteResult sqlRouteResult, final List<Object> parameters, final boolean isSingleRoute) {
        baseRule = shardingRule;
        this.optimizedStatement = sqlRouteResult.getOptimizedStatement();
        encryptInsertOptimizedStatement();
        sqlTokens = createSQLTokens(parameters, isSingleRoute);
        sqlBuilder = new SQLBuilder();
        parameterBuilder = createParameterBuilder(parameters);
        sqlRewriters = createSQLRewriters(sqlRouteResult);
    }
    
    public SQLRewriteEngine(final EncryptRule encryptRule, final OptimizedStatement optimizedStatement, final List<Object> parameters) {
        baseRule = encryptRule;
        this.optimizedStatement = optimizedStatement;
        encryptInsertOptimizedStatement();
        sqlTokens = createSQLTokens(parameters, true);
        sqlBuilder = new SQLBuilder();
        parameterBuilder = createParameterBuilder(parameters);
        sqlRewriters = createSQLRewriters();
    }
    
    public SQLRewriteEngine(final MasterSlaveRule masterSlaveRule, final OptimizedStatement optimizedStatement) {
        baseRule = masterSlaveRule;
        this.optimizedStatement = optimizedStatement;
        sqlTokens = createSQLTokens(Collections.emptyList(), true);
        sqlBuilder = new SQLBuilder();
        parameterBuilder = createParameterBuilder(Collections.emptyList());
        sqlRewriters = Collections.<SQLRewriter>singletonList(new BaseSQLRewriter());
    }
    
    private void encryptInsertOptimizedStatement() {
        ShardingEncryptorEngine shardingEncryptorEngine = getShardingEncryptorEngine();
        if (isNeededToEncrypt(shardingEncryptorEngine)) {
            encryptInsertOptimizeResultUnit(shardingEncryptorEngine);
        }
    }
    
    private ShardingEncryptorEngine getShardingEncryptorEngine() {
        if (baseRule instanceof ShardingRule) {
            return ((ShardingRule) baseRule).getEncryptRule().getEncryptorEngine();
        } else if (baseRule instanceof EncryptRule) {
            return ((EncryptRule) baseRule).getEncryptorEngine();
        }
        return new ShardingEncryptorEngine();
    }
    
    private boolean isNeededToEncrypt(final ShardingEncryptorEngine shardingEncryptorEngine) {
        return optimizedStatement instanceof ShardingInsertOptimizedStatement && !shardingEncryptorEngine.getEncryptTableNames().isEmpty();
    }
    
    
    private void encryptInsertOptimizeResultUnit(final ShardingEncryptorEngine encryptorEngine) {
        for (InsertOptimizeResultUnit unit : ((ShardingInsertOptimizedStatement) optimizedStatement).getUnits()) {
            for (String each : ((ShardingInsertOptimizedStatement) optimizedStatement).getColumnNames()) {
                encryptInsertOptimizeResult(unit, each, encryptorEngine);
            }
        }
    }
    
    private void encryptInsertOptimizeResult(final InsertOptimizeResultUnit unit, final String columnName, final ShardingEncryptorEngine encryptorEngine) {
        Optional<ShardingEncryptor> shardingEncryptor = encryptorEngine.getShardingEncryptor(optimizedStatement.getSQLStatement().getTables().getSingleTableName(), columnName);
        if (!shardingEncryptor.isPresent()) {
            return;
        }
        if (shardingEncryptor.get() instanceof ShardingQueryAssistedEncryptor) {
            Optional<String> assistedColumnName = encryptorEngine.getAssistedQueryColumn(optimizedStatement.getSQLStatement().getTables().getSingleTableName(), columnName);
            Preconditions.checkArgument(assistedColumnName.isPresent(), "Can not find assisted query Column Name");
            unit.setColumnValue(assistedColumnName.get(), ((ShardingQueryAssistedEncryptor) shardingEncryptor.get()).queryAssistedEncrypt(unit.getColumnValue(columnName).toString()));
        }
        unit.setColumnValue(columnName, shardingEncryptor.get().encrypt(unit.getColumnValue(columnName)));
    }
    
    private List<SQLToken> createSQLTokens(final List<Object> parameters, final boolean isSingleRoute) {
        List<SQLToken> result = new LinkedList<>();
        result.addAll(new BaseTokenGenerateEngine().generateSQLTokens(optimizedStatement, parameterBuilder, baseRule, isSingleRoute));
        if (baseRule instanceof ShardingRule) {
            ShardingRule shardingRule = (ShardingRule) baseRule;
            result.addAll(new ShardingTokenGenerateEngine().generateSQLTokens(optimizedStatement, parameterBuilder, shardingRule, isSingleRoute));
            result.addAll(new EncryptTokenGenerateEngine().generateSQLTokens(optimizedStatement, parameterBuilder, shardingRule.getEncryptRule(), isSingleRoute));
        } else if (baseRule instanceof EncryptRule) {
            result.addAll(new EncryptTokenGenerateEngine().generateSQLTokens(optimizedStatement, parameterBuilder, (EncryptRule) baseRule, isSingleRoute));
        }
        Collections.sort(result);
        return result;
    }
    
    private ParameterBuilder createParameterBuilder(final List<Object> parameters) {
        ParameterBuilder result = new ParameterBuilder(parameters);
        result.setInsertParameterUnits(optimizedStatement);
        return result;
    }
    
    private Collection<SQLRewriter> createSQLRewriters(final SQLRouteResult sqlRouteResult) {
        Collection<SQLRewriter> result = new LinkedList<>();
        result.add(new BaseSQLRewriter());
        result.add(new ShardingSQLRewriter(sqlRouteResult, optimizedStatement));
        if (optimizedStatement.getSQLStatement() instanceof DMLStatement) {
            result.add(new EncryptSQLRewriter(((ShardingRule) baseRule).getEncryptRule().getEncryptorEngine(), optimizedStatement));
        }
        return result;
    }
    
    private Collection<SQLRewriter> createSQLRewriters() {
        Collection<SQLRewriter> result = new LinkedList<>();
        result.add(new BaseSQLRewriter());
        if (optimizedStatement.getSQLStatement() instanceof DMLStatement) {
            result.add(new EncryptSQLRewriter(((EncryptRule) baseRule).getEncryptorEngine(), optimizedStatement));
        }
        return result;
    }
    
    /**
     * Initialize SQL rewrite engine.
     * 
     * @param sqlRouteResult sql route result
     */
    public void init(final SQLRouteResult sqlRouteResult) {
        parameterBuilder.setReplacedIndexAndParameters(sqlRouteResult);
        init();
    }
    
    /**
     * Initialize SQL rewrite engine.
     *
     */
    public void init() {
        sqlBuilder.setLogicSQL(optimizedStatement.getSQLStatement().getLogicSQL());
        sqlBuilder.getSqlTokens().addAll(sqlTokens);
        if (sqlTokens.isEmpty()) {
            sqlBuilder.appendLiterals(optimizedStatement.getSQLStatement().getLogicSQL());
            return;
        }
        sqlBuilder.appendLiterals(optimizedStatement.getSQLStatement().getLogicSQL().substring(0, sqlTokens.get(0).getStartIndex()));
        for (SQLToken each : sqlTokens) {
            for (SQLRewriter sqlRewriter : sqlRewriters) {
                sqlRewriter.rewrite(sqlBuilder, parameterBuilder, each);
            }
            sqlBuilder.appendLiteral(each);
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
        for (String each : optimizedStatement.getSQLStatement().getTables().getTableNames()) {
            String tableName = each.toLowerCase();
            if (!tableName.equals(tableUnit.getLogicTableName().toLowerCase()) && bindingTableRule.hasLogicTable(tableName)) {
                result.put(tableName, bindingTableRule.getBindingActualTable(dataSourceName, tableName, tableUnit.getActualTableName()));
            }
        }
        return result;
    }
}
