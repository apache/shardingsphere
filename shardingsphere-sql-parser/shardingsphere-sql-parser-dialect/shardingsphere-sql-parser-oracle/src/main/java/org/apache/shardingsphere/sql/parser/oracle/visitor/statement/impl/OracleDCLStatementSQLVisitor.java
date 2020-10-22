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

package org.apache.shardingsphere.sql.parser.oracle.visitor.statement.impl;

import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.DCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ObjectPrivilegeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.RevokeContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dcl.OracleAlterRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dcl.OracleAlterUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dcl.OracleCreateRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dcl.OracleCreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dcl.OracleDropRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dcl.OracleDropUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dcl.OracleGrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dcl.OracleRevokeStatement;

import java.util.Collection;
import java.util.Collections;

/**
 * DCL Statement SQL visitor for Oracle.
 */
public final class OracleDCLStatementSQLVisitor extends OracleStatementSQLVisitor implements DCLStatementSQLVisitor {
    
    @Override
    public ASTNode visitGrant(final GrantContext ctx) {
        OracleGrantStatement result = new OracleGrantStatement();
        if (null != ctx.objectPrivilegeClause()) {
            for (SimpleTableSegment each : getTableFromPrivilegeClause(ctx.objectPrivilegeClause())) {
                result.getTables().add(each);
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitRevoke(final RevokeContext ctx) {
        OracleRevokeStatement result = new OracleRevokeStatement();
        if (null != ctx.objectPrivilegeClause()) {
            for (SimpleTableSegment each : getTableFromPrivilegeClause(ctx.objectPrivilegeClause())) {
                result.getTables().add(each);
            }
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getTableFromPrivilegeClause(final ObjectPrivilegeClauseContext ctx) {
        return null == ctx.onObjectClause().tableName() ? Collections.emptyList() : Collections.singletonList((SimpleTableSegment) visit(ctx.onObjectClause().tableName()));
    }
    
    @Override
    public ASTNode visitCreateUser(final CreateUserContext ctx) {
        return new OracleCreateUserStatement();
    }
    
    @Override
    public ASTNode visitDropUser(final DropUserContext ctx) {
        return new OracleDropUserStatement();
    }
    
    @Override
    public ASTNode visitAlterUser(final AlterUserContext ctx) {
        return new OracleAlterUserStatement();
    }
    
    @Override
    public ASTNode visitCreateRole(final CreateRoleContext ctx) {
        return new OracleCreateRoleStatement();
    }
    
    @Override
    public ASTNode visitAlterRole(final AlterRoleContext ctx) {
        return new OracleAlterRoleStatement();
    }
    
    @Override
    public ASTNode visitDropRole(final DropRoleContext ctx) {
        return new OracleDropRoleStatement();
    }
}
