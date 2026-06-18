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

package org.apache.shardingsphere.infra.binder.mysql.bind.type;

import com.google.common.collect.LinkedHashMultimap;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.index.MySQLShowIndexStatement;

/**
 * Show index statement binder for MySQL.
 */
public final class MySQLShowIndexStatementBinder implements SQLStatementBinder<MySQLShowIndexStatement> {
    
    @Override
    public MySQLShowIndexStatement bind(final MySQLShowIndexStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        return copy(sqlStatement, SimpleTableSegmentBinder.bind(sqlStatement.getTable(), binderContext, LinkedHashMultimap.create()));
    }
    
    private MySQLShowIndexStatement copy(final MySQLShowIndexStatement sqlStatement, final SimpleTableSegment boundTable) {
        MySQLShowIndexStatement result = new MySQLShowIndexStatement(sqlStatement.getDatabaseType(), boundTable, sqlStatement.getFromDatabase().orElse(null));
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
