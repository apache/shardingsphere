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

package org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
     * @param insertColumnNamesAndIndexes insert column names and indexes
     * @param insertValueContexts insert value contexts
     * @param params SQL parameters
     * @return generate key context
     */
    public Optional<GeneratedKeyContext> createGenerateKeyContext(final Map<String, Integer> insertColumnNamesAndIndexes,
                                                                  final List<InsertValueContext> insertValueContexts, final List<Object> params) {
        String tableName = insertStatement.getTable().map(optional -> optional.getTableName().getIdentifier().getValue()).orElse("");
        return findGenerateKeyColumn(tableName).map(optional -> containsGenerateKey(insertColumnNamesAndIndexes, optional)
                ? findGeneratedKey(insertColumnNamesAndIndexes, insertValueContexts, params, optional)
                : new GeneratedKeyContext(optional, true));
    }
    
    private Optional<String> findGenerateKeyColumn(final String tableName) {
        if (!schema.containsTable(tableName)) {
            return Optional.empty();
        }
        for (ShardingSphereColumn each : schema.getTable(tableName).getAllColumns()) {
            if (each.isGenerated()) {
                return Optional.of(each.getName());
            }
        }
        return Optional.empty();
    }
    
    private boolean containsGenerateKey(final Map<String, Integer> insertColumnNamesAndIndexes, final String generateKeyColumnName) {
        String tableName = insertStatement.getTable().map(optional -> optional.getTableName().getIdentifier().getValue()).orElse("");
        return insertColumnNamesAndIndexes.isEmpty() ? schema.getVisibleColumnNames(tableName).size() == getValueCountForPerGroup() : insertColumnNamesAndIndexes.containsKey(generateKeyColumnName);
    }
    
    private int getValueCountForPerGroup() {
        if (!insertStatement.getValues().isEmpty()) {
            return insertStatement.getValues().iterator().next().getValues().size();
        }
        Optional<SetAssignmentSegment> setAssignment = insertStatement.getSetAssignment();
        if (setAssignment.isPresent()) {
            return setAssignment.get().getAssignments().size();
        }
        if (insertStatement.getInsertSelect().isPresent()) {
            return insertStatement.getInsertSelect().get().getSelect().getProjections().getProjections().size();
        }
        return 0;
    }
    
    private GeneratedKeyContext findGeneratedKey(final Map<String, Integer> insertColumnNamesAndIndexes, final List<InsertValueContext> insertValueContexts,
                                                 final List<Object> params, final String generateKeyColumnName) {
        int generateKeyIndex = findGenerateKeyIndex(insertColumnNamesAndIndexes, generateKeyColumnName);
        boolean generated = false;
        Collection<Comparable<?>> generatedValues = new LinkedList<>();
        for (InsertValueContext each : insertValueContexts) {
            ExpressionSegment expression = each.getValueExpressions().get(generateKeyIndex);
            getGeneratedValue(params, expression).ifPresent(generatedValues::add);
        }
        GeneratedKeyContext result = new GeneratedKeyContext(generateKeyColumnName, generated);
        result.getGeneratedValues().addAll(generatedValues);
        return result;
    }
    
    private int findGenerateKeyIndex(final Map<String, Integer> insertColumnNamesAndIndexes, final String generateKeyColumnName) {
        String tableName = insertStatement.getTable().map(optional -> optional.getTableName().getIdentifier().getValue()).orElse("");
        return insertColumnNamesAndIndexes.isEmpty() ? schema.getVisibleColumnAndIndexMap(tableName).get(generateKeyColumnName) : insertColumnNamesAndIndexes.get(generateKeyColumnName);
    }
    
    private Optional<Comparable<?>> getGeneratedValue(final List<Object> params, final ExpressionSegment expression) {
        if (expression instanceof ParameterMarkerExpressionSegment) {
            if (params.size() > ((ParameterMarkerExpressionSegment) expression).getParameterMarkerIndex()
                    && null != params.get(((ParameterMarkerExpressionSegment) expression).getParameterMarkerIndex())) {
                return Optional.of((Comparable<?>) params.get(((ParameterMarkerExpressionSegment) expression).getParameterMarkerIndex()));
            }
            return Optional.empty();
        }
        if (expression instanceof LiteralExpressionSegment && null != ((LiteralExpressionSegment) expression).getLiterals()) {
            return Optional.of((Comparable<?>) ((LiteralExpressionSegment) expression).getLiterals());
        }
        return Optional.empty();
    }
}
