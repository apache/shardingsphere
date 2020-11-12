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

package org.apache.shardingsphere.infra.binder.statement.dml;

import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.segment.insert.keygen.engine.GeneratedKeyContextEngine;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.OnDuplicateUpdateContext;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Insert SQL statement context.
 */
@Getter
public final class InsertStatementContext extends CommonSQLStatementContext<InsertStatement> implements TableAvailable {
    
    private final TablesContext tablesContext;
    
    private final List<String> columnNames;
    
    private final List<InsertValueContext> insertValueContexts;
    
    private final InsertSelectContext insertSelectContext;
    
    private final OnDuplicateUpdateContext onDuplicateKeyUpdateValueContext;
    
    private final GeneratedKeyContext generatedKeyContext;
    
    public InsertStatementContext(final ShardingSphereSchema schema, final List<Object> parameters, final InsertStatement sqlStatement) {
        super(sqlStatement);
        List<String> insertColumnNames = getInsertColumnNames();
        columnNames = useDefaultColumns() ? schema.getAllColumnNames(sqlStatement.getTable().getTableName().getIdentifier().getValue()) : insertColumnNames;
        AtomicInteger parametersOffset = new AtomicInteger(0);
        insertValueContexts = getInsertValueContexts(parameters, parametersOffset);
        insertSelectContext = getInsertSelectContext(schema, parameters, parametersOffset).orElse(null);
        tablesContext = getTablesContext(sqlStatement);
        onDuplicateKeyUpdateValueContext = getOnDuplicateKeyUpdateValueContext(parameters, parametersOffset).orElse(null);
        generatedKeyContext = new GeneratedKeyContextEngine(sqlStatement, schema).createGenerateKeyContext(insertColumnNames, getAllValueExpressions(sqlStatement), parameters).orElse(null);
    }
    
    private TablesContext getTablesContext(final InsertStatement sqlStatement) {
        List<SimpleTableSegment> result = new LinkedList<>();
        result.add(sqlStatement.getTable());
        if (sqlStatement.getInsertSelect().isPresent()) {
            result.addAll(insertSelectContext.getSelectStatementContext().getSimpleTableSegments());
        }
        return new TablesContext(result);
    }
    
    private List<InsertValueContext> getInsertValueContexts(final List<Object> parameters, final AtomicInteger parametersOffset) {
        List<InsertValueContext> result = new LinkedList<>();
        List<List<ExpressionSegment>> allValueExpressions = getAllValueExpressions(getSqlStatement());
        for (Collection<ExpressionSegment> each : allValueExpressions) {
            InsertValueContext insertValueContext = new InsertValueContext(each, parameters, parametersOffset.get());
            result.add(insertValueContext);
            parametersOffset.addAndGet(insertValueContext.getParameterCount());
        }
        return result;
    }
    
    private Optional<InsertSelectContext> getInsertSelectContext(final ShardingSphereSchema schema, final List<Object> parameters, final AtomicInteger parametersOffset) {
        if (!getSqlStatement().getInsertSelect().isPresent()) {
            return Optional.empty();
        }
        SubquerySegment insertSelectSegment = getSqlStatement().getInsertSelect().get();
        SelectStatementContext selectStatementContext = new SelectStatementContext(schema, parameters, insertSelectSegment.getSelect());
        InsertSelectContext insertSelectContext = new InsertSelectContext(selectStatementContext, parameters, parametersOffset.get());
        parametersOffset.addAndGet(insertSelectContext.getParameterCount());
        return Optional.of(insertSelectContext);
    }
    
    private Optional<OnDuplicateUpdateContext> getOnDuplicateKeyUpdateValueContext(final List<Object> parameters, final AtomicInteger parametersOffset) {
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = InsertStatementHandler.getOnDuplicateKeyColumnsSegment(getSqlStatement());
        if (!onDuplicateKeyColumnsSegment.isPresent()) {
            return Optional.empty();
        }
        Collection<AssignmentSegment> onDuplicateKeyColumns = onDuplicateKeyColumnsSegment.get().getColumns();
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(onDuplicateKeyColumns, parameters, parametersOffset.get());
        parametersOffset.addAndGet(onDuplicateUpdateContext.getParameterCount());
        return Optional.of(onDuplicateUpdateContext);
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
     * Get grouped parameters.
     *
     * @return grouped parameters
     */
    public List<List<Object>> getGroupedParameters() {
        List<List<Object>> result = new LinkedList<>();
        for (InsertValueContext each : insertValueContexts) {
            result.add(each.getParameters());
        }
        if (null != insertSelectContext) {
            result.add(insertSelectContext.getParameters());
        }
        return result;
    }
    
    /**
     * Get on duplicate key update parameters.
     *
     * @return on duplicate key update parameters
     */
    public List<Object> getOnDuplicateKeyUpdateParameters() {
        if (null == onDuplicateKeyUpdateValueContext) {
            return new ArrayList<>(0);
        }
        return onDuplicateKeyUpdateValueContext.getParameters();
    }
    
    /**
     * Get generated key context.
     *
     * @return generated key context
     */
    public Optional<GeneratedKeyContext> getGeneratedKeyContext() {
        return Optional.ofNullable(generatedKeyContext);
    }
    
    @Override
    public Collection<SimpleTableSegment> getAllTables() {
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromInsert(getSqlStatement());
        return tableExtractor.getRewriteTables();
    }
    
    /**
     * Judge whether use default columns or not.
     *
     * @return whether use default columns or not
     */
    public boolean useDefaultColumns() {
        InsertStatement insertStatement = getSqlStatement();
        Optional<SetAssignmentSegment> setAssignment = InsertStatementHandler.getSetAssignmentSegment(insertStatement);
        return insertStatement.getColumns().isEmpty() && !setAssignment.isPresent();
    }
    
    /**
     * Get value list count.
     *
     * @return value list count
     */
    public int getValueListCount() {
        InsertStatement insertStatement = getSqlStatement();
        Optional<SetAssignmentSegment> setAssignment = InsertStatementHandler.getSetAssignmentSegment(insertStatement);
        return setAssignment.isPresent() ? 1 : insertStatement.getValues().size();
    }
        
    /**
     * Get insert column names.
     *
     * @return column names collection
     */
    public List<String> getInsertColumnNames() {
        InsertStatement insertStatement = getSqlStatement();
        Optional<SetAssignmentSegment> setAssignment = InsertStatementHandler.getSetAssignmentSegment(insertStatement);
        return setAssignment.map(this::getColumnNamesForSetAssignment).orElseGet(() -> getColumnNamesForInsertColumns(insertStatement.getColumns()));
    }
    
    private List<String> getColumnNamesForSetAssignment(final SetAssignmentSegment setAssignment) {
        List<String> result = new LinkedList<>();
        for (AssignmentSegment each : setAssignment.getAssignments()) {
            result.add(each.getColumn().getIdentifier().getValue().toLowerCase());
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
        Optional<SetAssignmentSegment> setAssignment = InsertStatementHandler.getSetAssignmentSegment(insertStatement);
        return setAssignment.isPresent() ? Collections.singletonList(getAllValueExpressionsFromSetAssignment(setAssignment.get())) : getAllValueExpressionsFromValues(insertStatement.getValues());
    }
    
    private List<ExpressionSegment> getAllValueExpressionsFromSetAssignment(final SetAssignmentSegment setAssignment) {
        List<ExpressionSegment> result = new ArrayList<>(setAssignment.getAssignments().size());
        for (AssignmentSegment each : setAssignment.getAssignments()) {
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
