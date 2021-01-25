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

package org.apache.shardingsphere.proxy.backend.exception.fixture;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;

@RequiredArgsConstructor
public final class TestSQLStatementContextInstanceOfTableAvailable implements SQLStatementContext<SQLStatement>, TableAvailable {

    private final SQLStatement sqlStatement;

    private final Collection<SimpleTableSegment> allTables;

    private final TablesContext tablesContext;

    @Override
    public SQLStatement getSqlStatement() {
        return sqlStatement;
    }

    @Override
    public TablesContext getTablesContext() {
        return tablesContext;
    }

    @Override
    public Collection<SimpleTableSegment> getAllTables() {
        return allTables;
    }
}
