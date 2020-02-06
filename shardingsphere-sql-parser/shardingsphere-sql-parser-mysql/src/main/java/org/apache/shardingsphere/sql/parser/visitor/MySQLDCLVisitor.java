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

package org.apache.shardingsphere.sql.parser.visitor;

import org.apache.shardingsphere.sql.parser.MySQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterUserContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropUserContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PrivilegeClause_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PrivilegeLevel_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RenameUserContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetDefaultRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetPasswordContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.AlterUserStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.CreateRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.CreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.DropRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.DropUserStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.RenameUserStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.SetPasswordStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.SetRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.value.LiteralValue;


/**
 * MySQL DCL visitor.
 *
 * @author panjuan
 */
public final class MySQLDCLVisitor extends MySQLVisitor {
    
    @Override
    public ASTNode visitCreateUser(final CreateUserContext ctx) {
        return new CreateUserStatement();
    }
    
    @Override
    public ASTNode visitDropRole(final DropRoleContext ctx) {
        return new DropRoleStatement();
    }
    
    @Override
    public ASTNode visitSetDefaultRole(final SetDefaultRoleContext ctx) {
        return new SetRoleStatement();
    }
    
    @Override
    public ASTNode visitCreateRole(final CreateRoleContext ctx) {
        return new CreateRoleStatement();
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
    public ASTNode visitRenameUser(final RenameUserContext ctx) {
        return new RenameUserStatement();
    }
    
    @Override
    public ASTNode visitSetPassword(final SetPasswordContext ctx) {
        return new SetPasswordStatement();
    }

    @Override
    public ASTNode visitGrant(final GrantContext ctx) {
        GrantStatement result = new GrantStatement();
        PrivilegeClause_Context privilegeClause = ctx.privilegeClause_();
        if (null != privilegeClause && null != privilegeClause.onObjectClause_()) {
            PrivilegeLevel_Context privilegeLevel = privilegeClause.onObjectClause_().privilegeLevel_();
            TableNameContext tableNameContext = privilegeLevel.tableName();
            if (null != tableNameContext) {
                TableSegment tableSegment = createTableSegment(tableNameContext);
                result.getAllSQLSegments().add(tableSegment);
                result.getTables().add(tableSegment);
                return result;
            }
        }
        return result;
    }

    private TableSegment createTableSegment(final TableNameContext tableNameContext) {
        LiteralValue tableName = (LiteralValue) visit(tableNameContext.name());
        TableSegment tableSegment = new TableSegment(tableNameContext.getStart().getStartIndex(), tableNameContext.getStop().getStopIndex(), tableName.getLiteral());
        OwnerContext ownerContext = tableNameContext.owner();
        if (null != ownerContext) {
            LiteralValue literalValue = (LiteralValue) visit(ownerContext.identifier());
            SchemaSegment schemaSegment = new SchemaSegment(ownerContext.getStart().getStartIndex(), ownerContext.getStop().getStopIndex(), literalValue.getLiteral());
            tableSegment.setOwner(schemaSegment);
        }
        return tableSegment;
    }

}
