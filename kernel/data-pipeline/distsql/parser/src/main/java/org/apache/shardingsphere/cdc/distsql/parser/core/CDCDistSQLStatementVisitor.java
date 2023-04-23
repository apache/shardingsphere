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

package org.apache.shardingsphere.cdc.distsql.parser.core;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.cdc.distsql.statement.DropStreamingStatement;
import org.apache.shardingsphere.cdc.distsql.statement.ShowStreamingListStatement;
import org.apache.shardingsphere.cdc.distsql.statement.ShowStreamingStatusStatement;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.DropStreamingContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.ShowStreamingListContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.ShowStreamingStatusContext;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

/**
 * SQL statement visitor for CDC DistSQL.
 */
public final class CDCDistSQLStatementVisitor extends CDCDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitShowStreamingList(final ShowStreamingListContext ctx) {
        return new ShowStreamingListStatement();
    }
    
    @Override
    public ASTNode visitShowStreamingStatus(final ShowStreamingStatusContext ctx) {
        return new ShowStreamingStatusStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitDropStreaming(final DropStreamingContext ctx) {
        return new DropStreamingStatement(getIdentifierValue(ctx.jobId()));
    }
    
    private String getIdentifierValue(final ParseTree ctx) {
        return null == ctx ? null : new IdentifierValue(ctx.getText()).getValue();
    }
}
