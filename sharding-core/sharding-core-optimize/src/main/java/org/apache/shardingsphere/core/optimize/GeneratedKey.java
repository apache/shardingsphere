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

package org.apache.shardingsphere.core.optimize;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.old.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Generated key.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class GeneratedKey {
    
    private final String columnName;
    
    private final List<Comparable<?>> generatedKeys = new LinkedList<>();
    
    /**
     * Get generate key.
     *
     * @param shardingRule sharding rule
     * @param parameters SQL parameters
     * @param insertStatement insert statement
     * @return generate key
     */
    public static Optional<GeneratedKey> getGenerateKey(final ShardingRule shardingRule, final List<Object> parameters, final InsertStatement insertStatement) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
        if (!generateKeyColumnName.isPresent()) {
            return Optional.absent();
        }
        return isContainsGenerateKeyColumn(insertStatement, generateKeyColumnName.get()) 
                ? findGeneratedKey(parameters, insertStatement, generateKeyColumnName.get()) : Optional.of(createGeneratedKey(shardingRule, insertStatement, generateKeyColumnName.get()));
    }
    
    private static boolean isContainsGenerateKeyColumn(final InsertStatement insertStatement, final String generateKeyColumnName) {
        int valuesCount = insertStatement.getValues().isEmpty() ? 0 : insertStatement.getValues().get(0).getAssignments().size();
        return valuesCount == insertStatement.getColumnNames().size() && insertStatement.getColumnNames().contains(generateKeyColumnName);
    }
    
    private static Optional<GeneratedKey> findGeneratedKey(final List<Object> parameters, final InsertStatement insertStatement, final String generateKeyColumnName) {
        GeneratedKey result = null;
        for (SQLExpression each : findGenerateKeyExpressions(insertStatement, generateKeyColumnName)) {
            if (null == result) {
                result = new GeneratedKey(generateKeyColumnName);
            }
            if (each instanceof SQLPlaceholderExpression) {
                result.getGeneratedKeys().add((Comparable<?>) parameters.get(((SQLPlaceholderExpression) each).getIndex()));
            } else if (each instanceof SQLNumberExpression) {
                result.getGeneratedKeys().add((Comparable<?>) ((SQLNumberExpression) each).getNumber());
            } else if (each instanceof SQLTextExpression) {
                result.getGeneratedKeys().add(((SQLTextExpression) each).getText());
            }
        }
        return Optional.fromNullable(result);
    }
    
    private static Collection<SQLExpression> findGenerateKeyExpressions(final InsertStatement insertStatement, final String generateKeyColumnName) {
        Collection<SQLExpression> result = new LinkedList<>();
        Collection<String> columnNames = getColumnNames(insertStatement, generateKeyColumnName);
        for (InsertValue each : insertStatement.getValues()) {
            Optional<SQLExpression> generateKeyExpression = findGenerateKeyExpression(generateKeyColumnName, columnNames.iterator(), each);
            if (generateKeyExpression.isPresent()) {
                result.add(generateKeyExpression.get());
            }
        }
        return result;
    }
    
    private static Collection<String> getColumnNames(final InsertStatement insertStatement, final String generateKeyColumnName) {
        int valuesCount = insertStatement.getValues().isEmpty() ? 0 : insertStatement.getValues().get(0).getAssignments().size();
        Collection<String> result = new ArrayList<>(insertStatement.getColumnNames());
        if (valuesCount != insertStatement.getColumnNames().size()) {
            result.remove(generateKeyColumnName);
        }
        return result;
    }
    
    private static Optional<SQLExpression> findGenerateKeyExpression(final String generateKeyColumnName, final Iterator<String> columnNames, final InsertValue insertValue) {
        for (SQLExpression each : insertValue.getAssignments()) {
            String columnName = columnNames.next();
            if (generateKeyColumnName.equalsIgnoreCase(columnName)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private static GeneratedKey createGeneratedKey(final ShardingRule shardingRule, final InsertStatement insertStatement, final String generateKeyColumnName) {
        String tableName = insertStatement.getTables().getSingleTableName();
        GeneratedKey result = new GeneratedKey(generateKeyColumnName);
        for (int i = 0; i < insertStatement.getValues().size(); i++) {
            result.getGeneratedKeys().add(shardingRule.generateKey(tableName));
        }
        return result;
    }
}
