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

package org.apache.shardingsphere.sql.parser.sql92.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.RevokeContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dcl.SQL92GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dcl.SQL92RevokeStatement;
import org.apache.shardingsphere.sql.parser.sql92.visitor.statement.SQL92StatementVisitor;

/**
 * DCL statement visitor for SQL92.
 */
public final class SQL92DCLStatementVisitor extends SQL92StatementVisitor implements DCLStatementVisitor {
    
    @Override
    public ASTNode visitGrant(final GrantContext ctx) {
        SQL92GrantStatement result = new SQL92GrantStatement();
        if (null != ctx.privilegeClause()) {
            result.getTables().add((SimpleTableSegment) visit(ctx.privilegeClause().onObjectClause().privilegeLevel().tableName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitRevoke(final RevokeContext ctx) {
        SQL92RevokeStatement result = new SQL92RevokeStatement();
        if (null != ctx.privilegeClause()) {
            result.getTables().add((SimpleTableSegment) visit(ctx.privilegeClause().onObjectClause().privilegeLevel().tableName()));
        }
        return result;
    }
}
