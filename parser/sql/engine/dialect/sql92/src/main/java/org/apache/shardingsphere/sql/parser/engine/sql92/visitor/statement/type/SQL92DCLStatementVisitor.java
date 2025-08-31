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

package org.apache.shardingsphere.sql.parser.engine.sql92.visitor.statement.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.RevokeContext;
import org.apache.shardingsphere.sql.parser.engine.sql92.visitor.statement.SQL92StatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.RevokeStatement;

/**
 * DCL statement visitor for SQL92.
 */
public final class SQL92DCLStatementVisitor extends SQL92StatementVisitor implements DCLStatementVisitor {
    
    public SQL92DCLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitGrant(final GrantContext ctx) {
        GrantStatement result = new GrantStatement(getDatabaseType());
        if (null != ctx.privilegeClause()) {
            result.getTables().add((SimpleTableSegment) visit(ctx.privilegeClause().onObjectClause().privilegeLevel().tableName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitRevoke(final RevokeContext ctx) {
        RevokeStatement result = new RevokeStatement(getDatabaseType());
        if (null != ctx.privilegeClause()) {
            result.getTables().add((SimpleTableSegment) visit(ctx.privilegeClause().onObjectClause().privilegeLevel().tableName()));
        }
        return result;
    }
}
