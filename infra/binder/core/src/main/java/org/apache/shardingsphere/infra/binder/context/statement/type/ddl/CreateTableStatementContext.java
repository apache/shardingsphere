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
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateTableStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Create table statement context.
 */
@Getter
public final class CreateTableStatementContext implements SQLStatementContext, IndexContextAvailable {
    
    private final DatabaseType databaseType;
    
    private final CreateTableStatement sqlStatement;
    
    private final TablesContext tablesContext;
    
    public CreateTableStatementContext(final DatabaseType databaseType, final CreateTableStatement sqlStatement) {
        this.databaseType = databaseType;
        this.sqlStatement = sqlStatement;
        tablesContext = new TablesContext(getTables(sqlStatement));
    }
    
    private Collection<SimpleTableSegment> getTables(final CreateTableStatement sqlStatement) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        result.add(sqlStatement.getTable());
        for (ColumnDefinitionSegment each : sqlStatement.getColumnDefinitions()) {
            result.addAll(each.getReferencedTables());
        }
        for (ConstraintDefinitionSegment each : sqlStatement.getConstraintDefinitions()) {
            if (each.getReferencedTable().isPresent()) {
                result.add(each.getReferencedTable().get());
            }
        }
        return result;
    }
    
    @Override
    public Collection<IndexSegment> getIndexes() {
        Collection<IndexSegment> result = new LinkedList<>();
        for (ConstraintDefinitionSegment each : getSqlStatement().getConstraintDefinitions()) {
            each.getIndexName().ifPresent(result::add);
        }
        return result;
    }
    
    @Override
    public Collection<ColumnSegment> getIndexColumns() {
        return getSqlStatement().getConstraintDefinitions().stream().flatMap(each -> each.getIndexColumns().stream()).collect(Collectors.toList());
    }
}
