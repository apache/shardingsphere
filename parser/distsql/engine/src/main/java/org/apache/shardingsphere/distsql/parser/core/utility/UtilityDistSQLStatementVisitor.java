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

package org.apache.shardingsphere.distsql.parser.core.utility;

import org.apache.shardingsphere.distsql.parser.autogen.UtilityDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.UtilityDistSQLStatementParser.ParseSQLContext;
import org.apache.shardingsphere.distsql.parser.autogen.UtilityDistSQLStatementParser.PreviewSQLContext;
import org.apache.shardingsphere.distsql.statement.type.rul.sql.ParseStatement;
import org.apache.shardingsphere.distsql.statement.type.rul.sql.PreviewStatement;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;

/**
 * SQL statement visitor for utility DistSQL.
 */
public final class UtilityDistSQLStatementVisitor extends UtilityDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitPreviewSQL(final PreviewSQLContext ctx) {
        return new PreviewStatement(ctx.sql().getText().trim());
    }
    
    @Override
    public ASTNode visitParseSQL(final ParseSQLContext ctx) {
        return new ParseStatement(ctx.sql().getText().trim());
    }
}
