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

package org.apache.shardingsphere.single.distsql.parser.core;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.AllSchamesAndTablesFromStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.AllTablesContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.AllTablesFromSchemaContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.AllTablesFromStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.CountSingleTableContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.FromClauseContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.LoadSingleTableContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.SetDefaultSingleTableStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.ShowDefaultSingleTableStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.ShowSingleTableContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.ShowUnloadedSingleTablesContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.TableFromSchemaContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.TableFromStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.TableIdentifierContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.UnloadSingleTableContext;
import org.apache.shardingsphere.distsql.statement.rql.rule.database.CountRuleStatement;
import org.apache.shardingsphere.single.distsql.segment.SingleTableSegment;
import org.apache.shardingsphere.single.distsql.statement.rdl.LoadSingleTableStatement;
import org.apache.shardingsphere.single.distsql.statement.rdl.SetDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.single.distsql.statement.rdl.UnloadSingleTableStatement;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowSingleTableStatement;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowUnloadedSingleTableStatement;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for single DistSQL.
 */
public final class SingleDistSQLStatementVisitor extends SingleDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitCountSingleTable(final CountSingleTableContext ctx) {
        return new CountRuleStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()), "SINGLE");
    }
    
    @Override
    public ASTNode visitSetDefaultSingleTableStorageUnit(final SetDefaultSingleTableStorageUnitContext ctx) {
        return new SetDefaultSingleTableStorageUnitStatement(null == ctx.storageUnitName() ? null : getIdentifierValue(ctx.storageUnitName()));
    }
    
    @Override
    public ASTNode visitShowDefaultSingleTableStorageUnit(final ShowDefaultSingleTableStorageUnitContext ctx) {
        return new ShowDefaultSingleTableStorageUnitStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShowSingleTable(final ShowSingleTableContext ctx) {
        return new ShowSingleTableStatement(null == ctx.TABLE() ? null : getIdentifierValue(ctx.tableName()), null == ctx.showLike() ? null : getIdentifierValue(ctx.showLike().likePattern()),
                null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitLoadSingleTable(final LoadSingleTableContext ctx) {
        Collection<SingleTableSegment> tables = ctx.tableDefinition().tableIdentifier().stream().map(this::getSingleTableSegment).collect(Collectors.toSet());
        return new LoadSingleTableStatement(tables);
    }
    
    @Override
    public ASTNode visitUnloadSingleTable(final UnloadSingleTableContext ctx) {
        if (null != ctx.ALL() || null != ctx.ASTERISK_()) {
            return new UnloadSingleTableStatement(true, Collections.emptyList());
        }
        return new UnloadSingleTableStatement(false, ctx.tableNames().tableName().stream().map(this::getIdentifierValue).collect(Collectors.toSet()));
    }
    
    private SingleTableSegment getSingleTableSegment(final TableIdentifierContext ctx) {
        if (ctx instanceof AllTablesFromStorageUnitContext) {
            return new SingleTableSegment(getIdentifierValue(((AllTablesFromStorageUnitContext) ctx).storageUnitName()), null, "*");
        }
        if (ctx instanceof AllTablesFromSchemaContext) {
            AllTablesFromSchemaContext tableContext = (AllTablesFromSchemaContext) ctx;
            return new SingleTableSegment(getIdentifierValue(tableContext.storageUnitName()), getIdentifierValue(tableContext.schemaName()), "*");
        }
        if (ctx instanceof TableFromStorageUnitContext) {
            TableFromStorageUnitContext tableContext = (TableFromStorageUnitContext) ctx;
            return new SingleTableSegment(getIdentifierValue(tableContext.storageUnitName()), null, getIdentifierValue(tableContext.tableName()));
        }
        if (ctx instanceof TableFromSchemaContext) {
            TableFromSchemaContext tableContext = (TableFromSchemaContext) ctx;
            return new SingleTableSegment(getIdentifierValue(tableContext.storageUnitName()), getIdentifierValue(tableContext.schemaName()), getIdentifierValue(tableContext.tableName()));
        }
        if (ctx instanceof AllTablesContext) {
            return new SingleTableSegment("*", null, "*");
        }
        if (ctx instanceof AllSchamesAndTablesFromStorageUnitContext) {
            return new SingleTableSegment(getIdentifierValue(((AllSchamesAndTablesFromStorageUnitContext) ctx).storageUnitName()), "*", "*");
        }
        return new SingleTableSegment("*", "*", "*");
    }
    
    @Override
    public ASTNode visitShowUnloadedSingleTables(final ShowUnloadedSingleTablesContext ctx) {
        return null == ctx.fromClause() ? new ShowUnloadedSingleTableStatement(null, null, null) : visitShowUnloadedSingleTablesWithFromClause(ctx.fromClause());
    }
    
    private ASTNode visitShowUnloadedSingleTablesWithFromClause(final FromClauseContext ctx) {
        return new ShowUnloadedSingleTableStatement(null == ctx.storageUnitName() ? null : getIdentifierValue(ctx.storageUnitName()),
                null == ctx.schemaName() ? null : getIdentifierValue(ctx.schemaName()), null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    private String getIdentifierValue(final ParseTree context) {
        return null == context ? null : new IdentifierValue(context.getText()).getValue();
    }
}
