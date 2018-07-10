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

import com.google.common.base.Joiner;
import io.shardingsphere.core.api.yaml.YamlMasterSlaveDataSourceFactory;
import io.shardingsphere.core.api.yaml.YamlShardingDataSourceFactory;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.core.parsing.cache.ParsingResultCache;
import io.shardingsphere.dbtest.cases.assertion.IntegrateTestCasesLoader;
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
    
    private static IntegrateTestEnvironment integrateTestEnvironment = IntegrateTestEnvironment.getInstance();
    
    private static IntegrateTestCasesLoader integrateTestCasesLoader = IntegrateTestCasesLoader.getInstance();
    
    private final String shardingRuleType;
    
    private final DatabaseTypeEnvironment databaseTypeEnvironment;
    
    private final IntegrateTestCaseAssertion assertion;
    
    private final SQLCaseType caseType;
    
    private final String sql;
    
    private final String expectedDataFile;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final DataSource dataSource;
    
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    
    public BaseIntegrateTest(final String sqlCaseId, final String path, final IntegrateTestCaseAssertion assertion, final String shardingRuleType, 
                             final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) 
            throws IOException, JAXBException, SQLException, ParseException {
        this.shardingRuleType = shardingRuleType;
        this.databaseTypeEnvironment = databaseTypeEnvironment;
        this.assertion = assertion;
        this.caseType = caseType;
        sql = getSQL(sqlCaseId);
        expectedDataFile = getExpectedDataFile(path, shardingRuleType, databaseTypeEnvironment.getDatabaseType(), assertion.getExpectedDataFile());
        if (databaseTypeEnvironment.isEnabled()) {
            dataSourceMap = createDataSourceMap(shardingRuleType);
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
    
    @After
    public void tearDown() {
        if (dataSource instanceof ShardingDataSource) {
            ((ShardingDataSource) dataSource).close();
        }
        ParsingResultCache.getInstance().clear();
    }
}
