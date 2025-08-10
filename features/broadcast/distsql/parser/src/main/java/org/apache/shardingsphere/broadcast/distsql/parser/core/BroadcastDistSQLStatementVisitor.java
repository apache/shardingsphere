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

package org.apache.shardingsphere.broadcast.distsql.parser.core;

import org.apache.shardingsphere.broadcast.distsql.statement.CreateBroadcastTableRuleStatement;
import org.apache.shardingsphere.broadcast.distsql.statement.DropBroadcastTableRuleStatement;
import org.apache.shardingsphere.broadcast.distsql.statement.ShowBroadcastTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.autogen.BroadcastDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.BroadcastDistSQLStatementParser.CountBroadcastRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.BroadcastDistSQLStatementParser.CreateBroadcastTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.BroadcastDistSQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.BroadcastDistSQLStatementParser.DropBroadcastTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.BroadcastDistSQLStatementParser.ShowBroadcastTableRulesContext;
import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.CountRuleStatement;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.util.IdentifierValueUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Broadcast DistSQL statement visitor.
 */
public final class BroadcastDistSQLStatementVisitor extends BroadcastDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitCreateBroadcastTableRule(final CreateBroadcastTableRuleContext ctx) {
        return new CreateBroadcastTableRuleStatement(null != ctx.ifNotExists(), ctx.tableName().stream().map(IdentifierValueUtils::getValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropBroadcastTableRule(final DropBroadcastTableRuleContext ctx) {
        Collection<String> tableNames = ctx.tableName().stream().map(IdentifierValueUtils::getValue).collect(Collectors.toList());
        return new DropBroadcastTableRuleStatement(null != ctx.ifExists(), tableNames);
    }
    
    @Override
    public ASTNode visitShowBroadcastTableRules(final ShowBroadcastTableRulesContext ctx) {
        return new ShowBroadcastTableRulesStatement(null == ctx.databaseName() ? null : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName())));
    }
    
    @Override
    public ASTNode visitCountBroadcastRule(final CountBroadcastRuleContext ctx) {
        return new CountRuleStatement(null == ctx.databaseName() ? null : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName())), "BROADCAST");
    }
    
    @Override
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
}
