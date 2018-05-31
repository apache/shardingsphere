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

import io.shardingsphere.core.api.yaml.YamlMasterSlaveDataSourceFactory;
import io.shardingsphere.core.api.yaml.YamlShardingDataSourceFactory;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.dbtest.common.DatabaseUtil;
import io.shardingsphere.dbtest.config.DataSetsParser;
import io.shardingsphere.dbtest.config.bean.AssertSubDefinition;
import io.shardingsphere.dbtest.config.bean.DQLDataSetAssert;
import io.shardingsphere.dbtest.config.bean.DatasetDatabase;
import io.shardingsphere.dbtest.config.bean.DatasetDefinition;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.dbtest.env.datasource.DataSourceUtil;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * DQL assert engine.
 * 
 * @author zhangliang 
 */
public final class DQLAssertEngine {
    
    private final DQLDataSetAssert dqlDataSetAssert;
    
    private final String shardingRuleType;
    
    private final SQLCaseType caseType;
    
    private final DataSource dataSource;
    
    private final String rootPath;
    
    private final DataSetEnvironmentManager dataSetEnvironmentManager;
    
    public DQLAssertEngine(
            final DQLDataSetAssert dqlDataSetAssert, final String shardingRuleType, final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) throws IOException, JAXBException, SQLException {
        this.dqlDataSetAssert = dqlDataSetAssert;
        this.shardingRuleType = shardingRuleType;
        this.caseType = caseType;
        Map<String, DataSource> dataSourceMap = createDataSourceMap(SchemaEnvironmentManager.getDataSourceNames(shardingRuleType), databaseTypeEnvironment.getDatabaseType());
        dataSource = createDataSource(dataSourceMap);
        rootPath = dqlDataSetAssert.getPath().substring(0, dqlDataSetAssert.getPath().lastIndexOf(File.separator) + 1);
        dataSetEnvironmentManager = new DataSetEnvironmentManager(EnvironmentPath.getDataInitializeResourceFile(shardingRuleType), dataSourceMap);
    }
    
    private Map<String, DataSource> createDataSourceMap(final Collection<String> dataSourceNames, final DatabaseType databaseType) {
        Map<String, DataSource> result = new HashMap<>(dataSourceNames.size(), 1);
        for (String each : dataSourceNames) {
            result.put(each, DataSourceUtil.createDataSource(databaseType, each));
        }
        return result;
    }
    
    private DataSource createDataSource(final Map<String, DataSource> dataSourceMap) throws SQLException, IOException {
        return "masterslaveonly".equals(shardingRuleType)
                ? YamlMasterSlaveDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(shardingRuleType)))
                : YamlShardingDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(shardingRuleType)));
    }
    
    /**
     * Assert DQL.
     * 
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     * @throws SQLException
     * @throws ParseException
     */
    public void assertDQL() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, SQLException, ParseException {
        String rootSQL = SQLCasesLoader.getInstance().getSupportedSQL(dqlDataSetAssert.getId());
        String expectedDataFile = getExpectedDataFile(dqlDataSetAssert.getExpectedDataFile());
        try {
            dataSetEnvironmentManager.initialize();
            if (dqlDataSetAssert.getSubAsserts().isEmpty()) {
                doSelectUsePreparedStatement(expectedDataFile, dqlDataSetAssert, rootSQL);
                doSelectUsePreparedStatementToExecuteSelect(expectedDataFile, dqlDataSetAssert, rootSQL);
                doSelectUseStatement(expectedDataFile, dqlDataSetAssert, rootSQL);
                doSelectUseStatementToExecuteSelect(expectedDataFile, dqlDataSetAssert, rootSQL);
            } else {
                subAssertDQL(dqlDataSetAssert, rootSQL);
            }
        } finally {
            dataSetEnvironmentManager.clear();
        }
    }
    
    private void subAssertDQL(final DQLDataSetAssert dqlDataSetAssert, final String rootSQL) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        for (AssertSubDefinition each : dqlDataSetAssert.getSubAsserts()) {
            if (isSkip(each)) {
                continue;
            }
            String expectedDataFile = getExpectedDataFile(each.getExpectedDataFile());
            DQLDataSetAssert anAssertSub = new DQLDataSetAssert(
                    dqlDataSetAssert.getId(), expectedDataFile, dqlDataSetAssert.getShardingRuleTypes(), dqlDataSetAssert.getDatabaseTypes(), each.getParameter(), dqlDataSetAssert.getSubAsserts(), "");
            doSelectUsePreparedStatement(expectedDataFile, anAssertSub, rootSQL);
            doSelectUsePreparedStatementToExecuteSelect(expectedDataFile, anAssertSub, rootSQL);
            doSelectUseStatement(expectedDataFile, anAssertSub, rootSQL);
            doSelectUseStatementToExecuteSelect(expectedDataFile, anAssertSub, rootSQL);
        }
    }
    
    private String getExpectedDataFile(final String expectedDataFile) {
        String result = rootPath + "asserts/dql/" + shardingRuleType + "/" + expectedDataFile;
        if (!new File(result).exists()) {
            result = rootPath + "asserts/dql/" + expectedDataFile;
        }
        return result;
    }
    
    private boolean isSkip(final AssertSubDefinition assertSubDefinition) {
        for (String each : StringUtils.split(assertSubDefinition.getShardingRuleTypes(), ",")) {
            if (shardingRuleType.equals(each)) {
                return false;
            }
        }
        return true;
    }
    
    private void doSelectUseStatement(final String expectedDataFile, final DQLDataSetAssert anAssert, final String rootSQL) throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try (Connection connection = dataSource.getConnection()) {
            DatasetDatabase datasetDatabase = DatabaseUtil.selectUseStatement(connection, rootSQL, anAssert.getParameter());
            DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
            DatabaseUtil.assertDatas(checkDataset, datasetDatabase);
        }
    }
    
    private void doSelectUseStatementToExecuteSelect(final String expectedDataFile, final DQLDataSetAssert anAssert, final String rootSQL) throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try (Connection connection = dataSource.getConnection()) {
            DatasetDatabase datasetDatabase = DatabaseUtil.selectUseStatementToExecuteSelect(connection, rootSQL, anAssert.getParameter());
            DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
            DatabaseUtil.assertDatas(checkDataset, datasetDatabase);
        }
    }
    
    private void doSelectUsePreparedStatement(final String expectedDataFile, final DQLDataSetAssert anAssert, final String rootSQL) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try (Connection connection = dataSource.getConnection()) {
            DatasetDatabase ddPreparedStatement = DatabaseUtil.selectUsePreparedStatement(connection, rootSQL, anAssert.getParameter());
            DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
            DatabaseUtil.assertDatas(checkDataset, ddPreparedStatement);
        }
    }
    
    private void doSelectUsePreparedStatementToExecuteSelect(final String expectedDataFile, final DQLDataSetAssert anAssert, final String rootSQL) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try (Connection connection = dataSource.getConnection()) {
            DatasetDatabase datasetDatabase = DatabaseUtil.selectUsePreparedStatementToExecuteSelect(connection, rootSQL, anAssert.getParameter());
            DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
            DatabaseUtil.assertDatas(checkDataset, datasetDatabase);
        }
    }
}
