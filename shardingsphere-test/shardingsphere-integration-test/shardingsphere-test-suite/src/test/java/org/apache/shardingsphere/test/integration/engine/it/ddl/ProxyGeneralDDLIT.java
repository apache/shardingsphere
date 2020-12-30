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

package org.apache.shardingsphere.test.integration.engine.it.ddl;

import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.cases.IntegrateTestCaseType;
import org.apache.shardingsphere.test.integration.cases.assertion.ddl.DDLIntegrateTestCaseAssertion;
import org.apache.shardingsphere.test.integration.cases.assertion.root.SQLCaseType;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetIndex;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetMetadata;
import org.apache.shardingsphere.test.integration.engine.param.IntegrateTestParameters;
import org.apache.shardingsphere.test.integration.env.IntegrateTestEnvironment;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class ProxyGeneralDDLIT extends BaseDDLIT {
    
    public ProxyGeneralDDLIT(final String parentPath, final DDLIntegrateTestCaseAssertion assertion, final String ruleType,
                             final String databaseType, final SQLCaseType caseType, final String sql) throws IOException, JAXBException, SQLException, ParseException {
        super(parentPath, assertion, ruleType, DatabaseTypeRegistry.getActualDatabaseType(databaseType), caseType, sql);
    }
    
    @Parameters(name = "{2} -> {3} -> {4} -> {5}")
    public static Collection<Object[]> getParameters() {
        return IntegrateTestEnvironment.getInstance().isProxyEnvironment() ? IntegrateTestParameters.getParametersWithAssertion(IntegrateTestCaseType.DDL) : Collections.emptyList();
    }
    
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void assertExecuteUpdate() throws SQLException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            if (SQLCaseType.Literal == getCaseType()) {
                connection.createStatement().executeUpdate(getSql());
            } else {
                connection.prepareStatement(getSql()).executeUpdate();
            }
            assertTableMetaData(connection);
        }
    }
    
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void assertExecute() throws SQLException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            if (SQLCaseType.Literal == getCaseType()) {
                connection.createStatement().execute(getSql());
            } else {
                connection.prepareStatement(getSql()).execute();
            }
            assertTableMetaData(connection);
        }
    }
    
    private void assertTableMetaData(final Connection connection) throws SQLException {
        String tableName = ((DDLIntegrateTestCaseAssertion) getAssertion()).getTable();
        Optional<DataSetMetadata> expected = getDataSet().findMetadata(tableName);
        if (!expected.isPresent()) {
            assertFalse(containsTable(connection, tableName));
            return;
        }
        assertTableMetaData(getActualColumns(connection, tableName), getActualIndexes(connection, tableName), expected.get());
    }
    
    private void assertTableMetaData(final List<DataSetColumn> actualColumns, final List<DataSetIndex> actualIndexes, final DataSetMetadata expected) {
        for (DataSetColumn each : expected.getColumns()) {
            assertColumnMetaData(actualColumns, each);
        }
        for (DataSetIndex each : expected.getIndexes()) {
            assertIndexMetaData(actualIndexes, each);
        }
    }
    
    private boolean containsTable(final Connection connection, final String tableName) throws SQLException {
        return connection.getMetaData().getTables(null, null, tableName, new String[] {"TABLE"}).next();
    }
    
    private List<DataSetColumn> getActualColumns(final Connection connection, final String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(null, null, tableName, null)) {
            List<DataSetColumn> result = new LinkedList<>();
            while (resultSet.next()) {
                DataSetColumn each = new DataSetColumn();
                each.setName(resultSet.getString("COLUMN_NAME"));
                each.setType(resultSet.getString("TYPE_NAME").toLowerCase());
                result.add(each);
            }
            return result;
        }
    }
    
    private List<DataSetIndex> getActualIndexes(final Connection connection, final String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getIndexInfo(null, null, tableName, false, false)) {
            List<DataSetIndex> result = new LinkedList<>();
            while (resultSet.next()) {
                DataSetIndex each = new DataSetIndex();
                each.setName(resultSet.getString("INDEX_NAME"));
                each.setUnique(!resultSet.getBoolean("NON_UNIQUE"));
                each.setColumns(resultSet.getString("COLUMN_NAME"));
                result.add(each);
            }
            return result;
        }
    }
    
    private void assertColumnMetaData(final List<DataSetColumn> actual, final DataSetColumn expect) {
        for (DataSetColumn each : actual) {
            if (expect.getName().equals(each.getName())) {
                if ("MySQL".equals(getDatabaseType().getName()) && "integer".equals(expect.getType())) {
                    assertThat(each.getType(), is("int"));
                } else if ("PostgreSQL".equals(getDatabaseType().getName()) && "integer".equals(expect.getType())) {
                    assertThat(each.getType(), is("int4"));
                } else {
                    assertThat(each.getType(), is(expect.getType()));
                }
            }
        }
    }
    
    private void assertIndexMetaData(final List<DataSetIndex> actual, final DataSetIndex expect) {
        for (DataSetIndex each : actual) {
            if (expect.getName().equals(each.getName())) {
                assertThat(each.isUnique(), is(expect.isUnique()));
            }
        }
    }
}
