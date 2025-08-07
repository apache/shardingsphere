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

package org.apache.shardingsphere.infra.binder.engine.statement.dcl;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.GrantStatement;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Grant statement binder.
 */
public final class GrantStatementBinder implements SQLStatementBinder<GrantStatement> {
    
    @Override
    public GrantStatement bind(final GrantStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        Collection<SimpleTableSegment> tables = sqlStatement.getTables();
        if (tables.isEmpty()) {
            return sqlStatement;
        }
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        Collection<SimpleTableSegment> boundTables = new ArrayList<>();
        for (SimpleTableSegment each : tables) {
            boundTables.add(SimpleTableSegmentBinder.bind(each, binderContext, tableBinderContexts));
        }
        return copyGrantStatement(sqlStatement, boundTables);
    }
    
    private GrantStatement copyGrantStatement(final GrantStatement sqlStatement, final Collection<SimpleTableSegment> tables) {
        if (tables.equals(sqlStatement.getTables())) {
            return sqlStatement;
        }
        GrantStatement result = new GrantStatement(sqlStatement.getDatabaseType());
        result.getTables().addAll(tables);
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
