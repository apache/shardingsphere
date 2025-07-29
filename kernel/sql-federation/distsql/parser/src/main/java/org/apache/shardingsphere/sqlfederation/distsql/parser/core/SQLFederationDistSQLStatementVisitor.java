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

package org.apache.shardingsphere.sqlfederation.distsql.parser.core;

import org.apache.shardingsphere.distsql.parser.autogen.SQLFederationDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.SQLFederationDistSQLStatementParser.AlterSQLFederationRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.SQLFederationDistSQLStatementParser.CacheOptionContext;
import org.apache.shardingsphere.distsql.parser.autogen.SQLFederationDistSQLStatementParser.ShowSQLFederationRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.SQLFederationDistSQLStatementParser.SqlFederationRuleDefinitionContext;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.util.IdentifierValueUtils;
import org.apache.shardingsphere.sqlfederation.distsql.segment.CacheOptionSegment;
import org.apache.shardingsphere.sqlfederation.distsql.statement.queryable.ShowSQLFederationRuleStatement;
import org.apache.shardingsphere.sqlfederation.distsql.statement.updatable.AlterSQLFederationRuleStatement;

/**
 * SQL statement visitor for SQL federation DistSQL.
 */
public final class SQLFederationDistSQLStatementVisitor extends SQLFederationDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitShowSQLFederationRule(final ShowSQLFederationRuleContext ctx) {
        return new ShowSQLFederationRuleStatement();
    }
    
    @Override
    public ASTNode visitAlterSQLFederationRule(final AlterSQLFederationRuleContext ctx) {
        return visit(ctx.sqlFederationRuleDefinition());
    }
    
    @Override
    public ASTNode visitSqlFederationRuleDefinition(final SqlFederationRuleDefinitionContext ctx) {
        Boolean sqlFederationEnabled = null == ctx.sqlFederationEnabled() ? null : Boolean.parseBoolean(IdentifierValueUtils.getValue(ctx.sqlFederationEnabled().boolean_()));
        Boolean allQueryUseSQLFederation = null == ctx.allQueryUseSQLFederation() ? null : Boolean.parseBoolean(IdentifierValueUtils.getValue(ctx.allQueryUseSQLFederation().boolean_()));
        CacheOptionSegment executionPlanCache = null == ctx.executionPlanCache() ? null : visitCacheOption(ctx.executionPlanCache().cacheOption());
        return new AlterSQLFederationRuleStatement(sqlFederationEnabled, allQueryUseSQLFederation, executionPlanCache);
    }
    
    @Override
    public CacheOptionSegment visitCacheOption(final CacheOptionContext ctx) {
        return new CacheOptionSegment(
                null == ctx.initialCapacity() ? null : Integer.parseInt(IdentifierValueUtils.getValue(ctx.initialCapacity())),
                null == ctx.maximumSize() ? null : Long.parseLong(IdentifierValueUtils.getValue(ctx.maximumSize())));
    }
}
