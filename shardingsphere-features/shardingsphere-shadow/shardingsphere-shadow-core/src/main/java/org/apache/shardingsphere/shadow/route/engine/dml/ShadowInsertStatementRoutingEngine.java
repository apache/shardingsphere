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

package org.apache.shardingsphere.shadow.route.engine.dml;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.route.exception.UnsupportedShadowInsertValueException;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Shadow insert statement routing engine.
 */
@RequiredArgsConstructor
public final class ShadowInsertStatementRoutingEngine extends AbstractShadowDMLStatementRouteEngine {
    
    private final InsertStatementContext insertStatementContext;
    
    @Override
    protected Collection<SimpleTableSegment> getAllTables() {
        return insertStatementContext.getAllTables();
    }
    
    @Override
    protected ShadowOperationType getShadowOperationType() {
        return ShadowOperationType.INSERT;
    }
    
    @Override
    protected Optional<Collection<String>> parseSQLComments() {
        Collection<String> result = new LinkedList<>();
        insertStatementContext.getSqlStatement().getCommentSegments().forEach(each -> result.add(each.getText()));
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    @Override
    protected Iterator<Optional<ShadowColumnCondition>> getShadowColumnConditionIterator(final String shadowColumn) {
        return new ShadowColumnConditionIterator(shadowColumn, parseColumnNames().iterator(), insertStatementContext.getInsertValueContexts());
    }
    
    private Collection<String> parseColumnNames() {
        return insertStatementContext.getInsertColumnNames();
    }
    
    private class ShadowColumnConditionIterator implements Iterator<Optional<ShadowColumnCondition>> {
        
        private int index;
        
        private final String shadowColumn;
        
        private final Iterator<String> iterator;
        
        private final List<InsertValueContext> insertValueContexts;
        
        ShadowColumnConditionIterator(final String shadowColumn, final Iterator<String> iterator, final List<InsertValueContext> insertValueContexts) {
            index = 0;
            this.shadowColumn = shadowColumn;
            this.iterator = iterator;
            this.insertValueContexts = insertValueContexts;
        }
        
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }
        
        @Override
        public Optional<ShadowColumnCondition> next() {
            String columnName = iterator.next();
            if (!shadowColumn.equals(columnName)) {
                return Optional.empty();
            }
            Optional<Collection<Comparable<?>>> columnValues = getColumnValues(insertValueContexts, index);
            index++;
            return columnValues.map(each -> new ShadowColumnCondition(getSingleTableName(), columnName, each));
        }
        
        private Optional<Collection<Comparable<?>>> getColumnValues(final List<InsertValueContext> insertValueContexts, final int columnIndex) {
            Collection<Comparable<?>> result = new LinkedList<>();
            for (InsertValueContext each : insertValueContexts) {
                Object valueObject = each.getLiteralValue(columnIndex).orElseThrow(() -> new UnsupportedShadowInsertValueException(columnIndex));
                if (valueObject instanceof Comparable<?>) {
                    result.add((Comparable<?>) valueObject);
                } else {
                    return Optional.empty();
                }
            }
            return result.isEmpty() ? Optional.empty() : Optional.of(result);
        }
    }
}
