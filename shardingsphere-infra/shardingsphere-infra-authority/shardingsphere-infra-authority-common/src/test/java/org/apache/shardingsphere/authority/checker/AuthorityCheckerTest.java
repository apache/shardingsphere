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
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public final class AuthorityCheckerTest {

    static {
        ShardingSphereServiceLoader.register(SQLChecker.class);
    }

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData metaData;

    @Test
    public void testCheckSchemaByAllPrivilegesPermitted() {
        Collection<ShardingSphereUser> users = new LinkedList<>();
        ShardingSphereUser root = new ShardingSphereUser("root", "", "localhost");
        users.add(root);
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.emptyList(), new ShardingSphereAlgorithmConfiguration("ALL_PRIVILEGES_PERMITTED", new Properties()));
        AuthorityRule rule = new AuthorityRule(ruleConfig, Collections.emptyMap(), users);
        SQLChecker<AuthorityRule> sqlChecker = OrderedSPIRegistry.getRegisteredServices(Collections.singleton(rule), SQLChecker.class).get(rule);
        assertThat(sqlChecker, notNullValue());
        // any schema
        assertThat(sqlChecker.check("db0", new Grantee("root", "localhost"), rule), is(true));
    }

    @Test
    public void testCheckSchemaByNative() throws SQLException {
        Collection<ShardingSphereUser> users = new LinkedList<>();
        ShardingSphereUser root = new ShardingSphereUser("root", "", "localhost");
        users.add(root);
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.emptyList(), new ShardingSphereAlgorithmConfiguration("NATIVE", new Properties()));
        AuthorityRule rule = new AuthorityRule(ruleConfig, createMetaDataMap(users), users);
        SQLChecker<AuthorityRule> sqlChecker = OrderedSPIRegistry.getRegisteredServices(Collections.singleton(rule), SQLChecker.class).get(rule);
        assertThat(sqlChecker, notNullValue());
        assertThat(sqlChecker.check("db0", new Grantee("root", "localhost"), rule), is(true));
        assertThat(sqlChecker.check("db1", new Grantee("root", "localhost"), rule), is(false));
        assertThat(sqlChecker.check("db0", new Grantee("other", "localhost"), rule), is(false));
    }

    @Test
    public void testCheckUser() {
        Collection<ShardingSphereUser> users = new LinkedList<>();
        ShardingSphereUser root = new ShardingSphereUser("root", "", "localhost");
        users.add(root);
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.emptyList(), new ShardingSphereAlgorithmConfiguration("NATIVE", new Properties()));
        AuthorityRule rule = new AuthorityRule(ruleConfig, Collections.emptyMap(), users);
        SQLChecker<AuthorityRule> sqlChecker = OrderedSPIRegistry.getRegisteredServices(Collections.singleton(rule), SQLChecker.class).get(rule);
        assertThat(sqlChecker, notNullValue());
        assertThat(sqlChecker.check(new Grantee("root", "localhost"), rule), is(true));
        assertThat(sqlChecker.check(new Grantee("root", "192.168.0.1"), rule), is(false));
        assertThat(sqlChecker.check(new Grantee("admin", "localhost"), rule), is(false));
    }

    @Test
    public void testCheckSqlStatement() {
        Collection<ShardingSphereUser> users = new LinkedList<>();
        ShardingSphereUser root = new ShardingSphereUser("root", "", "localhost");
        users.add(root);
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.emptyList(), new ShardingSphereAlgorithmConfiguration("NATIVE", new Properties()));
        AuthorityRule rule = new AuthorityRule(ruleConfig, Collections.emptyMap(), users);
        SQLChecker<AuthorityRule> sqlChecker = OrderedSPIRegistry.getRegisteredServices(Collections.singleton(rule), SQLChecker.class).get(rule);
        assertThat(sqlChecker, notNullValue());
        SelectStatement selectStatement = mock(SelectStatement.class);
        CreateTableStatement createTableStatement = mock(CreateTableStatement.class);
        InsertStatement insertStatement = mock(InsertStatement.class);
        assertThat(sqlChecker.check(selectStatement, Collections.emptyList(), new Grantee("root", "localhost"), "db0", Collections.emptyMap(), rule).isPassed(), is(true));
        assertThat(sqlChecker.check(insertStatement, Collections.emptyList(), new Grantee("root", "localhost"), "db0", Collections.emptyMap(), rule).isPassed(), is(true));
        assertThat(sqlChecker.check(createTableStatement, Collections.emptyList(), new Grantee("root", "localhost"), "db0", Collections.emptyMap(), rule).isPassed(), is(true));
    }

    private Map<String, ShardingSphereMetaData> createMetaDataMap(final Collection<ShardingSphereUser> users) throws SQLException {
        when(metaData.getName()).thenReturn("db0");
        DataSource dataSource = mockDataSourceForPrivileges(users);
        when(metaData.getResource().getAllInstanceDataSources()).thenReturn(Collections.singletonList(dataSource));
        when(metaData.getSchema()).thenReturn(mock(ShardingSphereSchema.class));
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
        when(result.getBoolean("Super_priv")).thenReturn(false);
        when(result.getBoolean("Reload_priv")).thenReturn(false);
        when(result.getBoolean("Shutdown_priv")).thenReturn(false);
        when(result.getBoolean("Process_priv")).thenReturn(false);
        when(result.getBoolean("File_priv")).thenReturn(false);
        when(result.getBoolean("Show_db_priv")).thenReturn(false);
        when(result.getBoolean("Repl_slave_priv")).thenReturn(false);
        when(result.getBoolean("Repl_client_priv")).thenReturn(false);
        when(result.getBoolean("Create_user_priv")).thenReturn(false);
        when(result.getBoolean("Create_tablespace_priv")).thenReturn(false);
        when(result.getBoolean("Select_priv")).thenReturn(false);
        when(result.getBoolean("Insert_priv")).thenReturn(false);
        when(result.getBoolean("Update_priv")).thenReturn(false);
        when(result.getBoolean("Delete_priv")).thenReturn(false);
        when(result.getBoolean("Create_priv")).thenReturn(false);
        when(result.getBoolean("Alter_priv")).thenReturn(false);
        when(result.getBoolean("Drop_priv")).thenReturn(false);
        when(result.getBoolean("Grant_priv")).thenReturn(false);
        when(result.getBoolean("Index_priv")).thenReturn(false);
        when(result.getBoolean("References_priv")).thenReturn(false);
        when(result.getBoolean("Create_tmp_table_priv")).thenReturn(false);
        when(result.getBoolean("Lock_tables_priv")).thenReturn(false);
        when(result.getBoolean("Execute_priv")).thenReturn(false);
        when(result.getBoolean("Create_view_priv")).thenReturn(false);
        when(result.getBoolean("Show_view_priv")).thenReturn(false);
        when(result.getBoolean("Create_routine_priv")).thenReturn(false);
        when(result.getBoolean("Alter_routine_priv")).thenReturn(false);
        when(result.getBoolean("Event_priv")).thenReturn(false);
        when(result.getBoolean("Trigger_priv")).thenReturn(false);
        when(result.getString("user")).thenReturn("root");
        when(result.getString("host")).thenReturn("localhost");
        return result;
    }

    private ResultSet mockSchemaPrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("Db")).thenReturn("db0");
        when(result.getBoolean("Select_priv")).thenReturn(true);
        when(result.getBoolean("Insert_priv")).thenReturn(true);
        when(result.getBoolean("Update_priv")).thenReturn(true);
        when(result.getBoolean("Delete_priv")).thenReturn(true);
        when(result.getBoolean("Create_priv")).thenReturn(false);
        when(result.getBoolean("Alter_priv")).thenReturn(false);
        when(result.getBoolean("Drop_priv")).thenReturn(false);
        when(result.getBoolean("Grant_priv")).thenReturn(false);
        when(result.getBoolean("Index_priv")).thenReturn(false);
        when(result.getBoolean("References_priv")).thenReturn(false);
        when(result.getBoolean("Create_tmp_table_priv")).thenReturn(false);
        when(result.getBoolean("Lock_tables_priv")).thenReturn(false);
        when(result.getBoolean("Execute_priv")).thenReturn(false);
        when(result.getBoolean("Create_view_priv")).thenReturn(false);
        when(result.getBoolean("Show_view_priv")).thenReturn(false);
        when(result.getBoolean("Create_routine_priv")).thenReturn(false);
        when(result.getBoolean("Alter_routine_priv")).thenReturn(false);
        when(result.getBoolean("Event_priv")).thenReturn(false);
        when(result.getBoolean("Trigger_priv")).thenReturn(false);
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
