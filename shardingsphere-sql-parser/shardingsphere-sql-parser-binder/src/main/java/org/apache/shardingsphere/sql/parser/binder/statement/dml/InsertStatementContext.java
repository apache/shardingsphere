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

package org.apache.shardingsphere.sql.parser.binder.statement.dml;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.sql.parser.binder.segment.insert.keygen.engine.GeneratedKeyContextEngine;
import org.apache.shardingsphere.sql.parser.binder.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.sql.parser.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.sql.parser.binder.segment.insert.values.OnDuplicateUpdateContext;
import org.apache.shardingsphere.sql.parser.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.TableExtractUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Insert SQL statement context.
 */
@Getter
@ToString(callSuper = true)
public final class InsertStatementContext extends CommonSQLStatementContext<InsertStatement> implements TableAvailable {
    
    private final TablesContext tablesContext;
    
    private final List<String> columnNames;
    
    private final List<InsertValueContext> insertValueContexts;
    
    private final InsertSelectContext insertSelectContext;
    
    private final OnDuplicateUpdateContext onDuplicateKeyUpdateValueContext;
    
    private final GeneratedKeyContext generatedKeyContext;
    
    public InsertStatementContext(final SchemaMetaData schemaMetaData, final List<Object> parameters, final InsertStatement sqlStatement) {
        super(sqlStatement);
        columnNames = sqlStatement.useDefaultColumns() ? schemaMetaData.getAllColumnNames(sqlStatement.getTable().getTableName().getIdentifier().getValue()) : sqlStatement.getColumnNames();
        AtomicInteger parametersOffset = new AtomicInteger(0);
        insertValueContexts = getInsertValueContexts(parameters, parametersOffset);
        insertSelectContext = getInsertSelectContext(schemaMetaData, parameters, parametersOffset).orElse(null);
        tablesContext = getTablesContext(sqlStatement);
        onDuplicateKeyUpdateValueContext = getOnDuplicateKeyUpdateValueContext(parameters, parametersOffset).orElse(null);
        generatedKeyContext = new GeneratedKeyContextEngine(schemaMetaData).createGenerateKeyContext(parameters, sqlStatement).orElse(null);
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
        for (Collection<ExpressionSegment> each : getSqlStatement().getAllValueExpressions()) {
            InsertValueContext insertValueContext = new InsertValueContext(each, parameters, parametersOffset.get());
            result.add(insertValueContext);
            parametersOffset.addAndGet(insertValueContext.getParameterCount());
        }
        return result;
    }
    
    private Optional<InsertSelectContext> getInsertSelectContext(final SchemaMetaData schemaMetaData, final List<Object> parameters, final AtomicInteger parametersOffset) {
        if (!getSqlStatement().getInsertSelect().isPresent()) {
            return Optional.empty();
        }
        SubquerySegment insertSelectSegment = getSqlStatement().getInsertSelect().get();
        SelectStatementContext selectStatementContext = new SelectStatementContext(schemaMetaData, parameters, insertSelectSegment.getSelect());
        InsertSelectContext insertSelectContext = new InsertSelectContext(selectStatementContext, parameters, parametersOffset.get());
        parametersOffset.addAndGet(insertSelectContext.getParameterCount());
        return Optional.of(insertSelectContext);
    }
    
    private Optional<OnDuplicateUpdateContext> getOnDuplicateKeyUpdateValueContext(final List<Object> parameters, final AtomicInteger parametersOffset) {
        if (!getSqlStatement().getOnDuplicateKeyColumns().isPresent()) {
            return Optional.empty();
        }
        Collection<AssignmentSegment> onDuplicateKeyColumns = getSqlStatement().getOnDuplicateKeyColumns().get().getColumns();
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
        TableExtractUtils utils = new TableExtractUtils();
        utils.extractTablesFromInsert(getSqlStatement());
        return utils.getRewriteTables();
    }
}
