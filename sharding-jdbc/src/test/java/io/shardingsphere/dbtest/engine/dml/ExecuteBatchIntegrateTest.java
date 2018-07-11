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

package io.shardingsphere.dbtest.engine.dml;

import com.google.common.base.Joiner;
import io.shardingsphere.core.api.yaml.YamlMasterSlaveDataSourceFactory;
import io.shardingsphere.core.api.yaml.YamlShardingDataSourceFactory;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.core.parsing.cache.ParsingResultCache;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.util.InlineExpressionParser;
import io.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCase;
import io.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.cases.assertion.root.SQLValue;
import io.shardingsphere.dbtest.cases.dataset.DataSet;
import io.shardingsphere.dbtest.cases.dataset.metadata.DataSetColumn;
import io.shardingsphere.dbtest.cases.dataset.metadata.DataSetMetadata;
import io.shardingsphere.dbtest.cases.dataset.row.DataSetRow;
import io.shardingsphere.dbtest.engine.IntegrateTestParameters;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.dbtest.env.IntegrateTestEnvironment;
import io.shardingsphere.dbtest.env.dataset.DataSetEnvironmentManager;
import io.shardingsphere.dbtest.env.datasource.DataSourceUtil;
import io.shardingsphere.dbtest.env.schema.SchemaEnvironmentManager;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public final class ExecuteBatchIntegrateTest {
    
    private static IntegrateTestEnvironment integrateTestEnvironment = IntegrateTestEnvironment.getInstance();
    
    private static DataSetEnvironmentManager dataSetEnvironmentManager;
    
    private final String shardingRuleType;
    
    private final DatabaseTypeEnvironment databaseTypeEnvironment;
    
    private final IntegrateTestCase integrateTestCase;
    
    private final String sql;
    
    private final Collection<String> expectedDataFiles;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final DataSource dataSource;
    
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    
    public ExecuteBatchIntegrateTest(final String sqlCaseId, final IntegrateTestCase integrateTestCase, final String shardingRuleType, final DatabaseTypeEnvironment databaseTypeEnvironment) 
            throws IOException, JAXBException, SQLException {
        this.shardingRuleType = shardingRuleType;
        this.databaseTypeEnvironment = databaseTypeEnvironment;
        this.integrateTestCase = integrateTestCase;
        sql = SQLCasesLoader.getInstance().getSupportedSQL(sqlCaseId, SQLCaseType.Placeholder, Collections.emptyList());
        expectedDataFiles = new LinkedList<>();
        for (IntegrateTestCaseAssertion each : integrateTestCase.getIntegrateTestCaseAssertions()) {
            expectedDataFiles.add(getExpectedDataFile(integrateTestCase.getPath(), shardingRuleType, databaseTypeEnvironment.getDatabaseType(), each.getExpectedDataFile()));
        }
        if (databaseTypeEnvironment.isEnabled()) {
            dataSourceMap = createDataSourceMap(shardingRuleType);
            dataSource = createDataSource(dataSourceMap);
            dataSetEnvironmentManager = new DataSetEnvironmentManager(EnvironmentPath.getDataInitializeResourceFile(shardingRuleType), dataSourceMap);
        } else {
            dataSourceMap = null;
            dataSource = null;
            dataSetEnvironmentManager = null;
        }
    }
    
    private String getExpectedDataFile(final String path, final String shardingRuleType, final DatabaseType databaseType, final String expectedDataFile) {
        if (null == expectedDataFile) {
            return null;
        }
        String prefix = path.substring(0, path.lastIndexOf(File.separator));
        String result = Joiner.on("/").join(prefix, "dataset", shardingRuleType, databaseType.toString().toLowerCase(), expectedDataFile);
        if (new File(result).exists()) {
            return result;
        }
        result = Joiner.on("/").join(prefix, "dataset", shardingRuleType, expectedDataFile);
        if (new File(result).exists()) {
            return result;
        }
        return Joiner.on("/").join(prefix, "dataset", expectedDataFile);
    }
    
    private Map<String, DataSource> createDataSourceMap(final String shardingRuleType) throws IOException, JAXBException {
        Collection<String> dataSourceNames = SchemaEnvironmentManager.getDataSourceNames(shardingRuleType);
        Map<String, DataSource> result = new HashMap<>(dataSourceNames.size(), 1);
        for (String each : dataSourceNames) {
            result.put(each, DataSourceUtil.createDataSource(databaseTypeEnvironment.getDatabaseType(), each));
        }
        return result;
    }
    
    private DataSource createDataSource(final Map<String, DataSource> dataSourceMap) throws SQLException, IOException {
        return "masterslave".equals(shardingRuleType)
                ? YamlMasterSlaveDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(shardingRuleType)))
                : YamlShardingDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(shardingRuleType)));
    }
    
    @Parameters(name = "{0} -> Rule:{2} -> {3}")
    public static Collection<Object[]> getParameters() {
        return IntegrateTestParameters.getParametersWithCase(SQLType.DML);
    }
    
    @BeforeClass
    public static void createDatabasesAndTables() throws JAXBException, IOException, SQLException {
        for (String each : integrateTestEnvironment.getShardingRuleTypes()) {
            SchemaEnvironmentManager.dropDatabase(each);
        }
        for (String each : integrateTestEnvironment.getShardingRuleTypes()) {
            SchemaEnvironmentManager.createDatabase(each);
        }
        for (String each : integrateTestEnvironment.getShardingRuleTypes()) {
            SchemaEnvironmentManager.dropTable(each);
        }
        for (String each : integrateTestEnvironment.getShardingRuleTypes()) {
            SchemaEnvironmentManager.createTable(each);
        }
    }
    
    @AfterClass
    public static void dropDatabases() throws JAXBException, IOException {
        for (String each : integrateTestEnvironment.getShardingRuleTypes()) {
            SchemaEnvironmentManager.dropDatabase(each);
        }
    }
    
    @Before
    public void insertData() throws SQLException, ParseException {
        if (databaseTypeEnvironment.isEnabled()) {
            dataSetEnvironmentManager.initialize();
        }
    }
    
    @After
    public void clearData() throws SQLException {
        if (databaseTypeEnvironment.isEnabled()) {
            dataSetEnvironmentManager.clear();
        }
    }
    
    @After
    public void tearDown() {
        if (dataSource instanceof ShardingDataSource) {
            ((ShardingDataSource) dataSource).close();
        }
        ParsingResultCache.getInstance().clear();
    }
    
    @Test
    public void assertExecuteBatch() throws JAXBException, IOException, SQLException, ParseException {
        // TODO fix masterslave
        if (!databaseTypeEnvironment.isEnabled() || "masterslave".equals(shardingRuleType)) {
            return;
        }
        int[] actualUpdateCounts;
        try (Connection connection = dataSource.getConnection()) {
            actualUpdateCounts = executeBatchForPreparedStatement(connection);
        }
        assertDataSet(actualUpdateCounts);
    }
    
    private int[] executeBatchForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (IntegrateTestCaseAssertion each : integrateTestCase.getIntegrateTestCaseAssertions()) {
                addBatch(preparedStatement, each);
            }
            return preparedStatement.executeBatch();
        }
    }
    
    private void addBatch(final PreparedStatement preparedStatement, final IntegrateTestCaseAssertion assertion) throws ParseException, SQLException {
        for (SQLValue each : assertion.getSQLValues()) {
            preparedStatement.setObject(each.getIndex(), each.getValue());
        }
        preparedStatement.addBatch();
    }
    
    private void assertDataSet(final int[] actualUpdateCounts) throws SQLException, IOException, JAXBException {
        Collection<DataSet> expectedList = new LinkedList<>();
        assertThat(actualUpdateCounts.length, is(expectedDataFiles.size()));
        int count = 0;
        for (String each : expectedDataFiles) {
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
            try (Connection connection = dataSourceMap.get(dataNode.getDataSourceName()).getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM %s ORDER BY 1", dataNode.getTableName()))) {
                assertDataSet(preparedStatement, expected.findRows(dataNode), expectedDataSetMetadata);
            }
        }
    }
    
    private DataSet merge(final Collection<DataSet> expectedList) {
        DataSet result = new DataSet();
        Set<List<String>> rowValues = new HashSet<>();
        for (DataSet each : expectedList) {
            if (result.getMetadataList().isEmpty()) {
                result.getMetadataList().addAll(each.getMetadataList());
            }
            for (DataSetRow row : each.getRows()) {
                if (rowValues.add(row.getValues())) {
                    result.getRows().add(row);
                }
            }
        }
        Collections.sort(result.getRows(), new Comparator<DataSetRow>() {
            
            @Override
            public int compare(final DataSetRow o1, final DataSetRow o2) {
                return Integer.parseInt(o1.getValues().get(0)) - Integer.parseInt(o2.getValues().get(0));
            }
        });
        return result;
    }
    
    private void assertDataSet(final PreparedStatement actualPreparedStatement, final List<DataSetRow> expectedDataSetRows, final DataSetMetadata expectedDataSetMetadata) throws SQLException {
        try (ResultSet actualResultSet = actualPreparedStatement.executeQuery()) {
            assertMetaData(actualResultSet.getMetaData(), expectedDataSetMetadata.getColumns());
            assertRows(actualResultSet, expectedDataSetRows);
        }
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
    
    @Test
    public void assertClearBatch() throws SQLException, ParseException {
        // TODO fix masterslave
        if (!databaseTypeEnvironment.isEnabled() || "masterslave".equals(shardingRuleType)) {
            return;
        }
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                for (IntegrateTestCaseAssertion each : integrateTestCase.getIntegrateTestCaseAssertions()) {
                    addBatch(preparedStatement, each);
                }
                preparedStatement.clearBatch();
                assertThat(preparedStatement.executeBatch().length, is(0));
            }
        }
    }
}
