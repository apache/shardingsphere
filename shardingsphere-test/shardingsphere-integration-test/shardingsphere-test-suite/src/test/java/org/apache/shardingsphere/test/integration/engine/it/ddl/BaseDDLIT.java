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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineExpressionParser;
import org.apache.shardingsphere.test.integration.cases.assertion.ddl.DDLIntegrateTestCaseAssertion;
import org.apache.shardingsphere.test.integration.cases.assertion.root.SQLCaseType;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetIndex;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetMetadata;
import org.apache.shardingsphere.test.integration.engine.it.SingleIT;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.env.dataset.DataSetEnvironmentManager;
import org.apache.shardingsphere.test.integration.env.schema.SchemaEnvironmentManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public abstract class BaseDDLIT extends SingleIT {
    
    private final DataSetEnvironmentManager dataSetEnvironmentManager;
    
    protected BaseDDLIT(final String parentPath, final DDLIntegrateTestCaseAssertion assertion, final String ruleType, 
                        final DatabaseType databaseType, final SQLCaseType caseType, final String sql) throws IOException, JAXBException, SQLException, ParseException {
        super(parentPath, assertion, ruleType, databaseType, caseType, sql);
        dataSetEnvironmentManager = new DataSetEnvironmentManager(EnvironmentPath.getDataSetFile(ruleType), getActualDataSources());
    }
    
    @BeforeClass
    public static void initDatabases() throws IOException, JAXBException {
        SchemaEnvironmentManager.createDatabases();
    }
    
    @AfterClass
    public static void destroyDatabases() throws IOException, JAXBException {
        SchemaEnvironmentManager.dropDatabases();
    }
    
    @Before
    public final void initTables() throws SQLException, ParseException, IOException, JAXBException {
        SchemaEnvironmentManager.createTables();
        dataSetEnvironmentManager.fillData();
        try (Connection connection = getTargetDataSource().getConnection()) {
            executeInitSQLs(connection);
        }
        resetTargetDataSource();
    }
    
    private void executeInitSQLs(final Connection connection) throws SQLException {
        if (Strings.isNullOrEmpty(((DDLIntegrateTestCaseAssertion) getAssertion()).getInitSQL())) {
            return;
        }
        for (String each : Splitter.on(";").trimResults().splitToList(((DDLIntegrateTestCaseAssertion) getAssertion()).getInitSQL())) {
            connection.prepareStatement(each).executeUpdate();
        }
    }
    
    @After
    public final void destroyTables() throws JAXBException, IOException, SQLException {
        SchemaEnvironmentManager.dropTables();
        try (Connection connection = getTargetDataSource().getConnection()) {
            dropInitializedTable(connection);
        }
    }
    
    private void dropInitializedTable(final Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("DROP TABLE %s", ((DDLIntegrateTestCaseAssertion) getAssertion()).getTable()))) {
            preparedStatement.executeUpdate();
        } catch (final SQLException ignored) {
        }
    }
    
    protected final void assertTableMetaData() throws SQLException {
        String tableName = ((DDLIntegrateTestCaseAssertion) getAssertion()).getTable();
        DataSetMetadata expected = getDataSet().findMetadata(tableName);
        Collection<DataNode> dataNodes = new InlineExpressionParser(expected.getDataNodes()).splitAndEvaluate().stream().map(DataNode::new).collect(Collectors.toList());
        if (expected.getColumns().isEmpty()) {
            assertFalse(containsTable(dataNodes));
            return;
        }
        assertTableMetaData(getActualColumns(dataNodes), getActualIndexes(dataNodes), expected);
    }
    
    private void assertTableMetaData(final List<DataSetColumn> actualColumns, final List<DataSetIndex> actualIndexes, final DataSetMetadata expected) {
        for (DataSetColumn each : expected.getColumns()) {
            assertColumnMetaData(actualColumns, each);
        }
        for (DataSetIndex each : expected.getIndexes()) {
            assertIndexMetaData(actualIndexes, each);
        }
    }
    
    private boolean containsTable(final Collection<DataNode> dataNodes) throws SQLException {
        for (DataNode each : dataNodes) {
            try (Connection connection = getActualDataSources().get(each.getDataSourceName()).getConnection()) {
                if (containsTable(connection, each.getTableName())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean containsTable(final Connection connection, final String tableName) throws SQLException {
        return connection.getMetaData().getTables(null, null, tableName, new String[]{"TABLE"}).next();
    }
    
    private List<DataSetColumn> getActualColumns(final Collection<DataNode> dataNodes) throws SQLException {
        List<DataSetColumn> result = new LinkedList<>();
        for (DataNode each : dataNodes) {
            try (Connection connection = getActualDataSources().get(each.getDataSourceName()).getConnection()) {
                result.addAll(getActualColumns(connection, each.getTableName()));
            }
        }
        return result;
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
    
    private List<DataSetIndex> getActualIndexes(final Collection<DataNode> dataNodes) throws SQLException {
        List<DataSetIndex> result = new LinkedList<>();
        for (DataNode each : dataNodes) {
            try (Connection connection = getActualDataSources().get(each.getDataSourceName()).getConnection()) {
                result.addAll(getActualIndexes(connection, each.getTableName()));
            }
        }
        return result;
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
    
    // TODO need to assert line by line
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
    
    // TODO need to assert line by line
    private void assertIndexMetaData(final List<DataSetIndex> actual, final DataSetIndex expect) {
        for (DataSetIndex each : actual) {
            if (expect.getName().equals(each.getName())) {
                assertThat(each.isUnique(), is(expect.isUnique()));
            }
        }
    }
}
