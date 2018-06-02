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

import com.google.common.base.Splitter;
import io.shardingsphere.core.api.yaml.YamlMasterSlaveDataSourceFactory;
import io.shardingsphere.core.api.yaml.YamlShardingDataSourceFactory;
import io.shardingsphere.dbtest.common.DatabaseUtil;
import io.shardingsphere.dbtest.common.SQLValue;
import io.shardingsphere.dbtest.config.DataSetsParser;
import io.shardingsphere.dbtest.config.bean.DMLDataSetAssert;
import io.shardingsphere.dbtest.config.bean.DMLSubAssert;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public final class DMLAssertEngine {
    
    private final DMLDataSetAssert dmlDataSetAssert;
    
    private final String shardingRuleType;
    
    private final SQLCaseType caseType;
    
    private final DataSource dataSource;
    
    private final String sql;
    
    private final String rootPath;
    
    private final DataSetEnvironmentManager dataSetEnvironmentManager;
    
    public DMLAssertEngine(final String sqlCaseId, final String path, final DataSetEnvironmentManager dataSetEnvironmentManager, final DMLDataSetAssert dmlDataSetAssert, final Map<String, DataSource> dataSourceMap,
                           final String shardingRuleType, final SQLCaseType caseType) throws IOException, SQLException {
        this.dmlDataSetAssert = dmlDataSetAssert;
        this.shardingRuleType = shardingRuleType;
        this.caseType = caseType;
        dataSource = createDataSource(dataSourceMap);
        sql = SQLCasesLoader.getInstance().getSupportedSQL(sqlCaseId);
        rootPath = path.substring(0, path.lastIndexOf(File.separator) + 1);
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
        int resultDoUpdateUseStatementToExecuteUpdate = 0;
        int resultDoUpdateUseStatementToExecute = 0;
        int resultDoUpdateUsePreparedStatementToExecuteUpdate = 0;
        int resultDoUpdateUsePreparedStatementToExecute = 0;
        for (DMLSubAssert subAssert : dmlDataSetAssert.getSubAsserts()) {
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
            String expectedDataFile = rootPath + "asserts/dml/" + subAssert.getExpectedDataFile();
            resultDoUpdateUseStatementToExecuteUpdate = resultDoUpdateUseStatementToExecuteUpdate + doUpdateUseStatementToExecuteUpdate(expectedDataFile, subAssert);
            resultDoUpdateUseStatementToExecute = resultDoUpdateUseStatementToExecute + doUpdateUseStatementToExecute(expectedDataFile, subAssert);
            resultDoUpdateUsePreparedStatementToExecuteUpdate = resultDoUpdateUsePreparedStatementToExecuteUpdate + doUpdateUsePreparedStatementToExecuteUpdate(expectedDataFile, subAssert);
            resultDoUpdateUsePreparedStatementToExecute = resultDoUpdateUsePreparedStatementToExecute + doUpdateUsePreparedStatementToExecute(expectedDataFile, subAssert);
        }
        if (null != dmlDataSetAssert.getExpectedUpdate()) {
            assertEquals("Update row number error UpdateUseStatementToExecuteUpdate", dmlDataSetAssert.getExpectedUpdate().intValue(), resultDoUpdateUseStatementToExecuteUpdate);
            assertEquals("Update row number error UpdateUseStatementToExecute", dmlDataSetAssert.getExpectedUpdate().intValue(), resultDoUpdateUseStatementToExecute);
            assertEquals("Update row number error UpdateUsePreparedStatementToExecuteUpdate", dmlDataSetAssert.getExpectedUpdate().intValue(), resultDoUpdateUsePreparedStatementToExecuteUpdate);
            assertEquals("Update row number error UpdateUsePreparedStatementToExecute", dmlDataSetAssert.getExpectedUpdate().intValue(), resultDoUpdateUsePreparedStatementToExecute);
        }
    }
    
    private int doUpdateUsePreparedStatementToExecute(final String expectedDataFile, final DMLSubAssert dmlSubAssert) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try {
            dataSetEnvironmentManager.initialize();
            try (Connection connection = dataSource.getConnection()) {
                int result = DatabaseUtil.updateUsePreparedStatementToExecute(connection, sql, getSQLValues(dmlSubAssert.getParameters()));
                DataSetDefinitions expected = DataSetsParser.parse(new File(expectedDataFile), "data");
                if (null != dmlSubAssert.getExpectedUpdate()) {
                    assertEquals("Update row number error", dmlSubAssert.getExpectedUpdate().intValue(), result);
                }
                String checkSQL = dmlSubAssert.getExpectedSql();
                checkSQL = SQLCasesLoader.getInstance().getSupportedSQL(checkSQL);
                DataSetDefinitions actual = DatabaseUtil.selectUsePreparedStatement0(connection, checkSQL, dmlSubAssert.getExpectedParameter());
                DataSetAssert.assertDataSet(actual, expected);
                return result;
            }
        } finally {
            dataSetEnvironmentManager.clear();
        }
    }
    
    private int doUpdateUsePreparedStatementToExecuteUpdate(final String expectedDataFile, final DMLSubAssert dmlSubAssert) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try {
            dataSetEnvironmentManager.initialize();
            try (Connection connection = dataSource.getConnection()) {
                int result = DatabaseUtil.updateUsePreparedStatementToExecuteUpdate(connection, sql, getSQLValues(dmlSubAssert.getParameters()));
                DataSetDefinitions expected = DataSetsParser.parse(new File(expectedDataFile), "data");
                if (null != dmlSubAssert.getExpectedUpdate()) {
                    assertEquals("Update row number error", dmlSubAssert.getExpectedUpdate().intValue(), result);
                }
                String checkSQL = dmlSubAssert.getExpectedSql();
                checkSQL = SQLCasesLoader.getInstance().getSupportedSQL(checkSQL);
                DataSetDefinitions actual = DatabaseUtil.selectUsePreparedStatement0(connection, checkSQL, dmlSubAssert.getExpectedParameter());
                DataSetAssert.assertDataSet(actual, expected);
                return result;
            }
        } finally {
            dataSetEnvironmentManager.clear();
        }
    }
    
    private int doUpdateUseStatementToExecute(final String expectedDataFile, final DMLSubAssert dmlSubAssert) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try {
            dataSetEnvironmentManager.initialize();
            try (Connection connection = dataSource.getConnection()) {
                int result = DatabaseUtil.updateUseStatementToExecute(connection, sql, getSQLValues(dmlSubAssert.getParameters()));
                DataSetDefinitions expected = DataSetsParser.parse(new File(expectedDataFile), "data");
                if (null != dmlSubAssert.getExpectedUpdate()) {
                    assertEquals("Update row number error", dmlSubAssert.getExpectedUpdate().intValue(), result);
                }
                String checkSQL = dmlSubAssert.getExpectedSql();
                checkSQL = SQLCasesLoader.getInstance().getSupportedSQL(checkSQL);
                DataSetDefinitions actual = DatabaseUtil.selectUsePreparedStatement0(connection, checkSQL, dmlSubAssert.getExpectedParameter());
                DataSetAssert.assertDataSet(actual, expected);
                return result;
            }
        } finally {
            dataSetEnvironmentManager.clear();
        }
    }
    
    private int doUpdateUseStatementToExecuteUpdate(final String expectedDataFile, final DMLSubAssert dmlSubAssert) throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try {
            dataSetEnvironmentManager.initialize();
            try (Connection connection = dataSource.getConnection()) {
                int result = DatabaseUtil.updateUseStatementToExecuteUpdate(connection, sql, getSQLValues(dmlSubAssert.getParameters()));
                DataSetDefinitions expected = DataSetsParser.parse(new File(expectedDataFile), "data");
                if (null != dmlSubAssert.getExpectedUpdate()) {
                    assertEquals("Update row number error", dmlSubAssert.getExpectedUpdate().intValue(), result);
                }
                String checkSQL = dmlSubAssert.getExpectedSql();
                checkSQL = SQLCasesLoader.getInstance().getSupportedSQL(checkSQL);
                DataSetDefinitions actual = DatabaseUtil.selectUsePreparedStatement0(connection, checkSQL, dmlSubAssert.getExpectedParameter());
                DataSetAssert.assertDataSet(actual, expected);
                return result;
            }
        } finally {
            dataSetEnvironmentManager.clear();
        }
    }
    
    private Collection<SQLValue> getSQLValues(final String parameters) throws ParseException {
        if (null == parameters) {
            return Collections.emptyList();
        }
        Collection<SQLValue> result = new LinkedList<>();
        int count = 0;
        for (String each : Splitter.on(",").trimResults().splitToList(parameters)) {
            List<String> parameterPair = Splitter.on(":").trimResults().splitToList(each);
            result.add(new SQLValue(parameterPair.get(0), parameterPair.get(1), ++count));
        }
        return result;
    }
}
