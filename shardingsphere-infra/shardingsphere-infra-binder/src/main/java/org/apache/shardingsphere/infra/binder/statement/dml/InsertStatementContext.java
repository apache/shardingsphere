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
import org.apache.shardingsphere.infra.binder.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.segment.insert.keygen.engine.GeneratedKeyContextEngine;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.OnDuplicateUpdateContext;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.exception.DatabaseNotExistedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.InsertMultiTableElementSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleInsertStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Insert SQL statement context.
 */
@Getter
public final class InsertStatementContext extends CommonSQLStatementContext<InsertStatement> implements TableAvailable, ParameterAware {
    
    private final TablesContext tablesContext;
    
    private final Map<Integer, List<String>> columnNames = new LinkedHashMap<>();
    
    private final Map<String, ShardingSphereMetaData> metaDataMap;
    
    private final String defaultDatabaseName;
    
    private Map<Integer, List<String>> insertColumnNames = new LinkedHashMap<>();
    
    private final Map<Integer, List<List<ExpressionSegment>>> valueExpressions = new LinkedHashMap<>();
    
    private Map<Integer, List<InsertValueContext>> insertValueContexts = new LinkedHashMap<>();
    
    private InsertSelectContext insertSelectContext;
    
    private OnDuplicateUpdateContext onDuplicateKeyUpdateValueContext;
    
    private Map<Integer, GeneratedKeyContext> generatedKeyContexts = new LinkedHashMap<>();
    
    public InsertStatementContext(final Map<String, ShardingSphereMetaData> metaDataMap, final List<Object> parameters,
                                  final InsertStatement sqlStatement, final String defaultDatabaseName) {
        super(sqlStatement);
        this.metaDataMap = metaDataMap;
        this.defaultDatabaseName = defaultDatabaseName;
        tablesContext = new TablesContext(getAllSimpleTableSegments(), getDatabaseType());
        ShardingSphereSchema schema = getSchema(metaDataMap, defaultDatabaseName);
        AtomicInteger parametersOffset = new AtomicInteger(0);
        Integer index = 0;
        for (InsertStatement insertStatement : getInsertStatements()) {
            List<List<ExpressionSegment>> valueExpression = getAllValueExpressions(insertStatement);
            valueExpressions.put(index, valueExpression);
            insertValueContexts.put(index, getInsertValueContexts(parameters, parametersOffset, valueExpression));
            List<String> insertColumnNameList = getInsertColumnNames(insertStatement);
            insertColumnNames.put(index, insertColumnNameList);
            columnNames.put(index, useDefaultColumns() ? schema.getAllColumnNames(insertStatement.getTable().getTableName().getIdentifier().getValue()) : insertColumnNameList);
            generatedKeyContexts.put(index, new GeneratedKeyContextEngine(insertStatement, schema)
                    .createGenerateKeyContext(insertColumnNameList, getAllValueExpressions(insertStatement), parameters).orElse(null));
            index++;
        }
        onDuplicateKeyUpdateValueContext = getOnDuplicateKeyUpdateValueContext(parameters, parametersOffset).orElse(null);
        insertSelectContext = getInsertSelectContext(metaDataMap, parameters, parametersOffset, defaultDatabaseName).orElse(null);
    }
    
    private ShardingSphereSchema getSchema(final Map<String, ShardingSphereMetaData> metaDataMap, final String defaultDatabaseName) {
        String databaseName = tablesContext.getDatabaseName().orElse(defaultDatabaseName);
        ShardingSphereMetaData metaData = metaDataMap.get(databaseName);
        if (null == metaData) {
            throw new DatabaseNotExistedException(databaseName);
        }
        String defaultSchema = getDatabaseType().getDefaultSchema(databaseName);
        return tablesContext.getSchemaName().map(metaData::getSchemaByName).orElseGet(() -> metaData.getSchemaByName(defaultSchema));
    }
    
    private Collection<SimpleTableSegment> getAllSimpleTableSegments() {
        TableExtractor tableExtractor = new TableExtractor();
        getInsertStatements().stream().forEach(each -> tableExtractor.extractTablesFromInsert(each));
        return tableExtractor.getRewriteTables();
    }
    
    private List<InsertValueContext> getInsertValueContexts(final List<Object> parameters, final AtomicInteger parametersOffset, final List<List<ExpressionSegment>> valueExpressions) {
        List<InsertValueContext> result = new LinkedList<>();
        for (Collection<ExpressionSegment> each : valueExpressions) {
            InsertValueContext insertValueContext = new InsertValueContext(each, parameters, parametersOffset.get());
            result.add(insertValueContext);
            parametersOffset.addAndGet(insertValueContext.getParameterCount());
        }
        return result;
    }
    
