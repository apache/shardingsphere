/*
 * Copyright 1999-2015 dangdang.com.
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

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import io.shardingsphere.core.api.yaml.YamlMasterSlaveDataSourceFactory;
import io.shardingsphere.core.api.yaml.YamlShardingDataSourceFactory;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.util.InlineExpressionParser;
import io.shardingsphere.dbtest.common.DatabaseUtil;
import io.shardingsphere.dbtest.config.DataSetsParser;
import io.shardingsphere.dbtest.config.bean.AssertSubDefinition;
import io.shardingsphere.dbtest.config.bean.DDLDataSetAssert;
import io.shardingsphere.dbtest.config.bean.DMLDataSetAssert;
import io.shardingsphere.dbtest.config.bean.DQLDataSetAssert;
import io.shardingsphere.dbtest.config.bean.DataSetAssert;
import io.shardingsphere.dbtest.config.bean.DatasetDatabase;
import io.shardingsphere.dbtest.config.bean.DatasetDefinition;
import io.shardingsphere.dbtest.config.bean.ParameterDefinition;
import io.shardingsphere.dbtest.config.dataset.DataSetColumnMetadata;
import io.shardingsphere.dbtest.config.dataset.DataSetMetadata;
import io.shardingsphere.dbtest.config.dataset.DataSetRow;
import io.shardingsphere.dbtest.config.dataset.DataSetsRoot;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.dbtest.env.datasource.DataSourceUtil;
import io.shardingsphere.dbtest.env.schema.SchemaEnvironmentManager;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AssertEngine {
    
    /**
     * Run assert.
     * 
     * @param assertDefinition assert definition
     * @param shardingRuleType sharding rule type
     * @param databaseTypeEnvironment database type environment
     * @param caseType case type
     */
    public static void runAssert(final DataSetAssert assertDefinition, final String shardingRuleType, final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) throws IOException, SQLException, SAXException, ParserConfigurationException, XPathExpressionException, ParseException, JAXBException {
        if (!databaseTypeEnvironment.isEnabled()) {
            return;
        }
        String rootPath = assertDefinition.getPath().substring(0, assertDefinition.getPath().lastIndexOf(File.separator) + 1);
        String initDataPath = EnvironmentPath.getDataInitializeResourceFile(shardingRuleType);
        Map<String, DataSource> dataSourceMap = createDataSourceMap(SchemaEnvironmentManager.getDataSourceNames(shardingRuleType), databaseTypeEnvironment.getDatabaseType());
        DataSource dataSource = createDataSource(shardingRuleType, dataSourceMap);
        if (assertDefinition instanceof DQLDataSetAssert) {
            dqlRun((DQLDataSetAssert) assertDefinition, shardingRuleType, databaseTypeEnvironment.getDatabaseType(), initDataPath, rootPath, dataSource, dataSourceMap);
        } else if (assertDefinition instanceof DMLDataSetAssert) {
            dmlRun((DMLDataSetAssert) assertDefinition, shardingRuleType, databaseTypeEnvironment.getDatabaseType(), initDataPath, rootPath, dataSource, dataSourceMap);
        } else if (assertDefinition instanceof DDLDataSetAssert) {
            ddlRun((DDLDataSetAssert) assertDefinition, databaseTypeEnvironment.getDatabaseType(), shardingRuleType, rootPath, dataSource);
        }
    }
    
    private static Map<String, DataSource> createDataSourceMap(final Collection<String> dataSourceNames, final DatabaseType databaseType) {
        Map<String, DataSource> result = new HashMap<>(dataSourceNames.size(), 1);
        for (String each : dataSourceNames) {
            result.put(each, DataSourceUtil.createDataSource(databaseType, each));
        }
        return result;
    }
    
    private static DataSource createDataSource(final String shardingRuleType, final Map<String, DataSource> dataSourceMap) throws SQLException, IOException {
        return "masterslaveonly".equals(shardingRuleType)
                        ? YamlMasterSlaveDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(shardingRuleType)))
                        : YamlShardingDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(shardingRuleType)));
    }
    
    private static void ddlRun(final DDLDataSetAssert ddlDefinition, final DatabaseType databaseType, final String shardingRuleType, final String rootPath, final DataSource dataSource) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        String rootSQL = ddlDefinition.getSql();
        rootSQL = SQLCasesLoader.getInstance().getSupportedSQL(rootSQL);
        String expectedDataFile = rootPath + "asserts/ddl/" + shardingRuleType + "/" + ddlDefinition.getExpectedDataFile();
        if (!new File(expectedDataFile).exists()) {
            expectedDataFile = rootPath + "asserts/ddl/" + ddlDefinition.getExpectedDataFile();
        }
        if (ddlDefinition.getParameter().getValues().isEmpty() && ddlDefinition.getParameter().getValueReplaces().isEmpty()) {
            List<AssertSubDefinition> subAsserts = ddlDefinition.getSubAsserts();
            if (subAsserts.isEmpty()) {
                doUpdateUseStatementToExecuteUpdateDDL(shardingRuleType, databaseType, expectedDataFile, dataSource, ddlDefinition, rootSQL);
                doUpdateUseStatementToExecuteDDL(shardingRuleType, databaseType, expectedDataFile, dataSource, ddlDefinition, rootSQL);
                doUpdateUsePreparedStatementToExecuteUpdateDDL(shardingRuleType, databaseType, expectedDataFile, dataSource, ddlDefinition, rootSQL);
                doUpdateUsePreparedStatementToExecuteDDL(shardingRuleType, databaseType, expectedDataFile, dataSource, ddlDefinition, rootSQL);
            } else {
                ddlSubRun(shardingRuleType, databaseType, rootPath, dataSource, ddlDefinition, rootSQL, expectedDataFile, subAsserts);
            }
        } else {
            doUpdateUseStatementToExecuteUpdateDDL(shardingRuleType, databaseType, expectedDataFile, dataSource, ddlDefinition, rootSQL);
            doUpdateUseStatementToExecuteDDL(shardingRuleType, databaseType, expectedDataFile, dataSource, ddlDefinition, rootSQL);
            doUpdateUsePreparedStatementToExecuteUpdateDDL(shardingRuleType, databaseType, expectedDataFile, dataSource, ddlDefinition, rootSQL);
            doUpdateUsePreparedStatementToExecuteDDL(shardingRuleType, databaseType, expectedDataFile, dataSource, ddlDefinition, rootSQL);
            List<AssertSubDefinition> subAsserts = ddlDefinition.getSubAsserts();
            if (!subAsserts.isEmpty()) {
                ddlSubRun(shardingRuleType, databaseType, rootPath, dataSource, ddlDefinition, rootSQL, expectedDataFile, subAsserts);
            }
        }
    }
    
    private static void ddlSubRun(final String shardingRuleType, final DatabaseType databaseType, final String rootPath, final DataSource dataSource, final DDLDataSetAssert anAssert, final String rootsql, final String expectedDataFile, final List<AssertSubDefinition> subAsserts) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        for (AssertSubDefinition each : subAsserts) {
            if (!each.getDatabaseTypes().contains(databaseType)) {
                break;
            }
            String baseConfig = each.getShardingRuleTypes();
            if (StringUtils.isNotBlank(baseConfig)) {
                String[] baseConfigs = StringUtils.split(baseConfig, ",");
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
            if (parameter == null) {
                parameter = anAssert.getParameter();
            }
            DDLDataSetAssert anAssertSub = new DDLDataSetAssert(anAssert.getId(), anAssert.getInitSql(),
                    anAssert.getShardingRuleTypes(), anAssert.getDatabaseTypes(), anAssert.getCleanSql(), expectedDataFileSub,
                    anAssert.getSql(), anAssert.getTable(),
                    parameter, anAssert.getSubAsserts(), "");
            doUpdateUseStatementToExecuteUpdateDDL(shardingRuleType, databaseType, expectedDataFileTmp, dataSource, anAssertSub, rootsql);
            doUpdateUseStatementToExecuteDDL(shardingRuleType, databaseType, expectedDataFileTmp, dataSource, anAssertSub, rootsql);
            doUpdateUsePreparedStatementToExecuteUpdateDDL(shardingRuleType, databaseType, expectedDataFileTmp, dataSource, anAssertSub, rootsql);
            doUpdateUsePreparedStatementToExecuteDDL(shardingRuleType, databaseType, expectedDataFileTmp, dataSource, anAssertSub, rootsql);
        }
    }
    
    private static void dmlRun(final DMLDataSetAssert dmlDefinition, final String shardingRuleType, final DatabaseType databaseType, final String initDataFile, final String rootPath, final DataSource dataSource, final Map<String, DataSource> dataSourceMaps) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, SQLException, ParseException, JAXBException {
        String rootSQL = dmlDefinition.getSql();
        rootSQL = SQLCasesLoader.getInstance().getSupportedSQL(rootSQL);
        DataSetsRoot dataSetsRoot = unmarshal(initDataFile);
        Map<String, DatasetDefinition> datasetDefinitionMap = getDatasetDefinitionMap(dataSetsRoot);
        Map<String, String> sqls = getInitDatas(dataSetsRoot, datasetDefinitionMap);
        Preconditions.checkState(!datasetDefinitionMap.isEmpty(), "Use cases cannot be parsed");
        Preconditions.checkState(!sqls.isEmpty(), "The use case cannot initialize the data");
        String expectedDataFile = rootPath + "asserts/dml/" + shardingRuleType + "/" + dmlDefinition.getExpectedDataFile();
        if (!new File(expectedDataFile).exists()) {
            expectedDataFile = rootPath + "asserts/dml/" + dmlDefinition.getExpectedDataFile();
        }
        int resultDoUpdateUseStatementToExecuteUpdate = 0;
        int resultDoUpdateUseStatementToExecute = 0;
        int resultDoUpdateUsePreparedStatementToExecuteUpdate = 0;
        int resultDoUpdateUsePreparedStatementToExecute = 0;
        if (dmlDefinition.getParameter().getValues().isEmpty() && dmlDefinition.getParameter().getValueReplaces().isEmpty()) {
            List<AssertSubDefinition> subAsserts = dmlDefinition.getSubAsserts();
            if (subAsserts.isEmpty()) {
                resultDoUpdateUseStatementToExecuteUpdate = resultDoUpdateUseStatementToExecuteUpdate + doUpdateUseStatementToExecuteUpdate(expectedDataFile, dataSource, dataSourceMaps, dmlDefinition, rootSQL, datasetDefinitionMap, sqls);
                resultDoUpdateUseStatementToExecute = resultDoUpdateUseStatementToExecute + doUpdateUseStatementToExecute(expectedDataFile, dataSource, dataSourceMaps, dmlDefinition, rootSQL, datasetDefinitionMap, sqls);
                resultDoUpdateUsePreparedStatementToExecuteUpdate = resultDoUpdateUsePreparedStatementToExecuteUpdate + doUpdateUsePreparedStatementToExecuteUpdate(expectedDataFile, dataSource, dataSourceMaps, dmlDefinition, rootSQL, datasetDefinitionMap, sqls);
                resultDoUpdateUsePreparedStatementToExecute = resultDoUpdateUsePreparedStatementToExecute + doUpdateUsePreparedStatementToExecute(expectedDataFile, dataSource, dataSourceMaps, dmlDefinition, rootSQL, datasetDefinitionMap, sqls);
            } else {
                for (AssertSubDefinition subAssert : subAsserts) {
                    if (!subAssert.getDatabaseTypes().contains(databaseType)) {
                        break;
                    }
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
                        expectedDataFileSub = dmlDefinition.getExpectedDataFile();
                    } else {
                        expectedDataFileTmp = rootPath + "asserts/dml/" + shardingRuleType + "/" + expectedDataFileSub;
                        if (!new File(expectedDataFileTmp).exists()) {
                            expectedDataFileTmp = rootPath + "asserts/dml/" + expectedDataFileSub;
                        }
                    }
                    if (parameter == null) {
                        parameter = dmlDefinition.getParameter();
                    }
                    if (expectedParameter == null) {
                        expectedParameter = dmlDefinition.getParameter();
                    }
                    DMLDataSetAssert anAssertSub = new DMLDataSetAssert(dmlDefinition.getId(),
                            expectedDataFileSub, dmlDefinition.getShardingRuleTypes(), dmlDefinition.getDatabaseTypes(), subAssert.getExpectedUpdate(), dmlDefinition.getSql(),
                            dmlDefinition.getExpectedSql(), parameter, expectedParameter, dmlDefinition.getSubAsserts(), "");
                    resultDoUpdateUseStatementToExecuteUpdate = resultDoUpdateUseStatementToExecuteUpdate + doUpdateUseStatementToExecuteUpdate(expectedDataFileTmp, dataSource, dataSourceMaps, anAssertSub, rootSQL, datasetDefinitionMap, sqls);
                    resultDoUpdateUseStatementToExecute = resultDoUpdateUseStatementToExecute + doUpdateUseStatementToExecute(expectedDataFileTmp, dataSource, dataSourceMaps, anAssertSub, rootSQL, datasetDefinitionMap, sqls);
                    resultDoUpdateUsePreparedStatementToExecuteUpdate = resultDoUpdateUsePreparedStatementToExecuteUpdate + doUpdateUsePreparedStatementToExecuteUpdate(expectedDataFileTmp, dataSource, dataSourceMaps, anAssertSub, rootSQL, datasetDefinitionMap, sqls);
                    resultDoUpdateUsePreparedStatementToExecute = resultDoUpdateUsePreparedStatementToExecute + doUpdateUsePreparedStatementToExecute(expectedDataFileTmp, dataSource, dataSourceMaps, anAssertSub, rootSQL, datasetDefinitionMap, sqls);
                }
            }
        } else {
            resultDoUpdateUseStatementToExecuteUpdate = resultDoUpdateUseStatementToExecuteUpdate + doUpdateUseStatementToExecuteUpdate(expectedDataFile, dataSource, dataSourceMaps, dmlDefinition, rootSQL, datasetDefinitionMap, sqls);
            resultDoUpdateUseStatementToExecute = resultDoUpdateUseStatementToExecute + doUpdateUseStatementToExecute(expectedDataFile, dataSource, dataSourceMaps, dmlDefinition, rootSQL, datasetDefinitionMap, sqls);
            resultDoUpdateUsePreparedStatementToExecuteUpdate = resultDoUpdateUsePreparedStatementToExecuteUpdate + doUpdateUsePreparedStatementToExecuteUpdate(expectedDataFile, dataSource, dataSourceMaps, dmlDefinition, rootSQL, datasetDefinitionMap, sqls);
            resultDoUpdateUsePreparedStatementToExecute = resultDoUpdateUsePreparedStatementToExecute + doUpdateUsePreparedStatementToExecute(expectedDataFile, dataSource, dataSourceMaps, dmlDefinition, rootSQL, datasetDefinitionMap, sqls);
            List<AssertSubDefinition> subAsserts = dmlDefinition.getSubAsserts();
            if (!subAsserts.isEmpty()) {
                for (AssertSubDefinition subAssert : subAsserts) {
                    if (!subAssert.getDatabaseTypes().contains(databaseType)) {
                        break;
                    }
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
                        expectedDataFileSub = dmlDefinition.getExpectedDataFile();
                    } else {
                        expectedDataFileTmp = rootPath + "asserts/dml/" + shardingRuleType + "/" + expectedDataFileSub;
                        if (!new File(expectedDataFileTmp).exists()) {
                            expectedDataFileTmp = rootPath + "asserts/dml/" + expectedDataFileSub;
                        }
                    }
                    if (parameter == null) {
                        parameter = dmlDefinition.getParameter();
                    }
                    if (expectedParameter == null) {
                        expectedParameter = dmlDefinition.getParameter();
                    }
                    DMLDataSetAssert anAssertSub = new DMLDataSetAssert(dmlDefinition.getId(),
                            expectedDataFileSub, dmlDefinition.getShardingRuleTypes(), dmlDefinition.getDatabaseTypes(), subAssert.getExpectedUpdate(), dmlDefinition.getSql(),
                            dmlDefinition.getExpectedSql(), parameter, expectedParameter, dmlDefinition.getSubAsserts(), "");
                    resultDoUpdateUseStatementToExecuteUpdate = resultDoUpdateUseStatementToExecuteUpdate + doUpdateUseStatementToExecuteUpdate(expectedDataFileTmp, dataSource, dataSourceMaps, anAssertSub, rootSQL, datasetDefinitionMap, sqls);
                    resultDoUpdateUseStatementToExecute = resultDoUpdateUseStatementToExecute + doUpdateUseStatementToExecute(expectedDataFileTmp, dataSource, dataSourceMaps, anAssertSub, rootSQL, datasetDefinitionMap, sqls);
                    resultDoUpdateUsePreparedStatementToExecuteUpdate = resultDoUpdateUsePreparedStatementToExecuteUpdate + doUpdateUsePreparedStatementToExecuteUpdate(expectedDataFileTmp, dataSource, dataSourceMaps, anAssertSub, rootSQL, datasetDefinitionMap, sqls);
                    resultDoUpdateUsePreparedStatementToExecute = resultDoUpdateUsePreparedStatementToExecute + doUpdateUsePreparedStatementToExecute(expectedDataFileTmp, dataSource, dataSourceMaps, anAssertSub, rootSQL, datasetDefinitionMap, sqls);
                }
            }
        }
        if (null != dmlDefinition.getExpectedUpdate()) {
            Assert.assertEquals("Update row number error UpdateUseStatementToExecuteUpdate", dmlDefinition.getExpectedUpdate().intValue(), resultDoUpdateUseStatementToExecuteUpdate);
            Assert.assertEquals("Update row number error UpdateUseStatementToExecute", dmlDefinition.getExpectedUpdate().intValue(), resultDoUpdateUseStatementToExecute);
            Assert.assertEquals("Update row number error UpdateUsePreparedStatementToExecuteUpdate", dmlDefinition.getExpectedUpdate().intValue(), resultDoUpdateUsePreparedStatementToExecuteUpdate);
            Assert.assertEquals("Update row number error UpdateUsePreparedStatementToExecute", dmlDefinition.getExpectedUpdate().intValue(), resultDoUpdateUsePreparedStatementToExecute);
        }
    }
    
    private static void dqlRun(final DQLDataSetAssert dqlDefinition, final String shardingRuleType, final DatabaseType databaseType, final String initDataFile, final String rootPath, final DataSource dataSource, final Map<String, DataSource> dataSourceMaps) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, SQLException, ParseException, JAXBException {
        String rootSQL = dqlDefinition.getSql();
        rootSQL = SQLCasesLoader.getInstance().getSupportedSQL(rootSQL);
        DataSetsRoot dataSetsRoot = unmarshal(initDataFile);
        Map<String, DatasetDefinition> datasetDefinitionMap = getDatasetDefinitionMap(dataSetsRoot);
        Map<String, String> sqls = getInitDatas(dataSetsRoot, datasetDefinitionMap);
        Preconditions.checkState(!datasetDefinitionMap.isEmpty(), "Use cases cannot be parsed");
        Preconditions.checkState(!sqls.isEmpty(), "The use case cannot initialize the data");
        try {
            initData(dataSourceMaps, sqls, datasetDefinitionMap);
            String expectedDataFile = rootPath + "asserts/dql/" + shardingRuleType + "/" + dqlDefinition.getExpectedDataFile();
            if (!new File(expectedDataFile).exists()) {
                expectedDataFile = rootPath + "asserts/dql/" + dqlDefinition.getExpectedDataFile();
            }
            if (dqlDefinition.getParameter().getValues().isEmpty() && dqlDefinition.getParameter().getValueReplaces().isEmpty()) {
                List<AssertSubDefinition> subAsserts = dqlDefinition.getSubAsserts();
                if (subAsserts.isEmpty()) {
                    doSelectUsePreparedStatement(expectedDataFile, dataSource, dqlDefinition, rootSQL);
                    doSelectUsePreparedStatementToExecuteSelect(expectedDataFile, dataSource, dqlDefinition, rootSQL);
                    doSelectUseStatement(expectedDataFile, dataSource, dqlDefinition, rootSQL);
                    doSelectUseStatementToExecuteSelect(expectedDataFile, dataSource, dqlDefinition, rootSQL);
                } else {
                    dqlSubRun(databaseType, shardingRuleType, rootPath, dataSource, dqlDefinition, rootSQL, expectedDataFile, subAsserts);
                }
            } else {
                doSelectUsePreparedStatement(expectedDataFile, dataSource, dqlDefinition, rootSQL);
                doSelectUsePreparedStatementToExecuteSelect(expectedDataFile, dataSource, dqlDefinition, rootSQL);
                doSelectUseStatement(expectedDataFile, dataSource, dqlDefinition, rootSQL);
                doSelectUseStatementToExecuteSelect(expectedDataFile, dataSource, dqlDefinition, rootSQL);
                List<AssertSubDefinition> subAsserts = dqlDefinition.getSubAsserts();
                if (!subAsserts.isEmpty()) {
                    dqlSubRun(databaseType, shardingRuleType, rootPath, dataSource, dqlDefinition, rootSQL, expectedDataFile, subAsserts);
                }
            }
        } finally {
            clearData(dataSourceMaps, datasetDefinitionMap);
        }
    }
    
    private static void dqlSubRun(final DatabaseType databaseType, final String dbName, final String rootPath, final DataSource dataSource, final DQLDataSetAssert anAssert, final String rootSQL, final String expectedDataFile, final List<AssertSubDefinition> subAsserts) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        for (AssertSubDefinition subAssert : subAsserts) {
            if (!subAssert.getDatabaseTypes().contains(databaseType)) {
                break;
            }
            String baseSubConfig = subAssert.getShardingRuleTypes();
            if (StringUtils.isNotBlank(baseSubConfig)) {
                String[] baseConfigs = StringUtils.split(baseSubConfig, ",");
                boolean flag = true;
                for (String config : baseConfigs) {
                    if (dbName.equals(config)) {
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
            String expectedDataFileTmp = expectedDataFile;
            if (StringUtils.isBlank(expectedDataFileSub)) {
                expectedDataFileSub = anAssert.getExpectedDataFile();
            } else {
                expectedDataFileTmp = rootPath + "asserts/dql/" + dbName + "/" + expectedDataFileSub;
                if (!new File(expectedDataFileTmp).exists()) {
                    expectedDataFileTmp = rootPath + "asserts/dql/" + expectedDataFileSub;
                }
            }
            if (null == parameter) {
                parameter = anAssert.getParameter();
            }
            DQLDataSetAssert anAssertSub = new DQLDataSetAssert(anAssert.getId(),
                    expectedDataFileSub, anAssert.getShardingRuleTypes(), anAssert.getDatabaseTypes(), anAssert.getSql(),
                    parameter, anAssert.getSubAsserts(), "");
            doSelectUsePreparedStatement(expectedDataFileTmp, dataSource, anAssertSub, rootSQL);
            doSelectUsePreparedStatementToExecuteSelect(expectedDataFileTmp, dataSource, anAssertSub, rootSQL);
            doSelectUseStatement(expectedDataFileTmp, dataSource, anAssertSub, rootSQL);
            doSelectUseStatementToExecuteSelect(expectedDataFileTmp, dataSource, anAssertSub, rootSQL);
        }
    }
    
    private static int doUpdateUsePreparedStatementToExecute(final String expectedDataFile, final DataSource dataSource, final Map<String, DataSource> dataSourceMaps, final DMLDataSetAssert anAssert, final String rootsql, final Map<String, DatasetDefinition> mapDatasetDefinition, final Map<String, String> sqls) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try {
            initData(dataSourceMaps, sqls, mapDatasetDefinition);
            try (Connection connection = dataSource.getConnection()) {
                int actual = DatabaseUtil.updateUsePreparedStatementToExecute(connection, rootsql,
                        anAssert.getParameter());
                DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                
                if (anAssert.getExpectedUpdate() != null) {
                    Assert.assertEquals("Update row number error", anAssert.getExpectedUpdate().intValue(), actual);
                }
                String checksql = anAssert.getExpectedSql();
                checksql = SQLCasesLoader.getInstance().getSupportedSQL(checksql);
                DatasetDatabase ddPreparedStatement = DatabaseUtil.selectUsePreparedStatement(connection, checksql,
                        anAssert.getExpectedParameter());
                DatabaseUtil.assertDatas(checkDataset, ddPreparedStatement);
                return actual;
            }
        } finally {
            clearData(dataSourceMaps, mapDatasetDefinition);
        }
    }
    
    private static void doUpdateUsePreparedStatementToExecuteDDL(final String shardingRuleType, final DatabaseType databaseType, final String expectedDataFile, final DataSource dataSource, final DDLDataSetAssert anAssert, final String rootsql) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        try {
            try (Connection con = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(anAssert.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseType, anAssert.getCleanSql());
                }
                if (StringUtils.isNotBlank(anAssert.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseType, anAssert.getInitSql());
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
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseType, anAssert.getCleanSql());
            }
            if (StringUtils.isNotBlank(anAssert.getInitSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseType, anAssert.getInitSql());
            }
        }
    }
    
    private static int doUpdateUsePreparedStatementToExecuteUpdate(final String expectedDataFile, final DataSource dataSource, final Map<String, DataSource> dataSourceMaps, final DMLDataSetAssert anAssert, final String rootsql, final Map<String, DatasetDefinition> mapDatasetDefinition, final Map<String, String> sqls) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try {
            initData(dataSourceMaps, sqls, mapDatasetDefinition);
            try (Connection con = dataSource.getConnection()) {
                int actual = DatabaseUtil.updateUsePreparedStatementToExecuteUpdate(con, rootsql,
                        anAssert.getParameter());
                DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                
                if (null != anAssert.getExpectedUpdate()) {
                    Assert.assertEquals("Update row number error", anAssert.getExpectedUpdate().intValue(), actual);
                }
                String checkSQL = anAssert.getExpectedSql();
                checkSQL = SQLCasesLoader.getInstance().getSupportedSQL(checkSQL);
                DatasetDatabase ddPreparedStatement = DatabaseUtil.selectUsePreparedStatement(con, checkSQL, anAssert.getExpectedParameter());
                DatabaseUtil.assertDatas(checkDataset, ddPreparedStatement);
                return actual;
            }
        } finally {
            clearData(dataSourceMaps, mapDatasetDefinition);
        }
    }
    
    private static void doUpdateUsePreparedStatementToExecuteUpdateDDL(final String shardingRuleType, final DatabaseType databaseType, final String expectedDataFile, final DataSource dataSource, final DDLDataSetAssert anAssert, final String rootsql) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        try {
            try (Connection con = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(anAssert.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseType, anAssert.getCleanSql());
                }
                if (StringUtils.isNotBlank(anAssert.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseType, anAssert.getInitSql());
                }
                DatabaseUtil.updateUsePreparedStatementToExecuteUpdate(con, rootsql,
                        anAssert.getParameter());
                DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                String table = anAssert.getTable();
                List<DataSetColumnMetadata> columnDefinitions = DatabaseUtil.getColumnDefinitions(con, table);
                DatabaseUtil.assertConfigs(checkDataset, columnDefinitions, table);
            }
        } finally {
            if (StringUtils.isNotBlank(anAssert.getCleanSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseType, anAssert.getCleanSql());
            }
            if (StringUtils.isNotBlank(anAssert.getInitSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseType, anAssert.getInitSql());
            }
        }
    }
    
    private static int doUpdateUseStatementToExecute(final String expectedDataFile, final DataSource dataSource, final Map<String, DataSource> dataSourceMaps, final DMLDataSetAssert anAssert, final String rootsql, final Map<String, DatasetDefinition> mapDatasetDefinition, final Map<String, String> sqls) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try {
            initData(dataSourceMaps, sqls, mapDatasetDefinition);
            try (Connection connection = dataSource.getConnection()) {
                int actual = DatabaseUtil.updateUseStatementToExecute(connection, rootsql, anAssert.getParameter());
                DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                if (anAssert.getExpectedUpdate() != null) {
                    Assert.assertEquals("Update row number error", anAssert.getExpectedUpdate().intValue(), actual);
                }
                String checkSQL = anAssert.getExpectedSql();
                checkSQL = SQLCasesLoader.getInstance().getSupportedSQL(checkSQL);
                DatasetDatabase ddPreparedStatement = DatabaseUtil.selectUsePreparedStatement(connection, checkSQL, anAssert.getExpectedParameter());
                DatabaseUtil.assertDatas(checkDataset, ddPreparedStatement);
                return actual;
            }
        } finally {
            clearData(dataSourceMaps, mapDatasetDefinition);
        }
    }
    
    private static void doUpdateUseStatementToExecuteDDL(final String shardingRuleType, final DatabaseType databaseType, final String expectedDataFile, final DataSource dataSource, final DDLDataSetAssert anAssert, final String rootsql) throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        try {
            try (Connection con = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(anAssert.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseType, anAssert.getCleanSql());
                }
                if (StringUtils.isNotBlank(anAssert.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseType, anAssert.getInitSql());
                }
                DatabaseUtil.updateUseStatementToExecute(con, rootsql, anAssert.getParameter());
                DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                String table = anAssert.getTable();
                List<DataSetColumnMetadata> columnDefinitions = DatabaseUtil.getColumnDefinitions(con, table);
                DatabaseUtil.assertConfigs(checkDataset, columnDefinitions, table);
            }
        } finally {
            if (!Strings.isNullOrEmpty(anAssert.getCleanSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseType, anAssert.getCleanSql());
            }
            if (!Strings.isNullOrEmpty(anAssert.getInitSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseType, anAssert.getInitSql());
            }
        }
    }
    
    private static int doUpdateUseStatementToExecuteUpdate(final String expectedDataFile, final DataSource dataSource, final Map<String, DataSource> dataSourceMaps, final DMLDataSetAssert anAssert, final String rootsql, final Map<String, DatasetDefinition> mapDatasetDefinition, final Map<String, String> sqls) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try {
            initData(dataSourceMaps, sqls, mapDatasetDefinition);
            try (Connection connection = dataSource.getConnection()) {
                int actual = DatabaseUtil.updateUseStatementToExecuteUpdate(connection, rootsql, anAssert.getParameter());
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
            clearData(dataSourceMaps, mapDatasetDefinition);
        }
    }
    
    private static void doUpdateUseStatementToExecuteUpdateDDL(final String shardingRuleType, final DatabaseType databaseType, final String expectedDataFile, final DataSource dataSource, final DDLDataSetAssert anAssert, final String rootsql) throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, JAXBException {
        try {
            try (Connection con = dataSource.getConnection()) {
                if (StringUtils.isNotBlank(anAssert.getCleanSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseType, anAssert.getCleanSql());
                }
                if (StringUtils.isNotBlank(anAssert.getInitSql())) {
                    SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseType, anAssert.getInitSql());
                }
                DatabaseUtil.updateUseStatementToExecuteUpdate(con, rootsql, anAssert.getParameter());
                DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
                String table = anAssert.getTable();
                List<DataSetColumnMetadata> columnDefinitions = DatabaseUtil.getColumnDefinitions(con, table);
                DatabaseUtil.assertConfigs(checkDataset, columnDefinitions, table);
            }
        } finally {
            if (!Strings.isNullOrEmpty(anAssert.getCleanSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseType, anAssert.getCleanSql());
            }
            if (!Strings.isNullOrEmpty(anAssert.getInitSql())) {
                SchemaEnvironmentManager.executeSQL(shardingRuleType, databaseType, anAssert.getInitSql());
            }
        }
    }
    
    private static void doSelectUseStatement(final String expectedDataFile, final DataSource dataSource, final DQLDataSetAssert anAssert, final String rootSQL) throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try (Connection connection = dataSource.getConnection()) {
            DatasetDatabase datasetDatabase = DatabaseUtil.selectUseStatement(connection, rootSQL, anAssert.getParameter());
            DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
            DatabaseUtil.assertDatas(checkDataset, datasetDatabase);
        }
    }
    
    private static void doSelectUseStatementToExecuteSelect(final String expectedDataFile, final DataSource dataSource, final DQLDataSetAssert anAssert, final String rootSQL) throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try (Connection connection = dataSource.getConnection()) {
            DatasetDatabase datasetDatabase = DatabaseUtil.selectUseStatementToExecuteSelect(connection, rootSQL, anAssert.getParameter());
            DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
            DatabaseUtil.assertDatas(checkDataset, datasetDatabase);
        }
    }
    
    private static void doSelectUsePreparedStatement(final String expectedDataFile, final DataSource dataSource, final DQLDataSetAssert anAssert, final String rootSQL) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try (Connection connection = dataSource.getConnection()) {
            DatasetDatabase ddPreparedStatement = DatabaseUtil.selectUsePreparedStatement(connection, rootSQL, anAssert.getParameter());
            DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
            DatabaseUtil.assertDatas(checkDataset, ddPreparedStatement);
        }
    }
    
    private static void doSelectUsePreparedStatementToExecuteSelect(final String expectedDataFile, final DataSource dataSource, final DQLDataSetAssert anAssert, final String rootSQL) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try (Connection connection = dataSource.getConnection()) {
            DatasetDatabase datasetDatabase = DatabaseUtil.selectUsePreparedStatementToExecuteSelect(connection, rootSQL, anAssert.getParameter());
            DatasetDefinition checkDataset = DataSetsParser.parse(new File(expectedDataFile), "data");
            DatabaseUtil.assertDatas(checkDataset, datasetDatabase);
        }
    }
    
    private static DataSetsRoot unmarshal(final String path) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(path)) {
            return (DataSetsRoot) JAXBContext.newInstance(DataSetsRoot.class).createUnmarshaller().unmarshal(reader);
        }
    }
    
    private static Map<String, DatasetDefinition> getDatasetDefinitionMap(final DataSetsRoot dataSetsRoot) {
        Map<String, DatasetDefinition> result = new HashMap<>();
        for (DataSetMetadata each : dataSetsRoot.getMetadataList()) {
            for (String dataNodeStr : new InlineExpressionParser(each.getDataNodes()).evaluate()) {
                DataNode dataNode = new DataNode(dataNodeStr);
                if (!result.containsKey(dataNode.getDataSourceName())) {
                    result.put(dataNode.getDataSourceName(), new DatasetDefinition());
                }
                DatasetDefinition datasetDefinition = result.get(dataNode.getDataSourceName());
                datasetDefinition.getMetadatas().put(dataNode.getTableName(), each.getColumnMetadataList());
                datasetDefinition.getIndexMetadataList().put(dataNode.getTableName(), each.getIndexMetadataList());
                result.put(dataNode.getDataSourceName(), datasetDefinition);
            }
        }
        return result;
    }
    
    private static Map<String, String> getInitDatas(final DataSetsRoot dataSetsRoot, final Map<String, DatasetDefinition> datasetDefinitionMap) {
        Map<String, String> result = new LinkedHashMap<>();
        for (DataSetRow each : dataSetsRoot.getDataSetRows()) {
            DataNode dataNode = new DataNode(each.getDataNode());
            if (!datasetDefinitionMap.get(dataNode.getDataSourceName()).getDatas().containsKey(dataNode.getTableName())) {
                datasetDefinitionMap.get(dataNode.getDataSourceName()).getDatas().put(dataNode.getTableName(), new LinkedList<Map<String, String>>());
            }
            List<Map<String, String>> tableDataList = datasetDefinitionMap.get(dataNode.getDataSourceName()).getDatas().get(dataNode.getTableName());
            List<String> values = Splitter.on(',').trimResults().splitToList(each.getValues());
            int count = 0;
            Map<String, String> map = new LinkedHashMap<>(values.size(), 1);
            for (DataSetColumnMetadata column : datasetDefinitionMap.get(dataNode.getDataSourceName()).getMetadatas().get(dataNode.getTableName())) {
                map.put(column.getName(), values.get(count));
                count++;
            }
            String sql = DatabaseUtil.analyzeSQL(dataNode.getTableName(), datasetDefinitionMap.get(dataNode.getDataSourceName()).getMetadatas().get(dataNode.getTableName()));
            result.put(dataNode.getTableName(), sql);
            if (!datasetDefinitionMap.get(dataNode.getDataSourceName()).getDatas().containsKey(dataNode.getTableName())) {
                datasetDefinitionMap.get(dataNode.getDataSourceName()).getDatas().put(dataNode.getTableName(), new LinkedList<Map<String, String>>());
            }
            tableDataList.add(map);
            datasetDefinitionMap.get(dataNode.getDataSourceName()).getDatas().put(dataNode.getTableName(), tableDataList);
        }
        return result;
    }
    
    private static void initData(
            final Map<String, DataSource> dataSourceMap, final Map<String, String> sqls, final Map<String, DatasetDefinition> datasetDefinitionMap) throws SQLException, ParseException {
        clearData(dataSourceMap, datasetDefinitionMap);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            initData(entry.getValue(), sqls, datasetDefinitionMap.get(entry.getKey()));
        }
    }
    
    private static void initData(final DataSource dataSource, final Map<String, String> sqls, final DatasetDefinition datasetDefinition) throws SQLException, ParseException {
        for (Entry<String, List<Map<String, String>>> entry : datasetDefinition.getDatas().entrySet()) {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseUtil.insertUsePreparedStatement(connection, sqls.get(entry.getKey()), datasetDefinition.getDatas().get(entry.getKey()), datasetDefinition.getMetadatas().get(entry.getKey()));
            }
        }
    }
    
    private static void clearData(final Map<String, DataSource> dataSourceMap, final Map<String, DatasetDefinition> datasetDefinitionMap) throws SQLException {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            clearData(entry.getValue(), datasetDefinitionMap.get(entry.getKey()));
        }
    }
    
    private static void clearData(final DataSource dataSource, final DatasetDefinition datasetDefinition) throws SQLException {
        for (Entry<String, List<Map<String, String>>> entry : datasetDefinition.getDatas().entrySet()) {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseUtil.cleanAllUsePreparedStatement(connection, entry.getKey());
            }
        }
    }
}
