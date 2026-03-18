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

package org.apache.shardingsphere.infra.binder.postgresql.bind.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.prepare.PrepareStatementQuerySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.prepare.PrepareStatementQuerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dml.PostgreSQLCopyStatement;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Copy statement binder for PostgreSQL.
 */
public final class PostgreSQLCopyStatementBinder implements SQLStatementBinder<PostgreSQLCopyStatement> {
    
    @Override
    public PostgreSQLCopyStatement bind(final PostgreSQLCopyStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        Optional<SimpleTableSegment> boundTable = sqlStatement.getTable().map(optional -> SimpleTableSegmentBinder.bind(optional, binderContext, tableBinderContexts));
        Collection<ColumnSegment> boundColumns = sqlStatement.getColumns().stream()
                .map(each -> ColumnSegmentBinder.bind(each, SegmentType.COPY, binderContext, tableBinderContexts, LinkedHashMultimap.create())).collect(Collectors.toList());
        Optional<PrepareStatementQuerySegment> boundPrepareStatementQuery = sqlStatement.getPrepareStatementQuery().map(optional -> PrepareStatementQuerySegmentBinder.bind(optional, binderContext));
        return copy(sqlStatement, boundTable.orElse(null), boundColumns, boundPrepareStatementQuery.orElse(null));
    }
    
    private PostgreSQLCopyStatement copy(final PostgreSQLCopyStatement sqlStatement,
                                         final SimpleTableSegment boundTable, final Collection<ColumnSegment> boundColumns, final PrepareStatementQuerySegment boundPrepareStatementQuery) {
        PostgreSQLCopyStatement result = new PostgreSQLCopyStatement(sqlStatement.getDatabaseType(), boundTable, boundColumns, boundPrepareStatementQuery);
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
