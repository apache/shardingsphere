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

package org.apache.shardingsphere.infra.binder.engine.statement.ddl;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CommentStatement;

import java.util.Optional;

/**
 * Comment statement binder.
 */
public final class CommentStatementBinder implements SQLStatementBinder<CommentStatement> {
    
    @Override
    public CommentStatement bind(final CommentStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        Optional<SimpleTableSegment> boundTable = Optional.ofNullable(sqlStatement.getTable())
                .map(each -> SimpleTableSegmentBinder.bind(each, binderContext, tableBinderContexts));
        ColumnSegment boundColumn = sqlStatement.getColumn();
        return copy(sqlStatement, boundTable.orElse(null), boundColumn);
    }
    
    private CommentStatement copy(final CommentStatement sqlStatement, final SimpleTableSegment table, final ColumnSegment column) {
        CommentStatement result = new CommentStatement(sqlStatement.getDatabaseType());
        result.setTable(table);
        result.setColumn(column);
        result.setComment(sqlStatement.getComment());
        result.setIndexType(sqlStatement.getIndexType().orElse(null));
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
