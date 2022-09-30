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

package org.apache.shardingsphere.test.integration.engine.ddl;

import com.google.common.base.Splitter;
import org.apache.shardingsphere.dialect.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.util.expr.InlineExpressionParser;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetIndex;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.integration.engine.SingleITCase;
import org.apache.shardingsphere.test.integration.framework.param.model.AssertionParameterizedArray;
import org.junit.After;
import org.junit.Before;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public abstract class BaseDDLIT extends SingleITCase {
    
    public BaseDDLIT(final AssertionParameterizedArray parameterizedArray) {
        super(parameterizedArray);
    }
    
    @Before
    public final void init() throws Exception {
        assertNotNull("Expected affected table is required", getAssertion().getInitialSQL());
        assertNotNull("Init SQL is required", getAssertion().getInitialSQL().getAffectedTable());
        try (Connection connection = getTargetDataSource().getConnection()) {
            synchronizedExecuteInitSQLs(connection);
        }
    }
    
    private synchronized void synchronizedExecuteInitSQLs(final Connection connection) throws SQLException {
        if (null == getAssertion().getInitialSQL().getSql()) {
            return;
        }
        for (String each : Splitter.on(";").trimResults().splitToList(getAssertion().getInitialSQL().getSql())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(each)) {
                preparedStatement.executeUpdate();
            }
        }
    }
    
    @After
    public final void tearDown() {
        try (Connection connection = getTargetDataSource().getConnection()) {
            synchronizedExecuteDropSQLs(connection, String.format("DROP TABLE %s", getAssertion().getInitialSQL().getAffectedTable()));
        } catch (final SQLException | NoSuchTableException ignored) {
        }
    }
    
    private synchronized void synchronizedExecuteDropSQLs(final Connection connection, final String sql) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }
    
    protected final void assertTableMetaData() throws SQLException {
        String tableName = getAssertion().getInitialSQL().getAffectedTable();
        DataSetMetaData expected = getDataSet().findMetaData(tableName);
        Collection<DataNode> dataNodes = new InlineExpressionParser(expected.getDataNodes()).splitAndEvaluate().stream().map(DataNode::new).collect(Collectors.toList());
        if (expected.getColumns().isEmpty()) {
            assertNotContainsTable(dataNodes);
            return;
        }
        assertTableMetaData(getActualColumns(dataNodes), getActualIndexes(dataNodes), expected);
    }
    
    private void assertTableMetaData(final List<DataSetColumn> actualColumns, final List<DataSetIndex> actualIndexes, final DataSetMetaData expected) {
        assertColumnMetaData(actualColumns, expected.getColumns());
        assertIndexMetaData(actualIndexes, expected.getIndexes());
    }
    
    private void assertNotContainsTable(final Collection<DataNode> dataNodes) throws SQLException {
        for (DataNode each : dataNodes) {
            try (Connection connection = getActualDataSourceMap().get(each.getDataSourceName()).getConnection()) {
                assertNotContainsTable(connection, each.getTableName());
            }
        }
    }
    
    private void assertNotContainsTable(final Connection connection, final String tableName) throws SQLException {
        assertFalse(String.format("Table `%s` should not existed", tableName), connection.getMetaData().getTables(null, null, tableName, new String[]{"TABLE"}).next());
    }
    
    private List<DataSetColumn> getActualColumns(final Collection<DataNode> dataNodes) throws SQLException {
        Set<DataSetColumn> result = new LinkedHashSet<>();
        for (DataNode each : dataNodes) {
            try (Connection connection = getActualDataSourceMap().get(each.getDataSourceName()).getConnection()) {
                result.addAll(getActualColumns(connection, each.getTableName()));
            }
        }
        return new LinkedList<>(result);
    }
    
    private List<DataSetColumn> getActualColumns(final Connection connection, final String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(null, null, tableName, null)) {
            List<DataSetColumn> result = new LinkedList<>();
            while (resultSet.next()) {
                DataSetColumn each = new DataSetColumn();
                each.setName(resultSet.getString("COLUMN_NAME"));
                String typeName = resultSet.getString("TYPE_NAME");
                each.setType("CHARACTER VARYING".equals(typeName) ? "VARCHAR".toLowerCase() : typeName.toLowerCase());
                result.add(each);
            }
            return result;
        }
    }
    
    private List<DataSetIndex> getActualIndexes(final Collection<DataNode> dataNodes) throws SQLException {
        Set<DataSetIndex> result = new LinkedHashSet<>();
        for (DataNode each : dataNodes) {
            try (Connection connection = getActualDataSourceMap().get(each.getDataSourceName()).getConnection()) {
                result.addAll(getActualIndexes(connection, each.getTableName()));
            }
        }
        return new LinkedList<>(result);
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
    
    private void assertColumnMetaData(final List<DataSetColumn> actual, final List<DataSetColumn> expected) {
        assertThat("Size of actual columns is different with size of expected columns", actual.size(), is(expected.size()));
        for (int i = 0; i < actual.size(); i++) {
            assertColumnMetaData(actual.get(i), expected.get(i));
        }
    }
    
    private void assertColumnMetaData(final DataSetColumn actual, final DataSetColumn expected) {
        assertThat("Mismatched column name", actual.getName(), is(expected.getName()));
        if ("MySQL".equals(getDatabaseType().getType()) && "integer".equals(expected.getType())) {
            assertThat("Mismatched column type", actual.getType(), is("int"));
        } else if ("PostgreSQL".equals(getDatabaseType().getType()) && "integer".equals(expected.getType())) {
            assertThat("Mismatched column type", actual.getType(), is("int4"));
        } else if ("openGauss".equals(getDatabaseType().getType()) && "integer".equals(expected.getType())) {
            assertThat("Mismatched column type", actual.getType(), is("int4"));
        } else {
            assertThat("Mismatched column type", actual.getType(), is(expected.getType()));
        }
    }
    
    private void assertIndexMetaData(final List<DataSetIndex> actual, final List<DataSetIndex> expected) {
        for (DataSetIndex each : expected) {
            assertIndexMetaData(actual, each);
        }
    }
    
    private void assertIndexMetaData(final List<DataSetIndex> actual, final DataSetIndex expected) {
        for (DataSetIndex each : actual) {
            if (expected.getName().equals(each.getName())) {
                assertThat(each.isUnique(), is(expected.isUnique()));
            }
        }
    }
}
