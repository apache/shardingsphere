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
import io.shardingsphere.dbtest.jaxb.assertion.dql.DQLIntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.jaxb.dataset.expected.dataset.ExpectedDataSetsRoot;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

/**
 * DQL assert engine.
 * 
 * @author zhangliang 
 */
public final class DQLAssertEngine {
    
    private final DQLIntegrateTestCaseAssertion integrateTestCaseAssertion;
    
    private final SQLCaseType caseType;
    
    private final DataSource dataSource;
    
    private final String sql;
    
    private final String expectedDataFile;
    
    public DQLAssertEngine(final String sqlCaseId, final String path, final DQLIntegrateTestCaseAssertion integrateTestCaseAssertion, 
                           final Map<String, DataSource> dataSourceMap, final String shardingRuleType, final SQLCaseType caseType) throws IOException, SQLException {
        this.integrateTestCaseAssertion = integrateTestCaseAssertion;
        this.caseType = caseType;
        dataSource = createDataSource(shardingRuleType, dataSourceMap);
        sql = SQLCasesLoader.getInstance().getSupportedSQL(sqlCaseId);
        expectedDataFile = path.substring(0, path.lastIndexOf(File.separator) + 1) + "asserts/dql/" + integrateTestCaseAssertion.getExpectedDataFile();
    }
    
    private DataSource createDataSource(final String shardingRuleType, final Map<String, DataSource> dataSourceMap) throws SQLException, IOException {
        return "masterslaveonly".equals(shardingRuleType)
                ? YamlMasterSlaveDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(shardingRuleType)))
                : YamlShardingDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(shardingRuleType)));
    }
    
    public void assertExecuteQueryForPreparedStatement() throws SQLException, ParseException, IOException, JAXBException {
        try (Connection connection = dataSource.getConnection()) {
            assertDataSet(DatabaseUtil.executeQueryForPreparedStatement(connection, sql, integrateTestCaseAssertion.getSQLValues()));
        }
    }
    
    public void assertExecuteForPreparedStatement() throws SQLException, ParseException, IOException, JAXBException {
        try (Connection connection = dataSource.getConnection()) {
            assertDataSet(DatabaseUtil.executeDQLForPreparedStatement(connection, sql, integrateTestCaseAssertion.getSQLValues()));
        }
    }
    
    public void assertExecuteQueryForStatement() throws SQLException, IOException, ParseException, JAXBException {
        try (Connection connection = dataSource.getConnection()) {
            assertDataSet(DatabaseUtil.executeQueryForStatement(connection, sql, integrateTestCaseAssertion.getSQLValues()));
        }
    }
    
    public void assertExecuteForStatement() throws SQLException, IOException, ParseException, JAXBException {
        try (Connection connection = dataSource.getConnection()) {
            assertDataSet(DatabaseUtil.executeDQLForStatement(connection, sql, integrateTestCaseAssertion.getSQLValues()));
        }
    }
    
    private void assertDataSet(final DataSetDefinitions actual) throws IOException, JAXBException {
        ExpectedDataSetsRoot expected;
        try (FileReader reader = new FileReader(expectedDataFile)) {
            expected = (ExpectedDataSetsRoot) JAXBContext.newInstance(ExpectedDataSetsRoot.class).createUnmarshaller().unmarshal(reader);
        }
        DataSetAssert.assertDataSet(actual, expected);
    }
}
