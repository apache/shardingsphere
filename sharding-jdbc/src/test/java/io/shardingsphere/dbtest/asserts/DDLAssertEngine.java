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
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.dbtest.common.DatabaseUtil;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.dbtest.env.schema.SchemaEnvironmentManager;
import io.shardingsphere.dbtest.jaxb.assertion.ddl.DDLIntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.jaxb.dataset.expected.metadata.ExpectedColumn;
import io.shardingsphere.dbtest.jaxb.dataset.expected.metadata.ExpectedMetadataRoot;
import io.shardingsphere.test.sql.SQLCasesLoader;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public final class DDLAssertEngine {
    
    private final DDLIntegrateTestCaseAssertion integrateTestCaseAssertion;
    
    private final DatabaseType databaseType;
    
    private final DataSource dataSource;
    
    private final String sql;
    
    private final String expectedDataFile;
    
    public DDLAssertEngine(final String sqlCaseId, final String path, final DDLIntegrateTestCaseAssertion integrateTestCaseAssertion, final Map<String, DataSource> dataSourceMap,
                           final DatabaseType databaseType) throws IOException, SQLException {
        this.integrateTestCaseAssertion = integrateTestCaseAssertion;
        this.databaseType = databaseType;
        dataSource = createDataSource(dataSourceMap);
        sql = SQLCasesLoader.getInstance().getSupportedSQL(sqlCaseId);
        expectedDataFile = path.substring(0, path.lastIndexOf(File.separator) + 1) + "asserts/ddl/" + integrateTestCaseAssertion.getExpectedDataFile();
    }
    
    private DataSource createDataSource(final Map<String, DataSource> dataSourceMap) throws SQLException, IOException {
        return "masterslaveonly".equals(integrateTestCaseAssertion.getShardingRuleType())
                ? YamlMasterSlaveDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(integrateTestCaseAssertion.getShardingRuleType())))
                : YamlShardingDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(integrateTestCaseAssertion.getShardingRuleType())));
    }
    
    public void assertExecuteUpdateForPreparedStatement() throws SQLException, IOException, JAXBException {
        try {
            try (Connection connection = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(integrateTestCaseAssertion.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(integrateTestCaseAssertion.getShardingRuleType(), databaseType, integrateTestCaseAssertion.getCleanSql());
                }
                if (StringUtils.isNotBlank(integrateTestCaseAssertion.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(integrateTestCaseAssertion.getShardingRuleType(), databaseType, integrateTestCaseAssertion.getInitSql());
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql.replaceAll("%s", "?"))) {
                    preparedStatement.executeUpdate();
                }
                assertMetadata(connection);
            }
        } finally {
            if (StringUtils.isNotBlank(integrateTestCaseAssertion.getCleanSql())) {
                SchemaEnvironmentManager.executeSQL(integrateTestCaseAssertion.getShardingRuleType(), databaseType, integrateTestCaseAssertion.getCleanSql());
            }
            if (StringUtils.isNotBlank(integrateTestCaseAssertion.getInitSql())) {
                SchemaEnvironmentManager.executeSQL(integrateTestCaseAssertion.getShardingRuleType(), databaseType, integrateTestCaseAssertion.getInitSql());
            }
        }
    }
    
    public void assertExecuteForPreparedStatement() throws SQLException, IOException, JAXBException {
        try {
            try (Connection connection = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(integrateTestCaseAssertion.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(integrateTestCaseAssertion.getShardingRuleType(), databaseType, integrateTestCaseAssertion.getCleanSql());
                }
                if (StringUtils.isNotBlank(integrateTestCaseAssertion.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(integrateTestCaseAssertion.getShardingRuleType(), databaseType, integrateTestCaseAssertion.getInitSql());
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql.replaceAll("%s", "?"))) {
                    preparedStatement.execute();
                }
                assertMetadata(connection);
            }
        } finally {
            if (StringUtils.isNotBlank(integrateTestCaseAssertion.getCleanSql())) {
                SchemaEnvironmentManager.executeSQL(integrateTestCaseAssertion.getShardingRuleType(), databaseType, integrateTestCaseAssertion.getCleanSql());
            }
            if (StringUtils.isNotBlank(integrateTestCaseAssertion.getInitSql())) {
                SchemaEnvironmentManager.executeSQL(integrateTestCaseAssertion.getShardingRuleType(), databaseType, integrateTestCaseAssertion.getInitSql());
            }
        }
    }
    
    public void assertExecuteUpdateForStatement() throws SQLException, IOException, JAXBException {
        try {
            try (Connection connection = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(integrateTestCaseAssertion.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(integrateTestCaseAssertion.getShardingRuleType(), databaseType, integrateTestCaseAssertion.getCleanSql());
                }
                if (StringUtils.isNotBlank(integrateTestCaseAssertion.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(integrateTestCaseAssertion.getShardingRuleType(), databaseType, integrateTestCaseAssertion.getInitSql());
                }
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(sql);
                }
                assertMetadata(connection);
            }
        } finally {
            if (!Strings.isNullOrEmpty(integrateTestCaseAssertion.getCleanSql())) {
                SchemaEnvironmentManager.executeSQL(integrateTestCaseAssertion.getShardingRuleType(), databaseType, integrateTestCaseAssertion.getCleanSql());
            }
            if (!Strings.isNullOrEmpty(integrateTestCaseAssertion.getInitSql())) {
                SchemaEnvironmentManager.executeSQL(integrateTestCaseAssertion.getShardingRuleType(), databaseType, integrateTestCaseAssertion.getInitSql());
            }
        }
    }
    
    public void assertExecuteForStatement() throws SQLException, IOException, JAXBException {
        try {
            try (Connection connection = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(integrateTestCaseAssertion.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(integrateTestCaseAssertion.getShardingRuleType(), databaseType, integrateTestCaseAssertion.getCleanSql());
                }
                if (StringUtils.isNotBlank(integrateTestCaseAssertion.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(integrateTestCaseAssertion.getShardingRuleType(), databaseType, integrateTestCaseAssertion.getInitSql());
                }
                try (Statement statement = connection.createStatement()) {
                    statement.execute(sql);
                }
                assertMetadata(connection);
            }
        } finally {
            if (!Strings.isNullOrEmpty(integrateTestCaseAssertion.getCleanSql())) {
                SchemaEnvironmentManager.executeSQL(integrateTestCaseAssertion.getShardingRuleType(), databaseType, integrateTestCaseAssertion.getCleanSql());
            }
            if (!Strings.isNullOrEmpty(integrateTestCaseAssertion.getInitSql())) {
                SchemaEnvironmentManager.executeSQL(integrateTestCaseAssertion.getShardingRuleType(), databaseType, integrateTestCaseAssertion.getInitSql());
            }
        }
    }
    
    private void assertMetadata(final Connection connection) throws IOException, JAXBException, SQLException {
        ExpectedMetadataRoot expected;
        try (FileReader reader = new FileReader(expectedDataFile)) {
            expected = (ExpectedMetadataRoot) JAXBContext.newInstance(ExpectedMetadataRoot.class).createUnmarshaller().unmarshal(reader);
        }
        String tableName = integrateTestCaseAssertion.getTable();
        List<ExpectedColumn> actualColumns = DatabaseUtil.getExpectedColumns(connection, tableName);
        DatabaseUtil.assertConfigs(expected.find(tableName), actualColumns);
    }
}
