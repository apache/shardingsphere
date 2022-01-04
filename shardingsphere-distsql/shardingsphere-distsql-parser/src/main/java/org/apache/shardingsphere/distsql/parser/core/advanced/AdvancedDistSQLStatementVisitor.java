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

package org.apache.shardingsphere.distsql.parser.core.advanced;

import org.apache.shardingsphere.distsql.parser.autogen.AdvancedDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.AdvancedDistSQLStatementParser.ParseSQLContext;
import org.apache.shardingsphere.distsql.parser.autogen.AdvancedDistSQLStatementParser.PreviewSQLContext;
import org.apache.shardingsphere.distsql.parser.statement.ral.advanced.parse.ParseStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.advanced.preview.PreviewStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;

/**
 * SQL statement visitor for advanced dist SQL.
 */
public final class AdvancedDistSQLStatementVisitor extends AdvancedDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitPreviewSQL(final PreviewSQLContext ctx) {
        return new PreviewStatement(ctx.sql().getText().trim());
    }
    
    @Override
    public ASTNode visitParseSQL(final ParseSQLContext ctx) {
        return new ParseStatement(ctx.sql().getText().trim());
    }
}
