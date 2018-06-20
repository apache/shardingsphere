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

import io.shardingsphere.core.api.yaml.YamlMasterSlaveDataSourceFactory;
import io.shardingsphere.core.api.yaml.YamlShardingDataSourceFactory;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.core.parsing.cache.ParsingResultCache;
import io.shardingsphere.dbtest.cases.assertion.IntegrateTestCasesLoader;
import io.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCase;
import io.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.cases.assertion.root.SQLValue;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.dbtest.env.IntegrateTestEnvironment;
import io.shardingsphere.dbtest.env.datasource.DataSourceUtil;
import io.shardingsphere.dbtest.env.schema.SchemaEnvironmentManager;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@RunWith(Parameterized.class)
@Getter(AccessLevel.PROTECTED)
public abstract class BaseIntegrateTest {
    
    private static IntegrateTestCasesLoader integrateTestCasesLoader = IntegrateTestCasesLoader.getInstance();
    
    private static Map<String, Map<String, DataSource>> dataSourceMapWithShardingRuleType = new HashMap<>();
    
    private final DatabaseTypeEnvironment databaseTypeEnvironment;
    
    private final IntegrateTestCaseAssertion assertion;
    
    private final SQLCaseType caseType;
    
    private final int countInSameCase;
    
    private final String sql;
    
    private final String expectedDataFile;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final DataSource dataSource;
    
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    
    public BaseIntegrateTest(final String sqlCaseId, final String path, final IntegrateTestCaseAssertion assertion, final DatabaseTypeEnvironment databaseTypeEnvironment, 
                             final SQLCaseType caseType, final int countInSameCase) throws IOException, JAXBException, SQLException, ParseException {
        this.databaseTypeEnvironment = databaseTypeEnvironment;
        this.assertion = assertion;
        this.caseType = caseType;
        this.countInSameCase = countInSameCase;
        sql = getSQL(sqlCaseId);
        expectedDataFile = path.substring(0, path.lastIndexOf(File.separator) + 1) + "dataset/" + assertion.getExpectedDataFile();
        if (databaseTypeEnvironment.isEnabled()) {
            dataSourceMap = createDataSourceMap(assertion);
            dataSource = createDataSource(dataSourceMap);
        } else {
            dataSourceMap = null;
            dataSource = null;
        }
    }
    
    private String getSQL(final String sqlCaseId) throws ParseException {
        List<String> parameters = new LinkedList<>();
        for (SQLValue each : assertion.getSQLValues()) {
            parameters.add(each.toString());
        }
        return SQLCasesLoader.getInstance().getSupportedSQL(sqlCaseId, caseType, parameters);
    }
    
    private Map<String, DataSource> createDataSourceMap(final IntegrateTestCaseAssertion assertion) throws IOException, JAXBException {
        if (dataSourceMapWithShardingRuleType.containsKey(assertion.getShardingRuleType())) {
            return dataSourceMapWithShardingRuleType.get(assertion.getShardingRuleType());
        }
        Collection<String> dataSourceNames = SchemaEnvironmentManager.getDataSourceNames(assertion.getShardingRuleType());
        Map<String, DataSource> result = new HashMap<>(dataSourceNames.size(), 1);
        for (String each : dataSourceNames) {
            result.put(each, DataSourceUtil.createDataSource(databaseTypeEnvironment.getDatabaseType(), each));
        }
        dataSourceMapWithShardingRuleType.put(assertion.getShardingRuleType(), result);
        return result;
    }
    
    private DataSource createDataSource(final Map<String, DataSource> dataSourceMap) throws SQLException, IOException {
        return "masterslave".equals(assertion.getShardingRuleType())
                ? YamlMasterSlaveDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(assertion.getShardingRuleType())))
                : YamlShardingDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getShardingRuleResourceFile(assertion.getShardingRuleType())));
    }
    
    protected static Collection<Object[]> getParameters(final DatabaseType databaseType, final SQLCaseType caseType, final IntegrateTestCase integrateTestCase) {
        Collection<Object[]> result = new LinkedList<>();
        int countInSameCase = 0;
        for (IntegrateTestCaseAssertion assertion : integrateTestCase.getIntegrateTestCaseAssertions()) {
            Object[] data = new Object[6];
            data[0] = integrateTestCase.getSqlCaseId();
            data[1] = integrateTestCase.getPath();
            data[2] = assertion;
            data[3] = new DatabaseTypeEnvironment(databaseType, IntegrateTestEnvironment.getInstance().getDatabaseTypes().contains(databaseType));
            data[4] = caseType;
            data[5] = countInSameCase++;
            result.add(data);
        }
        return result;
    }
    
    @BeforeClass
    public static void createDatabasesAndTables() throws JAXBException, IOException {
        for (String each : integrateTestCasesLoader.getShardingRuleTypes()) {
            SchemaEnvironmentManager.dropDatabase(each);
        }
        for (String each : integrateTestCasesLoader.getShardingRuleTypes()) {
            SchemaEnvironmentManager.createDatabase(each);
        }
        for (String each : integrateTestCasesLoader.getShardingRuleTypes()) {
            SchemaEnvironmentManager.dropTable(each);
        }
        for (String each : integrateTestCasesLoader.getShardingRuleTypes()) {
            SchemaEnvironmentManager.createTable(each);
        }
    }
    
    @AfterClass
    public static void dropDatabases() throws JAXBException, IOException {
        for (String each : integrateTestCasesLoader.getShardingRuleTypes()) {
            SchemaEnvironmentManager.dropDatabase(each);
        }
    }
    
    @After
    public void tearDown() {
        if (dataSource instanceof ShardingDataSource) {
            ((ShardingDataSource) dataSource).close();
        }
        ParsingResultCache.getInstance().clear();
    }
}
