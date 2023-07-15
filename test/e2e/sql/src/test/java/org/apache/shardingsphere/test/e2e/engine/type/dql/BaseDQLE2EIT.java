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

package org.apache.shardingsphere.test.e2e.engine.type.dql;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.e2e.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.e2e.engine.composer.SingleE2EContainerComposer;
import org.apache.shardingsphere.test.e2e.env.DataSetEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Getter(AccessLevel.PROTECTED)
public abstract class BaseDQLE2EIT {
    
    private static final Collection<String> FILLED_SUITES = new HashSet<>();
    
    private DataSource expectedDataSource;
    
    private boolean useXMLAsExpectedDataset;
    
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
        fillDataOnlyOnce(testParam, containerComposer);
        expectedDataSource = null == containerComposer.getAssertion().getExpectedDataSourceName() || 1 == containerComposer.getExpectedDataSourceMap().size()
                ? containerComposer.getExpectedDataSourceMap().values().iterator().next()
                : containerComposer.getExpectedDataSourceMap().get(containerComposer.getAssertion().getExpectedDataSourceName());
        useXMLAsExpectedDataset = null != containerComposer.getAssertion().getExpectedDataFile();
    }
    
    private void fillDataOnlyOnce(final AssertionTestParameter testParam, final SingleE2EContainerComposer containerComposer) throws IOException, JAXBException {
        String cacheKey = testParam.getKey() + "-" + System.identityHashCode(containerComposer.getActualDataSourceMap());
        if (!FILLED_SUITES.contains(cacheKey)) {
            synchronized (FILLED_SUITES) {
                if (!FILLED_SUITES.contains(cacheKey)) {
                    new DataSetEnvironmentManager(
                            new ScenarioDataPath(testParam.getScenario()).getDataSetFile(Type.ACTUAL), containerComposer.getActualDataSourceMap()).fillData();
                    new DataSetEnvironmentManager(
                            new ScenarioDataPath(testParam.getScenario()).getDataSetFile(Type.EXPECTED), containerComposer.getExpectedDataSourceMap()).fillData();
                    FILLED_SUITES.add(cacheKey);
                }
            }
        }
    }
    
    protected final void assertResultSet(final ResultSet actualResultSet, final ResultSet expectedResultSet) throws SQLException {
        assertMetaData(actualResultSet.getMetaData(), expectedResultSet.getMetaData());
        assertRows(actualResultSet, expectedResultSet);
    }
    
    protected final void assertResultSet(final SingleE2EContainerComposer containerComposer, final ResultSet resultSet) throws SQLException {
        assertMetaData(resultSet.getMetaData(), getExpectedColumns(containerComposer));
        assertRows(resultSet, getNotAssertionColumns(containerComposer), containerComposer.getDataSet().getRows());
    }
    
    private Collection<DataSetColumn> getExpectedColumns(final SingleE2EContainerComposer containerComposer) {
        Collection<DataSetColumn> result = new LinkedList<>();
        for (DataSetMetaData each : containerComposer.getDataSet().getMetaDataList()) {
            result.addAll(each.getColumns());
        }
        return result;
    }
    
    private Collection<String> getNotAssertionColumns(final SingleE2EContainerComposer containerComposer) {
        Collection<String> result = new LinkedList<>();
        for (DataSetMetaData each : containerComposer.getDataSet().getMetaDataList()) {
            result.addAll(each.getColumns().stream().filter(column -> "false".equals(column.getAssertion())).map(DataSetColumn::getName).collect(Collectors.toList()));
        }
        return result;
    }
    
    private void assertMetaData(final ResultSetMetaData actualResultSetMetaData, final ResultSetMetaData expectedResultSetMetaData) throws SQLException {
        assertThat(actualResultSetMetaData.getColumnCount(), is(expectedResultSetMetaData.getColumnCount()));
        for (int i = 0; i < actualResultSetMetaData.getColumnCount(); i++) {
            assertThat(actualResultSetMetaData.getColumnLabel(i + 1).toLowerCase(), is(expectedResultSetMetaData.getColumnLabel(i + 1).toLowerCase()));
        }
    }
    
    private void assertMetaData(final ResultSetMetaData actual, final Collection<DataSetColumn> expected) throws SQLException {
        assertThat(actual.getColumnCount(), is(expected.size()));
        int index = 1;
        for (DataSetColumn each : expected) {
            assertThat(actual.getColumnLabel(index++).toLowerCase(), is(each.getName().toLowerCase()));
        }
    }
    
    private void assertRows(final ResultSet actualResultSet, final ResultSet expectedResultSet) throws SQLException {
        ResultSetMetaData actualMetaData = actualResultSet.getMetaData();
        ResultSetMetaData expectedMetaData = expectedResultSet.getMetaData();
        while (actualResultSet.next()) {
            assertTrue(expectedResultSet.next(), "Size of actual result set is different with size of expected result set.");
            assertRow(actualResultSet, actualMetaData, expectedResultSet, expectedMetaData);
        }
        assertFalse(expectedResultSet.next(), "Size of actual result set is different with size of expected result set.");
    }
    
    private void assertRows(final ResultSet actual, final Collection<String> notAssertionColumns, final List<DataSetRow> expected) throws SQLException {
        int rowCount = 0;
        ResultSetMetaData actualMetaData = actual.getMetaData();
        while (actual.next()) {
            assertTrue(rowCount < expected.size(), "Size of actual result set is different with size of expected dat set rows.");
            DataSetRow expectedRow = getExpectedRowAndRemoveMayNotExistRow(actual, notAssertionColumns, actualMetaData, expected, rowCount);
            assertRow(actual, notAssertionColumns, actualMetaData, expectedRow);
            rowCount++;
        }
        assertThat("Size of actual result set is different with size of expected dat set rows.", rowCount, is(expected.size()));
    }
    
    private DataSetRow getExpectedRowAndRemoveMayNotExistRow(final ResultSet actual, final Collection<String> notAssertionColumns, final ResultSetMetaData actualMetaData,
                                                             final List<DataSetRow> expected, final int rowCount) throws SQLException {
        if (!expected.get(rowCount).isMayNotExist()) {
            return expected.get(rowCount);
        }
        if (isMoveToNextExpectedRow(actual, notAssertionColumns, actualMetaData, expected, rowCount)) {
            expected.remove(rowCount);
        } else {
            return expected.get(rowCount);
        }
        return getExpectedRowAndRemoveMayNotExistRow(actual, notAssertionColumns, actualMetaData, expected, rowCount);
    }
    
    private boolean isMoveToNextExpectedRow(final ResultSet actual, final Collection<String> notAssertionColumns, final ResultSetMetaData actualMetaData,
                                            final List<DataSetRow> expected, final int rowCount) throws SQLException {
        int columnIndex = 1;
        for (String each : expected.get(rowCount).splitValues("|")) {
            String columnLabel = actualMetaData.getColumnLabel(columnIndex);
            if (!notAssertionColumns.contains(columnLabel)) {
                if (!each.equals(String.valueOf(actual.getObject(columnIndex)).trim()) || !each.equals(String.valueOf(actual.getObject(columnLabel)).trim())) {
                    return true;
                }
            }
            columnIndex++;
        }
        return false;
    }
    
    private void assertRow(final ResultSet actualResultSet, final ResultSetMetaData actualMetaData,
                           final ResultSet expectedResultSet, final ResultSetMetaData expectedMetaData) throws SQLException {
        for (int i = 0; i < actualMetaData.getColumnCount(); i++) {
            try {
                assertThat(actualResultSet.getObject(i + 1), is(expectedResultSet.getObject(i + 1)));
                assertThat(actualResultSet.getObject(actualMetaData.getColumnLabel(i + 1)), is(expectedResultSet.getObject(expectedMetaData.getColumnLabel(i + 1))));
            } catch (final AssertionError ex) {
                // FIXME #15593 verify accurate data types
                Object actualValue = actualResultSet.getObject(i + 1);
                Object expectedValue = expectedResultSet.getObject(i + 1);
                if (actualValue instanceof Double || actualValue instanceof Float || actualValue instanceof BigDecimal) {
                    assertThat(Math.floor(Double.parseDouble(actualValue.toString())), is(Math.floor(Double.parseDouble(expectedValue.toString()))));
                } else {
                    assertThat(String.valueOf(actualValue), is(String.valueOf(expectedValue)));
                }
            }
        }
    }
    
    private void assertRow(final ResultSet actual, final Collection<String> notAssertionColumns, final ResultSetMetaData actualMetaData, final DataSetRow expected) throws SQLException {
        int columnIndex = 1;
        for (String each : expected.splitValues("|")) {
            String columnLabel = actualMetaData.getColumnLabel(columnIndex);
            if (!notAssertionColumns.contains(columnLabel)) {
                assertObjectValue(actual, columnIndex, columnLabel, each);
            }
            columnIndex++;
        }
    }
    
    private void assertObjectValue(final ResultSet actual, final int columnIndex, final String columnLabel, final String expected) throws SQLException {
        assertThat(String.valueOf(actual.getObject(columnIndex)).trim(), is(expected));
        assertThat(String.valueOf(actual.getObject(columnLabel)).trim(), is(expected));
    }
}
