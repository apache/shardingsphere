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
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.util.InlineExpressionParser;
import io.shardingsphere.dbtest.common.DatabaseUtil;
import io.shardingsphere.dbtest.common.SQLValue;
import io.shardingsphere.dbtest.config.bean.DMLSubAssert;
import io.shardingsphere.dbtest.config.dataset.init.DataSetColumnMetadata;
import io.shardingsphere.dbtest.config.dataset.init.DataSetMetadata;
import io.shardingsphere.dbtest.config.dataset.init.DataSetRow;
import io.shardingsphere.dbtest.config.dataset.init.DataSetsRoot;
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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DMLAssertEngine {
    
    private final DMLSubAssert dmlSubAssert;
    
    private final String shardingRuleType;
    
    private final SQLCaseType caseType;
    
    private Map<String, DataSource> dataSourceMap;
    
    private final DataSource dataSource;
    
    private final String sql;
    
    private final String expectedDataFile;
    
    private final DataSetEnvironmentManager dataSetEnvironmentManager;
    
    public DMLAssertEngine(final String sqlCaseId, final String path, final DataSetEnvironmentManager dataSetEnvironmentManager, 
                           final DMLSubAssert dmlSubAssert, final Map<String, DataSource> dataSourceMap, final String shardingRuleType, final SQLCaseType caseType) throws IOException, SQLException {
        this.dmlSubAssert = dmlSubAssert;
        this.shardingRuleType = shardingRuleType;
        this.caseType = caseType;
        this.dataSourceMap = dataSourceMap;
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
     * 
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     * @throws ParseException parse exception
     * @throws JAXBException JAXB exception
     */
    public void assertDML() throws IOException, SQLException, ParseException, JAXBException {
        assertExecuteUpdateForPreparedStatement();
        assertExecuteForPreparedStatement();
        assertExecuteUpdateForStatement();
        assertExecuteForStatement();
    }
    
    private void assertExecuteUpdateForPreparedStatement() throws SQLException, ParseException, IOException, JAXBException {
        try {
            dataSetEnvironmentManager.initialize();
            try (Connection connection = dataSource.getConnection()) {
                assertThat(DatabaseUtil.executeUpdateForPreparedStatement(connection, sql, getSQLValues(dmlSubAssert.getParameters())), is(dmlSubAssert.getExpectedUpdate()));
            }
            assertDataSet();
        } finally {
            dataSetEnvironmentManager.clear();
        }
    }
    
    private void assertExecuteForPreparedStatement() throws SQLException, ParseException, IOException, JAXBException {
        try {
            dataSetEnvironmentManager.initialize();
            try (Connection connection = dataSource.getConnection()) {
                assertThat(DatabaseUtil.executeDMLForPreparedStatement(connection, sql, getSQLValues(dmlSubAssert.getParameters())), is(dmlSubAssert.getExpectedUpdate()));
            }
            assertDataSet();
        } finally {
            dataSetEnvironmentManager.clear();
        }
    }
    
    private void assertExecuteUpdateForStatement() throws SQLException, ParseException, IOException, JAXBException {
        try {
            dataSetEnvironmentManager.initialize();
            try (Connection connection = dataSource.getConnection()) {
                assertThat(DatabaseUtil.executeUpdateForStatement(connection, sql, getSQLValues(dmlSubAssert.getParameters())), is(dmlSubAssert.getExpectedUpdate()));
            }
            assertDataSet();
        } finally {
            dataSetEnvironmentManager.clear();
        }
    }
    
    private void assertExecuteForStatement() throws SQLException, ParseException, IOException, JAXBException {
        try {
            dataSetEnvironmentManager.initialize();
            try (Connection connection = dataSource.getConnection()) {
                assertThat(DatabaseUtil.executeDMLForStatement(connection, sql, getSQLValues(dmlSubAssert.getParameters())), is(dmlSubAssert.getExpectedUpdate()));
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
        assertThat("Only support single table for DML.", expected.getMetadataList().size(), is(1));
        DataSetMetadata dataSetMetadata = expected.getMetadataList().get(0);
        for (String each : new InlineExpressionParser(dataSetMetadata.getDataNodes()).evaluate()) {
            DataNode dataNode = new DataNode(each);
            try (Connection connection = dataSourceMap.get(dataNode.getDataSourceName()).getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM %s", dataNode.getTableName()))) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    int count = 0;
                    while (resultSet.next()) {
                        List<String> actualResultSetData = getResultSetData(dataSetMetadata, resultSet);
                        assertTrue(String.format("Cannot find actual record '%s' from data node '%s'", actualResultSetData, each), isMatch(each, actualResultSetData, expected.getDataSetRows()));
                        count++;
                    }
                    assertThat(String.format("Count of records are different for data node '%s'", each), count, is(countExpectedDataSetRows(each, expected.getDataSetRows())));
                }
            }
        }
    }
    
    private List<String> getResultSetData(final DataSetMetadata dataSetMetadata, final ResultSet resultSet) throws SQLException {
        List<String> result = new ArrayList<>(dataSetMetadata.getColumnMetadataList().size());
        for (DataSetColumnMetadata each : dataSetMetadata.getColumnMetadataList()) {
            Object resultSetValue = resultSet.getObject(each.getName());
            result.add(resultSetValue instanceof Date ? new SimpleDateFormat("yyyy-MM-dd").format(resultSetValue) : resultSetValue.toString());
        }
        return result;
    }
    
    private boolean isMatch(final String actualDataNode, final List<String> actualResultSetData, final List<DataSetRow> expectedDataSetRows) {
        for (DataSetRow each : expectedDataSetRows) {
            if (each.getDataNode().equals(actualDataNode) && isMatch(actualResultSetData, each)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isMatch(final List<String> actualResultSetData, final DataSetRow expectedDataSetRow) {
        int count = 0;
        for (String each : Splitter.on(",").trimResults().splitToList(expectedDataSetRow.getValues())) {
            if (!each.equals(actualResultSetData.get(count))) {
                return false;
            }
            count++;
        }
        return true;
    }
    
    private int countExpectedDataSetRows(final String actualDataNode, final List<DataSetRow> expectedDataSetRows) {
        int result = 0;
        for (DataSetRow each : expectedDataSetRows) {
            if (each.getDataNode().equals(actualDataNode)) {
                result++;
            }
            
        }
        return result;
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
