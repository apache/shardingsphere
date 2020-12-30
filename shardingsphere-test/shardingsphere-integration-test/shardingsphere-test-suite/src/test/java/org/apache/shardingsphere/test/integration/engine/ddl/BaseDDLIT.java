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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.cases.assertion.ddl.DDLIntegrateTestCaseAssertion;
import org.apache.shardingsphere.test.integration.cases.assertion.root.SQLCaseType;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetIndex;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetMetadata;
import org.apache.shardingsphere.test.integration.engine.SingleIT;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Slf4j
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
        resetTargetDataSource();
        dataSetEnvironmentManager.load();
    }
    
    @After
    public final void destroyTables() throws JAXBException, IOException {
        SchemaEnvironmentManager.dropTables();
    }
    
    protected final void assertTableMetaData(final Connection connection) throws SQLException {
        String tableName = ((DDLIntegrateTestCaseAssertion) getAssertion()).getTable();
        List<DataSetColumn> actualColumns = getActualColumns(connection, tableName);
        List<DataSetIndex> actualIndexes = getActualIndexes(connection, tableName);
        if (actualColumns.isEmpty() || actualIndexes.isEmpty()) {
            assertIfDropTable(actualColumns);
            assertIfDropIndex(actualIndexes);
            return;
        }
        try {
            assertTableMetaData(actualColumns, actualIndexes, getDataSet().findMetadata(tableName));
        } catch (final AssertionError ex) {
            log.error("[ERROR] SQL::{}, Parameter::{}, Expect::{}", getCaseIdentifier(), getAssertion().getParameters(), getAssertion().getExpectedDataFile());
            throw ex;
        }
    }
    
    private void assertTableMetaData(final List<DataSetColumn> actualColumns, final List<DataSetIndex> actualIndexes, final DataSetMetadata expected) {
        for (DataSetColumn each : expected.getColumns()) {
            assertColumnMetaData(actualColumns, each);
        }
        for (DataSetIndex each : expected.getIndexes()) {
            assertIndexMetaData(actualIndexes, each);
        }
    }
    
    private List<DataSetColumn> getActualColumns(final Connection connection, final String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        boolean isTableExisted = metaData.getTables(null, null, tableName, new String[] {"TABLE"}).next();
        if (!isTableExisted) {
            return Collections.emptyList();
        }
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
    
    private void assertIfDropTable(final List<DataSetColumn> actualColumns) {
        if (getSql().startsWith("DROP TABLE")) {
            assertTrue(actualColumns.isEmpty());
        }
    }
    
    private void assertIfDropIndex(final List<DataSetIndex> actualIndexes) {
        if (getSql().startsWith("DROP INDEX")) {
            assertTrue(actualIndexes.isEmpty());
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
    
    protected final void dropTableIfExisted(final Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("DROP TABLE %s", ((DDLIntegrateTestCaseAssertion) getAssertion()).getTable()))) {
            preparedStatement.executeUpdate();
        } catch (final SQLException ignored) {
        }
    }
}
