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
import io.shardingsphere.dbtest.config.bean.DMLSubAssert;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public final class DMLAssertEngine {
    
    private final DMLSubAssert dmlSubAssert;
    
    private final String shardingRuleType;
    
    private final SQLCaseType caseType;
    
    private final DataSource dataSource;
    
    private final String sql;
    
    private final String expectedDataFile;
    
    private final DataSetEnvironmentManager dataSetEnvironmentManager;
    
    public DMLAssertEngine(final String sqlCaseId, final String path, final DataSetEnvironmentManager dataSetEnvironmentManager, 
                           final DMLSubAssert dmlSubAssert, final Map<String, DataSource> dataSourceMap, final String shardingRuleType, final SQLCaseType caseType) throws IOException, SQLException {
        this.dmlSubAssert = dmlSubAssert;
        this.shardingRuleType = shardingRuleType;
        this.caseType = caseType;
        dataSource = createDataSource(dataSourceMap);
        sql = SQLCasesLoader.getInstance().getSupportedSQL(sqlCaseId);
        expectedDataFile = path.substring(0, path.lastIndexOf(File.separator) + 1) + "asserts/dml/" + dmlSubAssert.getExpectedDataFile();
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
        assertThat(doUpdateUseStatementToExecuteUpdate(), is(dmlSubAssert.getExpectedUpdate()));
        assertThat(doUpdateUseStatementToExecute(), is(dmlSubAssert.getExpectedUpdate()));
        assertThat(doUpdateUsePreparedStatementToExecuteUpdate(), is(dmlSubAssert.getExpectedUpdate()));
        assertThat(doUpdateUsePreparedStatementToExecute(), is(dmlSubAssert.getExpectedUpdate()));
    }
    
    private int doUpdateUsePreparedStatementToExecute() throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
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
    
    private int doUpdateUsePreparedStatementToExecuteUpdate() throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
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
    
    private int doUpdateUseStatementToExecute() throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
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
    
    private int doUpdateUseStatementToExecuteUpdate() throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
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
