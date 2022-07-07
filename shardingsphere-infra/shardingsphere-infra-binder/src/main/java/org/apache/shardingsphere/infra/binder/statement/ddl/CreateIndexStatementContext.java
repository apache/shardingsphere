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
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.IndexAvailable;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtil;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.CreateIndexStatementHandler;

import java.util.Collection;
import java.util.Collections;

/**
 * Create index statement context.
 */
@Getter
public final class CreateIndexStatementContext extends CommonSQLStatementContext<CreateIndexStatement> implements TableAvailable, IndexAvailable {
    
    private final TablesContext tablesContext;
    
    private final boolean generatedIndex;
    
    public CreateIndexStatementContext(final CreateIndexStatement sqlStatement) {
        super(sqlStatement);
        tablesContext = new TablesContext(sqlStatement.getTable(), getDatabaseType());
        generatedIndex = null == sqlStatement.getIndex();
    }
    
    @Override
    public Collection<SimpleTableSegment> getAllTables() {
        return null == getSqlStatement().getTable() ? Collections.emptyList() : Collections.singletonList(getSqlStatement().getTable());
    }
    
    @Override
    public Collection<IndexSegment> getIndexes() {
        if (null != getSqlStatement().getIndex()) {
            return Collections.singletonList(getSqlStatement().getIndex());
        }
        return CreateIndexStatementHandler.getGeneratedIndexStartIndex(getSqlStatement()).map(each -> Collections.singletonList(new IndexSegment(each, each,
                new IndexNameSegment(each, each, new IdentifierValue(IndexMetaDataUtil.getGeneratedLogicIndexName(getSqlStatement().getColumns())))))).orElseGet(Collections::emptyList);
    }
    
    @Override
    public Collection<ColumnSegment> getIndexColumns() {
        return getSqlStatement().getColumns();
    }
}
