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

package org.apache.shardingsphere.sql.parser.postgresql.visitor.impl.statement;

import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.impl.DCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AlterUserContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DropUserContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.PrivilegeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RevokeContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dcl.PostgreSQLAlterRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dcl.PostgreSQLAlterUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dcl.PostgreSQLCreateRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dcl.PostgreSQLCreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dcl.PostgreSQLDropRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dcl.PostgreSQLDropUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dcl.PostgreSQLGrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dcl.PostgreSQLRevokeStatement;

import java.util.Collection;
import java.util.Optional;

/**
 * DCL Statement SQL visitor for PostgreSQL.
 */
public final class PostgreSQLDCLStatementSQLVisitor extends PostgreSQLStatementSQLVisitor implements DCLStatementSQLVisitor {
    
    @Override
    public ASTNode visitGrant(final GrantContext ctx) {
        PostgreSQLGrantStatement result = new PostgreSQLGrantStatement();
        Optional<Collection<SimpleTableSegment>> tableSegmentOptional = null == ctx.privilegeClause() ? Optional.empty() : getTableFromPrivilegeClause(ctx.privilegeClause());
        tableSegmentOptional.ifPresent(tableSegment -> result.getTables().addAll(tableSegment));
        return result;
    }
    
    @Override
    public ASTNode visitRevoke(final RevokeContext ctx) {
        PostgreSQLRevokeStatement result = new PostgreSQLRevokeStatement();
        Optional<Collection<SimpleTableSegment>> tableSegmentOptional = null == ctx.privilegeClause() ? Optional.empty() : getTableFromPrivilegeClause(ctx.privilegeClause());
        tableSegmentOptional.ifPresent(tableSegment -> result.getTables().addAll(tableSegment));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Optional<Collection<SimpleTableSegment>> getTableFromPrivilegeClause(final PrivilegeClauseContext ctx) {
        if (null != ctx.onObjectClause() && null != ctx.onObjectClause().privilegeLevel()) {
            if (null != ctx.onObjectClause().privilegeLevel().tableNames()) {
                return Optional.of(((CollectionValue<SimpleTableSegment>) visit(ctx.onObjectClause().privilegeLevel().tableNames())).getValue());
            }
        }
        return Optional.empty();
    }
    
    @Override
    public ASTNode visitCreateUser(final CreateUserContext ctx) {
        return new PostgreSQLCreateUserStatement();
    }
    
    @Override
    public ASTNode visitDropUser(final DropUserContext ctx) {
        return new PostgreSQLDropUserStatement();
    }
    
    @Override
    public ASTNode visitAlterUser(final AlterUserContext ctx) {
        return new PostgreSQLAlterUserStatement();
    }
    
    @Override
    public ASTNode visitCreateRole(final CreateRoleContext ctx) {
        return new PostgreSQLCreateRoleStatement();
    }
    
    @Override
    public ASTNode visitAlterRole(final AlterRoleContext ctx) {
        return new PostgreSQLAlterRoleStatement();
    }
    
    @Override
    public ASTNode visitDropRole(final DropRoleContext ctx) {
        return new PostgreSQLDropRoleStatement();
    }
}
