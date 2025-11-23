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

package org.apache.shardingsphere.test.e2e.sql.it.sql.dql;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.util.datetime.DateTimeFormatterFactory;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Mode;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path.ScenarioDataPath.Type;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.e2e.sql.env.DataSetEnvironmentManager;
import org.apache.shardingsphere.test.e2e.sql.env.SQLE2EEnvironmentEngine;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.sql.it.SQLE2EIT;
import org.apache.shardingsphere.test.e2e.sql.it.SQLE2EITContext;
import org.awaitility.Awaitility;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Getter(AccessLevel.PROTECTED)
public abstract class BaseDQLE2EIT implements SQLE2EIT {
    
    private static final Collection<String> FILLED_SUITES = new HashSet<>();
    
    private DataSource expectedDataSource;
    
    private boolean useXMLAsExpectedDataset;
    
    @Setter
    private SQLE2EEnvironmentEngine environmentEngine;
    
    protected final void init(final AssertionTestParameter testParam, final SQLE2EITContext context) throws IOException, JAXBException {
        fillDataOnlyOnce(testParam);
        expectedDataSource = null == context.getAssertion().getExpectedDataSourceName() || 1 == getEnvironmentEngine().getExpectedDataSourceMap().size()
                ? getFirstExpectedDataSource(getEnvironmentEngine().getExpectedDataSourceMap().values())
                : getEnvironmentEngine().getExpectedDataSourceMap().get(context.getAssertion().getExpectedDataSourceName());
        useXMLAsExpectedDataset = null != context.getAssertion().getExpectedDataFile();
        if (0 != testParam.getTestCaseContext().getTestCase().getDelayAssertionSeconds()) {
            Awaitility.await().atMost(Duration.ofMinutes(5L)).pollDelay(testParam.getTestCaseContext().getTestCase().getDelayAssertionSeconds(), TimeUnit.SECONDS).until(() -> true);
        }
    }
    
    private void fillDataOnlyOnce(final AssertionTestParameter testParam) throws IOException, JAXBException {
        String cacheKey = testParam.getKey() + "-" + System.identityHashCode(getEnvironmentEngine().getActualDataSourceMap());
        if (!FILLED_SUITES.contains(cacheKey)) {
            synchronized (FILLED_SUITES) {
                if (!FILLED_SUITES.contains(cacheKey)) {
                    new DataSetEnvironmentManager(
                            new ScenarioDataPath(testParam.getScenario(), Type.ACTUAL).getDataSetFile(), getEnvironmentEngine().getActualDataSourceMap(), testParam.getDatabaseType()).fillData();
                    new DataSetEnvironmentManager(
                            new ScenarioDataPath(testParam.getScenario(), Type.EXPECTED).getDataSetFile(), getEnvironmentEngine().getExpectedDataSourceMap(), testParam.getDatabaseType()).fillData();
                    FILLED_SUITES.add(cacheKey);
                }
            }
        }
    }
    
    private DataSource getFirstExpectedDataSource(final Collection<DataSource> dataSources) {
        return dataSources.isEmpty() ? null : dataSources.iterator().next();
    }
    
    protected final void assertResultSet(final ResultSet actualResultSet, final ResultSet expectedResultSet, final AssertionTestParameter testParam) throws SQLException {
        assertMetaData(actualResultSet.getMetaData(), expectedResultSet.getMetaData(), testParam);
        assertRows(actualResultSet, expectedResultSet);
    }
    
    protected final void assertResultSet(final SQLE2EITContext context, final ResultSet resultSet) throws SQLException {
        assertMetaData(resultSet.getMetaData(), getExpectedColumns(context));
        assertRows(resultSet, getIgnoreAssertColumns(context), context.getDataSet().getRows());
    }
    
    private Collection<DataSetColumn> getExpectedColumns(final SQLE2EITContext context) {
        Collection<DataSetColumn> result = new LinkedList<>();
        for (DataSetMetaData each : context.getDataSet().getMetaDataList()) {
            result.addAll(each.getColumns());
        }
        return result;
    }
    
    private Collection<String> getIgnoreAssertColumns(final SQLE2EITContext context) {
        Collection<String> result = new LinkedList<>();
        for (DataSetMetaData each : context.getDataSet().getMetaDataList()) {
            result.addAll(each.getColumns().stream().filter(DataSetColumn::isIgnoreAssertData).map(DataSetColumn::getName).collect(Collectors.toList()));
        }
        return result;
    }
    
    private void assertMetaData(final ResultSetMetaData actualResultSetMetaData, final ResultSetMetaData expectedResultSetMetaData, final AssertionTestParameter testParam) throws SQLException {
        assertThat(actualResultSetMetaData.getColumnCount(), is(expectedResultSetMetaData.getColumnCount()));
        for (int i = 0; i < actualResultSetMetaData.getColumnCount(); i++) {
            assertThat(actualResultSetMetaData.getColumnLabel(i + 1), is(expectedResultSetMetaData.getColumnLabel(i + 1)));
            assertThat(actualResultSetMetaData.getColumnName(i + 1), is(expectedResultSetMetaData.getColumnName(i + 1)));
            if ("db_tbl_sql_federation".equals(testParam.getScenario())) {
                continue;
            }
            if ("jdbc".equals(testParam.getAdapter()) && Mode.CLUSTER == testParam.getMode() && "encrypt".equals(testParam.getScenario())
                    || "MySQL".equals(testParam.getDatabaseType().getType()) && "passthrough".equals(testParam.getScenario())) {
                // FIXME correct columnType with proxy adapter and other jdbc scenario
                assertThat(actualResultSetMetaData.getColumnType(i + 1), is(expectedResultSetMetaData.getColumnType(i + 1)));
            }
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
            assertTrue(rowCount < expected.size(), "Size of actual result set is different with size of expected data set rows.");
            assertRow(actual, notAssertionColumns, actualMetaData, expected.get(rowCount));
            rowCount++;
        }
        assertThat("Size of actual result set is different with size of expected data set rows.", rowCount, is(expected.size()));
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
                } else if (actualValue instanceof Timestamp && expectedValue instanceof LocalDateTime) {
                    // TODO Since mysql 8.0.23, for the DATETIME type, the mysql driver returns the LocalDateTime type, but the proxy returns the Timestamp type.
                    assertThat(((Timestamp) actualValue).toLocalDateTime(), is(expectedValue));
                } else if (Types.TIMESTAMP == actualMetaData.getColumnType(i + 1) || Types.TIMESTAMP == expectedMetaData.getColumnType(i + 1)) {
                    Object convertedActualValue = Types.TIMESTAMP == actualMetaData.getColumnType(i + 1)
                            ? actualResultSet.getTimestamp(i + 1).toLocalDateTime().format(DateTimeFormatterFactory.getDatetimeFormatter())
                            : actualValue;
                    Object convertedExpectedValue = Types.TIMESTAMP == expectedMetaData.getColumnType(i + 1)
                            ? expectedResultSet.getTimestamp(i + 1).toLocalDateTime().format(DateTimeFormatterFactory.getDatetimeFormatter())
                            : actualValue;
                    assertThat(String.valueOf(convertedActualValue), is(String.valueOf(convertedExpectedValue)));
                } else if (expectedValue instanceof Clob) {
                    assertThat(String.valueOf(actualValue), is(((Clob) expectedValue).getSubString(1, (int) ((Clob) expectedValue).length())));
                } else if (actualValue instanceof String && expectedValue instanceof byte[]) {
                    assertThat(actualValue, is(new String((byte[]) expectedValue)));
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
