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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.exception.core.exception.syntax.column.ColumnNotFoundException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parameter marker extractor for MySQL COM_STMT_PREPARE.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLComStmtPrepareParameterMarkerExtractor {
    
    /**
     * TODO Support more statements and syntax.
     * Find corresponding columns of parameter markers.
     *
     * @param sqlStatement SQL statement
     * @param schema schema
     * @return corresponding columns of parameter markers
     */
    public static List<ShardingSphereColumn> findColumnsOfParameterMarkers(final SQLStatement sqlStatement, final ShardingSphereSchema schema) {
        return sqlStatement instanceof InsertStatement && ((InsertStatement) sqlStatement).getTable().isPresent()
                ? findColumnsOfParameterMarkersForInsert((InsertStatement) sqlStatement, schema)
                : Collections.emptyList();
    }
    
    private static List<ShardingSphereColumn> findColumnsOfParameterMarkersForInsert(final InsertStatement insertStatement, final ShardingSphereSchema schema) {
        ShardingSphereTable table = schema.getTable(insertStatement.getTable().map(optional -> optional.getTableName().getIdentifier().getValue()).orElse(""));
        List<String> columnNamesOfInsert = getColumnNamesOfInsertStatement(insertStatement, table);
        List<ShardingSphereColumn> result = getParameterMarkerColumns(insertStatement, table, columnNamesOfInsert);
        insertStatement.getOnDuplicateKeyColumns().ifPresent(optional -> result.addAll(getOnDuplicateKeyParameterMarkerColumns(optional.getColumns(), table)));
        return result;
    }
    
    private static List<String> getColumnNamesOfInsertStatement(final InsertStatement insertStatement, final ShardingSphereTable table) {
        return insertStatement.getColumns().isEmpty()
                ? table.getColumnNames().stream().map(ShardingSphereIdentifier::getValue).collect(Collectors.toList())
                : insertStatement.getColumns().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList());
    }
    
    private static List<ShardingSphereColumn> getParameterMarkerColumns(final InsertStatement insertStatement, final ShardingSphereTable table, final List<String> columnNamesOfInsert) {
        List<ShardingSphereColumn> result = new ArrayList<>(insertStatement.getParameterMarkers().size());
        for (InsertValuesSegment each : insertStatement.getValues()) {
            result.addAll(getParameterMarkerColumns(table, columnNamesOfInsert, each));
        }
        return result;
    }
    
    private static List<ShardingSphereColumn> getParameterMarkerColumns(final ShardingSphereTable table, final List<String> columnNamesOfInsert, final InsertValuesSegment segment) {
        List<ShardingSphereColumn> result = new LinkedList<>();
        int index = 0;
        for (ExpressionSegment each : segment.getValues()) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                String columnName = columnNamesOfInsert.get(index);
                ShardingSpherePreconditions.checkState(table.containsColumn(columnName), () -> new ColumnNotFoundException(table.getName(), columnName));
                result.add(table.getColumn(columnName));
            }
            index++;
        }
        return result;
    }
    
    private static List<ShardingSphereColumn> getOnDuplicateKeyParameterMarkerColumns(final Collection<ColumnAssignmentSegment> onDuplicateKeyColumns, final ShardingSphereTable table) {
        List<ShardingSphereColumn> result = new LinkedList<>();
        for (ColumnAssignmentSegment each : onDuplicateKeyColumns) {
            if (each.getValue() instanceof ParameterMarkerExpressionSegment) {
                String columnName = each.getColumns().iterator().next().getIdentifier().getValue();
                ShardingSpherePreconditions.checkState(table.containsColumn(columnName), () -> new ColumnNotFoundException(table.getName(), columnName));
                result.add(table.getColumn(columnName));
            }
        }
        return result;
    }
}
