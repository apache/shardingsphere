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
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.segment.insert.InsertValueContext;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.rewrite.builder.sql.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.constant.EncryptDerivedColumnType;
import org.apache.shardingsphere.core.rewrite.constant.ShardingDerivedColumnType;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilderFactory;
import org.apache.shardingsphere.core.rewrite.parameter.rewriter.encrypt.EncryptParameterBuilderFactory;
import org.apache.shardingsphere.core.rewrite.parameter.rewriter.sharding.ShardingParameterBuilderFactory;
import org.apache.shardingsphere.core.rewrite.token.SQLTokenGenerators;
import org.apache.shardingsphere.core.rewrite.token.builder.BaseTokenGeneratorBuilder;
import org.apache.shardingsphere.core.rewrite.token.builder.EncryptTokenGenerateBuilder;
import org.apache.shardingsphere.core.rewrite.token.builder.ShardingTokenGenerateBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.SQLToken;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingCondition;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.route.router.sharding.keygen.GeneratedKey;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.encrypt.ShardingQueryAssistedEncryptor;

import java.util.Collections;
import java.util.Iterator;
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
            
    private final SQLStatementContext sqlStatementContext;
    
    private final List<Object> parameters;
    
    private final List<SQLToken> sqlTokens;
    
    private final SQLBuilder sqlBuilder;
    
    private final ParameterBuilder parameterBuilder;
    
    public SQLRewriteEngine(final ShardingRule shardingRule, final TableMetas tableMetas, 
                            final SQLRouteResult sqlRouteResult, final String sql, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        baseRule = shardingRule;
        sqlStatementContext = sqlRouteResult.getSqlStatementContext();
        if (sqlRouteResult.getSqlStatementContext() instanceof InsertSQLStatementContext) {
            processGeneratedKey((InsertSQLStatementContext) sqlRouteResult.getSqlStatementContext(), sqlRouteResult.getGeneratedKey().orNull());
            processEncrypt((InsertSQLStatementContext) sqlRouteResult.getSqlStatementContext(), shardingRule.getEncryptRule());
        }
        this.parameters = parameters;
        parameterBuilder = ShardingParameterBuilderFactory.build(shardingRule.getEncryptRule(), tableMetas, sqlRouteResult, parameters, isQueryWithCipherColumn);
        sqlTokens = createSQLTokens(tableMetas, parameters, 
                sqlRouteResult.getShardingConditions(), sqlRouteResult.getGeneratedKey().orNull(), sqlRouteResult.getRoutingResult().isSingleRouting(), isQueryWithCipherColumn);
        sqlBuilder = new SQLBuilder(sql, sqlTokens);
    }
    
    public SQLRewriteEngine(final EncryptRule encryptRule, final TableMetas tableMetas,
                            final SQLStatementContext sqlStatementContext, final String sql, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        baseRule = encryptRule;
        this.sqlStatementContext = sqlStatementContext;
        if (sqlStatementContext instanceof InsertSQLStatementContext) {
            processEncrypt((InsertSQLStatementContext) sqlStatementContext, encryptRule);
        }
        this.parameters = parameters;
        parameterBuilder = EncryptParameterBuilderFactory.build(encryptRule, tableMetas, sqlStatementContext, parameters, isQueryWithCipherColumn);
        sqlTokens = createSQLTokens(tableMetas, parameters, new ShardingConditions(Collections.<ShardingCondition>emptyList()), null, false, isQueryWithCipherColumn);
        sqlBuilder = new SQLBuilder(sql, sqlTokens);
    }
    
    public SQLRewriteEngine(final MasterSlaveRule masterSlaveRule, final SQLStatementContext sqlStatementContext, final String sql) {
        baseRule = masterSlaveRule;
        this.sqlStatementContext = sqlStatementContext;
        this.parameters = Collections.emptyList();
        parameterBuilder = ParameterBuilderFactory.newInstance(sqlStatementContext);
        sqlTokens = createSQLTokens(null, Collections.emptyList(), new ShardingConditions(Collections.<ShardingCondition>emptyList()), null, false, false);
        sqlBuilder = new SQLBuilder(sql, sqlTokens);
    }
    
    private void processGeneratedKey(final InsertSQLStatementContext insertSQLStatementContext, final GeneratedKey generatedKey) {
        if (null != generatedKey && generatedKey.isGenerated()) {
            Iterator<Comparable<?>> generatedValues = generatedKey.getGeneratedValues().descendingIterator();
            for (InsertValueContext each : insertSQLStatementContext.getInsertValueContexts()) {
                each.appendValue(generatedValues.next(), ShardingDerivedColumnType.KEY_GEN);
            }
        }
    }
    
    private void processEncrypt(final InsertSQLStatementContext insertSQLStatementContext, final EncryptRule encryptRule) {
        String tableName = insertSQLStatementContext.getTablesContext().getSingleTableName();
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        if (!encryptTable.isPresent()) {
            return;
        }
        for (String each : encryptTable.get().getLogicColumns()) {
            Optional<ShardingEncryptor> shardingEncryptor = encryptRule.findShardingEncryptor(tableName, each);
            if (shardingEncryptor.isPresent()) {
                encryptInsertValues(insertSQLStatementContext, encryptRule, shardingEncryptor.get(), tableName, each);
            }
        }
    }
    
    private void encryptInsertValues(final InsertSQLStatementContext insertSQLStatementContext, final EncryptRule encryptRule, final ShardingEncryptor shardingEncryptor,
                                     final String tableName, final String encryptLogicColumnName) {
        int columnIndex = insertSQLStatementContext.getColumnNames().indexOf(encryptLogicColumnName);
        for (InsertValueContext each : insertSQLStatementContext.getInsertValueContexts()) {
            encryptInsertValue(encryptRule, shardingEncryptor, tableName, columnIndex, each, encryptLogicColumnName);
        }
    }
    
    private void encryptInsertValue(final EncryptRule encryptRule, final ShardingEncryptor shardingEncryptor,
                                    final String tableName, final int columnIndex, final InsertValueContext insertValueContext, final String encryptLogicColumnName) {
        Object originalValue = insertValueContext.getValue(columnIndex);
        insertValueContext.setValue(columnIndex, shardingEncryptor.encrypt(originalValue));
        if (shardingEncryptor instanceof ShardingQueryAssistedEncryptor) {
            Optional<String> assistedColumnName = encryptRule.findAssistedQueryColumn(tableName, encryptLogicColumnName);
            Preconditions.checkArgument(assistedColumnName.isPresent(), "Can not find assisted query Column Name");
            insertValueContext.appendValue(((ShardingQueryAssistedEncryptor) shardingEncryptor).queryAssistedEncrypt(originalValue.toString()), EncryptDerivedColumnType.ENCRYPT);
        }
        if (encryptRule.findPlainColumn(tableName, encryptLogicColumnName).isPresent()) {
            insertValueContext.appendValue(originalValue, EncryptDerivedColumnType.ENCRYPT);
        }
    }
    
    private List<SQLToken> createSQLTokens(final TableMetas tableMetas, final List<Object> parameters, final ShardingConditions shardingConditions, 
                                           final GeneratedKey generatedKey, final boolean isSingleRoute, final boolean isQueryWithCipherColumn) {
        SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
        sqlTokenGenerators.addAll(new BaseTokenGeneratorBuilder().getSQLTokenGenerators());
        if (baseRule instanceof ShardingRule) {
            sqlTokenGenerators.addAll(new ShardingTokenGenerateBuilder((ShardingRule) baseRule, shardingConditions, generatedKey).getSQLTokenGenerators());
            sqlTokenGenerators.addAll(new EncryptTokenGenerateBuilder(((ShardingRule) baseRule).getEncryptRule(), isQueryWithCipherColumn).getSQLTokenGenerators());
        } else if (baseRule instanceof EncryptRule) {
            sqlTokenGenerators.addAll(new EncryptTokenGenerateBuilder((EncryptRule) baseRule, isQueryWithCipherColumn).getSQLTokenGenerators());
        }
        return sqlTokenGenerators.generateSQLTokens(sqlStatementContext, parameters, tableMetas, isSingleRoute);
    }
    
    /**
     * Generate SQL.
     * 
     * @return SQL unit
     */
    public SQLUnit generateSQL() {
        return new SQLUnit(sqlBuilder.toSQL(), parameterBuilder.getParameters(parameters));
    }
    
    /**
     * Generate SQL.
     * 
     * @param routingUnit routing unit
     * @param logicAndActualTables logic and actual tables
     * @return SQL unit
     */
    public SQLUnit generateSQL(final RoutingUnit routingUnit, final Map<String, String> logicAndActualTables) {
        return new SQLUnit(sqlBuilder.toSQL(routingUnit, logicAndActualTables), parameterBuilder.getParameters(parameters, routingUnit));
    }
}
