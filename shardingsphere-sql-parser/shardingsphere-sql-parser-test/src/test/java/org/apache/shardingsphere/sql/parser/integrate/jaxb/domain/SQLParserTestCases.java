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

package org.apache.shardingsphere.sql.parser.integrate.jaxb.domain;

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.CommonStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dal.DescribeStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dal.ShowCreateTableStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dal.ShowDatabasesStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dal.ShowIndexStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dal.ShowStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dal.ShowTableStatusStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dal.ShowTablesStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dal.UseStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dcl.AlterLoginStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dcl.AlterRoleStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dcl.AlterUserStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dcl.CreateLoginStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dcl.CreateRoleStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dcl.CreateUserStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dcl.DenyUserStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dcl.DropLoginStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dcl.DropRoleStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dcl.DropUserStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dcl.GrantStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dcl.RenameUserStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dcl.RevokeStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dcl.SetPasswordStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dcl.SetRoleStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.ddl.AlterIndexStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.ddl.AlterTableStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.ddl.CreateIndexStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.ddl.CreateTableStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.ddl.DropIndexStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.ddl.DropTableStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.ddl.TruncateStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dml.DeleteStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dml.InsertStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dml.SelectStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.dml.UpdateStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.tcl.BeginTransactionStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.tcl.CommitStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.tcl.RollbackStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.tcl.SavepointStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.tcl.SetAutoCommitStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.domain.statement.tcl.SetTransactionStatementTestCase;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL parser test cases.
 * 
 * @author zhangliang 
 */
@XmlRootElement(name = "sql-parser-test-cases")
@Getter
public final class SQLParserTestCases {
    
    @XmlElement(name = "select")
    private final List<SelectStatementTestCase> selectTestCases = new LinkedList<>();
    
    @XmlElement(name = "update")
    private final List<UpdateStatementTestCase> updateTestCases = new LinkedList<>();
    
    @XmlElement(name = "delete")
    private final List<DeleteStatementTestCase> deleteTestCases = new LinkedList<>();
    
