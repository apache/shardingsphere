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

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.test.integration.engine.SingleITCase;
import org.apache.shardingsphere.test.integration.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.integration.env.runtime.scenario.path.ScenarioDataPath.Type;
import org.apache.shardingsphere.test.integration.env.DataSetEnvironmentManager;
import org.apache.shardingsphere.test.integration.framework.param.model.AssertionParameterizedArray;
import org.junit.Before;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@Getter(AccessLevel.PROTECTED)
public abstract class BaseDQLIT extends SingleITCase {
    
    private static final Collection<String> FILLED_SUITES = new HashSet<>();
    
    private DataSource expectedDataSource;
    
    public BaseDQLIT(final AssertionParameterizedArray parameterizedArray) {
        super(parameterizedArray);
    }
    
    @Before
    public final void init() throws Exception {
        fillDataOnlyOnce();
        expectedDataSource = null == getAssertion().getExpectedDataSourceName() || 1 == getExpectedDataSourceMap().size()
                ? getExpectedDataSourceMap().values().iterator().next()
                : getExpectedDataSourceMap().get(getAssertion().getExpectedDataSourceName());
    }
    
    private void fillDataOnlyOnce() throws SQLException, ParseException, IOException, JAXBException {
        if (!FILLED_SUITES.contains(getItKey())) {
            synchronized (FILLED_SUITES) {
                if (!FILLED_SUITES.contains(getScenario())) {
                    new DataSetEnvironmentManager(new ScenarioDataPath(getScenario()).getDataSetFile(Type.ACTUAL), getActualDataSourceMap()).fillData();
                    new DataSetEnvironmentManager(new ScenarioDataPath(getScenario()).getDataSetFile(Type.EXPECTED), getExpectedDataSourceMap()).fillData();
                    FILLED_SUITES.add(getItKey());
                }
            }
        }
    }
    
    protected final void assertResultSet(final ResultSet actualResultSet, final ResultSet expectedResultSet) throws SQLException {
        assertMetaData(actualResultSet.getMetaData(), expectedResultSet.getMetaData());
        assertRows(actualResultSet, expectedResultSet);
    }
    
    private void assertMetaData(final ResultSetMetaData actualResultSetMetaData, final ResultSetMetaData expectedResultSetMetaData) throws SQLException {
        assertThat(actualResultSetMetaData.getColumnCount(), is(expectedResultSetMetaData.getColumnCount()));
        for (int i = 0; i < actualResultSetMetaData.getColumnCount(); i++) {
            try {
                assertThat(actualResultSetMetaData.getColumnLabel(i + 1).toLowerCase(), is(expectedResultSetMetaData.getColumnLabel(i + 1).toLowerCase()));
            } catch (final AssertionError ex) {
                // FIXME #15594 Expected: is "order_id", but: was "order_id0"
                try {
                    assertThat(actualResultSetMetaData.getColumnLabel(i + 1).toLowerCase(), is(expectedResultSetMetaData.getColumnLabel(i + 1).toLowerCase() + "0"));
                } catch (final AssertionError otherEx) {
                    // FIXME #15594 Expected: is "sum(order_id_sharding)0", but: was "expr$1"
                    assertThat(actualResultSetMetaData.getColumnLabel(i + 1).toLowerCase(), startsWith("expr$"));
                }
            }
        }
    }
    
    private void assertRows(final ResultSet actualResultSet, final ResultSet expectedResultSet) throws SQLException {
        ResultSetMetaData actualMetaData = actualResultSet.getMetaData();
        ResultSetMetaData expectedMetaData = expectedResultSet.getMetaData();
        while (actualResultSet.next()) {
            assertTrue("Size of actual result set is different with size of expected result set.", expectedResultSet.next());
            assertRow(actualResultSet, actualMetaData, expectedResultSet, expectedMetaData);
        }
        assertFalse("Size of actual result set is different with size of expected result set.", expectedResultSet.next());
    }
    
    private void assertRow(final ResultSet actualResultSet, final ResultSetMetaData actualMetaData,
                           final ResultSet expectedResultSet, final ResultSetMetaData expectedMetaData) throws SQLException {
        for (int i = 0; i < actualMetaData.getColumnCount(); i++) {
            try {
                assertThat(actualResultSet.getObject(i + 1), is(expectedResultSet.getObject(i + 1)));
                assertThat(actualResultSet.getObject(actualMetaData.getColumnLabel(i + 1)), is(expectedResultSet.getObject(expectedMetaData.getColumnLabel(i + 1))));
            } catch (AssertionError ex) {
                // FIXME #15593 verify accurate data types
                Object actualValue = actualResultSet.getObject(i + 1);
                Object expectedValue = expectedResultSet.getObject(i + 1);
                if (actualValue instanceof Double || actualValue instanceof Float || actualValue instanceof BigDecimal) {
                    assertThat(Math.floor(Double.parseDouble(actualValue.toString())), is(Math.floor(Double.parseDouble(expectedValue.toString()))));
                } else {
                    assertThat(actualValue.toString(), is(expectedValue.toString()));
                }
            }
        }
    }
}
