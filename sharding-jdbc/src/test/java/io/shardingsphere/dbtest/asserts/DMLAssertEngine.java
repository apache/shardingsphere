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
import io.shardingsphere.dbtest.common.DatabaseUtil;
import io.shardingsphere.dbtest.config.DataSetsParser;
import io.shardingsphere.dbtest.config.bean.AssertSubDefinition;
import io.shardingsphere.dbtest.config.bean.DMLDataSetAssert;
import io.shardingsphere.dbtest.config.bean.DatasetDatabase;
import io.shardingsphere.dbtest.config.bean.DatasetDefinition;
import io.shardingsphere.dbtest.config.bean.ParameterDefinition;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public final class DMLAssertEngine {
    
    private final DMLDataSetAssert dmlDataSetAssert;
    
    private final String shardingRuleType;
    
    private final SQLCaseType caseType;
    
    private final DataSource dataSource;
    
    private final String rootPath;
    
    private final DataSetEnvironmentManager dataSetEnvironmentManager;
    
    public DMLAssertEngine(final DataSetEnvironmentManager dataSetEnvironmentManager, final DMLDataSetAssert dmlDataSetAssert, final Map<String, DataSource> dataSourceMap,
                           final String shardingRuleType, final SQLCaseType caseType) throws IOException, SQLException {
        this.dmlDataSetAssert = dmlDataSetAssert;
        this.shardingRuleType = shardingRuleType;
        this.caseType = caseType;
        dataSource = createDataSource(dataSourceMap);
        rootPath = dmlDataSetAssert.getPath().substring(0, dmlDataSetAssert.getPath().lastIndexOf(File.separator) + 1);
        this.dataSetEnvironmentManager = dataSetEnvironmentManager;
    }
    
    private DataSource createDataSource(final Map<String, DataSource> dataSourceMap) throws SQLException, IOException {
        return "masterslaveonly".equals(shardingRuleType)
                ? YamlMasterSlaveDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(shardingRuleType)))
                : YamlShardingDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(shardingRuleType)));
    }
    
    /**
     * Assert DML.
     */
    public void assertDML() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, SQLException, ParseException {
        String rootSQL = SQLCasesLoader.getInstance().getSupportedSQL(dmlDataSetAssert.getId());
        String expectedDataFile = rootPath + "asserts/dml/" + shardingRuleType + "/" + dmlDataSetAssert.getExpectedDataFile();
        if (!new File(expectedDataFile).exists()) {
            expectedDataFile = rootPath + "asserts/dml/" + dmlDataSetAssert.getExpectedDataFile();
        }
        int resultDoUpdateUseStatementToExecuteUpdate = 0;
        int resultDoUpdateUseStatementToExecute = 0;
        int resultDoUpdateUsePreparedStatementToExecuteUpdate = 0;
        int resultDoUpdateUsePreparedStatementToExecute = 0;
        if (dmlDataSetAssert.getParameter().getValues().isEmpty()) {
            List<AssertSubDefinition> subAsserts = dmlDataSetAssert.getSubAsserts();
            if (subAsserts.isEmpty()) {
                resultDoUpdateUseStatementToExecuteUpdate = resultDoUpdateUseStatementToExecuteUpdate + doUpdateUseStatementToExecuteUpdate(expectedDataFile, dmlDataSetAssert, rootSQL);
                resultDoUpdateUseStatementToExecute = resultDoUpdateUseStatementToExecute + doUpdateUseStatementToExecute(expectedDataFile, dmlDataSetAssert, rootSQL);
                resultDoUpdateUsePreparedStatementToExecuteUpdate = resultDoUpdateUsePreparedStatementToExecuteUpdate + doUpdateUsePreparedStatementToExecuteUpdate(expectedDataFile, dmlDataSetAssert, rootSQL);
                resultDoUpdateUsePreparedStatementToExecute = resultDoUpdateUsePreparedStatementToExecute + doUpdateUsePreparedStatementToExecute(expectedDataFile, dmlDataSetAssert, rootSQL);
            } else {
                for (AssertSubDefinition subAssert : subAsserts) {
                    String baseConfigSub = subAssert.getShardingRuleTypes();
                    if (StringUtils.isNotBlank(baseConfigSub)) {
                        String[] baseConfigs = StringUtils.split(baseConfigSub, ",");
                        boolean flag = true;
                        for (String config : baseConfigs) {
                            if (shardingRuleType.equals(config)) {
                                flag = false;
                            }
                        }
                        //Skip use cases that do not need to run
                        if (flag) {
                            continue;
                        }
                    }
                    String expectedDataFileSub = subAssert.getExpectedDataFile();
                    ParameterDefinition parameter = subAssert.getParameter();
                    ParameterDefinition expectedParameter = subAssert.getExpectedParameter();
                    String expectedDataFileTmp = expectedDataFile;
                    if (StringUtils.isBlank(expectedDataFileSub)) {
                        expectedDataFileSub = dmlDataSetAssert.getExpectedDataFile();
                    } else {
                        expectedDataFileTmp = rootPath + "asserts/dml/" + shardingRuleType + "/" + expectedDataFileSub;
                        if (!new File(expectedDataFileTmp).exists()) {
                            expectedDataFileTmp = rootPath + "asserts/dml/" + expectedDataFileSub;
                        }
                    }
                    if (null == parameter) {
                        parameter = dmlDataSetAssert.getParameter();
                    }
                    if (null == expectedParameter) {
                        expectedParameter = dmlDataSetAssert.getParameter();
                    }
                    DMLDataSetAssert anAssertSub = new DMLDataSetAssert(
                            dmlDataSetAssert.getId(), expectedDataFileSub, dmlDataSetAssert.getShardingRuleTypes(), dmlDataSetAssert.getDatabaseTypes(), subAssert.getExpectedUpdate(),
                            dmlDataSetAssert.getExpectedSql(), parameter, expectedParameter, dmlDataSetAssert.getSubAsserts(), "");
                    resultDoUpdateUseStatementToExecuteUpdate = resultDoUpdateUseStatementToExecuteUpdate + doUpdateUseStatementToExecuteUpdate(expectedDataFileTmp, anAssertSub, rootSQL);
                    resultDoUpdateUseStatementToExecute = resultDoUpdateUseStatementToExecute + doUpdateUseStatementToExecute(expectedDataFileTmp, anAssertSub, rootSQL);
                    resultDoUpdateUsePreparedStatementToExecuteUpdate = resultDoUpdateUsePreparedStatementToExecuteUpdate + doUpdateUsePreparedStatementToExecuteUpdate(expectedDataFileTmp, anAssertSub, rootSQL);
                    resultDoUpdateUsePreparedStatementToExecute = resultDoUpdateUsePreparedStatementToExecute + doUpdateUsePreparedStatementToExecute(expectedDataFileTmp, anAssertSub, rootSQL);
                }
            }
        } else {
            resultDoUpdateUseStatementToExecuteUpdate = resultDoUpdateUseStatementToExecuteUpdate + doUpdateUseStatementToExecuteUpdate(expectedDataFile, dmlDataSetAssert, rootSQL);
            resultDoUpdateUseStatementToExecute = resultDoUpdateUseStatementToExecute + doUpdateUseStatementToExecute(expectedDataFile, dmlDataSetAssert, rootSQL);
            resultDoUpdateUsePreparedStatementToExecuteUpdate = resultDoUpdateUsePreparedStatementToExecuteUpdate + doUpdateUsePreparedStatementToExecuteUpdate(expectedDataFile, dmlDataSetAssert, rootSQL);
            resultDoUpdateUsePreparedStatementToExecute = resultDoUpdateUsePreparedStatementToExecute + doUpdateUsePreparedStatementToExecute(expectedDataFile, dmlDataSetAssert, rootSQL);
            List<AssertSubDefinition> subAsserts = dmlDataSetAssert.getSubAsserts();
            if (!subAsserts.isEmpty()) {
                for (AssertSubDefinition subAssert : subAsserts) {
                    String baseConfigSub = subAssert.getShardingRuleTypes();
                    if (StringUtils.isNotBlank(baseConfigSub)) {
                        String[] baseConfigs = StringUtils.split(baseConfigSub, ",");
                        boolean flag = true;
                        for (String config : baseConfigs) {
                            if (shardingRuleType.equals(config)) {
                                flag = false;
                            }
                        }
                        //Skip use cases that do not need to run
                        if (flag) {
                            continue;
                        }
                    }
                    String expectedDataFileSub = subAssert.getExpectedDataFile();
                    ParameterDefinition parameter = subAssert.getParameter();
                    ParameterDefinition expectedParameter = subAssert.getExpectedParameter();
                    String expectedDataFileTmp = expectedDataFile;
                    if (StringUtils.isBlank(expectedDataFileSub)) {
                        expectedDataFileSub = dmlDataSetAssert.getExpectedDataFile();
                    } else {
                        expectedDataFileTmp = rootPath + "asserts/dml/" + shardingRuleType + "/" + expectedDataFileSub;
                        if (!new File(expectedDataFileTmp).exists()) {
                            expectedDataFileTmp = rootPath + "asserts/dml/" + expectedDataFileSub;
                        }
                    }
                    if (null == parameter) {
                        parameter = dmlDataSetAssert.getParameter();
                    }
                    if (null == expectedParameter) {
                        expectedParameter = dmlDataSetAssert.getParameter();
                    }
                    DMLDataSetAssert anAssertSub = new DMLDataSetAssert(
                            dmlDataSetAssert.getId(), expectedDataFileSub, dmlDataSetAssert.getShardingRuleTypes(), dmlDataSetAssert.getDatabaseTypes(), subAssert.getExpectedUpdate(), 
                            dmlDataSetAssert.getExpectedSql(), parameter, expectedParameter, dmlDataSetAssert.getSubAsserts(), "");
                    resultDoUpdateUseStatementToExecuteUpdate = resultDoUpdateUseStatementToExecuteUpdate + doUpdateUseStatementToExecuteUpdate(expectedDataFileTmp, anAssertSub, rootSQL);
                    resultDoUpdateUseStatementToExecute = resultDoUpdateUseStatementToExecute + doUpdateUseStatementToExecute(expectedDataFileTmp, anAssertSub, rootSQL);
                    resultDoUpdateUsePreparedStatementToExecuteUpdate = resultDoUpdateUsePreparedStatementToExecuteUpdate + doUpdateUsePreparedStatementToExecuteUpdate(expectedDataFileTmp, anAssertSub, rootSQL);
                    resultDoUpdateUsePreparedStatementToExecute = resultDoUpdateUsePreparedStatementToExecute + doUpdateUsePreparedStatementToExecute(expectedDataFileTmp, anAssertSub, rootSQL);
                }
            }
        }
        if (null != dmlDataSetAssert.getExpectedUpdate()) {
            Assert.assertEquals("Update row number error UpdateUseStatementToExecuteUpdate", dmlDataSetAssert.getExpectedUpdate().intValue(), resultDoUpdateUseStatementToExecuteUpdate);
            Assert.assertEquals("Update row number error UpdateUseStatementToExecute", dmlDataSetAssert.getExpectedUpdate().intValue(), resultDoUpdateUseStatementToExecute);
            Assert.assertEquals("Update row number error UpdateUsePreparedStatementToExecuteUpdate", dmlDataSetAssert.getExpectedUpdate().intValue(), resultDoUpdateUsePreparedStatementToExecuteUpdate);
            Assert.assertEquals("Update row number error UpdateUsePreparedStatementToExecute", dmlDataSetAssert.getExpectedUpdate().intValue(), resultDoUpdateUsePreparedStatementToExecute);
        }
    }
    
    private int doUpdateUsePreparedStatementToExecute(final String expectedDataFile, final DMLDataSetAssert anAssert, final String rootSQL) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try {
            dataSetEnvironmentManager.initialize();
            try (Connection connection = dataSource.getConnection()) {
                int actual = DatabaseUtil.updateUsePreparedStatementToExecute(connection, rootSQL, anAssert.getParameter());
                DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                if (null != anAssert.getExpectedUpdate()) {
                    Assert.assertEquals("Update row number error", anAssert.getExpectedUpdate().intValue(), actual);
                }
                String checkSQL = anAssert.getExpectedSql();
                checkSQL = SQLCasesLoader.getInstance().getSupportedSQL(checkSQL);
                DatasetDatabase ddPreparedStatement = DatabaseUtil.selectUsePreparedStatement(connection, checkSQL, anAssert.getExpectedParameter());
                DatabaseUtil.assertDatas(checkDataset, ddPreparedStatement);
                return actual;
            }
        } finally {
            dataSetEnvironmentManager.clear();
        }
    }
    
    private int doUpdateUsePreparedStatementToExecuteUpdate(final String expectedDataFile, final DMLDataSetAssert anAssert, final String rootSQL) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try {
            dataSetEnvironmentManager.initialize();
            try (Connection connection = dataSource.getConnection()) {
                int result = DatabaseUtil.updateUsePreparedStatementToExecuteUpdate(connection, rootSQL, anAssert.getParameter());
                DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                if (null != anAssert.getExpectedUpdate()) {
                    Assert.assertEquals("Update row number error", anAssert.getExpectedUpdate().intValue(), result);
                }
                String checkSQL = anAssert.getExpectedSql();
                checkSQL = SQLCasesLoader.getInstance().getSupportedSQL(checkSQL);
                DatasetDatabase ddPreparedStatement = DatabaseUtil.selectUsePreparedStatement(connection, checkSQL, anAssert.getExpectedParameter());
                DatabaseUtil.assertDatas(checkDataset, ddPreparedStatement);
                return result;
            }
        } finally {
            dataSetEnvironmentManager.clear();
        }
    }
    
    private int doUpdateUseStatementToExecute(final String expectedDataFile, final DMLDataSetAssert anAssert, final String rootSQL) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try {
            dataSetEnvironmentManager.initialize();
            try (Connection connection = dataSource.getConnection()) {
                int result = DatabaseUtil.updateUseStatementToExecute(connection, rootSQL, anAssert.getParameter());
                DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                if (anAssert.getExpectedUpdate() != null) {
                    Assert.assertEquals("Update row number error", anAssert.getExpectedUpdate().intValue(), result);
                }
                String checkSQL = anAssert.getExpectedSql();
                checkSQL = SQLCasesLoader.getInstance().getSupportedSQL(checkSQL);
                DatasetDatabase ddPreparedStatement = DatabaseUtil.selectUsePreparedStatement(connection, checkSQL, anAssert.getExpectedParameter());
                DatabaseUtil.assertDatas(checkDataset, ddPreparedStatement);
                return result;
            }
        } finally {
            dataSetEnvironmentManager.clear();
        }
    }
    
    private int doUpdateUseStatementToExecuteUpdate(final String expectedDataFile, final DMLDataSetAssert dmlDataSetAssert, final String rootSQL) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try {
            dataSetEnvironmentManager.initialize();
            try (Connection connection = dataSource.getConnection()) {
                int actual = DatabaseUtil.updateUseStatementToExecuteUpdate(connection, rootSQL, dmlDataSetAssert.getParameter());
                DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                if (null != dmlDataSetAssert.getExpectedUpdate()) {
                    Assert.assertEquals("Update row number error", dmlDataSetAssert.getExpectedUpdate().intValue(), actual);
                }
                String checkSQL = dmlDataSetAssert.getExpectedSql();
                checkSQL = SQLCasesLoader.getInstance().getSupportedSQL(checkSQL);
                DatasetDatabase ddPreparedStatement = DatabaseUtil.selectUsePreparedStatement(connection, checkSQL, dmlDataSetAssert.getExpectedParameter());
                DatabaseUtil.assertDatas(checkDataset, ddPreparedStatement);
                return actual;
            }
        } finally {
            dataSetEnvironmentManager.clear();
        }
    }
}
