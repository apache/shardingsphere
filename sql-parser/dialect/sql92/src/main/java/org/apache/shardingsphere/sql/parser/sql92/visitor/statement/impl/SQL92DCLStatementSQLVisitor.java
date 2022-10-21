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

package org.apache.shardingsphere.sql.parser.sql92.visitor.statement.impl;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.type.DCLSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.PrivilegeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQL92StatementParser.RevokeContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dcl.SQL92GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dcl.SQL92RevokeStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * DCL Statement SQL visitor for SQL92.
 */
@NoArgsConstructor
public final class SQL92DCLStatementSQLVisitor extends SQL92StatementSQLVisitor implements DCLSQLVisitor, SQLStatementVisitor {
    
    public SQL92DCLStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @Override
    public ASTNode visitGrant(final GrantContext ctx) {
        SQL92GrantStatement result = new SQL92GrantStatement();
        if (null != ctx.privilegeClause()) {
            for (SimpleTableSegment each : getTableFromPrivilegeClause(ctx.privilegeClause())) {
                result.getTables().add(each);
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitRevoke(final RevokeContext ctx) {
        SQL92RevokeStatement result = new SQL92RevokeStatement();
        if (null != ctx.privilegeClause()) {
            for (SimpleTableSegment each : getTableFromPrivilegeClause(ctx.privilegeClause())) {
                result.getTables().add(each);
            }
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getTableFromPrivilegeClause(final PrivilegeClauseContext ctx) {
        return Collections.singletonList((SimpleTableSegment) visit(ctx.onObjectClause().privilegeLevel().tableName()));
    }
}
