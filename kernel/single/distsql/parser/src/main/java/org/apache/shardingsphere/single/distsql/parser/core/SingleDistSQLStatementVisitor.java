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
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.CountSingleTableContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.SetDefaultSingleTableStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.ShowDefaultSingleTableStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.SingleDistSQLStatementParser.ShowSingleTableContext;
import org.apache.shardingsphere.single.distsql.statement.rdl.SetDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.single.distsql.statement.rql.CountSingleTableStatement;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowSingleTableStatement;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

/**
 * SQL statement visitor for single DistSQL.
 */
public final class SingleDistSQLStatementVisitor extends SingleDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitCountSingleTable(final CountSingleTableContext ctx) {
        return new CountSingleTableStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
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
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    private String getIdentifierValue(final ParseTree context) {
        return null == context ? null : new IdentifierValue(context.getText()).getValue();
    }
}
