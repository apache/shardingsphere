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

package org.apache.shardingsphere.shadow.route.future.engine.dml;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.shadow.api.shadow.column.ShadowOperationType;
import org.apache.shardingsphere.shadow.route.future.engine.AbstractShadowRouteEngine;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowDetermineCondition;
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
public final class ShadowInsertStatementRoutingEngine extends AbstractShadowRouteEngine {
    
    private final InsertStatementContext insertStatementContext;
    
    @Override
    protected Optional<Collection<ShadowColumnCondition>> parseShadowColumnConditions() {
        Collection<ShadowColumnCondition> result = new LinkedList<>();
        Collection<String> columnNames = parseColumnNames();
        Iterator<String> columnNamesIt = columnNames.iterator();
        List<InsertValueContext> insertValueContexts = insertStatementContext.getInsertValueContexts();
        int index = 0;
        while (columnNamesIt.hasNext()) {
            String columnName = columnNamesIt.next();
            Optional<Collection<Comparable<?>>> columnValues = getColumnValues(insertValueContexts, index);
            columnValues.ifPresent(values -> result.add(new ShadowColumnCondition(columnName, values)));
            index++;
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private Optional<Collection<Comparable<?>>> getColumnValues(final List<InsertValueContext> insertValueContexts, final int index) {
        Collection<Comparable<?>> result = new LinkedList<>();
        for (InsertValueContext each : insertValueContexts) {
            Object valueObject = each.getValue(index);
            if (valueObject instanceof Comparable<?>) {
                result.add((Comparable<?>) valueObject);
            } else {
                return Optional.empty();
            }
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private Collection<String> parseColumnNames() {
        return insertStatementContext.getInsertColumnNames();
    }
    
    @Override
    protected ShadowDetermineCondition createShadowDetermineCondition() {
        return new ShadowDetermineCondition(ShadowOperationType.INSERT);
    }
    
    @Override
    protected Collection<SimpleTableSegment> getAllTables() {
        return insertStatementContext.getAllTables();
    }
    
    // FIXME refactor the method when sql parses the note and puts it in the statement context
    @Override
    protected Optional<Collection<String>> parseSqlNotes() {
        Collection<String> result = new LinkedList<>();
        result.add("/*foo=bar,shadow=true*/");
        result.add("/*aaa=bbb*/");
        return Optional.of(result);
    }
}
