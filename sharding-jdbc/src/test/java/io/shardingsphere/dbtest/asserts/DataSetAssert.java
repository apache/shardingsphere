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
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.util.InlineExpressionParser;
import io.shardingsphere.dbtest.cases.dataset.expected.dataset.ExpectedDataSetsRoot;
import io.shardingsphere.dbtest.cases.dataset.expected.metadata.ExpectedColumn;
import io.shardingsphere.dbtest.cases.dataset.expected.metadata.ExpectedMetadata;
import io.shardingsphere.dbtest.cases.dataset.init.DataSetColumnMetadata;
import io.shardingsphere.dbtest.cases.dataset.init.DataSetMetadata;
import io.shardingsphere.dbtest.cases.dataset.init.DataSetRow;
import io.shardingsphere.dbtest.cases.dataset.init.DataSetsRoot;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Data set assert.
 *
 * @author zhangliang
 */
@Slf4j
public final class DataSetAssert {
    
    /**
     * Assert data set.
     *
     * @param actual actual
     * @param expected expected
     */
    public static void assertDataSet(final DataSetDefinitions actual, final ExpectedDataSetsRoot expected) {
        assertData(actual.getDataList().get("data"), expected);
    }
    
    private static void assertData(final List<Map<String, String>> actual, final ExpectedDataSetsRoot expected) {
        assertThat(actual.size(), is(expected.getDataSetRows().size()));
        List<String> expectedColumnNames = Splitter.on(",").trimResults().splitToList(expected.getColumns().getValues());
        int count = 0;
        for (Map<String, String> each : actual) {
            List<String> expectedValues = Splitter.on(",").trimResults().splitToList(expected.getDataSetRows().get(count++).getValues());
            assertData(each, expectedValues, expectedColumnNames);
        }
    }
    
    private static void assertData(final Map<String, String> actual, final List<String> expectedValues, final List<String> expectedColumnNames) {
        assertThat(actual.size(), is(expectedValues.size()));
        assertThat(actual.size(), is(expectedColumnNames.size()));
        int count = 0;
        for (String each : expectedValues) {
            assertThat(actual.get(expectedColumnNames.get(count++)), is(each));
        }
    }
    
    /**
     * Assert data set.
     * 
     * @param actualDataSourceMap actual data source map
     * @param expected expected
     * @throws SQLException SQL exception
     */
    public static void assertDataSet(final Map<String, DataSource> actualDataSourceMap, final DataSetsRoot expected) throws SQLException {
        assertThat("Only support single table for DML.", expected.getMetadataList().size(), is(1));
        DataSetMetadata dataSetMetadata = expected.getMetadataList().get(0);
        for (String each : new InlineExpressionParser(dataSetMetadata.getDataNodes()).evaluate()) {
            DataNode dataNode = new DataNode(each);
            try (Connection connection = actualDataSourceMap.get(dataNode.getDataSourceName()).getConnection();
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
    
    private static List<String> getResultSetData(final DataSetMetadata dataSetMetadata, final ResultSet resultSet) throws SQLException {
        List<String> result = new ArrayList<>(dataSetMetadata.getColumnMetadataList().size());
        for (DataSetColumnMetadata each : dataSetMetadata.getColumnMetadataList()) {
            Object resultSetValue = resultSet.getObject(each.getName());
            result.add(resultSetValue instanceof Date ? new SimpleDateFormat("yyyy-MM-dd").format(resultSetValue) : resultSetValue.toString());
        }
        return result;
    }
    
    private static boolean isMatch(final String actualDataNode, final List<String> actualResultSetData, final List<DataSetRow> expectedDataSetRows) {
        for (DataSetRow each : expectedDataSetRows) {
            if (each.getDataNode().equals(actualDataNode) && isMatch(actualResultSetData, each)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isMatch(final List<String> actualResultSetData, final DataSetRow expectedDataSetRow) {
        int count = 0;
        for (String each : Splitter.on(",").trimResults().splitToList(expectedDataSetRow.getValues())) {
            if (!each.equals(actualResultSetData.get(count))) {
                return false;
            }
            count++;
        }
        return true;
    }
    
    private static int countExpectedDataSetRows(final String actualDataNode, final List<DataSetRow> expectedDataSetRows) {
        int result = 0;
        for (DataSetRow each : expectedDataSetRows) {
            if (each.getDataNode().equals(actualDataNode)) {
                result++;
            }
            
        }
        return result;
    }
    
    /**
     * Comparative data set.
     *
     * @param expected expected
     * @param actual actual
     */
    public static void assertMetadata(final List<ExpectedColumn> actual, final ExpectedMetadata expected) {
        for (ExpectedColumn each : expected.getColumns()) {
            assertMetadata(actual, each);
        }
    }
    
    private static void assertMetadata(final List<ExpectedColumn> actual, final ExpectedColumn expect) {
        for (ExpectedColumn each : actual) {
            if (expect.getName().equals(each.getName())) {
                assertThat(each.getType(), is(expect.getType()));
            }
        }
    }
}
