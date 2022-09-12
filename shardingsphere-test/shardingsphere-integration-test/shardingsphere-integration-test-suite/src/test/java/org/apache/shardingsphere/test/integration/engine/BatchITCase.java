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

package org.apache.shardingsphere.test.integration.engine;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.util.expr.InlineExpressionParser;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.integration.cases.dataset.DataSet;
import org.apache.shardingsphere.test.integration.cases.dataset.DataSetLoader;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.integration.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.integration.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.integration.env.runtime.scenario.path.ScenarioDataPath.Type;
import org.apache.shardingsphere.test.integration.env.DataSetEnvironmentManager;
import org.apache.shardingsphere.test.integration.framework.param.model.CaseParameterizedArray;
import org.junit.After;
import org.junit.Before;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class BatchITCase extends BaseITCase {
    
    private final Collection<DataSet> dataSets = new LinkedList<>();
    
    private final String parentPath;
    
    private DataSetEnvironmentManager dataSetEnvironmentManager;
    
    public BatchITCase(final CaseParameterizedArray parameterizedArray) {
        super(parameterizedArray);
        parentPath = parameterizedArray.getTestCaseContext().getParentPath();
    }
    
    @Before
    public void init() throws Exception {
        for (IntegrationTestCaseAssertion each : getItCase().getAssertions()) {
            dataSets.add(DataSetLoader.load(parentPath, getScenario(), getDatabaseType(), each.getExpectedDataFile()));
        }
        dataSetEnvironmentManager = new DataSetEnvironmentManager(new ScenarioDataPath(getScenario()).getDataSetFile(Type.ACTUAL), getActualDataSourceMap());
        dataSetEnvironmentManager.fillData();
    }
    
    @After
    public void tearDown() {
        dataSetEnvironmentManager.cleanData();
    }
    
    protected final void assertDataSets(final int[] actualUpdateCounts) throws SQLException {
        DataSet expected = getDataSet(actualUpdateCounts);
        assertThat("Only support single table for DML.", expected.getMetaDataList().size(), is(1));
        DataSetMetaData expectedDataSetMetaData = expected.getMetaDataList().get(0);
        for (String each : new InlineExpressionParser(expectedDataSetMetaData.getDataNodes()).splitAndEvaluate()) {
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
        dataSet.getRows().sort(Comparator.comparingInt(o -> Integer.parseInt(o.splitValues(",").get(0))));
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
    
    private void assertRows(final ResultSet actualResultSet, final List<DataSetRow> expectedDatSetRows) throws SQLException {
        int count = 0;
        while (actualResultSet.next()) {
            int index = 1;
            for (String each : expectedDatSetRows.get(count).splitValues(",")) {
                if (Types.DATE == actualResultSet.getMetaData().getColumnType(index)) {
                    if (!NOT_VERIFY_FLAG.equals(each)) {
                        assertThat(new SimpleDateFormat("yyyy-MM-dd").format(actualResultSet.getDate(index)), is(each));
                    }
                } else {
                    assertThat(String.valueOf(actualResultSet.getObject(index)), is(each));
                }
                index++;
            }
            count++;
        }
        assertThat("Size of actual result set is different with size of expected dat set rows.", count, is(expectedDatSetRows.size()));
    }
}
