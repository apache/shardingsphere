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
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.AddConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.DropIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.RenameIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.AlterTableStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Alter table statement context.
 */
@Getter
public final class AlterTableStatementContext implements SQLStatementContext, IndexContextAvailable {
    
    private final DatabaseType databaseType;
    
    private final AlterTableStatement sqlStatement;
    
    private final TablesContext tablesContext;
    
    public AlterTableStatementContext(final DatabaseType databaseType, final AlterTableStatement sqlStatement) {
        this.databaseType = databaseType;
        this.sqlStatement = sqlStatement;
        tablesContext = new TablesContext(getTables(sqlStatement));
    }
    
    private Collection<SimpleTableSegment> getTables(final AlterTableStatement sqlStatement) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        result.add(sqlStatement.getTable());
        if (sqlStatement.getRenameTable().isPresent()) {
            result.add(sqlStatement.getRenameTable().get());
        }
        for (AddColumnDefinitionSegment each : sqlStatement.getAddColumnDefinitions()) {
            for (ColumnDefinitionSegment columnDefinition : each.getColumnDefinitions()) {
                result.addAll(columnDefinition.getReferencedTables());
            }
        }
        for (ModifyColumnDefinitionSegment each : sqlStatement.getModifyColumnDefinitions()) {
            result.addAll(each.getColumnDefinition().getReferencedTables());
        }
        for (AddConstraintDefinitionSegment each : sqlStatement.getAddConstraintDefinitions()) {
            each.getConstraintDefinition().getReferencedTable().ifPresent(result::add);
        }
        return result;
    }
    
    @Override
    public Collection<IndexSegment> getIndexes() {
        Collection<IndexSegment> result = new LinkedList<>();
        for (AddConstraintDefinitionSegment each : getSqlStatement().getAddConstraintDefinitions()) {
            each.getConstraintDefinition().getIndexName().ifPresent(result::add);
        }
        getSqlStatement().getDropIndexDefinitions().stream().map(DropIndexDefinitionSegment::getIndexSegment).forEach(result::add);
        for (RenameIndexDefinitionSegment each : getSqlStatement().getRenameIndexDefinitions()) {
            result.add(each.getIndexSegment());
            result.add(each.getRenameIndexSegment());
        }
        return result;
    }
    
    @Override
    public Collection<ColumnSegment> getIndexColumns() {
        Collection<ColumnSegment> result = new LinkedList<>();
        for (AddConstraintDefinitionSegment each : getSqlStatement().getAddConstraintDefinitions()) {
            result.addAll(each.getConstraintDefinition().getIndexColumns());
        }
        return result;
    }
}
