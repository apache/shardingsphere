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

package org.apache.shardingsphere.core.optimizer.engine.sharding.insert;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.optimizer.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimizer.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimizer.engine.sharding.OptimizeEngine;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.parsing.parser.token.InsertValuesToken;
import org.apache.shardingsphere.core.parsing.parser.token.InsertValuesToken.InsertColumnValue;
import org.apache.shardingsphere.core.parsing.parser.token.ItemsToken;
import org.apache.shardingsphere.core.routing.GeneratedKey;
import org.apache.shardingsphere.core.routing.strategy.value.ListRouteValue;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.encrypt.ShardingQueryAssistedEncryptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert optimize engine.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class InsertOptimizeEngine implements OptimizeEngine {
    
    private final ShardingRule shardingRule;
    
    private final InsertStatement insertStatement;
    
    private final List<Object> parameters;
    
    private final GeneratedKey generatedKey;
    
    @Override
    public ShardingConditions optimize() {
        List<AndCondition> andConditions = insertStatement.getRouteConditions().getOrCondition().getAndConditions();
        List<InsertValue> insertValues = insertStatement.getInsertValues().getInsertValues();
        InsertValuesToken insertValuesToken = getInsertValuesToken();
        Iterator<Comparable<?>> generatedKeys = createGeneratedKeys();
        List<ShardingCondition> result = new ArrayList<>(andConditions.size());
        int parametersCount = 0;
        for (int i = 0; i < andConditions.size(); i++) {
            InsertValue insertValue = insertValues.get(i);
            List<Object> currentParameters = new ArrayList<>(insertValue.getParametersCount() + 1);
            if (0 != insertValue.getParametersCount()) {
                currentParameters = getCurrentParameters(parametersCount, insertValue.getParametersCount());
                parametersCount = parametersCount + insertValue.getParametersCount();
            }
            ShardingCondition shardingCondition = createShardingCondition(andConditions.get(i));
            insertValuesToken.addInsertColumnValue(insertValue.getColumnValues(), currentParameters);
            if (isNeededToAppendGeneratedKey()) {
                Comparable<?> currentGeneratedKey = generatedKeys.next();
                fillInsertStatementWithGeneratedKeyName(insertValuesToken);
                fillInsertColumnValueWithColumnValue(insertValuesToken.getColumnValues().get(i), currentGeneratedKey);
                fillShardingCondition(shardingCondition, currentGeneratedKey);
            }
            if (isNeededToEncrypt()) {
                encryptInsertColumnValues(insertValuesToken, i);
            }
            result.add(shardingCondition);
        }
        return new ShardingConditions(result);
    }
    
    private InsertValuesToken getInsertValuesToken() {
        InsertValuesToken result = insertStatement.getInsertValuesToken();
        clearCacheColumnValues(result);
        result.getColumnNames().addAll(insertStatement.getInsertColumnNames());
        return result;
    }
    
    private void clearCacheColumnValues(final InsertValuesToken insertValuesToken) {
        if (!insertValuesToken.getColumnNames().isEmpty() && !insertValuesToken.getColumnValues().isEmpty()) {
            insertValuesToken.getColumnNames().clear();
            insertValuesToken.getColumnValues().clear();
        }
    }
    
    private Iterator<Comparable<?>> createGeneratedKeys() {
        return isNeededToAppendGeneratedKey() ? generatedKey.getGeneratedKeys().iterator() : null;
    }
    
    private List<Object> getCurrentParameters(final int beginCount, final int increment) {
        List<Object> result = new ArrayList<>(increment + 1);
        result.addAll(parameters.subList(beginCount, beginCount + increment));
        return result;
    }
    
    private ShardingCondition createShardingCondition(final AndCondition andCondition) {
        ShardingCondition result = new ShardingCondition();
        result.getShardingValues().addAll(getShardingValues(andCondition));
        return result;
    }
    
    private Collection<ListRouteValue> getShardingValues(final AndCondition andCondition) {
        Collection<ListRouteValue> result = new LinkedList<>();
        for (Condition each : andCondition.getConditions()) {
            result.add(new ListRouteValue<>(each.getColumn().getName(), each.getColumn().getTableName(), each.getConditionValues(parameters)));
        }
        return result;
    }
    
    private boolean isNeededToAppendGeneratedKey() {
        String tableName = insertStatement.getTables().getSingleTableName();
        Optional<String> generateKeyColumn = shardingRule.findGenerateKeyColumnName(tableName);
        return generateKeyColumn.isPresent() && !insertStatement.getColumns().contains(new Column(generateKeyColumn.get(), tableName));
    }
    
    private void fillInsertStatementWithGeneratedKeyName(final InsertValuesToken insertValuesToken) {
        String generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName()).get();
        insertValuesToken.getColumnNames().add(generateKeyColumnName);
        fillInsertStatementWithColumnName(insertValuesToken, generateKeyColumnName);
    }
    
    private void fillInsertStatementWithColumnName(final InsertValuesToken insertValuesToken, final String columnName) {
        if (DefaultKeyword.VALUES == insertValuesToken.getType() && isNotExisted(columnName)) {
            ItemsToken itemsToken = new ItemsToken(insertStatement.getColumnsListLastIndex());
            itemsToken.getItems().add(columnName);
            insertStatement.getSQLTokens().add(itemsToken);
        }
    }
    
    private boolean isNotExisted(final String insertColumnName) {
        return Collections2.filter(insertStatement.getItemsTokens(), new Predicate<ItemsToken>() {
            
            @Override
            public boolean apply(final ItemsToken input) {
                return input.getItems().contains(insertColumnName);
            }
        }).isEmpty();
    }
    
    private boolean isNeededToEncrypt() {
        return shardingRule.getShardingEncryptorEngine().isHasShardingEncryptorStrategy(insertStatement.getTables().getSingleTableName());
    }
    
    private void encryptInsertColumnValues(final InsertValuesToken insertValuesToken, final int insertColumnValueIndex) {
        for (int i = 0; i < insertValuesToken.getColumnNames().size(); i++) {
            Optional<ShardingEncryptor> shardingEncryptor = 
                    shardingRule.getShardingEncryptorEngine().getShardingEncryptor(insertStatement.getTables().getSingleTableName(), insertValuesToken.getColumnName(i));
            if (shardingEncryptor.isPresent()) {
                handleEncryptColumnsAndValues(insertValuesToken, insertColumnValueIndex, i, shardingEncryptor.get());
            }
        }
    }
    
    private void handleEncryptColumnsAndValues(final InsertValuesToken insertValuesToken, final int insertColumnValueIndex, final int columnIndex, final ShardingEncryptor shardingEncryptor) {
        InsertColumnValue insertColumnValue = insertValuesToken.getColumnValues().get(insertColumnValueIndex);
        if (shardingEncryptor instanceof ShardingQueryAssistedEncryptor) {
            String columnName = insertValuesToken.getColumnName(columnIndex);
            String assistedColumnName = shardingRule.getShardingEncryptorEngine().getAssistedQueryColumn(insertStatement.getTables().getSingleTableName(), columnName).get();
            insertValuesToken.getColumnNames().add(assistedColumnName);
            fillInsertStatementWithColumnName(insertValuesToken, assistedColumnName);
            fillInsertColumnValueWithColumnValue(
                    insertColumnValue, ((ShardingQueryAssistedEncryptor) shardingEncryptor).queryAssistedEncrypt(insertColumnValue.getColumnValue(columnIndex).toString()));
        }
        insertColumnValue.setColumnValue(columnIndex, shardingEncryptor.encrypt(insertColumnValue.getColumnValue(columnIndex)));
    }
    
    private void fillInsertColumnValueWithColumnValue(final InsertColumnValue insertColumnValue, final Comparable<?> columnValue) {
        if (!parameters.isEmpty()) {
            insertColumnValue.getValues().add(new SQLPlaceholderExpression(parameters.size() - 1));
            insertColumnValue.getParameters().add(columnValue);
        } else if (columnValue.getClass() == String.class) {
            insertColumnValue.getValues().add(new SQLTextExpression(columnValue.toString()));
        } else {
            insertColumnValue.getValues().add(new SQLNumberExpression((Number) columnValue));
        }
    }
    
    private void fillShardingCondition(final ShardingCondition shardingCondition, final Comparable<?> currentGeneratedKey) {
        String tableName = insertStatement.getTables().getSingleTableName();
        String generateKeyColumnName = shardingRule.findGenerateKeyColumnName(tableName).get();
        if (shardingRule.isShardingColumn(generateKeyColumnName, tableName)) {
            Column generateKeyColumn = new Column(generateKeyColumnName, tableName);
            List<Comparable<?>> conditionValues = new GeneratedKeyCondition(generateKeyColumn, -1, currentGeneratedKey).getConditionValues(parameters);
            shardingCondition.getShardingValues().add(new ListRouteValue<>(generateKeyColumn.getName(), generateKeyColumn.getTableName(), conditionValues));
        }
        insertStatement.setContainGenerateKey(true);
    }
}
