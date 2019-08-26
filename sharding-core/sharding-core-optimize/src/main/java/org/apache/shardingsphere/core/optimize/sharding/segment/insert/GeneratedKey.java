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

package org.apache.shardingsphere.core.optimize.sharding.segment.insert;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

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
@ToString
public final class GeneratedKey {
    
    private final String columnName;
    
    private final boolean generated;
    
    private final List<Comparable<?>> generatedValues = new LinkedList<>();
    
    /**
     * Get generate key.
     *
     * @param shardingRule sharding rule
     * @param parameters SQL parameters
     * @param insertStatement insert statement
     * @param insertColumns insert columns
     * @return generate key
     */
    public static Optional<GeneratedKey> getGenerateKey(final ShardingRule shardingRule, 
                                                        final List<Object> parameters, final InsertStatement insertStatement, final ShardingInsertColumns insertColumns) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTable().getTableName());
        if (!generateKeyColumnName.isPresent()) {
            return Optional.absent();
        }
        return insertColumns.getRegularColumnNames().contains(generateKeyColumnName.get()) 
                ? findGeneratedKey(parameters, insertStatement, insertColumns, generateKeyColumnName.get())
                : Optional.of(createGeneratedKey(shardingRule, insertStatement, generateKeyColumnName.get()));
    }
    
    private static Optional<GeneratedKey> findGeneratedKey(
            final List<Object> parameters, final InsertStatement insertStatement, final ShardingInsertColumns insertColumns, final String generateKeyColumnName) {
        GeneratedKey result = null;
        for (ExpressionSegment each : findGenerateKeyExpressionSegments(insertStatement, insertColumns, generateKeyColumnName)) {
            if (null == result) {
                result = new GeneratedKey(generateKeyColumnName, false);
            }
            if (each instanceof ParameterMarkerExpressionSegment) {
                result.getGeneratedValues().add((Comparable<?>) parameters.get(((ParameterMarkerExpressionSegment) each).getParameterMarkerIndex()));
            } else if (each instanceof LiteralExpressionSegment) {
                result.getGeneratedValues().add((Comparable<?>) ((LiteralExpressionSegment) each).getLiterals());
            }
        }
        return Optional.fromNullable(result);
    }
    
    private static Collection<ExpressionSegment> findGenerateKeyExpressionSegments(
            final InsertStatement insertStatement, final ShardingInsertColumns insertColumns, final String generateKeyColumnName) {
        Collection<ExpressionSegment> result = new LinkedList<>();
        Collection<String> columnNames = insertColumns.getRegularColumnNames();
        for (Collection<ExpressionSegment> each : insertStatement.getAllValueExpressions()) {
            Optional<ExpressionSegment> generateKeyExpression = findGenerateKeyExpressionSegment(generateKeyColumnName, columnNames.iterator(), each);
            if (generateKeyExpression.isPresent()) {
                result.add(generateKeyExpression.get());
            }
        }
        return result;
    }
    
    private static Optional<ExpressionSegment> findGenerateKeyExpressionSegment(final String generateKeyColumnName, 
                                                                                final Iterator<String> columnNames, final Collection<ExpressionSegment> expressionSegments) {
        for (ExpressionSegment each : expressionSegments) {
            if (generateKeyColumnName.equalsIgnoreCase(columnNames.next())) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private static GeneratedKey createGeneratedKey(final ShardingRule shardingRule, final InsertStatement insertStatement, final String generateKeyColumnName) {
        GeneratedKey result = new GeneratedKey(generateKeyColumnName, true);
        for (int i = 0; i < insertStatement.getValueListCount(); i++) {
            result.getGeneratedValues().add(shardingRule.generateKey(insertStatement.getTable().getTableName()));
        }
        return result;
    }
}