    private Optional<InsertSelectContext> getInsertSelectContext(final Map<String, ShardingSphereMetaData> metaDataMap, final List<Object> parameters,
                                                                 final AtomicInteger parametersOffset, final String defaultDatabaseName) {
        if (!getSqlStatement().getInsertSelect().isPresent()) {
            return Optional.empty();
        }
        SubquerySegment insertSelectSegment = getSqlStatement().getInsertSelect().get();
        SelectStatementContext selectStatementContext = new SelectStatementContext(metaDataMap, parameters, insertSelectSegment.getSelect(), defaultDatabaseName);
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
    public Map<Integer, Iterator<String>> getDescendingColumnNames() {
        Map<Integer, Iterator<String>> result = new HashMap<>(columnNames.size(), 1);
        for (Integer index : columnNames.keySet()) {
            result.put(index, new LinkedList<>(columnNames.get(index)).descendingIterator());
        }
        return result;
    }
    
    /**
     * Get grouped parameters.
     *
     * @return grouped parameters
     */
    public Map<Integer, List<List<Object>>> getGroupedParameters() {
        Map<Integer, List<List<Object>>> result = new HashMap<>(insertValueContexts.size(), 1);
        for (Integer index : insertValueContexts.keySet()) {
            result.put(index, getGroupedParameters(index));
        }
        return result;
    }
    
    private List<List<Object>> getGroupedParameters(final Integer index) {
        List<List<Object>> result = new LinkedList<>();
        for (InsertValueContext each : insertValueContexts.get(index)) {
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
    public Map<Integer, Optional<GeneratedKeyContext>> getGeneratedKeyContext() {
        Map<Integer, Optional<GeneratedKeyContext>> result = new HashMap<>(generatedKeyContexts.size(), 1);
        for (Integer index : generatedKeyContexts.keySet()) {
            result.put(index, Optional.ofNullable(generatedKeyContexts.get(index)));
        }
        return result;
    }
    
    @Override
    public Collection<SimpleTableSegment> getAllTables() {
        return tablesContext.getTables();
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
    
    private List<String> getInsertColumnNames(final InsertStatement insertStatement) {
        Optional<SetAssignmentSegment> setAssignment = InsertStatementHandler.getSetAssignmentSegment(insertStatement);
        return setAssignment.map(this::getColumnNamesForSetAssignment).orElseGet(() -> getColumnNamesForInsertColumns(insertStatement.getColumns()));
    }
    
    /**
     * Get insert column names.
     *
     * @return column names collection
     */
    public Map<Integer, List<String>> getInsertColumnNames() {
        return insertColumnNames;
    }
    
    private List<String> getColumnNamesForSetAssignment(final SetAssignmentSegment setAssignment) {
        List<String> result = new LinkedList<>();
        for (AssignmentSegment each : setAssignment.getAssignments()) {
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
        Optional<SetAssignmentSegment> setAssignment = InsertStatementHandler.getSetAssignmentSegment(insertStatement);
        return setAssignment
                .map(optional -> Collections.singletonList(getAllValueExpressionsFromSetAssignment(optional))).orElseGet(() -> getAllValueExpressionsFromValues(insertStatement.getValues()));
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
    
    /**
     * Get first index on MultiInsertStatement.
     *
     * @return column names collection
     */
    public Map<Integer, SimpleTableSegment> getTables() {
        Collection<InsertStatement> insertStatements = getInsertStatements();
        Map<Integer, SimpleTableSegment> result = new HashMap<>(insertStatements.size(), 1);
        Integer index = 0;
        for (InsertStatement insertStatement : insertStatements) {
            result.put(index, insertStatement.getTable());
            index++;
        }
        return result;
    }
    
    /**
     * Get InsertStatement collection from MultiInsertStatement.
     *
     * @return InsertStatement collection
     */
    public Collection<InsertStatement> getInsertStatements() {
        Optional<InsertMultiTableElementSegment> optional = getInsertMultiTableElementSegment();
        if (optional.isPresent()) {
            return new LinkedList<>(optional.get().getInsertStatements());
        }
        return Collections.singletonList(getSqlStatement());
    }
    
    private Optional<InsertMultiTableElementSegment> getInsertMultiTableElementSegment() {
        if (getSqlStatement() instanceof OracleInsertStatement) {
            return ((OracleInsertStatement) getSqlStatement()).getInsertMultiTableElementSegment();
        }
        return Optional.empty();
    }
    
    @Override
    public void setUpParameters(final List<Object> parameters) {
        AtomicInteger parametersOffset = new AtomicInteger(0);
        ShardingSphereSchema schema = getSchema(metaDataMap, defaultDatabaseName);
        Collection<InsertStatement> insertStatements = getInsertStatements();
        Integer index = 0;
        for (InsertStatement insertStatement : insertStatements) {
            List<InsertValueContext> insertValueContext = getInsertValueContexts(parameters, parametersOffset, valueExpressions.get(index));
            insertValueContexts.put(index, insertValueContext);
            generatedKeyContexts.put(index, new GeneratedKeyContextEngine(insertStatement, schema).createGenerateKeyContext(getInsertColumnNames(insertStatement),
                    valueExpressions.get(index), parameters).orElse(null));
            index++;
        }
        onDuplicateKeyUpdateValueContext = getOnDuplicateKeyUpdateValueContext(parameters, parametersOffset).orElse(null);
        insertSelectContext = getInsertSelectContext(metaDataMap, parameters, parametersOffset, defaultDatabaseName).orElse(null);
    }
}
