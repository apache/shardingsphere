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

package org.apache.shardingsphere.test.integration.engine.dql;

import org.apache.shardingsphere.test.integration.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.integration.engine.SingleITCase;
import org.apache.shardingsphere.test.integration.env.scenario.ScenarioPath;
import org.apache.shardingsphere.test.integration.env.scenario.dataset.DataSetEnvironmentManager;
import org.apache.shardingsphere.test.integration.framework.param.model.AssertionParameterizedArray;
import org.junit.Before;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class BaseDQLIT extends SingleITCase {
    
    private static final Collection<String> FILLED_SUITES = new HashSet<>();
    
    public BaseDQLIT(final AssertionParameterizedArray parameterizedArray) {
        super(parameterizedArray);
    }
    
    @Before
    public final void init() throws Exception {
        fillDataOnlyOnce();
    }
    
    private void fillDataOnlyOnce() throws SQLException, ParseException, IOException, JAXBException {
        if (!FILLED_SUITES.contains(getItKey())) {
            synchronized (FILLED_SUITES) {
                if (!FILLED_SUITES.contains(getScenario())) {
                    new DataSetEnvironmentManager(new ScenarioPath(getScenario()).getDataSetFile(), getActualDataSourceMap()).fillData();
                    new DataSetEnvironmentManager(Objects.requireNonNull(ScenarioPath.class.getClassLoader().getResource("env/common/verification/dataset/dataset.xml")).getFile(), 
                            Collections.singletonMap("verification_dataset", getVerificationDataSource())).fillData();
                    FILLED_SUITES.add(getItKey());
                }
            }
        }
    }
    
    protected final void assertResultSet(final ResultSet actualResultSet, final ResultSet verificationResultSet) throws SQLException {
        assertMetaData(actualResultSet.getMetaData(), verificationResultSet.getMetaData());
        if (getDataSet().isIgnoreRowOrder()) {
            assertRowsIgnoreOrder(actualResultSet, getDataSet().getRows());
        } else {
            assertRows(actualResultSet, getDataSet().getRows());
        }
    }
    
    private void assertMetaData(final ResultSetMetaData actualResultSetMetaData, final ResultSetMetaData verificationResultSetMetaData) throws SQLException {
        assertThat(actualResultSetMetaData.getColumnCount(), is(verificationResultSetMetaData.getColumnCount()));
        for (int i = 0; i < actualResultSetMetaData.getColumnCount();i++) {
            assertThat(actualResultSetMetaData.getColumnLabel(i + 1).toLowerCase(), is(verificationResultSetMetaData.getColumnLabel(i + 1).toLowerCase()));
        }
    }
    
    private void assertRows(final ResultSet actual, final List<DataSetRow> expected) throws SQLException {
        int rowCount = 0;
        ResultSetMetaData actualMetaData = actual.getMetaData();
        while (actual.next()) {
            assertTrue("Size of actual result set is different with size of expected dat set rows.", rowCount < expected.size());
            assertRow(actual, actualMetaData, expected.get(rowCount));
            rowCount++;
        }
        assertThat("Size of actual result set is different with size of expected dat set rows.", rowCount, is(expected.size()));
    }
    
    private void assertRowsIgnoreOrder(final ResultSet actual, final List<DataSetRow> expected) throws SQLException {
        int rowCount = 0;
        ResultSetMetaData actualMetaData = actual.getMetaData();
        while (actual.next()) {
            assertTrue("Size of actual result set is different with size of expected dat set rows.", rowCount < expected.size());
            assertTrue(String.format("Actual result set does not exist in expected, row count [%d].", rowCount), assertContains(actual, actualMetaData, expected));
            rowCount++;
        }
        assertThat("Size of actual result set is different with size of expected dat set rows.", rowCount, is(expected.size()));
    }
    
    private boolean assertContains(final ResultSet actual, final ResultSetMetaData actualMetaData, final List<DataSetRow> expected) throws SQLException {
        for (DataSetRow each : expected) {
            if (isSameRow(actual, actualMetaData, each)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSameRow(final ResultSet actual, final ResultSetMetaData actualMetaData, final DataSetRow expected) throws SQLException {
        int columnIndex = 1;
        for (String each : expected.splitValues(",")) {
            if (!isSameDateValue(actual, columnIndex, actualMetaData.getColumnLabel(columnIndex), each)) {
                return false;
            }
            columnIndex++;
        }
        return true;
    }
    
    private boolean isSameDateValue(final ResultSet actual, final int columnIndex, final String columnLabel, final String expected) throws SQLException {
        if (Types.DATE == actual.getMetaData().getColumnType(columnIndex)) {
            assertDateValue(actual, columnIndex, columnLabel, expected);
            if (NOT_VERIFY_FLAG.equals(expected)) {
                return true;
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return expected.equals(dateFormat.format(actual.getDate(columnIndex))) && expected.equals(dateFormat.format(actual.getDate(columnLabel)));
        } else {
            return expected.equals(String.valueOf(actual.getObject(columnIndex))) && expected.equals(String.valueOf(actual.getObject(columnLabel)));
        }
    }
    
    private void assertRow(final ResultSet actual, final ResultSetMetaData actualMetaData, final DataSetRow expected) throws SQLException {
        int columnIndex = 1;
        for (String each : expected.splitValues(",")) {
            String columnLabel = actualMetaData.getColumnLabel(columnIndex);
            if (Types.DATE == actual.getMetaData().getColumnType(columnIndex)) {
                assertDateValue(actual, columnIndex, columnLabel, each);
            } else {
                assertObjectValue(actual, columnIndex, columnLabel, each);
            }
            columnIndex++;
        }
    }
    
    private void assertDateValue(final ResultSet actual, final int columnIndex, final String columnLabel, final String expected) throws SQLException {
        if (NOT_VERIFY_FLAG.equals(expected)) {
            return;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        assertThat(dateFormat.format(actual.getDate(columnIndex)), is(expected));
        assertThat(dateFormat.format(actual.getDate(columnLabel)), is(expected));
    }
    
    private void assertObjectValue(final ResultSet actual, final int columnIndex, final String columnLabel, final String expected) throws SQLException {
        assertThat(String.valueOf(actual.getObject(columnIndex)), is(expected));
        assertThat(String.valueOf(actual.getObject(columnLabel)), is(expected));
    }
}
