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

package org.apache.shardingsphere.infra.binder.sqlserver.bind.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.user.SQLServerDenyUserStatement;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Deny user statement binder for SQLServer.
 */
public final class SQLServerDenyUserStatementBinder implements SQLStatementBinder<SQLServerDenyUserStatement> {
    
    @Override
    public SQLServerDenyUserStatement bind(final SQLServerDenyUserStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SimpleTableSegment boundTable = SimpleTableSegmentBinder.bind(sqlStatement.getTable(), binderContext, tableBinderContexts);
        Collection<ColumnSegment> boundColumns = sqlStatement.getColumns().stream()
                .map(each -> ColumnSegmentBinder.bind(each, SegmentType.DEFINITION_COLUMNS, binderContext, tableBinderContexts, LinkedHashMultimap.create())).collect(Collectors.toList());
        return copy(sqlStatement, boundTable, boundColumns);
    }
    
    private SQLServerDenyUserStatement copy(final SQLServerDenyUserStatement sqlStatement, final SimpleTableSegment boundTable, final Collection<ColumnSegment> boundColumns) {
        SQLServerDenyUserStatement result = new SQLServerDenyUserStatement(sqlStatement.getDatabaseType());
        result.setTable(boundTable);
        sqlStatement.getColumns().addAll(boundColumns);
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
