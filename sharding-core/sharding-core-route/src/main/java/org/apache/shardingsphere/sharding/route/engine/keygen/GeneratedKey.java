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

package org.apache.shardingsphere.sharding.route.engine.keygen;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
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
    
    private final LinkedList<Comparable<?>> generatedValues = new LinkedList<>();
    
    /**
     * Get generate key.
     *
     * @param shardingRule sharding rule
     * @param tableMetas table metas
     * @param parameters SQL parameters
     * @param insertStatement insert statement
     * @return generate key
     */
    public static Optional<GeneratedKey> getGenerateKey(final ShardingRule shardingRule, final TableMetas tableMetas, final List<Object> parameters, final InsertStatement insertStatement) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTable().getTableName());
        if (!generateKeyColumnName.isPresent()) {
            return Optional.absent();
        }
        return Optional.of(containsGenerateKey(tableMetas, insertStatement, generateKeyColumnName.get())
                ? findGeneratedKey(tableMetas, parameters, insertStatement, generateKeyColumnName.get()) : createGeneratedKey(shardingRule, insertStatement, generateKeyColumnName.get()));
    }
    
    private static boolean containsGenerateKey(final TableMetas tableMetas, final InsertStatement insertStatement, final String generateKeyColumnName) {
        return insertStatement.getColumnNames().isEmpty()
                ? tableMetas.getAllColumnNames(insertStatement.getTable().getTableName()).size() == insertStatement.getValueCountForPerGroup()
                : insertStatement.getColumnNames().contains(generateKeyColumnName);
    }
    
    private static GeneratedKey findGeneratedKey(final TableMetas tableMetas, final List<Object> parameters, final InsertStatement insertStatement, final String generateKeyColumnName) {
        GeneratedKey result = new GeneratedKey(generateKeyColumnName, false);
        for (ExpressionSegment each : findGenerateKeyExpressions(tableMetas, insertStatement, generateKeyColumnName)) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                result.getGeneratedValues().add((Comparable<?>) parameters.get(((ParameterMarkerExpressionSegment) each).getParameterMarkerIndex()));
            } else if (each instanceof LiteralExpressionSegment) {
                result.getGeneratedValues().add((Comparable<?>) ((LiteralExpressionSegment) each).getLiterals());
            }
        }
        return result;
    }
    
    private static Collection<ExpressionSegment> findGenerateKeyExpressions(final TableMetas tableMetas, final InsertStatement insertStatement, final String generateKeyColumnName) {
        Collection<ExpressionSegment> result = new LinkedList<>();
        for (List<ExpressionSegment> each : insertStatement.getAllValueExpressions()) {
            result.add(each.get(findGenerateKeyIndex(tableMetas, insertStatement, generateKeyColumnName.toLowerCase())));
        }
        return result;
    }
    
    private static int findGenerateKeyIndex(final TableMetas tableMetas, final InsertStatement insertStatement, final String generateKeyColumnName) {
        return insertStatement.getColumnNames().isEmpty()
                ? tableMetas.getAllColumnNames(insertStatement.getTable().getTableName()).indexOf(generateKeyColumnName) : insertStatement.getColumnNames().indexOf(generateKeyColumnName);
    }
    
    private static GeneratedKey createGeneratedKey(final ShardingRule shardingRule, final InsertStatement insertStatement, final String generateKeyColumnName) {
        GeneratedKey result = new GeneratedKey(generateKeyColumnName, true);
        for (int i = 0; i < insertStatement.getValueListCount(); i++) {
            result.getGeneratedValues().add(shardingRule.generateKey(insertStatement.getTable().getTableName()));
        }
        return result;
    }
}
