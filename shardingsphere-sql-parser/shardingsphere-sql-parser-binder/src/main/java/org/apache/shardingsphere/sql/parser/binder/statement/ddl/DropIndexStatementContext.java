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

package org.apache.shardingsphere.sql.parser.binder.statement.ddl;

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.IndexAvailable;
import org.apache.shardingsphere.sql.parser.binder.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;

import java.util.Collection;
import java.util.Collections;

/**
 * Drop index statement context.
 */
@Getter
public final class DropIndexStatementContext extends CommonSQLStatementContext<DropIndexStatement> implements TableAvailable, IndexAvailable {
    
    private final TablesContext tablesContext;
    
    public DropIndexStatementContext(final DropIndexStatement sqlStatement) {
        super(sqlStatement);
        tablesContext = new TablesContext(sqlStatement.getTable());
    }
    
    @Override
    public Collection<SimpleTableSegment> getAllTables() {
        return null == getSqlStatement().getTable() ? Collections.emptyList() : Collections.singletonList(getSqlStatement().getTable());
    }
    
    @Override
    public Collection<IndexSegment> getIndexes() {
        return getSqlStatement().getIndexes();
    }
}
