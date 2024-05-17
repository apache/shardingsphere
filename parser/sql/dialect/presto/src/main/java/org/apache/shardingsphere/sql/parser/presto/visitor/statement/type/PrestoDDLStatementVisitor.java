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

package org.apache.shardingsphere.sql.parser.presto.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.CharsetNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.CreateViewContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.DropViewContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.FunctionNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.TableElementContext;
import org.apache.shardingsphere.sql.parser.presto.visitor.statement.PrestoStatementVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.CreateDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.charset.CharsetNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.FunctionNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.presto.ddl.PrestoCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.presto.ddl.PrestoCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.presto.ddl.PrestoDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.presto.ddl.PrestoDropViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.presto.dml.PrestoSelectStatement;

/**
 * DDL statement visitor for Presto.
 */
public final class PrestoDDLStatementVisitor extends PrestoStatementVisitor implements DDLStatementVisitor {
    
    @Override
    public ASTNode visitCreateView(final CreateViewContext ctx) {
        PrestoCreateViewStatement result = new PrestoCreateViewStatement();
        result.setView((SimpleTableSegment) visit(ctx.viewName()));
        result.setViewDefinition(getOriginalText(ctx.select()));
        result.setSelect((PrestoSelectStatement) visit(ctx.select()));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropView(final DropViewContext ctx) {
        PrestoDropViewStatement result = new PrestoDropViewStatement();
        result.getViews().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.viewNames())).getValue());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        PrestoCreateTableStatement result = new PrestoCreateTableStatement(null != ctx.ifNotExists());
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        if (null != ctx.createDefinitionClause()) {
            CollectionValue<CreateDefinitionSegment> createDefinitions = (CollectionValue<CreateDefinitionSegment>) visit(ctx.createDefinitionClause());
            for (CreateDefinitionSegment each : createDefinitions.getValue()) {
                if (each instanceof ColumnDefinitionSegment) {
                    result.getColumnDefinitions().add((ColumnDefinitionSegment) each);
                } else if (each instanceof ConstraintDefinitionSegment) {
                    result.getConstraintDefinitions().add((ConstraintDefinitionSegment) each);
                }
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateDefinitionClause(final CreateDefinitionClauseContext ctx) {
        CollectionValue<CreateDefinitionSegment> result = new CollectionValue<>();
        for (TableElementContext each : ctx.tableElementList().tableElement()) {
            if (null != each.columnDefinition()) {
                result.getValue().add((ColumnDefinitionSegment) visit(each.columnDefinition()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitCharsetName(final CharsetNameContext ctx) {
        return new CharsetNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        ColumnSegment column = new ColumnSegment(ctx.column_name.start.getStartIndex(), ctx.column_name.stop.getStopIndex(), (IdentifierValue) visit(ctx.column_name));
        DataTypeSegment dataTypeSegment = (DataTypeSegment) visit(ctx.fieldDefinition().dataType());
        return new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, dataTypeSegment, false, false);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        PrestoDropTableStatement result = new PrestoDropTableStatement(null != ctx.ifExists());
        result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.tableList())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitFunctionName(final FunctionNameContext ctx) {
        FunctionNameSegment result = new FunctionNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
        if (null != ctx.owner()) {
            result.setOwner((OwnerSegment) visit(ctx.owner()));
        }
        return result;
    }
    
}
