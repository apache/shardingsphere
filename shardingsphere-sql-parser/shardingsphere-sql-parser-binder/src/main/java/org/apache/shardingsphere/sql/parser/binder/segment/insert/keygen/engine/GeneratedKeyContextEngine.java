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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;

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
    
    private final InsertStatement insertStatement;
    
    private final SchemaMetaData schemaMetaData;
    
    /**
     * Create generate key context.
     *
     * @param columnNames column names
     * @param valueExpressions value expressions
     * @param parameters SQL parameters
     * @return generate key context
     */
    public Optional<GeneratedKeyContext> createGenerateKeyContext(final List<String> columnNames, final List<List<ExpressionSegment>> valueExpressions, final List<Object> parameters) {
        String tableName = insertStatement.getTable().getTableName().getIdentifier().getValue();
        return findGenerateKeyColumn(tableName).map(generateKeyColumnName -> containsGenerateKey(columnNames, generateKeyColumnName)
                ? findGeneratedKey(columnNames, valueExpressions, parameters, generateKeyColumnName) : new GeneratedKeyContext(generateKeyColumnName, true));
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
    
    private boolean containsGenerateKey(final List<String> columnNames, final String generateKeyColumnName) {
        return columnNames.isEmpty() ? schemaMetaData.getAllColumnNames(insertStatement.getTable().getTableName().getIdentifier().getValue()).size() == getValueCountForPerGroup()
                : columnNames.contains(generateKeyColumnName);
    }
    
    private int getValueCountForPerGroup() {
        if (!insertStatement.getValues().isEmpty()) {
            return insertStatement.getValues().iterator().next().getValues().size();
        }
        Optional<SetAssignmentSegment> setAssignment = InsertStatementHandler.getSetAssignmentSegment(insertStatement);
        if (setAssignment.isPresent()) {
            return setAssignment.get().getAssignments().size();
        }
        if (insertStatement.getInsertSelect().isPresent()) {
            return insertStatement.getInsertSelect().get().getSelect().getProjections().getProjections().size();
        }
        return 0;
    }
    
    private GeneratedKeyContext findGeneratedKey(final List<String> columnNames, final List<List<ExpressionSegment>> valueExpressions, 
                                                 final List<Object> parameters, final String generateKeyColumnName) {
        GeneratedKeyContext result = new GeneratedKeyContext(generateKeyColumnName, false);
        for (ExpressionSegment each : findGenerateKeyExpressions(columnNames, valueExpressions, generateKeyColumnName)) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                result.getGeneratedValues().add((Comparable<?>) parameters.get(((ParameterMarkerExpressionSegment) each).getParameterMarkerIndex()));
            } else if (each instanceof LiteralExpressionSegment) {
                result.getGeneratedValues().add((Comparable<?>) ((LiteralExpressionSegment) each).getLiterals());
            }
        }
        return result;
    }
    
    private Collection<ExpressionSegment> findGenerateKeyExpressions(final List<String> columnNames, final List<List<ExpressionSegment>> valueExpressions, final String generateKeyColumnName) {
        Collection<ExpressionSegment> result = new LinkedList<>();
        for (List<ExpressionSegment> each : valueExpressions) {
            result.add(each.get(findGenerateKeyIndex(columnNames, generateKeyColumnName.toLowerCase())));
        }
        return result;
    }
    
    private int findGenerateKeyIndex(final List<String> columnNames, final String generateKeyColumnName) {
        return columnNames.isEmpty() ? schemaMetaData.getAllColumnNames(insertStatement.getTable().getTableName().getIdentifier().getValue()).indexOf(generateKeyColumnName) 
                : columnNames.indexOf(generateKeyColumnName);
    }
}
