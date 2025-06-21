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

package org.apache.shardingsphere.infra.binder.context.statement.type.ddl;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.available.IndexContextAvailable;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;

/**
 * Create index statement context.
 */
@Getter
public final class CreateIndexStatementContext implements SQLStatementContext, IndexContextAvailable {
    
    private final DatabaseType databaseType;
    
    private final CreateIndexStatement sqlStatement;
    
    private final TablesContext tablesContext;
    
    private final boolean generatedIndex;
    
    public CreateIndexStatementContext(final DatabaseType databaseType, final CreateIndexStatement sqlStatement) {
        this.databaseType = databaseType;
        this.sqlStatement = sqlStatement;
        tablesContext = new TablesContext(sqlStatement.getTable());
        generatedIndex = null == sqlStatement.getIndex();
    }
    
    @Override
    public Collection<IndexSegment> getIndexes() {
        if (null == getSqlStatement().getIndex()) {
            return getSqlStatement().getGeneratedIndexStartIndex().map(each -> Collections.singletonList(new IndexSegment(each, each,
                    new IndexNameSegment(each, each, new IdentifierValue(IndexMetaDataUtils.getGeneratedLogicIndexName(getSqlStatement().getColumns())))))).orElseGet(Collections::emptyList);
        }
        return Collections.singleton(getSqlStatement().getIndex());
    }
    
    @Override
    public Collection<ColumnSegment> getIndexColumns() {
        return getSqlStatement().getColumns();
    }
}
