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
import io.shardingsphere.dbtest.config.bean.DatasetDefinition;
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
    
    private final DataSetEnvironmentManager dataSetEnvironmentManager;
    
    public DDLAssertEngine(final DataSetEnvironmentManager dataSetEnvironmentManager, final DDLDataSetAssert ddlDataSetAssert, final Map<String, DataSource> dataSourceMap,
                           final String shardingRuleType, final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) throws IOException, SQLException {
        this.ddlDataSetAssert = ddlDataSetAssert;
        this.shardingRuleType = shardingRuleType;
        this.databaseTypeEnvironment = databaseTypeEnvironment;
        this.caseType = caseType;
        dataSource = createDataSource(dataSourceMap);
        rootPath = ddlDataSetAssert.getPath().substring(0, ddlDataSetAssert.getPath().lastIndexOf(File.separator) + 1);
        this.dataSetEnvironmentManager = dataSetEnvironmentManager;
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
                doUpdateUseStatementToExecuteUpdateDDL(expectedDataFile, ddlDataSetAssert, rootSQL);
                doUpdateUseStatementToExecuteDDL(expectedDataFile, ddlDataSetAssert, rootSQL);
                doUpdateUsePreparedStatementToExecuteUpdateDDL(expectedDataFile, ddlDataSetAssert, rootSQL);
                doUpdateUsePreparedStatementToExecuteDDL(expectedDataFile, ddlDataSetAssert, rootSQL);
            } else {
                ddlSubRun(ddlDataSetAssert, rootSQL, expectedDataFile, subAsserts);
            }
        } else {
            doUpdateUseStatementToExecuteUpdateDDL(expectedDataFile, ddlDataSetAssert, rootSQL);
            doUpdateUseStatementToExecuteDDL(expectedDataFile, ddlDataSetAssert, rootSQL);
            doUpdateUsePreparedStatementToExecuteUpdateDDL(expectedDataFile, ddlDataSetAssert, rootSQL);
            doUpdateUsePreparedStatementToExecuteDDL(expectedDataFile, ddlDataSetAssert, rootSQL);
            List<AssertSubDefinition> subAsserts = ddlDataSetAssert.getSubAsserts();
            if (!subAsserts.isEmpty()) {
                ddlSubRun(ddlDataSetAssert, rootSQL, expectedDataFile, subAsserts);
            }
        }
    }
    
    private void ddlSubRun(final DDLDataSetAssert anAssert, final String rootSQL, final String expectedDataFile, final List<AssertSubDefinition> subAsserts) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        for (AssertSubDefinition each : subAsserts) {
            if (isSkip(each)) {
                continue;
            }
            String expectedDataFileSub = each.getExpectedDataFile();
            ParameterDefinition parameter = each.getParameter();
            String expectedDataFileTmp = expectedDataFile;
            if (StringUtils.isBlank(expectedDataFileSub)) {
                expectedDataFileSub = anAssert.getExpectedDataFile();
            } else {
                expectedDataFileTmp = rootPath + "asserts/ddl/" + shardingRuleType + "/" + expectedDataFileSub;
                if (!new File(expectedDataFileTmp).exists()) {
                    expectedDataFileTmp = rootPath + "asserts/ddl/" + expectedDataFileSub;
                }
            }
            if (null == parameter) {
                parameter = anAssert.getParameter();
            }
            DDLDataSetAssert anAssertSub = new DDLDataSetAssert(
                    anAssert.getId(), anAssert.getInitSql(), anAssert.getShardingRuleTypes(), anAssert.getDatabaseTypes(), anAssert.getCleanSql(), expectedDataFileSub, anAssert.getTable(), 
                    parameter, anAssert.getSubAsserts(), "");
            doUpdateUseStatementToExecuteUpdateDDL(expectedDataFileTmp, anAssertSub, rootSQL);
            doUpdateUseStatementToExecuteDDL(expectedDataFileTmp, anAssertSub, rootSQL);
            doUpdateUsePreparedStatementToExecuteUpdateDDL(expectedDataFileTmp, anAssertSub, rootSQL);
            doUpdateUsePreparedStatementToExecuteDDL(expectedDataFileTmp, anAssertSub, rootSQL);
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
    
    private void doUpdateUsePreparedStatementToExecuteDDL(final String expectedDataFile, final DDLDataSetAssert anAssert, final String rootsql) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        try {
            try (Connection con = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(anAssert.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getCleanSql());
                }
                if (StringUtils.isNotBlank(anAssert.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getInitSql());
                }
                DatabaseUtil.updateUsePreparedStatementToExecute(con, rootsql,
                        anAssert.getParameter());
                DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                
                String table = anAssert.getTable();
                List<DataSetColumnMetadata> columnDefinitions = DatabaseUtil.getColumnDefinitions(con, table);
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
    
    private void doUpdateUsePreparedStatementToExecuteUpdateDDL(final String expectedDataFile, final DDLDataSetAssert anAssert, final String rootSQL) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        try {
            try (Connection con = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(anAssert.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getCleanSql());
                }
                if (StringUtils.isNotBlank(anAssert.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getInitSql());
                }
                DatabaseUtil.updateUsePreparedStatementToExecuteUpdate(con, rootSQL,
                        anAssert.getParameter());
                DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                String table = anAssert.getTable();
                List<DataSetColumnMetadata> columnDefinitions = DatabaseUtil.getColumnDefinitions(con, table);
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
    
    private void doUpdateUseStatementToExecuteDDL(final String expectedDataFile, final DDLDataSetAssert anAssert, final String rootSQL) throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        try {
            try (Connection con = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(anAssert.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getCleanSql());
                }
                if (StringUtils.isNotBlank(anAssert.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getInitSql());
                }
                DatabaseUtil.updateUseStatementToExecute(con, rootSQL, anAssert.getParameter());
                DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                String table = anAssert.getTable();
                List<DataSetColumnMetadata> columnDefinitions = DatabaseUtil.getColumnDefinitions(con, table);
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
    
    private void doUpdateUseStatementToExecuteUpdateDDL(final String expectedDataFile, final DDLDataSetAssert anAssert, final String rootSQL) throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        try {
            try (Connection con = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(anAssert.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getCleanSql());
                }
                if (StringUtils.isNotBlank(anAssert.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseTypeEnvironment.getDatabaseType(), anAssert.getInitSql());
                }
                DatabaseUtil.updateUseStatementToExecuteUpdate(con, rootSQL, anAssert.getParameter());
                DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                String table = anAssert.getTable();
                List<DataSetColumnMetadata> columnDefinitions = DatabaseUtil.getColumnDefinitions(con, table);
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
