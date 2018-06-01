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
import io.shardingsphere.dbtest.config.bean.AssertSubDefinition;
import io.shardingsphere.dbtest.config.bean.DDLDataSetAssert;
import io.shardingsphere.dbtest.config.bean.ParameterDefinition;
import io.shardingsphere.dbtest.config.dataset.DataSetColumnMetadata;
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
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public final class DDLAssertEngine {
    
    private final DDLDataSetAssert ddlDataSetAssert;
    
    private final String shardingRuleType;
    
    private final DatabaseTypeEnvironment databaseTypeEnvironment;
    
    private final SQLCaseType caseType;
    
    private final DataSource dataSource;
    
    private final String rootPath;
    
    public DDLAssertEngine(final DDLDataSetAssert ddlDataSetAssert, final Map<String, DataSource> dataSourceMap,
                           final String shardingRuleType, final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) throws IOException, SQLException {
        this.ddlDataSetAssert = ddlDataSetAssert;
        this.shardingRuleType = shardingRuleType;
        this.databaseTypeEnvironment = databaseTypeEnvironment;
        this.caseType = caseType;
        dataSource = createDataSource(dataSourceMap);
        rootPath = ddlDataSetAssert.getPath().substring(0, ddlDataSetAssert.getPath().lastIndexOf(File.separator) + 1);
    }
    
    private DataSource createDataSource(final Map<String, DataSource> dataSourceMap) throws SQLException, IOException {
        return "masterslaveonly".equals(shardingRuleType)
                ? YamlMasterSlaveDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(shardingRuleType)))
                : YamlShardingDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(shardingRuleType)));
    }
    
    /**
     * Assert DDL.
     */
    public void assertDDL() throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        String rootSQL = SQLCasesLoader.getInstance().getSupportedSQL(ddlDataSetAssert.getId());
        String expectedDataFile = rootPath + "asserts/ddl/" + shardingRuleType + "/" + ddlDataSetAssert.getExpectedDataFile();
        if (!new File(expectedDataFile).exists()) {
            expectedDataFile = rootPath + "asserts/ddl/" + ddlDataSetAssert.getExpectedDataFile();
        }
        if (ddlDataSetAssert.getParameter().getValues().isEmpty()) {
            List<AssertSubDefinition> subAsserts = ddlDataSetAssert.getSubAsserts();
            if (subAsserts.isEmpty()) {
                doUpdateUseStatementToExecuteUpdate(expectedDataFile, ddlDataSetAssert, rootSQL);
                doUpdateUseStatementToExecute(expectedDataFile, ddlDataSetAssert, rootSQL);
                doUpdateUsePreparedStatementToExecuteUpdate(expectedDataFile, ddlDataSetAssert, rootSQL);
                doUpdateUsePreparedStatementToExecute(expectedDataFile, ddlDataSetAssert, rootSQL);
            } else {
                ddlSubRun(rootSQL, expectedDataFile, subAsserts);
            }
        } else {
            doUpdateUseStatementToExecuteUpdate(expectedDataFile, ddlDataSetAssert, rootSQL);
            doUpdateUseStatementToExecute(expectedDataFile, ddlDataSetAssert, rootSQL);
            doUpdateUsePreparedStatementToExecuteUpdate(expectedDataFile, ddlDataSetAssert, rootSQL);
            doUpdateUsePreparedStatementToExecute(expectedDataFile, ddlDataSetAssert, rootSQL);
            List<AssertSubDefinition> subAsserts = ddlDataSetAssert.getSubAsserts();
            if (!subAsserts.isEmpty()) {
                ddlSubRun(rootSQL, expectedDataFile, subAsserts);
            }
        }
    }
    
    private void ddlSubRun(final String rootSQL, final String expectedDataFile, final List<AssertSubDefinition> subAsserts) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        for (AssertSubDefinition each : subAsserts) {
            if (isSkip(each)) {
                continue;
            }
            String expectedDataFileSub = each.getExpectedDataFile();
            ParameterDefinition parameter = each.getParameter();
            String expectedDataFileTmp = expectedDataFile;
            if (StringUtils.isBlank(expectedDataFileSub)) {
                expectedDataFileSub = ddlDataSetAssert.getExpectedDataFile();
            } else {
                expectedDataFileTmp = rootPath + "asserts/ddl/" + shardingRuleType + "/" + expectedDataFileSub;
                if (!new File(expectedDataFileTmp).exists()) {
                    expectedDataFileTmp = rootPath + "asserts/ddl/" + expectedDataFileSub;
                }
            }
            if (null == parameter) {
                parameter = ddlDataSetAssert.getParameter();
            }
            DDLDataSetAssert anAssertSub = new DDLDataSetAssert(
                    ddlDataSetAssert.getId(), ddlDataSetAssert.getInitSql(), ddlDataSetAssert.getShardingRuleTypes(), ddlDataSetAssert.getDatabaseTypes(), ddlDataSetAssert.getCleanSql(), expectedDataFileSub, ddlDataSetAssert.getTable(), 
                    parameter, ddlDataSetAssert.getSubAsserts(), "");
            doUpdateUseStatementToExecuteUpdate(expectedDataFileTmp, anAssertSub, rootSQL);
            doUpdateUseStatementToExecute(expectedDataFileTmp, anAssertSub, rootSQL);
            doUpdateUsePreparedStatementToExecuteUpdate(expectedDataFileTmp, anAssertSub, rootSQL);
            doUpdateUsePreparedStatementToExecute(expectedDataFileTmp, anAssertSub, rootSQL);
        }
    }
    
    private boolean isSkip(final AssertSubDefinition assertSubDefinition) {
        for (String each : StringUtils.split(assertSubDefinition.getShardingRuleTypes(), ",")) {
            if (shardingRuleType.equals(each)) {
                return false;
            }
        }
        return true;
    }
    
    private void doUpdateUsePreparedStatementToExecute(final String expectedDataFile, final DDLDataSetAssert ddlDataSetAssert, final String rootSQL) 
            throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        try {
            try (Connection con = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(ddlDataSetAssert.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlDataSetAssert.getCleanSql());
                }
                if (StringUtils.isNotBlank(ddlDataSetAssert.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlDataSetAssert.getInitSql());
                }
                DatabaseUtil.updateUsePreparedStatementToExecute(con, rootSQL, ddlDataSetAssert.getParameter());
                DataSetDefinitions checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                String table = ddlDataSetAssert.getTable();
                List<DataSetColumnMetadata> columnDefinitions = DatabaseUtil.getColumnDefinitions(con, table);
                DatabaseUtil.assertConfigs(checkDataset, columnDefinitions, table);
            }
        } finally {
            if (StringUtils.isNotBlank(ddlDataSetAssert.getCleanSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlDataSetAssert.getCleanSql());
            }
            if (StringUtils.isNotBlank(ddlDataSetAssert.getInitSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), ddlDataSetAssert.getInitSql());
            }
        }
    }
    
    private void doUpdateUsePreparedStatementToExecuteUpdate(final String expectedDataFile, final DDLDataSetAssert anAssert, final String rootSQL) 
            throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        try {
            try (Connection connection = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(anAssert.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getCleanSql());
                }
                if (StringUtils.isNotBlank(anAssert.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getInitSql());
                }
                DatabaseUtil.updateUsePreparedStatementToExecuteUpdate(connection, rootSQL,
                        anAssert.getParameter());
                DataSetDefinitions checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                String table = anAssert.getTable();
                List<DataSetColumnMetadata> columnDefinitions = DatabaseUtil.getColumnDefinitions(connection, table);
                DatabaseUtil.assertConfigs(checkDataset, columnDefinitions, table);
            }
        } finally {
            if (StringUtils.isNotBlank(anAssert.getCleanSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getCleanSql());
            }
            if (StringUtils.isNotBlank(anAssert.getInitSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getInitSql());
            }
        }
    }
    
    private void doUpdateUseStatementToExecute(final String expectedDataFile, final DDLDataSetAssert anAssert, final String rootSQL) 
            throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        try {
            try (Connection connection = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(anAssert.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getCleanSql());
                }
                if (StringUtils.isNotBlank(anAssert.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getInitSql());
                }
                DatabaseUtil.updateUseStatementToExecute(connection, rootSQL, anAssert.getParameter());
                DataSetDefinitions checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                String table = anAssert.getTable();
                List<DataSetColumnMetadata> columnDefinitions = DatabaseUtil.getColumnDefinitions(connection, table);
                DatabaseUtil.assertConfigs(checkDataset, columnDefinitions, table);
            }
        } finally {
            if (!Strings.isNullOrEmpty(anAssert.getCleanSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getCleanSql());
            }
            if (!Strings.isNullOrEmpty(anAssert.getInitSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getInitSql());
            }
        }
    }
    
    private void doUpdateUseStatementToExecuteUpdate(final String expectedDataFile, final DDLDataSetAssert anAssert, final String rootSQL) 
            throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        try {
            try (Connection connection = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(anAssert.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getCleanSql());
                }
                if (StringUtils.isNotBlank(anAssert.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getInitSql());
                }
                DatabaseUtil.updateUseStatementToExecuteUpdate(connection, rootSQL, anAssert.getParameter());
                DataSetDefinitions checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                String table = anAssert.getTable();
                List<DataSetColumnMetadata> columnDefinitions = DatabaseUtil.getColumnDefinitions(connection, table);
                DatabaseUtil.assertConfigs(checkDataset, columnDefinitions, table);
            }
        } finally {
            if (!Strings.isNullOrEmpty(anAssert.getCleanSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getCleanSql());
            }
            if (!Strings.isNullOrEmpty(anAssert.getInitSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getInitSql());
            }
        }
    }
}
