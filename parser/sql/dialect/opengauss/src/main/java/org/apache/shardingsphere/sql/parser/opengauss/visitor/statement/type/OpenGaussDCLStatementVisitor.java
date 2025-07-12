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

package org.apache.shardingsphere.sql.parser.opengauss.visitor.statement.type;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.PrivilegeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.RevokeContext;
import org.apache.shardingsphere.sql.parser.opengauss.visitor.statement.OpenGaussStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.AlterRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.CreateRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.DropRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.AlterUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.CreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.DropUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;

import java.util.Collection;
import java.util.Collections;

/**
 * DCL statement visitor for openGauss.
 */
public final class OpenGaussDCLStatementVisitor extends OpenGaussStatementVisitor implements DCLStatementVisitor {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    @Override
    public ASTNode visitGrant(final GrantContext ctx) {
        GrantStatement result = new GrantStatement(databaseType);
        if (containsTableSegment(ctx.privilegeClause())) {
            result.getTables().addAll(getTableSegments(ctx.privilegeClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitRevoke(final RevokeContext ctx) {
        RevokeStatement result = new RevokeStatement(databaseType);
        if (containsTableSegment(ctx.privilegeClause())) {
            result.getTables().addAll(getTableSegments(ctx.privilegeClause()));
        }
        return result;
    }
    
    private boolean containsTableSegment(final PrivilegeClauseContext ctx) {
        return null != ctx && null != ctx.onObjectClause() && null != ctx.onObjectClause().privilegeLevel() && null != ctx.onObjectClause().privilegeLevel().tableNames();
    }
    
    @SuppressWarnings("unchecked")
    private Collection<SimpleTableSegment> getTableSegments(final PrivilegeClauseContext ctx) {
        return ((CollectionValue<SimpleTableSegment>) visit(ctx.onObjectClause().privilegeLevel().tableNames())).getValue();
    }
    
    @Override
    public ASTNode visitCreateUser(final CreateUserContext ctx) {
        return new CreateUserStatement(databaseType);
    }
    
    @Override
    public ASTNode visitDropUser(final DropUserContext ctx) {
        return new DropUserStatement(databaseType, Collections.emptyList());
    }
    
    @Override
    public ASTNode visitAlterUser(final AlterUserContext ctx) {
        return new AlterUserStatement(databaseType, null);
    }
    
    @Override
    public ASTNode visitCreateRole(final CreateRoleContext ctx) {
        return new CreateRoleStatement(databaseType);
    }
    
    @Override
    public ASTNode visitAlterRole(final AlterRoleContext ctx) {
        return new AlterRoleStatement(databaseType);
    }
    
    @Override
    public ASTNode visitDropRole(final DropRoleContext ctx) {
        return new DropRoleStatement(databaseType);
    }
}
