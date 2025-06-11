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

package org.apache.shardingsphere.infra.binder.engine.statement.dal;

import com.google.common.collect.LinkedHashMultimap;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateTableStatement;

/**
 * Show create table statement binder.
 */
public final class ShowCreateTableStatementBinder implements SQLStatementBinder<ShowCreateTableStatement> {
    
    @Override
    public ShowCreateTableStatement bind(final ShowCreateTableStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        return copy(sqlStatement, SimpleTableSegmentBinder.bind(sqlStatement.getTable(), binderContext, LinkedHashMultimap.create()));
    }
    
    private ShowCreateTableStatement copy(final ShowCreateTableStatement sqlStatement, final SimpleTableSegment boundTable) {
        ShowCreateTableStatement result = new ShowCreateTableStatement(boundTable);
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
