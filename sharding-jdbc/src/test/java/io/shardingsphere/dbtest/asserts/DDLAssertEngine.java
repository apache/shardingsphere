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
import io.shardingsphere.dbtest.config.DataSetsParser;
import io.shardingsphere.dbtest.config.bean.DDLSubAssert;
import io.shardingsphere.dbtest.config.dataset.init.DataSetColumnMetadata;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.dbtest.env.schema.SchemaEnvironmentManager;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public final class DDLAssertEngine {
    
    private final DDLSubAssert ddlSubAssert;
    
    private final String shardingRuleType;
    
    private final DatabaseTypeEnvironment databaseTypeEnvironment;
    
    private final SQLCaseType caseType;
    
    private final DataSource dataSource;
    
    private final String sql;
    
    private final String expectedDataFile;
    
    public DDLAssertEngine(final String sqlCaseId, final String path, final DDLSubAssert ddlSubAssert, final Map<String, DataSource> dataSourceMap,
                           final String shardingRuleType, final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) throws IOException, SQLException {
        this.ddlSubAssert = ddlSubAssert;
        this.shardingRuleType = shardingRuleType;
        this.databaseTypeEnvironment = databaseTypeEnvironment;
        this.caseType = caseType;
        dataSource = createDataSource(dataSourceMap);
        sql = SQLCasesLoader.getInstance().getSupportedSQL(sqlCaseId);
        expectedDataFile = path.substring(0, path.lastIndexOf(File.separator) + 1) + "asserts/ddl/" + ddlSubAssert.getExpectedDataFile();
    }
    
    private DataSource createDataSource(final Map<String, DataSource> dataSourceMap) throws SQLException, IOException {
        return "masterslaveonly".equals(shardingRuleType)
                ? YamlMasterSlaveDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(shardingRuleType)))
                : YamlShardingDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(shardingRuleType)));
    }
    
    /**
     * Assert DDL.
     */
    public void assertDDL() throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        doUpdateUseStatementToExecuteUpdate();
        doUpdateUseStatementToExecute();
        doUpdateUsePreparedStatementToExecuteUpdate();
        doUpdateUsePreparedStatementToExecute();
    }
    
    private void doUpdateUsePreparedStatementToExecute() throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        try {
            try (Connection connection = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(ddlSubAssert.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlSubAssert.getCleanSql());
                }
                if (StringUtils.isNotBlank(ddlSubAssert.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlSubAssert.getInitSql());
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql.replaceAll("%s", "?"))) {
                    preparedStatement.execute();
                }
                DataSetDefinitions checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                String table = ddlSubAssert.getTable();
                List<DataSetColumnMetadata> columnDefinitions = DatabaseUtil.getColumnDefinitions(connection, table);
                DatabaseUtil.assertConfigs(checkDataset, columnDefinitions, table);
            }
        } finally {
            if (StringUtils.isNotBlank(ddlSubAssert.getCleanSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlSubAssert.getCleanSql());
            }
            if (StringUtils.isNotBlank(ddlSubAssert.getInitSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlSubAssert.getInitSql());
            }
        }
    }
    
    private void doUpdateUsePreparedStatementToExecuteUpdate() throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        try {
            try (Connection connection = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(ddlSubAssert.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlSubAssert.getCleanSql());
                }
                if (StringUtils.isNotBlank(ddlSubAssert.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlSubAssert.getInitSql());
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql.replaceAll("%s", "?"))) {
                    preparedStatement.executeUpdate();
                }
                DataSetDefinitions checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                String table = ddlSubAssert.getTable();
                List<DataSetColumnMetadata> columnDefinitions = DatabaseUtil.getColumnDefinitions(connection, table);
                DatabaseUtil.assertConfigs(checkDataset, columnDefinitions, table);
            }
        } finally {
            if (StringUtils.isNotBlank(ddlSubAssert.getCleanSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlSubAssert.getCleanSql());
            }
            if (StringUtils.isNotBlank(ddlSubAssert.getInitSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlSubAssert.getInitSql());
            }
        }
    }
    
    private void doUpdateUseStatementToExecute() throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        try {
            try (Connection connection = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(ddlSubAssert.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlSubAssert.getCleanSql());
                }
                if (StringUtils.isNotBlank(ddlSubAssert.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlSubAssert.getInitSql());
                }
                try (Statement statement = connection.createStatement()) {
                    statement.execute(sql);
                }
                DataSetDefinitions checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                String table = ddlSubAssert.getTable();
                List<DataSetColumnMetadata> columnDefinitions = DatabaseUtil.getColumnDefinitions(connection, table);
                DatabaseUtil.assertConfigs(checkDataset, columnDefinitions, table);
            }
        } finally {
            if (!Strings.isNullOrEmpty(ddlSubAssert.getCleanSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlSubAssert.getCleanSql());
            }
            if (!Strings.isNullOrEmpty(ddlSubAssert.getInitSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlSubAssert.getInitSql());
            }
        }
    }
    
    private void doUpdateUseStatementToExecuteUpdate() throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        try {
            try (Connection connection = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(ddlSubAssert.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlSubAssert.getCleanSql());
                }
                if (StringUtils.isNotBlank(ddlSubAssert.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlSubAssert.getInitSql());
                }
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(sql);
                }
                DataSetDefinitions checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                String table = ddlSubAssert.getTable();
                List<DataSetColumnMetadata> columnDefinitions = DatabaseUtil.getColumnDefinitions(connection, table);
                DatabaseUtil.assertConfigs(checkDataset, columnDefinitions, table);
            }
        } finally {
            if (!Strings.isNullOrEmpty(ddlSubAssert.getCleanSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlSubAssert.getCleanSql());
            }
            if (!Strings.isNullOrEmpty(ddlSubAssert.getInitSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlSubAssert.getInitSql());
            }
        }
    }
}
