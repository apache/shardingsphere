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

package org.apache.shardingsphere.authority.distsql.parser.core;

import org.apache.shardingsphere.authority.distsql.parser.statement.ShowAuthorityRuleStatement;
import org.apache.shardingsphere.distsql.parser.autogen.AuthorityDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.AuthorityDistSQLStatementParser.ShowAuthorityRuleContext;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;

/**
 * SQL statement visitor for authority dist SQL.
 */
public final class AuthorityDistSQLStatementVisitor extends AuthorityDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitShowAuthorityRule(final ShowAuthorityRuleContext ctx) {
        return new ShowAuthorityRuleStatement();
    }
}
