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

package org.apache.shardingsphere.infra.binder.segment.insert.keygen.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Generated key context.
 */
@RequiredArgsConstructor
public final class GeneratedKeyContextEngine {
    
    private final InsertStatement insertStatement;
    
    private final ShardingSphereSchema schema;
    
    /**
     * Create generate key context.
     *
     * @param insertColumnNames insert column names
     * @param insertValueContexts insert value contexts
     * @param parameters SQL parameters
     * @return generate key context
     */
    public Optional<GeneratedKeyContext> createGenerateKeyContext(final List<String> insertColumnNames, final List<InsertValueContext> insertValueContexts, final List<Object> parameters) {
        String tableName = insertStatement.getTable().getTableName().getIdentifier().getValue();
        return findGenerateKeyColumn(tableName).map(optional -> containsGenerateKey(insertColumnNames, optional)
                ? findGeneratedKey(insertColumnNames, insertValueContexts, parameters, optional) : new GeneratedKeyContext(optional, true));
    }
    
    private Optional<String> findGenerateKeyColumn(final String tableName) {
        if (!schema.containsTable(tableName)) {
            return Optional.empty();
        }
        for (Entry<String, ColumnMetaData> entry : schema.get(tableName).getColumns().entrySet()) {
            if (entry.getValue().isGenerated()) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }
    
    private boolean containsGenerateKey(final List<String> insertColumnNames, final String generateKeyColumnName) {
        return insertColumnNames.isEmpty() ? schema.getAllColumnNames(insertStatement.getTable().getTableName().getIdentifier().getValue()).size() == getValueCountForPerGroup()
                : insertColumnNames.contains(generateKeyColumnName);
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
    
    private GeneratedKeyContext findGeneratedKey(final List<String> insertColumnNames, final List<InsertValueContext> insertValueContexts, 
                                                 final List<Object> parameters, final String generateKeyColumnName) {
        GeneratedKeyContext result = new GeneratedKeyContext(generateKeyColumnName, false);
        for (InsertValueContext each : insertValueContexts) {
            ExpressionSegment expression = each.getValueExpressions().get(findGenerateKeyIndex(insertColumnNames, generateKeyColumnName.toLowerCase()));
            if (expression instanceof ParameterMarkerExpressionSegment) {
                if (parameters.isEmpty()) {
                    continue;
                }
                result.getGeneratedValues().add((Comparable<?>) parameters.get(((ParameterMarkerExpressionSegment) expression).getParameterMarkerIndex() + each.getLastParametersOffset()));
            } else if (expression instanceof LiteralExpressionSegment) {
                result.getGeneratedValues().add((Comparable<?>) ((LiteralExpressionSegment) expression).getLiterals());
            }
        }
        return result;
    }
    
    private int findGenerateKeyIndex(final List<String> insertColumnNames, final String generateKeyColumnName) {
        return insertColumnNames.isEmpty() ? schema.getAllColumnNames(insertStatement.getTable().getTableName().getIdentifier().getValue()).indexOf(generateKeyColumnName) 
                : insertColumnNames.indexOf(generateKeyColumnName);
    }
}
