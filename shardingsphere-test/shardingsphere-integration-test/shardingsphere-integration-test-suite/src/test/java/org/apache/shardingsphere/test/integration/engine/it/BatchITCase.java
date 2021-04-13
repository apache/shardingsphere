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

package org.apache.shardingsphere.test.integration.engine.it;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineExpressionParser;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.integration.cases.dataset.DataSet;
import org.apache.shardingsphere.test.integration.cases.dataset.DataSetLoader;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetMetadata;
import org.apache.shardingsphere.test.integration.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.env.dataset.DataSetEnvironmentManager;
import org.apache.shardingsphere.test.integration.junit.compose.GovernanceContainerCompose;
import org.apache.shardingsphere.test.integration.junit.param.model.CaseParameterizedArray;
import org.junit.After;
import org.junit.Before;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Getter(AccessLevel.PROTECTED)
public abstract class BatchITCase extends BaseITCase {
    
    private final Collection<DataSet> dataSets = new LinkedList<>();
    
    private final String parentPath;
    
    private DataSetEnvironmentManager dataSetEnvironmentManager;
    
    public BatchITCase(final CaseParameterizedArray parameterizedArray) {
        super(parameterizedArray);
        this.parentPath = parameterizedArray.getTestCaseContext().getParentPath();
    }
    
    @Before
    public void fillData() throws SQLException, ParseException, IOException, JAXBException {
        for (IntegrationTestCaseAssertion each : getIntegrationTestCase().getAssertions()) {
            dataSets.add(DataSetLoader.load(getParentPath(), getScenario(), getDatabaseType(), each.getExpectedDataFile()));
        }
        dataSetEnvironmentManager = new DataSetEnvironmentManager(EnvironmentPath.getDataSetFile(getScenario()), getStorageContainer().getDataSourceMap());
        dataSetEnvironmentManager.fillData();
    }
    
    @Override
    protected String getSQL() throws ParseException {
        return getIntegrationTestCase().getSql();
    }
    
    @After
    public void clearData() {
        dataSetEnvironmentManager.clearData();
    }
    
    protected final void assertDataSets(final int[] actualUpdateCounts) throws SQLException {
        DataSet expected = getDataSet(actualUpdateCounts);
        assertThat("Only support single table for DML.", expected.getMetadataList().size(), is(1));
        DataSetMetadata expectedDataSetMetadata = expected.getMetadataList().get(0);
        for (String each : new InlineExpressionParser(expectedDataSetMetadata.getDataNodes()).splitAndEvaluate()) {
            DataNode dataNode = new DataNode(each);
            try (Connection connection = getCompose() instanceof GovernanceContainerCompose
                    ? getCompose().getDataSourceMap().get("adapterForReader").getConnection() : getStorageContainer().getDataSourceMap().get(dataNode.getDataSourceName()).getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM %s ORDER BY 1", dataNode.getTableName()))) {
                assertDataSet(preparedStatement, expected.findRows(dataNode), expectedDataSetMetadata);
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
            mergeMetadata(each, result);
            mergeRow(each, result, existedRows);
        }
        sortRow(result);
        return result;
    }
    
    private void mergeMetadata(final DataSet original, final DataSet dist) {
        if (dist.getMetadataList().isEmpty()) {
            dist.getMetadataList().addAll(original.getMetadataList());
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
        dataSet.getRows().sort(Comparator.comparingInt(o -> Integer.parseInt(o.getValues().get(0))));
    }
    
    private void assertDataSet(final PreparedStatement actualPreparedStatement, final List<DataSetRow> expectedDataSetRows, final DataSetMetadata expectedDataSetMetadata) throws SQLException {
        try (ResultSet actualResultSet = actualPreparedStatement.executeQuery()) {
            assertMetaData(actualResultSet.getMetaData(), expectedDataSetMetadata.getColumns());
            assertRows(actualResultSet, expectedDataSetRows);
        }
    }
    
    private void assertMetaData(final ResultSetMetaData actualMetaData, final Collection<DataSetColumn> columnMetadataList) throws SQLException {
        assertThat(actualMetaData.getColumnCount(), is(columnMetadataList.size()));
        int index = 1;
        for (DataSetColumn each : columnMetadataList) {
            assertThat(actualMetaData.getColumnLabel(index++), is(each.getName()));
        }
    }
    
    private void assertRows(final ResultSet actualResultSet, final List<DataSetRow> expectedDatSetRows) throws SQLException {
        int count = 0;
        while (actualResultSet.next()) {
            int index = 1;
            for (String each : expectedDatSetRows.get(count).getValues()) {
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
