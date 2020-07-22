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

package org.apache.shardingsphere.sql.parser.oracle.visitor.impl;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DCLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ObjectPrivilegeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.RevokeContext;
import org.apache.shardingsphere.sql.parser.oracle.visitor.OracleVisitor;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.AlterRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.AlterUserStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.CreateRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.CreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.DropRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.DropUserStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.RevokeStatement;

import java.util.Collection;
import java.util.Collections;

/**
 * DCL visitor for Oracle.
 */
public final class OracleDCLVisitor extends OracleVisitor implements DCLVisitor {
    
    @Override
    public ASTNode visitGrant(final GrantContext ctx) {
        GrantStatement result = new GrantStatement();
        if (null != ctx.objectPrivilegeClause()) {
            for (SimpleTableSegment each : getTableFromPrivilegeClause(ctx.objectPrivilegeClause())) {
                result.getTables().add(each);
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitRevoke(final RevokeContext ctx) {
        RevokeStatement result = new RevokeStatement();
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
        return new CreateUserStatement();
    }
    
    @Override
    public ASTNode visitDropUser(final DropUserContext ctx) {
        return new DropUserStatement();
    }
    
    @Override
    public ASTNode visitAlterUser(final AlterUserContext ctx) {
        return new AlterUserStatement();
    }
    
    @Override
    public ASTNode visitCreateRole(final CreateRoleContext ctx) {
        return new CreateRoleStatement();
    }
    
    @Override
    public ASTNode visitAlterRole(final AlterRoleContext ctx) {
        return new AlterRoleStatement();
    }
    
    @Override
    public ASTNode visitDropRole(final DropRoleContext ctx) {
        return new DropRoleStatement();
    }
}
