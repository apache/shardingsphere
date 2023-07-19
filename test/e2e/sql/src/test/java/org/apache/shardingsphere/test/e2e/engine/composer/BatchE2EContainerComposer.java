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

package org.apache.shardingsphere.test.e2e.engine.composer;

import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.expr.core.InlineExpressionParserFactory;
import org.apache.shardingsphere.test.e2e.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.e2e.cases.dataset.DataSet;
import org.apache.shardingsphere.test.e2e.cases.dataset.DataSetLoader;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.e2e.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.e2e.env.DataSetEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;
import org.apache.shardingsphere.test.e2e.framework.param.model.CaseTestParameter;

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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Batch E2E container composer.
 */
public final class BatchE2EContainerComposer extends E2EContainerComposer implements AutoCloseable {
    
    private final DatabaseType databaseType;
    
    private final Collection<DataSet> dataSets = new LinkedList<>();
    
    private final DataSetEnvironmentManager dataSetEnvironmentManager;
    
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public BatchE2EContainerComposer(final CaseTestParameter testParam) throws JAXBException, IOException {
        super(testParam);
        databaseType = testParam.getDatabaseType();
        for (IntegrationTestCaseAssertion each : testParam.getTestCaseContext().getTestCase().getAssertions()) {
            dataSets.add(DataSetLoader.load(testParam.getTestCaseContext().getParentPath(), testParam.getScenario(), testParam.getDatabaseType(), testParam.getMode(), each.getExpectedDataFile()));
        }
        dataSetEnvironmentManager = new DataSetEnvironmentManager(new ScenarioDataPath(testParam.getScenario()).getDataSetFile(Type.ACTUAL), getActualDataSourceMap());
        dataSetEnvironmentManager.fillData();
    }
    
    /**
     * Assert data sets.
     * 
     * @param actualUpdateCounts actual update counts
     * @throws SQLException SQL exception
     */
    public void assertDataSets(final int[] actualUpdateCounts) throws SQLException {
        DataSet expected = getDataSet(actualUpdateCounts);
        assertThat("Only support single table for DML.", expected.getMetaDataList().size(), is(1));
        DataSetMetaData expectedDataSetMetaData = expected.getMetaDataList().get(0);
        for (String each : InlineExpressionParserFactory.newInstance().splitAndEvaluate(expectedDataSetMetaData.getDataNodes())) {
            DataNode dataNode = new DataNode(each);
            DataSource dataSource = getActualDataSourceMap().get(dataNode.getDataSourceName());
            try (
                    Connection connection = dataSource.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM %s ORDER BY 1", dataNode.getTableName()))) {
                assertDataSet(preparedStatement, expected.findRows(dataNode), expectedDataSetMetaData);
            }
        }
    }
    
    private DataSet getDataSet(final int[] actualUpdateCounts) {
        Collection<DataSet> dataSets = new LinkedList<>();
        assertThat(actualUpdateCounts.length, is(this.dataSets.size()));
        int count = 0;
        for (DataSet each : this.dataSets) {
            assertThat(actualUpdateCounts[count], is(each.getUpdateCount()));
            dataSets.add(each);
            count++;
        }
        return mergeDataSets(dataSets);
    }
    
    private DataSet mergeDataSets(final Collection<DataSet> dataSets) {
        DataSet result = new DataSet();
        Set<DataSetRow> existedRows = new HashSet<>();
        for (DataSet each : dataSets) {
            mergeMetaData(each, result);
            mergeRow(each, result, existedRows);
        }
        sortRow(result);
        return result;
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
    
    private void assertDataSet(final PreparedStatement actualPreparedStatement, final List<DataSetRow> expectedDataSetRows, final DataSetMetaData expectedDataSetMetaData) throws SQLException {
        try (ResultSet actualResultSet = actualPreparedStatement.executeQuery()) {
            assertMetaData(actualResultSet.getMetaData(), expectedDataSetMetaData.getColumns());
            assertRows(actualResultSet, expectedDataSetRows);
        }
    }
    
    private void assertMetaData(final ResultSetMetaData actualMetaData, final Collection<DataSetColumn> columnMetaDataList) throws SQLException {
        assertThat(actualMetaData.getColumnCount(), is(columnMetaDataList.size()));
        int index = 1;
        for (DataSetColumn each : columnMetaDataList) {
            assertThat(actualMetaData.getColumnLabel(index++), is(each.getName()));
        }
    }
    
    private void assertRows(final ResultSet actual, final List<DataSetRow> expectedDatSetRows) throws SQLException {
        int count = 0;
        while (actual.next()) {
            int columnIndex = 1;
            for (String expected : expectedDatSetRows.get(count).splitValues(", ")) {
                if (Types.DATE == actual.getMetaData().getColumnType(columnIndex)) {
                    if (!E2EContainerComposer.NOT_VERIFY_FLAG.equals(expected)) {
                        assertThat(dateTimeFormatter.format(actual.getDate(columnIndex).toLocalDate()), is(expected));
                    }
                } else if (Types.CHAR == actual.getMetaData().getColumnType(columnIndex)
                        && ("PostgreSQL".equals(databaseType.getType()) || "openGauss".equals(databaseType.getType()))) {
                    assertThat(String.valueOf(actual.getObject(columnIndex)).trim(), is(expected));
                } else if (isPostgreSQLOrOpenGaussMoney(actual.getMetaData().getColumnTypeName(columnIndex))) {
                    assertThat(actual.getString(columnIndex), is(expected));
                } else if (Types.BINARY == actual.getMetaData().getColumnType(columnIndex)) {
                    assertThat(actual.getObject(columnIndex), is(expected.getBytes(StandardCharsets.UTF_8)));
                } else {
                    assertThat(String.valueOf(actual.getObject(columnIndex)), is(expected));
                }
                columnIndex++;
            }
            count++;
        }
        assertThat("Size of actual result set is different with size of expected dat set rows.", count, is(expectedDatSetRows.size()));
    }
    
    private boolean isPostgreSQLOrOpenGaussMoney(final String columnTypeName) {
        return "money".equalsIgnoreCase(columnTypeName) && ("PostgreSQL".equals(databaseType.getType()) || "openGauss".equals(databaseType.getType()));
    }
    
    @Override
    public void close() {
        dataSetEnvironmentManager.cleanData();
    }
}
