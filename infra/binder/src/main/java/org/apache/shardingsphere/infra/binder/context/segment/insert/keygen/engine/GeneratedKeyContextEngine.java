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

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;

import java.util.List;
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
     * @param params SQL parameters
     * @return generate key context
     */
    public Optional<GeneratedKeyContext> createGenerateKeyContext(final List<String> insertColumnNames, final List<InsertValueContext> insertValueContexts, final List<Object> params) {
        String tableName = Optional.ofNullable(insertStatement.getTable()).map(optional -> optional.getTableName().getIdentifier().getValue()).orElse("");
        return findGenerateKeyColumn(tableName).map(optional -> containsGenerateKey(insertColumnNames, optional)
                ? findGeneratedKey(insertColumnNames, insertValueContexts, params, optional)
                : new GeneratedKeyContext(optional, true));
    }
    
    private Optional<String> findGenerateKeyColumn(final String tableName) {
        if (!schema.containsTable(tableName)) {
            return Optional.empty();
        }
        for (ShardingSphereColumn each : schema.getTable(tableName).getColumnValues()) {
            if (each.isGenerated()) {
                return Optional.of(each.getName());
            }
        }
        return Optional.empty();
    }
    
    private boolean containsGenerateKey(final List<String> insertColumnNames, final String generateKeyColumnName) {
        return insertColumnNames.isEmpty() ? schema.getVisibleColumnNames(insertStatement.getTable().getTableName().getIdentifier().getValue()).size() == getValueCountForPerGroup()
                : new CaseInsensitiveSet<>(insertColumnNames).contains(generateKeyColumnName);
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
                                                 final List<Object> params, final String generateKeyColumnName) {
        GeneratedKeyContext result = new GeneratedKeyContext(generateKeyColumnName, false);
        for (InsertValueContext each : insertValueContexts) {
            ExpressionSegment expression = each.getValueExpressions().get(findGenerateKeyIndex(insertColumnNames, generateKeyColumnName.toLowerCase()));
            if (expression instanceof ParameterMarkerExpressionSegment) {
                if (params.isEmpty()) {
                    continue;
                }
                if (null != params.get(((ParameterMarkerExpressionSegment) expression).getParameterMarkerIndex())) {
                    result.getGeneratedValues().add((Comparable<?>) params.get(((ParameterMarkerExpressionSegment) expression).getParameterMarkerIndex()));
                }
            } else if (expression instanceof LiteralExpressionSegment) {
                result.getGeneratedValues().add((Comparable<?>) ((LiteralExpressionSegment) expression).getLiterals());
            }
        }
        return result;
    }
    
    private int findGenerateKeyIndex(final List<String> insertColumnNames, final String generateKeyColumnName) {
        return insertColumnNames.isEmpty() ? schema.getVisibleColumnNames(insertStatement.getTable().getTableName().getIdentifier().getValue()).indexOf(generateKeyColumnName)
                : insertColumnNames.indexOf(generateKeyColumnName);
    }
}
