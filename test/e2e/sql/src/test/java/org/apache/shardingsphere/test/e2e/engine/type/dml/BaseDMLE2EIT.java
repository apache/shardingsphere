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

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.expr.core.InlineExpressionParserFactory;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.e2e.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.e2e.engine.composer.E2EContainerComposer;
import org.apache.shardingsphere.test.e2e.engine.composer.SingleE2EContainerComposer;
import org.apache.shardingsphere.test.e2e.env.DataSetEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;
import org.apache.shardingsphere.test.e2e.framework.database.DatabaseAssertionMetaData;
import org.apache.shardingsphere.test.e2e.framework.database.DatabaseAssertionMetaDataFactory;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;
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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class BaseDMLE2EIT {
    
    private static final String DATA_COLUMN_DELIMITER = ", ";
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private final DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
    
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
    
    protected final void assertDataSet(final AssertionTestParameter testParam, final SingleE2EContainerComposer containerComposer, final int actualUpdateCount) throws SQLException {
        assertThat("Only support single table for DML.", containerComposer.getDataSet().getMetaDataList().size(), is(1));
        assertThat(actualUpdateCount, is(containerComposer.getDataSet().getUpdateCount()));
        DataSetMetaData expectedDataSetMetaData = containerComposer.getDataSet().getMetaDataList().get(0);
        for (String each : InlineExpressionParserFactory.newInstance().splitAndEvaluate(expectedDataSetMetaData.getDataNodes())) {
            DataNode dataNode = new DataNode(each);
            DataSource dataSource = containerComposer.getActualDataSourceMap().get(dataNode.getDataSourceName());
            try (
                    Connection connection = dataSource.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(generateFetchActualDataSQL(testParam, containerComposer, dataNode))) {
                assertDataSet(testParam, preparedStatement, expectedDataSetMetaData, containerComposer.getDataSet().findRows(dataNode));
            }
        }
    }
    
    private void assertDataSet(final AssertionTestParameter testParam,
                               final PreparedStatement actualPreparedStatement, final DataSetMetaData expectedDataSetMetaData, final List<DataSetRow> expectedDataSetRows) throws SQLException {
        try (ResultSet actualResultSet = actualPreparedStatement.executeQuery()) {
            assertMetaData(actualResultSet.getMetaData(), expectedDataSetMetaData.getColumns());
            assertRows(testParam, actualResultSet, expectedDataSetRows);
        }
    }
    
    private String generateFetchActualDataSQL(final AssertionTestParameter testParam, final SingleE2EContainerComposer containerComposer, final DataNode dataNode) throws SQLException {
        Optional<DatabaseAssertionMetaData> databaseAssertionMetaData = DatabaseAssertionMetaDataFactory.newInstance(testParam.getDatabaseType());
        if (databaseAssertionMetaData.isPresent()) {
            String primaryKeyColumnName = databaseAssertionMetaData.get().getPrimaryKeyColumnName(
                    containerComposer.getActualDataSourceMap().get(dataNode.getDataSourceName()), dataNode.getTableName());
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
    
    private void assertRows(final AssertionTestParameter testParam, final ResultSet actual, final List<DataSetRow> expected) throws SQLException {
        int rowCount = 0;
        while (actual.next()) {
            int columnIndex = 1;
            for (String each : expected.get(rowCount).splitValues(DATA_COLUMN_DELIMITER)) {
                assertValue(testParam, actual, columnIndex, each);
                columnIndex++;
            }
            rowCount++;
        }
        assertThat("Size of actual result set is different with size of expected dat set rows.", rowCount, is(expected.size()));
    }
    
    private void assertValue(final AssertionTestParameter testParam, final ResultSet actual, final int columnIndex, final String expected) throws SQLException {
        if (E2EContainerComposer.NOT_VERIFY_FLAG.equals(expected)) {
            return;
        }
        if (Types.DATE == actual.getMetaData().getColumnType(columnIndex)) {
            assertThat(dateFormatter.format(actual.getDate(columnIndex).toLocalDate()), is(expected));
        } else if (Arrays.asList(Types.TIME, Types.TIME_WITH_TIMEZONE).contains(actual.getMetaData().getColumnType(columnIndex))) {
            assertThat(timeFormatter.format(actual.getTime(columnIndex).toLocalTime()), is(expected));
        } else if (Arrays.asList(Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE).contains(actual.getMetaData().getColumnType(columnIndex))) {
            assertThat(timestampFormatter.format(actual.getTimestamp(columnIndex).toLocalDateTime()), is(expected));
        } else if (Types.CHAR == actual.getMetaData().getColumnType(columnIndex)
                && ("PostgreSQL".equals(testParam.getDatabaseType().getType()) || "openGauss".equals(testParam.getDatabaseType().getType())
                        || "Oracle".equals(testParam.getDatabaseType().getType()))) {
            assertThat(String.valueOf(actual.getObject(columnIndex)).trim(), is(expected));
        } else if (isPostgreSQLOrOpenGaussMoney(testParam, actual.getMetaData().getColumnTypeName(columnIndex))) {
            assertThat(actual.getString(columnIndex), is(expected));
        } else if (Types.BINARY == actual.getMetaData().getColumnType(columnIndex)) {
            assertThat(actual.getObject(columnIndex), is(expected.getBytes(StandardCharsets.UTF_8)));
        } else {
            assertThat(String.valueOf(actual.getObject(columnIndex)), is(expected));
        }
    }
    
    private boolean isPostgreSQLOrOpenGaussMoney(final AssertionTestParameter testParam, final String columnTypeName) {
        return "money".equalsIgnoreCase(columnTypeName) && ("PostgreSQL".equals(testParam.getDatabaseType().getType()) || "openGauss".equals(testParam.getDatabaseType().getType()));
    }
    
    protected void assertGeneratedKeys(final AssertionTestParameter testParam, final SingleE2EContainerComposer containerComposer, final ResultSet generatedKeys) throws SQLException {
        if (null == containerComposer.getGeneratedKeyDataSet()) {
            return;
        }
        assertThat("Only support single table for DML.", containerComposer.getGeneratedKeyDataSet().getMetaDataList().size(), is(1));
        assertMetaData(generatedKeys.getMetaData(), containerComposer.getGeneratedKeyDataSet().getMetaDataList().get(0).getColumns());
        assertRows(testParam, generatedKeys, containerComposer.getGeneratedKeyDataSet().getRows());
    }
}
