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

package org.apache.shardingsphere.authority.checker;

import org.apache.shardingsphere.authority.api.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.executor.check.SQLChecker;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AuthorityCheckerTest {
    
    static {
        ShardingSphereServiceLoader.register(SQLChecker.class);
    }
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData metaData;
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertCheckSchemaByAllPrivilegesPermitted() {
        Collection<ShardingSphereUser> users = new LinkedList<>();
        ShardingSphereUser root = new ShardingSphereUser("root", "", "localhost");
        users.add(root);
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.emptyList(), new ShardingSphereAlgorithmConfiguration("ALL_PRIVILEGES_PERMITTED", new Properties()));
        AuthorityRule rule = new AuthorityRule(ruleConfig, Collections.emptyMap(), users);
        SQLChecker<AuthorityRule> sqlChecker = OrderedSPIRegistry.getRegisteredServices(SQLChecker.class, Collections.singleton(rule)).get(rule);
        assertNotNull(sqlChecker);
        assertTrue(sqlChecker.check("db0", new Grantee("root", "localhost"), rule));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertCheckSchemaByNative() throws SQLException {
        Collection<ShardingSphereUser> users = new LinkedList<>();
        ShardingSphereUser root = new ShardingSphereUser("root", "", "localhost");
        users.add(root);
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.emptyList(), new ShardingSphereAlgorithmConfiguration("NATIVE", new Properties()));
        AuthorityRule rule = new AuthorityRule(ruleConfig, createMetaDataMap(users), users);
        SQLChecker<AuthorityRule> sqlChecker = OrderedSPIRegistry.getRegisteredServices(SQLChecker.class, Collections.singleton(rule)).get(rule);
        assertNotNull(sqlChecker);
        assertTrue(sqlChecker.check("db0", new Grantee("root", "localhost"), rule));
        assertFalse(sqlChecker.check("db1", new Grantee("root", "localhost"), rule));
        assertFalse(sqlChecker.check("db0", new Grantee("other", "localhost"), rule));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertCheckUser() {
        Collection<ShardingSphereUser> users = new LinkedList<>();
        ShardingSphereUser root = new ShardingSphereUser("root", "", "localhost");
        users.add(root);
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.emptyList(), new ShardingSphereAlgorithmConfiguration("NATIVE", new Properties()));
        AuthorityRule rule = new AuthorityRule(ruleConfig, Collections.emptyMap(), users);
        SQLChecker<AuthorityRule> sqlChecker = OrderedSPIRegistry.getRegisteredServices(SQLChecker.class, Collections.singleton(rule)).get(rule);
        assertNotNull(sqlChecker);
        assertTrue(sqlChecker.check(new Grantee("root", "localhost"), rule));
        assertFalse(sqlChecker.check(new Grantee("root", "192.168.0.1"), rule));
        assertFalse(sqlChecker.check(new Grantee("admin", "localhost"), rule));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertCheckSQLStatement() {
        Collection<ShardingSphereUser> users = new LinkedList<>();
        ShardingSphereUser root = new ShardingSphereUser("root", "", "localhost");
        users.add(root);
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.emptyList(), new ShardingSphereAlgorithmConfiguration("NATIVE", new Properties()));
        AuthorityRule rule = new AuthorityRule(ruleConfig, Collections.emptyMap(), users);
        SQLChecker<AuthorityRule> sqlChecker = OrderedSPIRegistry.getRegisteredServices(SQLChecker.class, Collections.singleton(rule)).get(rule);
        assertNotNull(sqlChecker);
        SelectStatement selectStatement = mock(SelectStatement.class);
        CreateTableStatement createTableStatement = mock(CreateTableStatement.class);
        InsertStatement insertStatement = mock(InsertStatement.class);
        assertTrue(sqlChecker.check(selectStatement, Collections.emptyList(), new Grantee("root", "localhost"), "db0", Collections.emptyMap(), rule).isPassed());
        assertTrue(sqlChecker.check(insertStatement, Collections.emptyList(), new Grantee("root", "localhost"), "db0", Collections.emptyMap(), rule).isPassed());
        assertTrue(sqlChecker.check(createTableStatement, Collections.emptyList(), new Grantee("root", "localhost"), "db0", Collections.emptyMap(), rule).isPassed());
    }
    
    private Map<String, ShardingSphereMetaData> createMetaDataMap(final Collection<ShardingSphereUser> users) throws SQLException {
        when(metaData.getName()).thenReturn("db0");
        DataSource dataSource = mockDataSourceForPrivileges(users);
        when(metaData.getResource().getAllInstanceDataSources()).thenReturn(Collections.singletonList(dataSource));
        when(metaData.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return Collections.singletonMap("db0", metaData);
    }
    
    private DataSource mockDataSourceForPrivileges(final Collection<ShardingSphereUser> users) throws SQLException {
        ResultSet globalPrivilegeResultSet = mockGlobalPrivilegeResultSet();
        ResultSet schemaPrivilegeResultSet = mockSchemaPrivilegeResultSet();
        ResultSet tablePrivilegeResultSet = mockTablePrivilegeResultSet();
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        String globalPrivilegeSQL = "SELECT * FROM mysql.user WHERE (user, host) in (%s)";
        String schemaPrivilegeSQL = "SELECT * FROM mysql.db WHERE (user, host) in (%s)";
        String tablePrivilegeSQL = "SELECT Db, Table_name, Table_priv FROM mysql.tables_priv WHERE (user, host) in (%s)";
        String useHostTuples = users.stream().map(item -> String.format("('%s', '%s')", item.getGrantee().getUsername(), item.getGrantee().getHostname())).collect(Collectors.joining(", "));
        when(result.getConnection().createStatement().executeQuery(String.format(globalPrivilegeSQL, useHostTuples))).thenReturn(globalPrivilegeResultSet);
        when(result.getConnection().createStatement().executeQuery(String.format(schemaPrivilegeSQL, useHostTuples))).thenReturn(schemaPrivilegeResultSet);
        when(result.getConnection().createStatement().executeQuery(String.format(tablePrivilegeSQL, useHostTuples))).thenReturn(tablePrivilegeResultSet);
        when(result.getConnection().getMetaData().getURL()).thenReturn("jdbc:mysql://localhost:3306/test");
        return result;
    }
    
    private ResultSet mockGlobalPrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("user")).thenReturn("root");
        when(result.getString("host")).thenReturn("localhost");
        return result;
    }
    
    private ResultSet mockSchemaPrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("Db")).thenReturn("db0");
        when(result.getObject("Select_priv")).thenReturn(true);
        when(result.getObject("Insert_priv")).thenReturn(true);
        when(result.getObject("Update_priv")).thenReturn(true);
        when(result.getObject("Delete_priv")).thenReturn(true);
        when(result.getString("user")).thenReturn("root");
        when(result.getString("host")).thenReturn("localhost");
        return result;
    }
    
    private ResultSet mockTablePrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        when(result.next()).thenReturn(true, false);
        when(result.getString("Db")).thenReturn("db0");
        when(result.getString("Table_name")).thenReturn("sys_config");
        when(result.getArray("Table_priv").getArray()).thenReturn(new String[]{"Select"});
        when(result.getString("user")).thenReturn("root");
        when(result.getString("host")).thenReturn("localhost");
        return result;
    }
}
