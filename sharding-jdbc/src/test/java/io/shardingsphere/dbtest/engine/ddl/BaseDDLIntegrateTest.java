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

package io.shardingsphere.dbtest.engine.ddl;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.dbtest.cases.assertion.ddl.DDLIntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.cases.dataset.DataSet;
import io.shardingsphere.dbtest.cases.dataset.metadata.DataSetColumn;
import io.shardingsphere.dbtest.cases.dataset.metadata.DataSetMetadata;
import io.shardingsphere.dbtest.engine.BaseIntegrateTest;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.dbtest.env.dataset.DataSetEnvironmentManager;
import io.shardingsphere.test.sql.SQLCaseType;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
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

@RunWith(Parameterized.class)
public abstract class BaseDDLIntegrateTest extends BaseIntegrateTest {
    
    private final DDLIntegrateTestCaseAssertion assertion;
    
    private final DatabaseType databaseType;
    
    public BaseDDLIntegrateTest(final String sqlCaseId, final String path, final DDLIntegrateTestCaseAssertion assertion, final String shardingRuleType,
                                final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) throws IOException, JAXBException, SQLException, ParseException {
        super(sqlCaseId, path, assertion, shardingRuleType, databaseTypeEnvironment, caseType);
        this.assertion = assertion;
        databaseType = databaseTypeEnvironment.getDatabaseType();
    }
    
    @Before
    public void insertData() throws SQLException, ParseException, IOException, JAXBException {
        if (getDatabaseTypeEnvironment().isEnabled()) {
            new DataSetEnvironmentManager(EnvironmentPath.getDataInitializeResourceFile(getShardingRuleType()), getDataSourceMap()).initialize();
        }
    }
    
    protected void assertMetadata(final Connection connection) throws IOException, JAXBException, SQLException {
        // TODO case for drop (index) and truncate, add assert later
        if (null == assertion.getExpectedDataFile()) {
            return;
        }
        DataSet expected;
        try (FileReader reader = new FileReader(getExpectedDataFile())) {
            expected = (DataSet) JAXBContext.newInstance(DataSet.class).createUnmarshaller().unmarshal(reader);
        }
        String tableName = assertion.getTable();
        List<DataSetColumn> actualColumns = getActualColumns(connection, tableName);
        assertMetadata(actualColumns, expected.findMetadata(tableName));
    }
    
    private void assertMetadata(final List<DataSetColumn> actual, final DataSetMetadata expected) {
        for (DataSetColumn each : expected.getColumns()) {
            assertMetadata(actual, each);
        }
    }
    
    private void assertMetadata(final List<DataSetColumn> actual, final DataSetColumn expect) {
        for (DataSetColumn each : actual) {
            if (expect.getName().equals(each.getName())) {
                if (DatabaseType.MySQL == databaseType && "integer".equals(expect.getType())) {
                    assertThat(each.getType(), is("int"));
                } else if (DatabaseType.PostgreSQL == databaseType && "integer".equals(expect.getType())) {
                    assertThat(each.getType(), is("int4"));
                } else {
                    assertThat(each.getType(), is(expect.getType()));
                }
            }
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
    
    protected void dropTableIfExisted(final Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("DROP TABLE %s", assertion.getTable()))) {
            preparedStatement.executeUpdate();
            // CHECKSTYLE: OFF
        } catch (final SQLException ignored) {
            // CHECKSTYLE: ON
        }
    }
}