    @XmlElement(name = "insert")
    private final List<InsertStatementTestCase> insertTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-table")
    private final List<CreateTableStatementTestCase> createTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-table")
    private final List<AlterTableStatementTestCase> alterTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-table")
    private final List<DropTableStatementTestCase> dropTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "truncate")
    private final List<TruncateStatementTestCase> truncateTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-index")
    private final List<CreateIndexStatementTestCase> createIndexTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-index")
    private final List<AlterIndexStatementTestCase> alterIndexTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-index")
    private final List<DropIndexStatementTestCase> dropIndexTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-transaction")
    private final List<SetTransactionStatementTestCase> setTransactionTestCases = new LinkedList<>();
    
    @XmlElement(name = "begin-transaction")
    private final List<BeginTransactionStatementTestCase> beginTransactionTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-auto-commit")
    private final List<SetAutoCommitStatementTestCase> setAutoCommitTestCases = new LinkedList<>();
    
    @XmlElement(name = "commit")
    private final List<CommitStatementTestCase> commitTestCases = new LinkedList<>();
    
    @XmlElement(name = "rollback")
    private final List<RollbackStatementTestCase> rollbackTestCases = new LinkedList<>();
    
    @XmlElement(name = "savepoint")
    private final List<SavepointStatementTestCase> savepointTestCases = new LinkedList<>();
    
    @XmlElement(name = "grant")
    private final List<GrantStatementTestCase> grantTestCases = new LinkedList<>();
    
    @XmlElement(name = "revoke")
    private final List<RevokeStatementTestCase> revokeTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-user")
    private final List<CreateUserStatementTestCase> createUserTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-user")
    private final List<AlterUserStatementTestCase> alterUserTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-user")
    private final List<DropUserStatementTestCase> dropUserTestCases = new LinkedList<>();
    
    @XmlElement(name = "rename-user")
    private final List<RenameUserStatementTestCase> renameUserTestCases = new LinkedList<>();
    
    @XmlElement(name = "deny-user")
    private final List<DenyUserStatementTestCase> denyUserTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-login")
    private final List<CreateLoginStatementTestCase> createLoginTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-login")
    private final List<AlterLoginStatementTestCase> alterLoginTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-login")
    private final List<DropLoginStatementTestCase> dropLoginTestCases = new LinkedList<>();
    
    @XmlElement(name = "create-role")
    private final List<CreateRoleStatementTestCase> createRoleTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-role")
    private final List<AlterRoleStatementTestCase> alterRoleTestCases = new LinkedList<>();
    
    @XmlElement(name = "drop-role")
    private final List<DropRoleStatementTestCase> dropRoleTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-role")
    private final List<SetRoleStatementTestCase> setRoleTestCases = new LinkedList<>();
    
    @XmlElement(name = "set-password")
    private final List<SetPasswordStatementTestCase> setPasswordTestCases = new LinkedList<>();
    
    @XmlElement(name = "use")
    private final List<UseStatementTestCase> useTestCases = new LinkedList<>();
    
    @XmlElement(name = "describe")
    private final List<DescribeStatementTestCase> describeTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-databases")
    private final List<ShowDatabasesStatementTestCase> showDatabasesTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-tables")
    private final List<ShowTablesStatementTestCase> showTablesTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-columns")
    private final List<ShowTablesStatementTestCase> showColumnsTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-create-table")
    private final List<ShowCreateTableStatementTestCase> showCreateTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "show-table-status")
    private final List<ShowTableStatusStatementTestCase> showTableStatusTestCases = new LinkedList<>();

    @XmlElement(name = "show-index")
    private final List<ShowIndexStatementTestCase> showIndexTestCases = new LinkedList<>();
    
    @XmlElement(name = "show")
    private final List<ShowStatementTestCase> showTestCases = new LinkedList<>();
    
    @XmlElement(name = "common")
    private final List<CommonStatementTestCase> commonTestCases = new LinkedList<>();
    
    /**
     * Get all SQL parser test cases.
     * 
     * @return all SQL parser test cases
     */
    public Map<String, SQLParserTestCase> getAllSQLParserTestCases() {
        Map<String, SQLParserTestCase> result = new HashMap<>();
        result.putAll(getSQLParserTestCases(selectTestCases));
        result.putAll(getSQLParserTestCases(updateTestCases));
        result.putAll(getSQLParserTestCases(deleteTestCases));
        result.putAll(getSQLParserTestCases(insertTestCases));
        result.putAll(getSQLParserTestCases(createTableTestCases));
        result.putAll(getSQLParserTestCases(alterTableTestCases));
        result.putAll(getSQLParserTestCases(dropTableTestCases));
        result.putAll(getSQLParserTestCases(truncateTestCases));
        result.putAll(getSQLParserTestCases(createIndexTestCases));
        result.putAll(getSQLParserTestCases(alterIndexTestCases));
        result.putAll(getSQLParserTestCases(dropIndexTestCases));
        result.putAll(getSQLParserTestCases(setTransactionTestCases));
        result.putAll(getSQLParserTestCases(beginTransactionTestCases));
        result.putAll(getSQLParserTestCases(setAutoCommitTestCases));
        result.putAll(getSQLParserTestCases(commitTestCases));
        result.putAll(getSQLParserTestCases(rollbackTestCases));
        result.putAll(getSQLParserTestCases(savepointTestCases));
        result.putAll(getSQLParserTestCases(grantTestCases));
        result.putAll(getSQLParserTestCases(revokeTestCases));
        result.putAll(getSQLParserTestCases(createUserTestCases));
        result.putAll(getSQLParserTestCases(alterUserTestCases));
        result.putAll(getSQLParserTestCases(dropUserTestCases));
        result.putAll(getSQLParserTestCases(renameUserTestCases));
        result.putAll(getSQLParserTestCases(denyUserTestCases));
        result.putAll(getSQLParserTestCases(createLoginTestCases));
        result.putAll(getSQLParserTestCases(alterLoginTestCases));
        result.putAll(getSQLParserTestCases(dropLoginTestCases));
        result.putAll(getSQLParserTestCases(createRoleTestCases));
        result.putAll(getSQLParserTestCases(alterRoleTestCases));
        result.putAll(getSQLParserTestCases(dropRoleTestCases));
        result.putAll(getSQLParserTestCases(setRoleTestCases));
        result.putAll(getSQLParserTestCases(setPasswordTestCases));
        result.putAll(getSQLParserTestCases(useTestCases));
        result.putAll(getSQLParserTestCases(describeTestCases));
        result.putAll(getSQLParserTestCases(showDatabasesTestCases));
        result.putAll(getSQLParserTestCases(showTablesTestCases));
        result.putAll(getSQLParserTestCases(showColumnsTestCases));
        result.putAll(getSQLParserTestCases(showCreateTableTestCases));
        result.putAll(getSQLParserTestCases(showTableStatusTestCases));
        result.putAll(getSQLParserTestCases(showIndexTestCases));
        result.putAll(getSQLParserTestCases(showTestCases));
        result.putAll(getSQLParserTestCases(commonTestCases));
        return result;
    }
    
    private Map<String, SQLParserTestCase> getSQLParserTestCases(final List<? extends SQLParserTestCase> sqlParserTestCases) {
        Map<String, SQLParserTestCase> result = new HashMap<>(sqlParserTestCases.size(), 1);
        for (SQLParserTestCase each : sqlParserTestCases) {
            result.put(each.getSqlCaseId(), each);
        }
        return result;
    }
}
