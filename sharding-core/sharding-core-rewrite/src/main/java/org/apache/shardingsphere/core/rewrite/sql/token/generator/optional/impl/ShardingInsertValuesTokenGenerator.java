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
import org.apache.shardingsphere.core.optimize.segment.insert.InsertValueContext;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rewrite.constant.EncryptDerivedColumnType;
import org.apache.shardingsphere.core.rewrite.constant.ShardingDerivedColumnType;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.SQLRouteResultAware;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.ShardingRuleAware;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.optional.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.impl.InsertValuesToken;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingCondition;
import org.apache.shardingsphere.core.route.router.sharding.keygen.GeneratedKey;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.encrypt.ShardingQueryAssistedEncryptor;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Insert values token generator for sharding.
 *
 * @author panjuan
 */
@Setter
public final class ShardingInsertValuesTokenGenerator implements OptionalSQLTokenGenerator, ShardingRuleAware, SQLRouteResultAware {
    
    private ShardingRule shardingRule;
    
    private SQLRouteResult sqlRouteResult;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof InsertStatement && !sqlStatementContext.getSqlStatement().findSQLSegments(InsertValuesSegment.class).isEmpty();
    }
    
    @Override
    public InsertValuesToken generateSQLToken(final SQLStatementContext sqlStatementContext) {
        processGeneratedKey((InsertSQLStatementContext) sqlStatementContext, sqlRouteResult.getGeneratedKey().orNull());
        processEncrypt((InsertSQLStatementContext) sqlStatementContext, shardingRule.getEncryptRule());
        Collection<InsertValuesSegment> insertValuesSegments = sqlStatementContext.getSqlStatement().findSQLSegments(InsertValuesSegment.class);
        InsertValuesToken result = new InsertValuesToken(getStartIndex(insertValuesSegments), getStopIndex(insertValuesSegments));
        Iterator<ShardingCondition> shardingConditionIterator = null == sqlRouteResult || sqlRouteResult.getShardingConditions().getConditions().isEmpty()
                ? null : sqlRouteResult.getShardingConditions().getConditions().iterator();
        for (InsertValueContext each : ((InsertSQLStatementContext) sqlStatementContext).getInsertValueContexts()) {
            Collection<DataNode> dataNodes = null == shardingConditionIterator ? Collections.<DataNode>emptyList() : shardingConditionIterator.next().getDataNodes();
            result.addInsertValueToken(each.getValueExpressions(), dataNodes);
        }
        return result;
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
    
    private int getStartIndex(final Collection<InsertValuesSegment> segments) {
        int result = segments.iterator().next().getStartIndex();
        for (InsertValuesSegment each : segments) {
            result = result > each.getStartIndex() ? each.getStartIndex() : result;
        }
        return result;
    }
    
    private int getStopIndex(final Collection<InsertValuesSegment> segments) {
        int result = segments.iterator().next().getStopIndex();
        for (InsertValuesSegment each : segments) {
            result = result < each.getStopIndex() ? each.getStopIndex() : result;
        }
        return result;
    }
}
