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

package io.shardingsphere.dbtest.engine;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.util.InlineExpressionParser;
import io.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCase;
import io.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.cases.dataset.DataSet;
import io.shardingsphere.dbtest.cases.dataset.metadata.DataSetColumn;
import io.shardingsphere.dbtest.cases.dataset.metadata.DataSetMetadata;
import io.shardingsphere.dbtest.cases.dataset.row.DataSetRow;
import io.shardingsphere.dbtest.engine.util.IntegrateTestParameters;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.dbtest.env.dataset.DataSetEnvironmentManager;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Getter(value = AccessLevel.PROTECTED)
public abstract class BatchIntegrateTest extends BaseIntegrateTest {
    
    private static DataSetEnvironmentManager dataSetEnvironmentManager;
    
    private final IntegrateTestCase integrateTestCase;
    
    private final String sql;
    
    private final Collection<String> expectedDataFiles;
    
    public BatchIntegrateTest(final String sqlCaseId, final IntegrateTestCase integrateTestCase, 
                              final String shardingRuleType, final DatabaseTypeEnvironment databaseTypeEnvironment) throws IOException, JAXBException, SQLException {
        super(shardingRuleType, databaseTypeEnvironment);
        this.integrateTestCase = integrateTestCase;
        sql = SQLCasesLoader.getInstance().getSupportedSQL(sqlCaseId, SQLCaseType.Placeholder, Collections.emptyList());
        expectedDataFiles = new LinkedList<>();
        for (IntegrateTestCaseAssertion each : integrateTestCase.getIntegrateTestCaseAssertions()) {
            expectedDataFiles.add(getExpectedDataFile(integrateTestCase.getPath(), shardingRuleType, databaseTypeEnvironment.getDatabaseType(), each.getExpectedDataFile()));
        }
        dataSetEnvironmentManager = databaseTypeEnvironment.isEnabled() ? new DataSetEnvironmentManager(EnvironmentPath.getDataInitializeResourceFile(shardingRuleType), getDataSourceMap()) : null;
    }
    
    @Parameters(name = "{0} -> Rule:{2} -> {3}")
    public static Collection<Object[]> getParameters() {
        return IntegrateTestParameters.getParametersWithCase(SQLType.DML);
    }
    
    @BeforeClass
    public static void initDatabasesAndTables() {
        createDatabasesAndTables();
    }
    
    @AfterClass
    public static void destroyDatabasesAndTables() {
        dropDatabases();
    }
    
    @Before
    public void insertData() throws SQLException, ParseException {
        if (getDatabaseTypeEnvironment().isEnabled()) {
            dataSetEnvironmentManager.initialize();
        }
    }
    
    @After
    public void clearData() throws SQLException {
        if (getDatabaseTypeEnvironment().isEnabled()) {
            dataSetEnvironmentManager.clear();
        }
    }
    
    protected final void assertDataSet(final int[] actualUpdateCounts) throws SQLException, IOException, JAXBException {
        Collection<DataSet> expectedList = new LinkedList<>();
        assertThat(actualUpdateCounts.length, is(getExpectedDataFiles().size()));
        int count = 0;
        for (String each : getExpectedDataFiles()) {
            try (FileReader reader = new FileReader(each)) {
                DataSet expected = (DataSet) JAXBContext.newInstance(DataSet.class).createUnmarshaller().unmarshal(reader);
                assertThat(actualUpdateCounts[count], is(expected.getUpdateCount()));
                expectedList.add(expected);
            }
            count++;
        }
        DataSet expected = merge(expectedList);
        assertThat("Only support single table for DML.", expected.getMetadataList().size(), is(1));
        DataSetMetadata expectedDataSetMetadata = expected.getMetadataList().get(0);
        for (String each : new InlineExpressionParser(expectedDataSetMetadata.getDataNodes()).splitAndEvaluate()) {
            DataNode dataNode = new DataNode(each);
            try (Connection connection = getDataSourceMap().get(dataNode.getDataSourceName()).getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM %s ORDER BY 1", dataNode.getTableName()))) {
                assertDataSet(preparedStatement, expected.findRows(dataNode), expectedDataSetMetadata);
            }
        }
    }
    
    private void assertDataSet(final PreparedStatement actualPreparedStatement, final List<DataSetRow> expectedDataSetRows, final DataSetMetadata expectedDataSetMetadata) throws SQLException {
        try (ResultSet actualResultSet = actualPreparedStatement.executeQuery()) {
            assertMetaData(actualResultSet.getMetaData(), expectedDataSetMetadata.getColumns());
            assertRows(actualResultSet, expectedDataSetRows);
        }
    }
    
    private DataSet merge(final Collection<DataSet> expectedList) {
        DataSet result = new DataSet();
        Set<List<String>> existedRowValues = new HashSet<>();
        for (DataSet each : expectedList) {
            mergeMetadata(each, result);
            mergeRow(each, result, existedRowValues);
        }
        sortRow(result);
        return result;
    }
    
    private void mergeMetadata(final DataSet original, final DataSet dist) {
        if (dist.getMetadataList().isEmpty()) {
            dist.getMetadataList().addAll(original.getMetadataList());
        }
    }
    
    private void mergeRow(final DataSet original, final DataSet dist, final Set<List<String>> existedRowValues) {
        for (DataSetRow each : original.getRows()) {
            if (existedRowValues.add(each.getValues())) {
                dist.getRows().add(each);
            }
        }
    }
    
    private void sortRow(final DataSet dataSet) {
        Collections.sort(dataSet.getRows(), new Comparator<DataSetRow>() {
            
            @Override
            public int compare(final DataSetRow o1, final DataSetRow o2) {
                return Integer.parseInt(o1.getValues().get(0)) - Integer.parseInt(o2.getValues().get(0));
            }
        });
    }
    
    private void assertMetaData(final ResultSetMetaData actualMetaData, final List<DataSetColumn> columnMetadataList) throws SQLException {
        assertThat(actualMetaData.getColumnCount(), is(columnMetadataList.size()));
        int index = 1;
        for (DataSetColumn each : columnMetadataList) {
            assertThat(actualMetaData.getColumnLabel(index++), is(each.getName()));
        }
    }
    
    private void assertRows(final ResultSet actualResultSet, final List<DataSetRow> expectedDatSetRows) throws SQLException {
        int count = 0;
        while (actualResultSet.next()) {
            int index = 1;
            for (String each : expectedDatSetRows.get(count).getValues()) {
                if (Types.DATE == actualResultSet.getMetaData().getColumnType(index)) {
                    assertThat(new SimpleDateFormat("yyyy-MM-dd").format(actualResultSet.getDate(index)), is(each));
                } else {
                    assertThat(String.valueOf(actualResultSet.getObject(index)), is(each));
                }
                index++;
            }
            count++;
        }
        assertThat("Size of actual result set is different with size of expected dat set rows.", count, is(expectedDatSetRows.size()));
    }
}
