/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.e2e.engine.type.dml;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.expr.core.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.util.datetime.DateTimeFormatterFactory;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.e2e.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.e2e.engine.composer.BatchE2EContainerComposer;
import org.apache.shardingsphere.test.e2e.engine.composer.E2EContainerComposer;
import org.apache.shardingsphere.test.e2e.engine.composer.SingleE2EContainerComposer;
import org.apache.shardingsphere.test.e2e.env.DataSetEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;
import org.apache.shardingsphere.test.e2e.framework.database.DatabaseAssertionMetaData;
import org.apache.shardingsphere.test.e2e.framework.database.DatabaseAssertionMetaDataFactory;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.framework.param.model.CaseTestParameter;
import org.junit.jupiter.api.AfterEach;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class BaseDMLE2EIT {
    
    private static final String DATA_COLUMN_DELIMITER = ", ";
    
    private DataSetEnvironmentManager dataSetEnvironmentManager;
    
    /**
     * Init.
     *
     * @param testParam test parameter
     * @param containerComposer container composer
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     * @throws JAXBException JAXB exception
     */
    public final void init(final AssertionTestParameter testParam, final SingleE2EContainerComposer containerComposer) throws SQLException, IOException, JAXBException {
        dataSetEnvironmentManager = new DataSetEnvironmentManager(new ScenarioDataPath(testParam.getScenario()).getDataSetFile(Type.ACTUAL), containerComposer.getActualDataSourceMap());
        dataSetEnvironmentManager.fillData();
    }
    
    @AfterEach
    void tearDown() {
        // TODO make sure test case can not be null
        if (null != dataSetEnvironmentManager) {
            dataSetEnvironmentManager.cleanData();
        }
    }
    
    protected final void assertDataSet(final SingleE2EContainerComposer containerComposer, final int actualUpdateCount, final AssertionTestParameter testParam) throws SQLException {
        assertThat(actualUpdateCount, is(containerComposer.getDataSet().getUpdateCount()));
        for (DataSetMetaData each : containerComposer.getDataSet().getMetaDataList()) {
            assertDataSet(containerComposer, each, testParam);
        }
    }
    
    private void assertDataSet(final SingleE2EContainerComposer containerComposer, final DataSetMetaData expectedDataSetMetaData, final AssertionTestParameter testParam) throws SQLException {
        Map<String, DatabaseType> databaseTypes = DatabaseEnvironmentManager.getDatabaseTypes(testParam.getScenario(), testParam.getDatabaseType());
        for (String each : InlineExpressionParserFactory.newInstance(expectedDataSetMetaData.getDataNodes()).splitAndEvaluate()) {
            DataNode dataNode = new DataNode(each);
            DataSource dataSource = containerComposer.getActualDataSourceMap().get(dataNode.getDataSourceName());
            DatabaseType databaseType = databaseTypes.get(dataNode.getDataSourceName());
            try (
                    Connection connection = dataSource.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(generateFetchActualDataSQL(containerComposer.getActualDataSourceMap(), dataNode, databaseType))) {
                assertDataSet(preparedStatement, expectedDataSetMetaData, containerComposer.getDataSet().findRows(dataNode), databaseType);
            }
        }
    }
    
    private void assertDataSet(final PreparedStatement actualPreparedStatement, final DataSetMetaData expectedDataSetMetaData, final List<DataSetRow> expectedDataSetRows,
                               final DatabaseType databaseType) throws SQLException {
        try (ResultSet actualResultSet = actualPreparedStatement.executeQuery()) {
            assertMetaData(actualResultSet.getMetaData(), expectedDataSetMetaData.getColumns());
            assertRows(actualResultSet, expectedDataSetRows, databaseType);
        }
    }
    
    protected final void assertDataSet(final BatchE2EContainerComposer containerComposer, final int[] actualUpdateCounts, final CaseTestParameter testParam) throws SQLException {
        for (DataSetMetaData each : containerComposer.getDataSet(actualUpdateCounts).getMetaDataList()) {
            assertDataSet(containerComposer, actualUpdateCounts, each, testParam);
        }
    }
    
    private void assertDataSet(final BatchE2EContainerComposer containerComposer, final int[] actualUpdateCounts, final DataSetMetaData expectedDataSetMetaData,
                               final CaseTestParameter testParam) throws SQLException {
        Map<String, DatabaseType> databaseTypes = DatabaseEnvironmentManager.getDatabaseTypes(testParam.getScenario(), testParam.getDatabaseType());
        for (String each : InlineExpressionParserFactory.newInstance(expectedDataSetMetaData.getDataNodes()).splitAndEvaluate()) {
            DataNode dataNode = new DataNode(each);
            DatabaseType databaseType = databaseTypes.get(dataNode.getDataSourceName());
            DataSource dataSource = containerComposer.getActualDataSourceMap().get(dataNode.getDataSourceName());
            try (
                    Connection connection = dataSource.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(generateFetchActualDataSQL(containerComposer.getActualDataSourceMap(), dataNode, databaseType))) {
                assertDataSet(preparedStatement, expectedDataSetMetaData, containerComposer.getDataSet(actualUpdateCounts).findRows(dataNode), databaseType);
            }
        }
    }
    
    private String generateFetchActualDataSQL(final Map<String, DataSource> actualDataSourceMap, final DataNode dataNode, final DatabaseType databaseType) throws SQLException {
        Optional<DatabaseAssertionMetaData> databaseAssertionMetaData = DatabaseAssertionMetaDataFactory.newInstance(databaseType);
        if (databaseAssertionMetaData.isPresent()) {
            String primaryKeyColumnName = databaseAssertionMetaData.get().getPrimaryKeyColumnName(actualDataSourceMap.get(dataNode.getDataSourceName()), dataNode.getTableName());
            return String.format("SELECT * FROM %s ORDER BY %s ASC", dataNode.getTableName(), primaryKeyColumnName);
        }
        return String.format("SELECT * FROM %s", dataNode.getTableName());
    }
    
    private void assertMetaData(final ResultSetMetaData actual, final Collection<DataSetColumn> expected) throws SQLException {
        assertThat(actual.getColumnCount(), is(expected.size()));
        int index = 1;
        for (DataSetColumn each : expected) {
            assertThat(actual.getColumnLabel(index++).toUpperCase(), is(each.getName().toUpperCase()));
        }
    }
    
    private void assertRows(final ResultSet actual, final List<DataSetRow> expected, final DatabaseType databaseType) throws SQLException {
        int rowCount = 0;
        while (actual.next()) {
            int columnIndex = 1;
            for (String each : expected.get(rowCount).splitValues(DATA_COLUMN_DELIMITER)) {
                assertValue(actual, columnIndex, each, databaseType);
                columnIndex++;
            }
            rowCount++;
        }
        assertThat("Size of actual result set is different with size of expected dat set rows.", rowCount, is(expected.size()));
    }
    
    private void assertValue(final ResultSet actual, final int columnIndex, final String expected, final DatabaseType databaseType) throws SQLException {
        if (E2EContainerComposer.NOT_VERIFY_FLAG.equals(expected)) {
            return;
        }
        if (Types.DATE == actual.getMetaData().getColumnType(columnIndex)) {
            assertThat(DateTimeFormatterFactory.getDateFormatter().format(actual.getDate(columnIndex).toLocalDate()), is(expected));
        } else if (Arrays.asList(Types.TIME, Types.TIME_WITH_TIMEZONE).contains(actual.getMetaData().getColumnType(columnIndex))) {
            assertThat(DateTimeFormatterFactory.getTimeFormatter().format(actual.getTime(columnIndex).toLocalTime()), is(expected));
        } else if (Arrays.asList(Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE).contains(actual.getMetaData().getColumnType(columnIndex))) {
            if ("Oracle".equals(databaseType.getType()) && "DATE".equalsIgnoreCase(actual.getMetaData().getColumnTypeName(columnIndex)) || "openGauss".equals(databaseType.getType())) {
                assertThat(DateTimeFormatterFactory.getDateFormatter().format(actual.getDate(columnIndex).toLocalDate()), is(expected));
            } else {
                assertThat(DateTimeFormatterFactory.getShortMillsFormatter().format(actual.getTimestamp(columnIndex).toLocalDateTime()), is(expected));
            }
        } else if (Types.CHAR == actual.getMetaData().getColumnType(columnIndex)
                && ("PostgreSQL".equals(databaseType.getType()) || "openGauss".equals(databaseType.getType())
                        || "Oracle".equals(databaseType.getType()))) {
            assertThat(String.valueOf(actual.getObject(columnIndex)).trim(), is(expected));
        } else if (isPostgreSQLOrOpenGaussMoney(actual.getMetaData().getColumnTypeName(columnIndex), databaseType)) {
            assertThat(actual.getString(columnIndex), is(expected));
        } else if (Types.BINARY == actual.getMetaData().getColumnType(columnIndex)) {
            assertThat(actual.getObject(columnIndex), is(expected.getBytes(StandardCharsets.UTF_8)));
        } else {
            assertThat(String.valueOf(actual.getObject(columnIndex)), is(expected));
        }
    }
    
    private boolean isPostgreSQLOrOpenGaussMoney(final String columnTypeName, final DatabaseType databaseType) {
        return "money".equalsIgnoreCase(columnTypeName) && ("PostgreSQL".equals(databaseType.getType()) || "openGauss".equals(databaseType.getType()));
    }
    
    protected void assertGeneratedKeys(final SingleE2EContainerComposer containerComposer, final ResultSet generatedKeys, final DatabaseType databaseType) throws SQLException {
        if (null == containerComposer.getGeneratedKeyDataSet()) {
            return;
        }
        assertThat("Only support single table for DML.", containerComposer.getGeneratedKeyDataSet().getMetaDataList().size(), is(1));
        assertMetaData(generatedKeys.getMetaData(), containerComposer.getGeneratedKeyDataSet().getMetaDataList().get(0).getColumns());
        assertRows(generatedKeys, containerComposer.getGeneratedKeyDataSet().getRows(), databaseType);
    }
}
