/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.dbtest.asserts;

import com.google.common.base.Strings;
import io.shardingsphere.core.api.yaml.YamlMasterSlaveDataSourceFactory;
import io.shardingsphere.core.api.yaml.YamlShardingDataSourceFactory;
import io.shardingsphere.dbtest.common.DatabaseUtil;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.dbtest.jaxb.assertion.ddl.DDLIntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.jaxb.dataset.expected.metadata.ExpectedColumn;
import io.shardingsphere.dbtest.jaxb.dataset.expected.metadata.ExpectedMetadataRoot;
import io.shardingsphere.test.sql.SQLCasesLoader;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public final class DDLAssertEngine {
    
    private final DDLIntegrateTestCaseAssertion assertion;
    
    private final DataSource dataSource;
    
    private final String sql;
    
    private final String expectedDataFile;
    
    public DDLAssertEngine(final String sqlCaseId, final String path, final DDLIntegrateTestCaseAssertion assertion, final Map<String, DataSource> dataSourceMap) throws IOException, SQLException {
        this.assertion = assertion;
        dataSource = createDataSource(dataSourceMap);
        sql = SQLCasesLoader.getInstance().getSupportedSQL(sqlCaseId);
        expectedDataFile = path.substring(0, path.lastIndexOf(File.separator) + 1) + "asserts/ddl/" + assertion.getExpectedDataFile();
    }
    
    private DataSource createDataSource(final Map<String, DataSource> dataSourceMap) throws SQLException, IOException {
        return "masterslaveonly".equals(assertion.getShardingRuleType())
                ? YamlMasterSlaveDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(assertion.getShardingRuleType())))
                : YamlShardingDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(assertion.getShardingRuleType())));
    }
    
    public void assertExecuteUpdateForPreparedStatement() throws SQLException, IOException, JAXBException {
        try (Connection connection = dataSource.getConnection()) {
            dropTableIfExisted(connection);
            if (!Strings.isNullOrEmpty(assertion.getInitSql())) {
                connection.prepareStatement(assertion.getInitSql()).executeUpdate();
            }
            connection.prepareStatement(sql.replaceAll("%s", "?")).executeUpdate();
            assertMetadata(connection);
            dropTableIfExisted(connection);
        }
    }
    
    public void assertExecuteForPreparedStatement() throws SQLException, IOException, JAXBException {
        try (Connection connection = dataSource.getConnection()) {
            dropTableIfExisted(connection);
            if (!Strings.isNullOrEmpty(assertion.getInitSql())) {
                connection.prepareStatement(assertion.getInitSql()).executeUpdate();
            }
            connection.prepareStatement(sql.replaceAll("%s", "?")).execute();
            assertMetadata(connection);
            dropTableIfExisted(connection);
        }
    }
    
    public void assertExecuteUpdateForStatement() throws SQLException, IOException, JAXBException {
        try (Connection connection = dataSource.getConnection()) {
            dropTableIfExisted(connection);
            if (!Strings.isNullOrEmpty(assertion.getInitSql())) {
                connection.prepareStatement(assertion.getInitSql()).executeUpdate();
            }
            connection.createStatement().executeUpdate(sql);
            assertMetadata(connection);
            dropTableIfExisted(connection);
        }
    }
    
    public void assertExecuteForStatement() throws SQLException, IOException, JAXBException {
        try (Connection connection = dataSource.getConnection()) {
            dropTableIfExisted(connection);
            if (!Strings.isNullOrEmpty(assertion.getInitSql())) {
                connection.prepareStatement(assertion.getInitSql()).executeUpdate();
            }
            connection.createStatement().execute(sql);
            assertMetadata(connection);
            dropTableIfExisted(connection);
        }
    }
    
    private void dropTableIfExisted(final Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("DROP TABLE %s", assertion.getTable()))) {
            preparedStatement.executeUpdate();
        } catch (final SQLException ex) {
        }
    }
    
    private void assertMetadata(final Connection connection) throws IOException, JAXBException, SQLException {
        ExpectedMetadataRoot expected;
        try (FileReader reader = new FileReader(expectedDataFile)) {
            expected = (ExpectedMetadataRoot) JAXBContext.newInstance(ExpectedMetadataRoot.class).createUnmarshaller().unmarshal(reader);
        }
        String tableName = assertion.getTable();
        List<ExpectedColumn> actualColumns = DatabaseUtil.getExpectedColumns(connection, tableName);
        DatabaseUtil.assertConfigs(expected.find(tableName), actualColumns);
    }
}
