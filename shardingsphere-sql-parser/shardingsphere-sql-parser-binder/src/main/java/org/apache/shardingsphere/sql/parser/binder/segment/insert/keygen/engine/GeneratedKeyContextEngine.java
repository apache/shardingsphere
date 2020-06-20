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

package org.apache.shardingsphere.sql.parser.binder.segment.insert.keygen.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.ReplaceStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Generated key context.
 */
@RequiredArgsConstructor
public final class GeneratedKeyContextEngine {
    
    private final SchemaMetaData schemaMetaData;
    
    /**
     * Create insert generate key context.
     *
     * @param parameters SQL parameters
     * @param insertStatement insert statement
     * @return generate key context
     */
    public Optional<GeneratedKeyContext> createInsertGenerateKeyContext(final List<Object> parameters, final InsertStatement insertStatement) {
        SimpleTableSegment table = insertStatement.getTable();
        String tableName = table.getTableName().getIdentifier().getValue();
        List<String> columnNames = insertStatement.getColumnNames();
        int valueCountForPerGroup = insertStatement.getValueCountForPerGroup();
        return findGenerateKeyColumn(tableName).map(generateKeyColumnName -> containsGenerateKey(columnNames, table, valueCountForPerGroup, generateKeyColumnName)
                ? findInsertGeneratedKey(parameters, insertStatement, generateKeyColumnName) : new GeneratedKeyContext(generateKeyColumnName, true));
    }
    
    /**
     * Create replace generate key context.
     *
     * @param parameters SQL parameters
     * @param replaceStatement replace statement
     * @return generate key context
     */
    public Optional<GeneratedKeyContext> createReplaceGenerateKeyContext(final List<Object> parameters, final ReplaceStatement replaceStatement) {
        SimpleTableSegment table = replaceStatement.getTable();
        String tableName = replaceStatement.getTable().getTableName().getIdentifier().getValue();
        List<String> columnNames = replaceStatement.getColumnNames();
        int valueCountForPerGroup = replaceStatement.getValueCountForPerGroup();
        return findGenerateKeyColumn(tableName).map(generateKeyColumnName -> containsGenerateKey(columnNames, table, valueCountForPerGroup, generateKeyColumnName)
                ? findReplaceGeneratedKey(parameters, replaceStatement, generateKeyColumnName) : new GeneratedKeyContext(generateKeyColumnName, true));
    }
    
    private Optional<String> findGenerateKeyColumn(final String tableName) {
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
    
    private boolean containsGenerateKey(final List<String> columnNames, final SimpleTableSegment table, final int valueCountForPerGroup, final String generateKeyColumnName) {
        return columnNames.isEmpty()
                ? schemaMetaData.getAllColumnNames(table.getTableName().getIdentifier().getValue()).size() == valueCountForPerGroup
                : columnNames.contains(generateKeyColumnName);
    }
    
    private GeneratedKeyContext findInsertGeneratedKey(final List<Object> parameters, final InsertStatement insertStatement, final String generateKeyColumnName) {
        GeneratedKeyContext result = new GeneratedKeyContext(generateKeyColumnName, false);
        SimpleTableSegment table = insertStatement.getTable();
        List<String> columnNames = insertStatement.getColumnNames();
        List<List<ExpressionSegment>> allValueExpressions = insertStatement.getAllValueExpressions();
        for (ExpressionSegment each : findGenerateKeyExpressions(allValueExpressions, columnNames, table, generateKeyColumnName)) {
            assembleGeneratedValues(parameters, result, each);
        }
        return result;
    }
    
    private GeneratedKeyContext findReplaceGeneratedKey(final List<Object> parameters, final ReplaceStatement replaceStatement, final String generateKeyColumnName) {
        GeneratedKeyContext result = new GeneratedKeyContext(generateKeyColumnName, false);
        List<List<ExpressionSegment>> allValueExpressions = replaceStatement.getAllValueExpressions();
        List<String> columnNames = replaceStatement.getColumnNames();
        SimpleTableSegment table = replaceStatement.getTable();
        for (ExpressionSegment each : findGenerateKeyExpressions(allValueExpressions, columnNames, table, generateKeyColumnName)) {
            assembleGeneratedValues(parameters, result, each);
        }
        return result;
    }
    
    private void assembleGeneratedValues(final List<Object> parameters, final GeneratedKeyContext result, final ExpressionSegment each) {
        if (each instanceof ParameterMarkerExpressionSegment) {
            result.getGeneratedValues().add((Comparable<?>) parameters.get(((ParameterMarkerExpressionSegment) each).getParameterMarkerIndex()));
        } else if (each instanceof LiteralExpressionSegment) {
            result.getGeneratedValues().add((Comparable<?>) ((LiteralExpressionSegment) each).getLiterals());
        }
    }
    
    private Collection<ExpressionSegment> findGenerateKeyExpressions(final List<List<ExpressionSegment>> allValueExpressions, final List<String> columnNames,
                                                                     final SimpleTableSegment table, final String generateKeyColumnName) {
        Collection<ExpressionSegment> result = new LinkedList<>();
        for (List<ExpressionSegment> each : allValueExpressions) {
            result.add(each.get(findGenerateKeyIndex(columnNames, table, generateKeyColumnName.toLowerCase())));
        }
        return result;
    }
    
    private int findGenerateKeyIndex(final List<String> columnNames, final SimpleTableSegment table, final String generateKeyColumnName) {
        return columnNames.isEmpty() ? schemaMetaData.getAllColumnNames(table.getTableName().getIdentifier().getValue()).indexOf(generateKeyColumnName)
                : columnNames.indexOf(generateKeyColumnName);
    }
}
