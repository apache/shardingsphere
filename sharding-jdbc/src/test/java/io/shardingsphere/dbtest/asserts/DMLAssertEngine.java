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
import io.shardingsphere.dbtest.jaxb.assertion.dml.DMLIntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.jaxb.dataset.init.DataSetsRoot;
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DMLAssertEngine {
    
    private final DMLIntegrateTestCaseAssertion integrateTestCaseAssertion;
    
    private final String shardingRuleType;
    
    private final SQLCaseType caseType;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final DataSource dataSource;
    
    private final String sql;
    
    private final String expectedDataFile;
    
    private final DataSetEnvironmentManager dataSetEnvironmentManager;
    
    public DMLAssertEngine(final String sqlCaseId, final String path, final DataSetEnvironmentManager dataSetEnvironmentManager, final DMLIntegrateTestCaseAssertion integrateTestCaseAssertion, 
                           final Map<String, DataSource> dataSourceMap, final String shardingRuleType, final SQLCaseType caseType) throws IOException, SQLException {
        this.integrateTestCaseAssertion = integrateTestCaseAssertion;
        this.shardingRuleType = shardingRuleType;
        this.caseType = caseType;
        this.dataSourceMap = dataSourceMap;
        dataSource = createDataSource(dataSourceMap);
        sql = SQLCasesLoader.getInstance().getSupportedSQL(sqlCaseId);
        expectedDataFile = path.substring(0, path.lastIndexOf(File.separator) + 1) + "asserts/dml/" + integrateTestCaseAssertion.getExpectedDataFile();
        this.dataSetEnvironmentManager = dataSetEnvironmentManager;
    }
    
    private DataSource createDataSource(final Map<String, DataSource> dataSourceMap) throws SQLException, IOException {
        return "masterslaveonly".equals(shardingRuleType)
                ? YamlMasterSlaveDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(shardingRuleType)))
                : YamlShardingDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(shardingRuleType)));
    }
    
    public void assertExecuteUpdateForPreparedStatement() throws SQLException, ParseException, IOException, JAXBException {
        try {
            dataSetEnvironmentManager.initialize();
            try (Connection connection = dataSource.getConnection()) {
                assertThat(DatabaseUtil.executeUpdateForPreparedStatement(connection, sql, integrateTestCaseAssertion.getSQLValues()), is(integrateTestCaseAssertion.getExpectedUpdate()));
            }
            assertDataSet();
        } finally {
            dataSetEnvironmentManager.clear();
        }
    }
    
    public void assertExecuteForPreparedStatement() throws SQLException, ParseException, IOException, JAXBException {
        try {
            dataSetEnvironmentManager.initialize();
            try (Connection connection = dataSource.getConnection()) {
                assertThat(DatabaseUtil.executeDMLForPreparedStatement(connection, sql, integrateTestCaseAssertion.getSQLValues()), is(integrateTestCaseAssertion.getExpectedUpdate()));
            }
            assertDataSet();
        } finally {
            dataSetEnvironmentManager.clear();
        }
    }
    
    public void assertExecuteUpdateForStatement() throws SQLException, ParseException, IOException, JAXBException {
        try {
            dataSetEnvironmentManager.initialize();
            try (Connection connection = dataSource.getConnection()) {
                assertThat(DatabaseUtil.executeUpdateForStatement(connection, sql, integrateTestCaseAssertion.getSQLValues()), is(integrateTestCaseAssertion.getExpectedUpdate()));
            }
            assertDataSet();
        } finally {
            dataSetEnvironmentManager.clear();
        }
    }
    
    public void assertExecuteForStatement() throws SQLException, ParseException, IOException, JAXBException {
        try {
            dataSetEnvironmentManager.initialize();
            try (Connection connection = dataSource.getConnection()) {
                assertThat(DatabaseUtil.executeDMLForStatement(connection, sql, integrateTestCaseAssertion.getSQLValues()), is(integrateTestCaseAssertion.getExpectedUpdate()));
            }
            assertDataSet();
        } finally {
            dataSetEnvironmentManager.clear();
        }
    }
    
    private void assertDataSet() throws IOException, JAXBException, SQLException {
        DataSetsRoot expected;
        try (FileReader reader = new FileReader(expectedDataFile)) {
            expected = (DataSetsRoot) JAXBContext.newInstance(DataSetsRoot.class).createUnmarshaller().unmarshal(reader);
        }
        DataSetAssert.assertDataSet(dataSourceMap, expected);
    }
}
