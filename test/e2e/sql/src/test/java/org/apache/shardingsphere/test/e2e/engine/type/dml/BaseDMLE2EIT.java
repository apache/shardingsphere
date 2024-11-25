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

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.expr.core.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.util.datetime.DateTimeFormatterFactory;
import org.apache.shardingsphere.test.e2e.cases.casse.assertion.E2ETestCaseAssertion;
import org.apache.shardingsphere.test.e2e.cases.dataset.DataSet;
import org.apache.shardingsphere.test.e2e.cases.dataset.DataSetLoader;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.e2e.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.e2e.engine.context.E2ETestContext;
import org.apache.shardingsphere.test.e2e.env.DataSetEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.E2EEnvironmentAware;
import org.apache.shardingsphere.test.e2e.env.E2EEnvironmentEngine;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;
import org.apache.shardingsphere.test.e2e.framework.database.DatabaseAssertionMetaData;
import org.apache.shardingsphere.test.e2e.framework.database.DatabaseAssertionMetaDataFactory;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.framework.param.model.CaseTestParameter;
import org.apache.shardingsphere.test.e2e.framework.param.model.E2ETestParameter;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class BaseDMLE2EIT implements E2EEnvironmentAware {
    
    private static final String DATA_COLUMN_DELIMITER = ", ";
    
    private DataSetEnvironmentManager dataSetEnvironmentManager;
    
    @Getter
    private E2EEnvironmentEngine environmentEngine;
    
    @Override
    public final void setEnvironmentEngine(final E2EEnvironmentEngine environmentEngine) {
        this.environmentEngine = environmentEngine;
    }
    
    /**
     * Init.
     *
     * @param testParam test parameter
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     * @throws JAXBException JAXB exception
     */
    protected void init(final E2ETestParameter testParam) throws SQLException, IOException, JAXBException {
        dataSetEnvironmentManager =
                new DataSetEnvironmentManager(new ScenarioDataPath(testParam.getScenario()).getDataSetFile(Type.ACTUAL), getEnvironmentEngine().getActualDataSourceMap(), testParam.getDatabaseType());
        dataSetEnvironmentManager.fillData();
    }
    
    void tearDown() {
        // TODO make sure test case can not be null
        if (null != dataSetEnvironmentManager) {
            dataSetEnvironmentManager.cleanData();
        }
    }
    
    private DataSet getDataSet(final int[] actualUpdateCounts, final Collection<DataSet> dataSets, final String sql) {
        Collection<DataSet> result = new LinkedList<>();
        assertThat(actualUpdateCounts.length, is(dataSets.size()));
        int count = 0;
        for (DataSet each : dataSets) {
            if (Statement.SUCCESS_NO_INFO != actualUpdateCounts[count]) {
                assertThat(actualUpdateCounts[count], is(each.getUpdateCount()));
            }
            result.add(each);
            count++;
        }
        return mergeDataSets(result, sql);
    }
    
    private DataSet mergeDataSets(final Collection<DataSet> dataSets, final String sql) {
        DataSet result = new DataSet();
        Set<DataSetRow> existedRows = new HashSet<>();
        for (DataSet each : dataSets) {
            mergeMetaData(each, result);
            if (sql.trim().toUpperCase().startsWith("DELETE")) {
                mergeDeleteRow(each, result, existedRows);
                continue;
            } else if (sql.trim().toUpperCase().startsWith("UPDATE")) {
                mergeUpdateRow(each, result, existedRows);
                continue;
            }
            mergeRow(each, result, existedRows);
        }
        sortRow(result);
        return result;
    }
    
    private void mergeDeleteRow(final DataSet original, final DataSet dist, final Set<DataSetRow> existedRows) {
        Collection<DataSetRow> removedRows = getDifferentRows(existedRows, new HashSet<>(original.getRows()));
        mergeRow(original, dist, existedRows);
        dist.getRows().removeAll(removedRows);
    }
    
    private Collection<DataSetRow> getDifferentRows(final Set<DataSetRow> existedRows, final Set<DataSetRow> originalRows) {
        if (existedRows.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<DataSetRow> result = new HashSet<>();
        result.addAll(Sets.difference(existedRows, originalRows));
        result.addAll(Sets.difference(originalRows, existedRows));
        return result;
    }
    
    private void mergeUpdateRow(final DataSet original, final DataSet dist, final Set<DataSetRow> existedRows) {
        Collection<DataSetRow> removedRows = getDifferentRows(existedRows, new HashSet<>(original.getRows())).stream().filter(each -> !each.isUpdated()).collect(Collectors.toList());
        mergeRow(original, dist, existedRows);
        dist.getRows().removeAll(removedRows);
    }
    
    private void mergeMetaData(final DataSet original, final DataSet dist) {
        if (dist.getMetaDataList().isEmpty()) {
            dist.getMetaDataList().addAll(original.getMetaDataList());
        }
    }
    
    private void mergeRow(final DataSet original, final DataSet dist, final Set<DataSetRow> existedRows) {
        for (DataSetRow each : original.getRows()) {
            if (existedRows.add(each)) {
                dist.getRows().add(each);
            }
        }
    }
    
    private void sortRow(final DataSet dataSet) {
        dataSet.getRows().sort(Comparator.comparingLong(o -> Long.parseLong(o.splitValues(",").get(0))));
    }
    
    protected final void assertDataSet(final E2ETestContext context, final int actualUpdateCount, final AssertionTestParameter testParam) throws SQLException {
        assertThat(actualUpdateCount, is(context.getDataSet().getUpdateCount()));
        for (DataSetMetaData each : context.getDataSet().getMetaDataList()) {
            assertDataSet(context, each, testParam);
        }
    }
    
    private void assertDataSet(final E2ETestContext context, final DataSetMetaData expectedDataSetMetaData, final AssertionTestParameter testParam) throws SQLException {
        Map<String, DatabaseType> databaseTypes = DatabaseEnvironmentManager.getDatabaseTypes(testParam.getScenario(), testParam.getDatabaseType());
        for (String each : InlineExpressionParserFactory.newInstance(expectedDataSetMetaData.getDataNodes()).splitAndEvaluate()) {
            DataNode dataNode = new DataNode(each);
            DataSource dataSource = getEnvironmentEngine().getActualDataSourceMap().get(dataNode.getDataSourceName());
            DatabaseType databaseType = databaseTypes.get(dataNode.getDataSourceName());
            try (
                    Connection connection = dataSource.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(generateFetchActualDataSQL(getEnvironmentEngine().getActualDataSourceMap(), dataNode, databaseType))) {
                assertDataSet(preparedStatement, expectedDataSetMetaData, context.getDataSet().findRows(dataNode), databaseType);
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
    
    protected final void assertDataSet(final int[] actualUpdateCounts, final CaseTestParameter testParam) throws SQLException {
        Collection<DataSet> dataSets = new LinkedList<>();
        for (E2ETestCaseAssertion each : testParam.getTestCaseContext().getTestCase().getAssertions()) {
            dataSets.add(DataSetLoader.load(testParam.getTestCaseContext().getParentPath(), testParam.getScenario(), testParam.getDatabaseType(), testParam.getMode(), each.getExpectedDataFile()));
        }
        DataSet dataSet = getDataSet(actualUpdateCounts, dataSets, testParam.getTestCaseContext().getTestCase().getSql());
        for (DataSetMetaData each : dataSet.getMetaDataList()) {
            assertDataSet(each, testParam, dataSet);
        }
    }
    
    private void assertDataSet(final DataSetMetaData expectedDataSetMetaData, final CaseTestParameter testParam, final DataSet dataSet) throws SQLException {
        Map<String, DatabaseType> databaseTypes = DatabaseEnvironmentManager.getDatabaseTypes(testParam.getScenario(), testParam.getDatabaseType());
        for (String each : InlineExpressionParserFactory.newInstance(expectedDataSetMetaData.getDataNodes()).splitAndEvaluate()) {
            DataNode dataNode = new DataNode(each);
            DatabaseType databaseType = databaseTypes.get(dataNode.getDataSourceName());
            DataSource dataSource = getEnvironmentEngine().getActualDataSourceMap().get(dataNode.getDataSourceName());
            try (
                    Connection connection = dataSource.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(generateFetchActualDataSQL(getEnvironmentEngine().getActualDataSourceMap(), dataNode, databaseType))) {
                assertDataSet(preparedStatement, expectedDataSetMetaData, dataSet.findRows(dataNode), databaseType);
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
        assertThat("Size of actual result set is different with size of expected data set rows.", rowCount, is(expected.size()));
    }
    
    private void assertValue(final ResultSet actual, final int columnIndex, final String expected, final DatabaseType databaseType) throws SQLException {
        if (E2ETestContext.NOT_VERIFY_FLAG.equals(expected)) {
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
    
    protected void assertGeneratedKeys(final AssertionTestParameter testParam, final ResultSet generatedKeys, final DatabaseType databaseType) throws SQLException {
        DataSet generatedKeyDataSet = null == testParam.getAssertion() || null == testParam.getAssertion().getExpectedGeneratedKeyDataFile()
                ? null
                : DataSetLoader.load(
                        testParam.getTestCaseContext().getParentPath(), testParam.getScenario(), testParam.getDatabaseType(), testParam.getMode(),
                        testParam.getAssertion().getExpectedGeneratedKeyDataFile());
        if (null == generatedKeyDataSet) {
            return;
        }
        assertThat("Only support single table for DML.", generatedKeyDataSet.getMetaDataList().size(), is(1));
        assertMetaData(generatedKeys.getMetaData(), generatedKeyDataSet.getMetaDataList().get(0).getColumns());
        assertRows(generatedKeys, generatedKeyDataSet.getRows(), databaseType);
    }
    
    protected void executeInitSQLs(final E2ETestCaseAssertion assertion) throws SQLException {
        if (null == assertion.getInitialSQL()) {
            return;
        }
        try (Connection connection = getEnvironmentEngine().getTargetDataSource().getConnection()) {
            executeInitSQLs(assertion, connection);
        }
    }
    
    private void executeInitSQLs(final E2ETestCaseAssertion assertion, final Connection connection) throws SQLException {
        if (null == assertion.getInitialSQL().getSql()) {
            return;
        }
        for (String each : Splitter.on(";").trimResults().omitEmptyStrings().splitToList(assertion.getInitialSQL().getSql())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(each)) {
                preparedStatement.executeUpdate();
            }
            waitCompleted();
        }
    }
    
    private void waitCompleted() {
        Awaitility.await().pollDelay(1500L, TimeUnit.MILLISECONDS).until(() -> true);
    }
    
    protected void executeDestroySQLs(final E2ETestCaseAssertion assertion) throws SQLException {
        if (null != assertion.getDestroySQL()) {
            try (Connection connection = getEnvironmentEngine().getTargetDataSource().getConnection()) {
                executeDestroySQLs(assertion, connection);
            }
        }
    }
    
    private void executeDestroySQLs(final E2ETestCaseAssertion assertion, final Connection connection) throws SQLException {
        if (null == assertion.getDestroySQL().getSql()) {
            return;
        }
        for (String each : Splitter.on(";").trimResults().omitEmptyStrings().splitToList(assertion.getDestroySQL().getSql())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(each)) {
                preparedStatement.executeUpdate();
            }
            waitCompleted();
        }
    }
}
