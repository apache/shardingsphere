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

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.dbtest.asserts.DDLAssertEngine;
import io.shardingsphere.dbtest.asserts.DataSetEnvironmentManager;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.dbtest.env.datasource.DataSourceUtil;
import io.shardingsphere.dbtest.env.schema.SchemaEnvironmentManager;
import io.shardingsphere.dbtest.jaxb.assertion.IntegrateTestCasesLoader;
import io.shardingsphere.dbtest.jaxb.assertion.ddl.DDLIntegrateTestCase;
import io.shardingsphere.dbtest.jaxb.assertion.ddl.DDLIntegrateTestCaseAssertion;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import org.junit.Before;
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
import java.util.Map;

@RunWith(Parameterized.class)
public final class DDLIntegrateTest extends BaseIntegrateTest {
    
    private static SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private static IntegrateTestCasesLoader integrateTestCasesLoader = IntegrateTestCasesLoader.getInstance();
    
    private final DatabaseTypeEnvironment databaseTypeEnvironment;
    
    private final SQLCaseType caseType;
    
    private final DataSetEnvironmentManager dataSetEnvironmentManager;
    
    private final DDLAssertEngine ddlAssertEngine;
    
    public DDLIntegrateTest(final String sqlCaseId, final String path, final DDLIntegrateTestCaseAssertion integrateTestCaseAssertion,
                            final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) throws IOException, JAXBException, SQLException {
        this.databaseTypeEnvironment = databaseTypeEnvironment;
        this.caseType = caseType;
        if (databaseTypeEnvironment.isEnabled()) {
            Map<String, DataSource> dataSourceMap = createDataSourceMap(integrateTestCaseAssertion);
            dataSetEnvironmentManager = new DataSetEnvironmentManager(EnvironmentPath.getDataInitializeResourceFile(integrateTestCaseAssertion.getShardingRuleType()), dataSourceMap);
            ddlAssertEngine = new DDLAssertEngine(sqlCaseId, path, integrateTestCaseAssertion, dataSourceMap, databaseTypeEnvironment.getDatabaseType());
        } else {
            dataSetEnvironmentManager = null;
            ddlAssertEngine = null;
        }
    }
    
    private Map<String, DataSource> createDataSourceMap(final DDLIntegrateTestCaseAssertion integrateTestCaseAssertion) throws IOException, JAXBException {
        Collection<String> dataSourceNames = SchemaEnvironmentManager.getDataSourceNames(integrateTestCaseAssertion.getShardingRuleType());
        Map<String, DataSource> result = new HashMap<>(dataSourceNames.size(), 1);
        for (String each : dataSourceNames) {
            result.put(each, DataSourceUtil.createDataSource(databaseTypeEnvironment.getDatabaseType(), each));
        }
        return result;
    }
    
    @Parameters(name = "{0} -> {2} -> {3} -> {4}")
    public static Collection<Object[]> getParameters() {
        // TODO sqlCasesLoader size should eq integrateTestCasesLoader size
        // assertThat(sqlCasesLoader.countAllSupportedSQLCases(), is(integrateTestCasesLoader.countAllDataSetTestCases()));
        Collection<Object[]> result = new LinkedList<>();
        for (Object[] each : sqlCasesLoader.getSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class)) {
            String sqlCaseId = each[0].toString();
            DatabaseType databaseType = (DatabaseType) each[1];
            SQLCaseType caseType = (SQLCaseType) each[2];
            DDLIntegrateTestCase integrateTestCase = integrateTestCasesLoader.getDDLIntegrateTestCase(sqlCaseId);
            // TODO remove when transfer finished
            if (null == integrateTestCase) {
                continue;
            }
            if (getDatabaseTypes(integrateTestCase.getDatabaseTypes()).contains(databaseType)) {
                result.addAll(getParameters(databaseType, caseType, integrateTestCase));
            }
            
        }
        return result;
    }
    
    @Before
    public void insertData() throws SQLException, ParseException {
        if (databaseTypeEnvironment.isEnabled()) {
            dataSetEnvironmentManager.initialize(false);
        }
    }
    
    @Test
    public void assertExecuteUpdate() throws JAXBException, IOException, SQLException {
        if (!databaseTypeEnvironment.isEnabled()) {
            return;
        }
        if (SQLCaseType.Literal == caseType) {
            ddlAssertEngine.assertExecuteUpdateForStatement();
        } else {
            ddlAssertEngine.assertExecuteUpdateForPreparedStatement();
        }
    }
    
    @Test
    public void assertExecute() throws JAXBException, IOException, SQLException {
        if (!databaseTypeEnvironment.isEnabled()) {
            return;
        }
        if (SQLCaseType.Literal == caseType) {
            ddlAssertEngine.assertExecuteForStatement();
        } else {
            ddlAssertEngine.assertExecuteForPreparedStatement();
        }
    }
}
