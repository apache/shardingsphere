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

package org.apache.shardingsphere.test.integration.engine.rdl;

import com.google.common.base.Splitter;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.integration.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.integration.engine.SingleITCase;
import org.apache.shardingsphere.test.integration.framework.param.model.AssertionParameterizedArray;
import org.junit.After;
import org.junit.Before;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class BaseRDLIT extends SingleITCase {
    
    public BaseRDLIT(final AssertionParameterizedArray parameterizedArray) {
        super(parameterizedArray);
    }
    
    @Before
    public final void init() throws Exception {
        try (Connection connection = getTargetDataSource().getConnection()) {
            executeInitSQLs(connection);
        }
        sleep();
    }
    
    @After
    public final void tearDown() throws Exception {
        if (null != getAssertion().getDestroySQL()) {
            try (Connection connection = getTargetDataSource().getConnection()) {
                executeDestroySQLs(connection);
            }
        }
        sleep();
    }
    
    private void executeInitSQLs(final Connection connection) throws SQLException {
        if (null == getAssertion().getInitialSQL() || null == getAssertion().getInitialSQL().getSql()) {
            return;
        }
        for (String each : Splitter.on(";").trimResults().splitToList(getAssertion().getInitialSQL().getSql())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(each)) {
                preparedStatement.executeUpdate();
            }
        }
    }
    
    private void executeDestroySQLs(final Connection connection) throws SQLException {
        if (null == getAssertion().getDestroySQL().getSql()) {
            return;
        }
        for (String each : Splitter.on(";").trimResults().splitToList(getAssertion().getDestroySQL().getSql())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(each)) {
                preparedStatement.executeUpdate();
            }
        }
    }
    
    protected void sleep() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (final InterruptedException ignored) {
        }
    }
    
    protected final void assertResultSet(final ResultSet resultSet) throws SQLException {
        assertMetaData(resultSet.getMetaData(), getExpectedColumns());
        assertRows(resultSet, getDataSet().getRows());
    }
    
    private Collection<DataSetColumn> getExpectedColumns() {
        Collection<DataSetColumn> result = new LinkedList<>();
        for (DataSetMetaData each : getDataSet().getMetaDataList()) {
            result.addAll(each.getColumns());
        }
        return result;
    }
    
    private void assertMetaData(final ResultSetMetaData actual, final Collection<DataSetColumn> expected) throws SQLException {
        assertThat(actual.getColumnCount(), is(expected.size()));
        int index = 1;
        for (DataSetColumn each : expected) {
            assertThat(actual.getColumnLabel(index++).toLowerCase(), is(each.getName().toLowerCase()));
        }
    }
    
    private void assertRows(final ResultSet actual, final List<DataSetRow> expected) throws SQLException {
        int rowCount = 0;
        ResultSetMetaData actualMetaData = actual.getMetaData();
        while (actual.next()) {
            assertTrue("Size of actual result set is different with size of expected dat set rows", rowCount < expected.size());
            assertRow(actual, actualMetaData, expected.get(rowCount));
            rowCount++;
        }
        assertThat("Size of actual result set is different with size of expected dat set rows", rowCount, is(expected.size()));
    }
    
    private void assertRow(final ResultSet actual, final ResultSetMetaData actualMetaData, final DataSetRow expected) throws SQLException {
        int columnIndex = 1;
        for (String each : expected.splitValues("|")) {
            String columnLabel = actualMetaData.getColumnLabel(columnIndex);
            assertObjectValue(actual, columnIndex, columnLabel, each);
            columnIndex++;
        }
    }
    
    private void assertObjectValue(final ResultSet actual, final int columnIndex, final String columnLabel, final String expected) throws SQLException {
        assertThat(String.valueOf(actual.getObject(columnIndex)), is(expected));
        assertThat(String.valueOf(actual.getObject(columnLabel)), is(expected));
    }
}
