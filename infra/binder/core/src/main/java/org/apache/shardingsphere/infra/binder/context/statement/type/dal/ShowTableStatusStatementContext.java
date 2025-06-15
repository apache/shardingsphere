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

package org.apache.shardingsphere.infra.binder.context.statement.type.dal;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.RemoveAvailable;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowTableStatusStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Show table status statement context.
 */
@Getter
public final class ShowTableStatusStatementContext implements SQLStatementContext, RemoveAvailable {
    
    private final DatabaseType databaseType;
    
    private final ShowTableStatusStatement sqlStatement;
    
    private final TablesContext tablesContext;
    
    public ShowTableStatusStatementContext(final DatabaseType databaseType, final ShowTableStatusStatement sqlStatement) {
        this.databaseType = databaseType;
        this.sqlStatement = sqlStatement;
        tablesContext = new TablesContext(Collections.emptyList());
    }
    
    @Override
    public Collection<SQLSegment> getRemoveSegments() {
        Collection<SQLSegment> result = new LinkedList<>();
        getSqlStatement().getFromDatabase().ifPresent(result::add);
        return result;
    }
}
