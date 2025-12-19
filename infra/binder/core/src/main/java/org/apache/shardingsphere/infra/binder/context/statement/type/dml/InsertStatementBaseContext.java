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

package org.apache.shardingsphere.infra.binder.context.statement.type.dml;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Insert SQL statement base context.
 */
@Getter
public final class InsertStatementBaseContext implements SQLStatementContext {
    
    private final ShardingSphereMetaData metaData;
    
    private final ShardingSphereSchema schema;
    
    private final InsertStatement sqlStatement;
    
    private final TablesContext tablesContext;
    
    private final String currentDatabaseName;
    
    private final Map<String, Integer> insertColumnNamesAndIndexes;
    
    private final List<List<ExpressionSegment>> valueExpressions;
    
    private final List<String> columnNames;
    
    public InsertStatementBaseContext(final InsertStatement sqlStatement, final ShardingSphereMetaData metaData, final String currentDatabaseName) {
        this.metaData = metaData;
        this.sqlStatement = sqlStatement;
        this.currentDatabaseName = currentDatabaseName;
        valueExpressions = getAllValueExpressions(sqlStatement);
        Collection<TableSegment> tableSegments = getAllSimpleTableSegments();
        tablesContext = new TablesContext(tableSegments, Collections.emptyMap());
        List<String> insertColumnNames = getInsertColumnNames();
        schema = getSchema(metaData, currentDatabaseName);
        columnNames = containsInsertColumns()
                ? insertColumnNames
                : sqlStatement.getTable().map(optional -> schema.getVisibleColumnNames(optional.getTableName().getIdentifier().getValue())).orElseGet(Collections::emptyList);
        insertColumnNamesAndIndexes = createInsertColumnNamesAndIndexes(insertColumnNames);
    }
    
    private Map<String, Integer> createInsertColumnNamesAndIndexes(final List<String> insertColumnNames) {
        if (containsInsertColumns()) {
            Map<String, Integer> result = new CaseInsensitiveMap<>(insertColumnNames.size(), 1F);
            int index = 0;
            for (String each : insertColumnNames) {
                result.put(each, index++);
            }
            return result;
        }
        return Collections.emptyMap();
    }
    
    private ShardingSphereSchema getSchema(final ShardingSphereMetaData metaData, final String currentDatabaseName) {
        String databaseName = tablesContext.getDatabaseName().orElse(currentDatabaseName);
        ShardingSpherePreconditions.checkNotNull(databaseName, NoDatabaseSelectedException::new);
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        ShardingSpherePreconditions.checkNotNull(database, () -> new UnknownDatabaseException(databaseName));
        String defaultSchema = new DatabaseTypeRegistry(sqlStatement.getDatabaseType()).getDefaultSchemaName(databaseName);
        return tablesContext.getSchemaName().map(database::getSchema).orElseGet(() -> database.getSchema(defaultSchema));
    }
    
    private Collection<TableSegment> getAllSimpleTableSegments() {
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromInsert(sqlStatement);
        return new LinkedList<>(tableExtractor.getRewriteTables());
    }
    
    /**
     * Get column names for descending order.
     *
     * @return column names for descending order
     */
    public Iterator<String> getDescendingColumnNames() {
        return new LinkedList<>(columnNames).descendingIterator();
    }
    
    /**
     * Judge whether contains insert columns.
     *
     * @return contains insert columns or not
     */
    public boolean containsInsertColumns() {
        InsertStatement insertStatement = sqlStatement;
        return !insertStatement.getColumns().isEmpty() || insertStatement.getSetAssignment().isPresent();
    }
    
    /**
     * Get value list count.
     *
     * @return value list count
     */
    public int getValueListCount() {
        InsertStatement insertStatement = sqlStatement;
        return insertStatement.getSetAssignment().isPresent() ? 1 : insertStatement.getValues().size();
    }
    
    /**
     * Get insert column names.
     *
     * @return column names collection
     */
    public List<String> getInsertColumnNames() {
        return sqlStatement.getSetAssignment().map(this::getColumnNamesForSetAssignment).orElseGet(() -> getColumnNamesForInsertColumns(sqlStatement.getColumns()));
    }
    
    private List<String> getColumnNamesForSetAssignment(final SetAssignmentSegment setAssignment) {
        List<String> result = new LinkedList<>();
        for (ColumnAssignmentSegment each : setAssignment.getAssignments()) {
            result.add(each.getColumns().get(0).getIdentifier().getValue().toLowerCase());
        }
        return result;
    }
    
    private List<String> getColumnNamesForInsertColumns(final Collection<ColumnSegment> columns) {
        List<String> result = new LinkedList<>();
        for (ColumnSegment each : columns) {
            result.add(each.getIdentifier().getValue().toLowerCase());
        }
        return result;
    }
    
    private List<List<ExpressionSegment>> getAllValueExpressions(final InsertStatement insertStatement) {
        Optional<SetAssignmentSegment> setAssignment = insertStatement.getSetAssignment();
        return setAssignment
                .map(optional -> Collections.singletonList(getAllValueExpressionsFromSetAssignment(optional))).orElseGet(() -> getAllValueExpressionsFromValues(insertStatement.getValues()));
    }
    
    private List<ExpressionSegment> getAllValueExpressionsFromSetAssignment(final SetAssignmentSegment setAssignment) {
        List<ExpressionSegment> result = new ArrayList<>(setAssignment.getAssignments().size());
        for (ColumnAssignmentSegment each : setAssignment.getAssignments()) {
            result.add(each.getValue());
        }
        return result;
    }
    
    private List<List<ExpressionSegment>> getAllValueExpressionsFromValues(final Collection<InsertValuesSegment> values) {
        List<List<ExpressionSegment>> result = new ArrayList<>(values.size());
        for (InsertValuesSegment each : values) {
            result.add(each.getValues());
        }
        return result;
    }
}
