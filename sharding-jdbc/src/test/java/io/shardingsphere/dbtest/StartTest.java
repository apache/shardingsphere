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

package io.shardingsphere.dbtest;

import com.google.common.base.Splitter;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.dbtest.asserts.DDLAssertEngine;
import io.shardingsphere.dbtest.asserts.DMLAssertEngine;
import io.shardingsphere.dbtest.asserts.DQLAssertEngine;
import io.shardingsphere.dbtest.asserts.DataSetEnvironmentManager;
import io.shardingsphere.dbtest.jaxb.assertion.IntegrateTestCasesLoader;
import io.shardingsphere.dbtest.jaxb.assertion.ddl.DDLIntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.jaxb.assertion.dml.DMLIntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.jaxb.assertion.dql.DQLIntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.jaxb.assertion.root.IntegrateTestCase;
import io.shardingsphere.dbtest.jaxb.assertion.root.IntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.dbtest.env.IntegrateTestEnvironment;
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
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public final class StartTest {
    
    private static SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private static IntegrateTestCasesLoader integrateTestCasesLoader = IntegrateTestCasesLoader.getInstance();
    
    private static boolean isInitialized = IntegrateTestEnvironment.getInstance().isInitialized();
    
    private static boolean isCleaned = IntegrateTestEnvironment.getInstance().isInitialized();
    
    private final String sqlCaseId;
    
    private final String path;
    
    private final IntegrateTestCaseAssertion integrateTestCaseAssertion;
    
    private final String shardingRuleType;
    
    private final DatabaseTypeEnvironment databaseTypeEnvironment;
    
    private final SQLCaseType caseType;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final DataSetEnvironmentManager dataSetEnvironmentManager;
    
    public StartTest(final String sqlCaseId, final String path, final IntegrateTestCaseAssertion integrateTestCaseAssertion, 
                     final String shardingRuleType, final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) throws IOException, JAXBException {
        this.sqlCaseId = sqlCaseId;
        this.path = path;
        this.integrateTestCaseAssertion = integrateTestCaseAssertion;
        this.shardingRuleType = shardingRuleType;
        this.databaseTypeEnvironment = databaseTypeEnvironment;
        this.caseType = caseType;
        if (databaseTypeEnvironment.isEnabled()) {
            dataSourceMap = createDataSourceMap();
            dataSetEnvironmentManager = new DataSetEnvironmentManager(EnvironmentPath.getDataInitializeResourceFile(shardingRuleType), dataSourceMap);
        } else {
            dataSourceMap = null;
            dataSetEnvironmentManager = null;
        }
    }
    
    private Map<String, DataSource> createDataSourceMap() throws IOException, JAXBException {
        Collection<String> dataSourceNames = SchemaEnvironmentManager.getDataSourceNames(shardingRuleType);
        Map<String, DataSource> result = new HashMap<>(dataSourceNames.size(), 1);
        for (String each : dataSourceNames) {
            result.put(each, DataSourceUtil.createDataSource(databaseTypeEnvironment.getDatabaseType(), each));
        }
        return result;
    }
    
    @Parameters(name = "{0} -> Rule:{3} -> {4}")
    public static Collection<Object[]> getParameters() {
        // TODO sqlCasesLoader size should eq integrateTestCasesLoader size
        // assertThat(sqlCasesLoader.countAllSupportedSQLCases(), is(integrateTestCasesLoader.countAllDataSetTestCases()));
        Collection<Object[]> result = new LinkedList<>();
        for (Object[] each : sqlCasesLoader.getSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class)) {
            String sqlCaseId = each[0].toString();
            DatabaseType databaseType = (DatabaseType) each[1];
            SQLCaseType caseType = (SQLCaseType) each[2];
            IntegrateTestCase integrateTestCase = integrateTestCasesLoader.getIntegrateTestCase(sqlCaseId);
            // TODO remove when transfer finished
            if (null == integrateTestCase) {
                continue;
            }
            if (!getDatabaseTypes(integrateTestCase.getDatabaseTypes()).contains(databaseType)) {
                continue;
            }
            for (IntegrateTestCaseAssertion assertion : integrateTestCase.getIntegrateTestCaseAssertions()) {
                Object[] data = new Object[6];
                data[0] = integrateTestCase.getSqlCaseId();
                data[1] = integrateTestCase.getPath();
                data[2] = assertion;
                data[3] = assertion.getShardingRuleType();
                data[4] = new DatabaseTypeEnvironment(databaseType, IntegrateTestEnvironment.getInstance().getDatabaseTypes().contains(databaseType));
                data[5] = caseType;
                result.add(data);
            }
        }
        return result;
    }
    
    private static List<DatabaseType> getDatabaseTypes(final String databaseTypes) {
        List<DatabaseType> result = new LinkedList<>();
        for (String each : Splitter.on(",").trimResults().splitToList(databaseTypes)) {
            result.add(DatabaseType.valueOf(each));
        }
        return result;
    }
    
    @BeforeClass
    public static void createDatabasesAndTables() throws JAXBException, IOException {
        if (isInitialized) {
            isInitialized = false;
        } else {
            for (String each : integrateTestCasesLoader.getShardingRuleTypes()) {
                SchemaEnvironmentManager.dropDatabase(each);
            }
        }
        for (String each : integrateTestCasesLoader.getShardingRuleTypes()) {
            SchemaEnvironmentManager.createDatabase(each);
        }
        for (String each : integrateTestCasesLoader.getShardingRuleTypes()) {
            SchemaEnvironmentManager.createTable(each);
        }
    }
    
    @Before
    public void insertData() throws SQLException, ParseException {
        if (databaseTypeEnvironment.isEnabled()) {
            dataSetEnvironmentManager.initialize();
        }
    }
    
    @AfterClass
    public static void dropDatabases() throws JAXBException, IOException {
        if (isCleaned) {
            for (String each : integrateTestCasesLoader.getShardingRuleTypes()) {
                SchemaEnvironmentManager.dropDatabase(each);
            }
            isCleaned = false;
        }
    }
    
    @After
    public void clearData() throws SQLException {
        if (databaseTypeEnvironment.isEnabled()) {
            dataSetEnvironmentManager.clear();
        }
    }
    
    @Test
    public void test() throws JAXBException, ParseException, IOException, SQLException {
        if (!databaseTypeEnvironment.isEnabled()) {
            return;
        }
        if (integrateTestCaseAssertion instanceof DQLIntegrateTestCaseAssertion) {
            new DQLAssertEngine(sqlCaseId, path, (DQLIntegrateTestCaseAssertion) integrateTestCaseAssertion, dataSourceMap, shardingRuleType, caseType).assertDQL();
        } else if (integrateTestCaseAssertion instanceof DMLIntegrateTestCaseAssertion) {
            new DMLAssertEngine(sqlCaseId, path, dataSetEnvironmentManager, (DMLIntegrateTestCaseAssertion) integrateTestCaseAssertion, dataSourceMap, shardingRuleType, caseType).assertDML();
        } else if (integrateTestCaseAssertion instanceof DDLIntegrateTestCaseAssertion) {
            new DDLAssertEngine(sqlCaseId, path, (DDLIntegrateTestCaseAssertion) integrateTestCaseAssertion, dataSourceMap, shardingRuleType, databaseTypeEnvironment, caseType).assertDDL();
        } else {
            throw new UnsupportedOperationException(integrateTestCaseAssertion.getClass().getName());
        }
    }
}
