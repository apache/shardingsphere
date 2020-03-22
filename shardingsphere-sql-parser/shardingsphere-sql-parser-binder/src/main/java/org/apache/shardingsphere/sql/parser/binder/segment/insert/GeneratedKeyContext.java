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

package org.apache.shardingsphere.sql.parser.binder.segment.insert;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Generated key context.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class GeneratedKeyContext {
    
    private final String columnName;
    
    private final boolean generated;
    
    private final LinkedList<Comparable<?>> generatedValues = new LinkedList<>();
    
    /**
     * Get generate key.
     *
     * @param schemaMetaData schema meta data
     * @param parameters SQL parameters
     * @param insertStatement insert statement
     * @return generate key
     */
    public static Optional<GeneratedKeyContext> getGenerateKey(final SchemaMetaData schemaMetaData, final List<Object> parameters, final InsertStatement insertStatement) {
        String tableName = insertStatement.getTable().getTableName().getIdentifier().getValue();
        return findGenerateKeyColumn(schemaMetaData, tableName).map(generateKeyColumnName -> containsGenerateKey(schemaMetaData, insertStatement, generateKeyColumnName)
                ? findGeneratedKey(schemaMetaData, parameters, insertStatement, generateKeyColumnName) : new GeneratedKeyContext(generateKeyColumnName, true));
    }
    
    private static Optional<String> findGenerateKeyColumn(final SchemaMetaData schemaMetaData, final String tableName) {
        if (!schemaMetaData.containsTable(tableName)) {
            return Optional.empty();
        }
        for (Entry<String, ColumnMetaData> entry : schemaMetaData.get(tableName).getColumns().entrySet()) {
            if (entry.getValue().isGenerated()) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }
    
    private static boolean containsGenerateKey(final SchemaMetaData schemaMetaData, final InsertStatement insertStatement, final String generateKeyColumnName) {
        return insertStatement.getColumnNames().isEmpty()
                ? schemaMetaData.getAllColumnNames(insertStatement.getTable().getTableName().getIdentifier().getValue()).size() == insertStatement.getValueCountForPerGroup()
                : insertStatement.getColumnNames().contains(generateKeyColumnName);
    }
    
    private static GeneratedKeyContext findGeneratedKey(final SchemaMetaData schemaMetaData, final List<Object> parameters, final InsertStatement insertStatement, final String generateKeyColumnName) {
        GeneratedKeyContext result = new GeneratedKeyContext(generateKeyColumnName, false);
        for (ExpressionSegment each : findGenerateKeyExpressions(schemaMetaData, insertStatement, generateKeyColumnName)) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                result.getGeneratedValues().add((Comparable<?>) parameters.get(((ParameterMarkerExpressionSegment) each).getParameterMarkerIndex()));
            } else if (each instanceof LiteralExpressionSegment) {
                result.getGeneratedValues().add((Comparable<?>) ((LiteralExpressionSegment) each).getLiterals());
            }
        }
        return result;
    }
    
    private static Collection<ExpressionSegment> findGenerateKeyExpressions(final SchemaMetaData schemaMetaData, final InsertStatement insertStatement, final String generateKeyColumnName) {
        Collection<ExpressionSegment> result = new LinkedList<>();
        for (List<ExpressionSegment> each : insertStatement.getAllValueExpressions()) {
            result.add(each.get(findGenerateKeyIndex(schemaMetaData, insertStatement, generateKeyColumnName.toLowerCase())));
        }
        return result;
    }
    
    private static int findGenerateKeyIndex(final SchemaMetaData schemaMetaData, final InsertStatement insertStatement, final String generateKeyColumnName) {
        return insertStatement.getColumnNames().isEmpty() ? schemaMetaData.getAllColumnNames(insertStatement.getTable().getTableName().getIdentifier().getValue()).indexOf(generateKeyColumnName) 
                : insertStatement.getColumnNames().indexOf(generateKeyColumnName);
    }
}
