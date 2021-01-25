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

package org.apache.shardingsphere.infra.binder.statement.ddl;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.type.IndexAvailable;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.AlterIndexStatementHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Alter index statement context.
 */
@Getter
public final class AlterIndexStatementContext extends CommonSQLStatementContext<AlterIndexStatement> implements TableAvailable, IndexAvailable {
    
    private final TablesContext tablesContext;
    
    public AlterIndexStatementContext(final AlterIndexStatement sqlStatement) {
        super(sqlStatement);
        SimpleTableSegment simpleTableSegment = AlterIndexStatementHandler.getSimpleTableSegment(sqlStatement).orElse(null);
        tablesContext = new TablesContext(simpleTableSegment);
    }
    
    @Override
    public Collection<SimpleTableSegment> getAllTables() {
        Optional<SimpleTableSegment> simpleTableSegment = AlterIndexStatementHandler.getSimpleTableSegment(getSqlStatement());
        return simpleTableSegment.isPresent() ? Collections.singletonList(simpleTableSegment.get()) : Collections.emptyList();
    }
    
    @Override
    public Collection<IndexSegment> getIndexes() {
        return getSqlStatement().getIndex().isPresent() ? Collections.singletonList(getSqlStatement().getIndex().get()) : Collections.emptyList();
    }
}
