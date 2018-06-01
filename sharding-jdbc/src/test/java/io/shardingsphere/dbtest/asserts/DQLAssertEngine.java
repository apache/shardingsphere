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
import io.shardingsphere.dbtest.config.bean.DQLSubAssert;
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

/**
 * DQL assert engine.
 * 
 * @author zhangliang 
 */
public final class DQLAssertEngine {
    
    private final DQLSubAssert dqlSubAssert;
    
    private final SQLCaseType caseType;
    
    private final DataSource dataSource;
    
    private final String sql;
    
    private final String expectedDataFile;
    
    public DQLAssertEngine(final String sqlCaseId, final String path, final DQLSubAssert dqlSubAssert, 
                           final Map<String, DataSource> dataSourceMap, final String shardingRuleType, final SQLCaseType caseType) throws IOException, SQLException {
        this.dqlSubAssert = dqlSubAssert;
        this.caseType = caseType;
        dataSource = createDataSource(shardingRuleType, dataSourceMap);
        sql = SQLCasesLoader.getInstance().getSupportedSQL(sqlCaseId);
        expectedDataFile = path.substring(0, path.lastIndexOf(File.separator) + 1) + "asserts/dql/" + dqlSubAssert.getExpectedDataFile();
    }
    
    private DataSource createDataSource(final String shardingRuleType, final Map<String, DataSource> dataSourceMap) throws SQLException, IOException {
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
        assertExecuteQueryForPreparedStatement();
        assertExecuteDQLForPreparedStatement();
        assertExecuteQueryForStatement();
        assertExecuteDQLForStatement();
    }
    
    private void assertExecuteQueryForPreparedStatement() throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try (Connection connection = dataSource.getConnection()) {
            DataSetDefinitions actual = DatabaseUtil.executeQueryForPreparedStatement(connection, sql, getSQLValues(dqlSubAssert.getParameters()));
            DataSetDefinitions expected = DataSetsParser.parse(new File(expectedDataFile), "data");
            DatabaseUtil.assertDataSet(actual, expected);
        }
    }
    
    private void assertExecuteDQLForPreparedStatement() throws SQLException, ParseException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        try (Connection connection = dataSource.getConnection()) {
            DataSetDefinitions actual = DatabaseUtil.executeDQLForPreparedStatement(connection, sql, getSQLValues(dqlSubAssert.getParameters()));
            DataSetDefinitions expected = DataSetsParser.parse(new File(expectedDataFile), "data");
            DatabaseUtil.assertDataSet(actual, expected);
        }
    }
    
    private void assertExecuteQueryForStatement() throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, ParseException {
        try (Connection connection = dataSource.getConnection()) {
            DataSetDefinitions actual = DatabaseUtil.executeQueryForStatement(connection, sql, getSQLValues(dqlSubAssert.getParameters()));
            DataSetDefinitions expected = DataSetsParser.parse(new File(expectedDataFile), "data");
            DatabaseUtil.assertDataSet(actual, expected);
        }
    }
    
    private void assertExecuteDQLForStatement() throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, ParseException {
        try (Connection connection = dataSource.getConnection()) {
            DataSetDefinitions actual = DatabaseUtil.executeDQLForStatement(connection, sql, getSQLValues(dqlSubAssert.getParameters()));
            DataSetDefinitions expected = DataSetsParser.parse(new File(expectedDataFile), "data");
            DatabaseUtil.assertDataSet(actual, expected);
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
