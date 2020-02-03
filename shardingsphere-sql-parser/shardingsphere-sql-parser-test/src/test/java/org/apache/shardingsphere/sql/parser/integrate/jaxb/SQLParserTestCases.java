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

package org.apache.shardingsphere.sql.parser.integrate.jaxb;

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.CommonStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.SQLParserTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dal.DescribeStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dal.ShowCreateTableStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dal.ShowDatabasesStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dal.ShowIndexStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dal.ShowStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dal.ShowTableStatusStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dal.ShowTablesStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dal.UseStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dcl.GrantStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dcl.RevokeStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.ddl.AlterTableStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.ddl.CreateIndexStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.ddl.CreateTableStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.ddl.DropIndexStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.ddl.DropTableStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.ddl.TruncateStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dml.DeleteStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dml.InsertStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dml.SelectStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.dml.UpdateStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.tcl.BeginTransactionStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.tcl.CommitStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.tcl.RollbackStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.tcl.SavepointStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.tcl.SetAutoCommitStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.tcl.SetTransactionStatementTestCase;

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
        result.putAll(getSQLParserTestCases(dropIndexTestCases));
        result.putAll(getSQLParserTestCases(setTransactionTestCases));
        result.putAll(getSQLParserTestCases(beginTransactionTestCases));
        result.putAll(getSQLParserTestCases(setAutoCommitTestCases));
        result.putAll(getSQLParserTestCases(commitTestCases));
        result.putAll(getSQLParserTestCases(rollbackTestCases));
        result.putAll(getSQLParserTestCases(savepointTestCases));
        result.putAll(getSQLParserTestCases(grantTestCases));
        result.putAll(getSQLParserTestCases(revokeTestCases));
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
