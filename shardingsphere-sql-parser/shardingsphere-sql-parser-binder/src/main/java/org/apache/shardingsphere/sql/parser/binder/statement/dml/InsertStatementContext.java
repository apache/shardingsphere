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
import org.apache.shardingsphere.sql.parser.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.sql.parser.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Insert SQL statement context.
 */
@Getter
@ToString(callSuper = true)
public final class InsertStatementContext extends CommonSQLStatementContext<InsertStatement> implements TableAvailable {
    
    private final TablesContext tablesContext;
    
    private final List<String> columnNames;
    
    private final List<InsertValueContext> insertValueContexts;
    
    private final GeneratedKeyContext generatedKeyContext;
    
    public InsertStatementContext(final SchemaMetaData schemaMetaData, final List<Object> parameters, final InsertStatement sqlStatement) {
        super(sqlStatement);
        tablesContext = new TablesContext(sqlStatement.getTable());
        columnNames = sqlStatement.useDefaultColumns() ? schemaMetaData.getAllColumnNames(sqlStatement.getTable().getTableName().getIdentifier().getValue()) : sqlStatement.getColumnNames();
        insertValueContexts = getInsertValueContexts(parameters);
        generatedKeyContext = new GeneratedKeyContextEngine(schemaMetaData).createGenerateKeyContext(parameters, sqlStatement).orElse(null);
    }
    
    private List<InsertValueContext> getInsertValueContexts(final List<Object> parameters) {
        List<InsertValueContext> result = new LinkedList<>();
        int parametersOffset = 0;
        for (Collection<ExpressionSegment> each : getSqlStatement().getAllValueExpressions()) {
            InsertValueContext insertValueContext = new InsertValueContext(each, parameters, parametersOffset);
            result.add(insertValueContext);
            parametersOffset += insertValueContext.getParametersCount();
        }
        return result;
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
        return result;
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
        return Collections.singletonList(getSqlStatement().getTable());
    }
}
