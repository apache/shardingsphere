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

package org.apache.shardingsphere.infra.binder.context.statement.ddl;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.ConstraintAvailable;
import org.apache.shardingsphere.infra.binder.context.type.IndexAvailable;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.AddConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.DropConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.alter.ValidateConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.DropIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.RenameIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Alter table statement context.
 */
@Getter
public final class AlterTableStatementContext extends CommonSQLStatementContext implements TableAvailable, IndexAvailable, ConstraintAvailable {
    
    private final TablesContext tablesContext;
    
    public AlterTableStatementContext(final AlterTableStatement sqlStatement) {
        super(sqlStatement);
        tablesContext = new TablesContext(sqlStatement.getTable(), getDatabaseType());
    }
    
    @Override
    public AlterTableStatement getSqlStatement() {
        return (AlterTableStatement) super.getSqlStatement();
    }
    
    @Override
    public Collection<SimpleTableSegment> getAllTables() {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        result.add(getSqlStatement().getTable());
        if (getSqlStatement().getRenameTable().isPresent()) {
            result.add(getSqlStatement().getRenameTable().get());
        }
        for (AddColumnDefinitionSegment each : getSqlStatement().getAddColumnDefinitions()) {
            for (ColumnDefinitionSegment columnDefinition : each.getColumnDefinitions()) {
                result.addAll(columnDefinition.getReferencedTables());
            }
        }
        for (ModifyColumnDefinitionSegment each : getSqlStatement().getModifyColumnDefinitions()) {
            result.addAll(each.getColumnDefinition().getReferencedTables());
        }
        for (AddConstraintDefinitionSegment each : getSqlStatement().getAddConstraintDefinitions()) {
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
    public Collection<ConstraintSegment> getConstraints() {
        Collection<ConstraintSegment> result = new LinkedList<>();
        for (AddConstraintDefinitionSegment each : getSqlStatement().getAddConstraintDefinitions()) {
            each.getConstraintDefinition().getConstraintName().ifPresent(result::add);
        }
        getSqlStatement().getValidateConstraintDefinitions().stream().map(ValidateConstraintDefinitionSegment::getConstraintName).forEach(result::add);
        getSqlStatement().getDropConstraintDefinitions().stream().map(DropConstraintDefinitionSegment::getConstraintName).forEach(result::add);
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
