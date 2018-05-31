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
import io.shardingsphere.dbtest.asserts.AssertEngine;
import io.shardingsphere.dbtest.asserts.DQLAssertEngine;
import io.shardingsphere.dbtest.asserts.DataSetAssertLoader;
import io.shardingsphere.dbtest.config.bean.AssertSubDefinition;
import io.shardingsphere.dbtest.config.bean.DQLDataSetAssert;
import io.shardingsphere.dbtest.config.bean.DataSetAssert;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.IntegrateTestEnvironment;
import io.shardingsphere.dbtest.env.schema.SchemaEnvironmentManager;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import lombok.RequiredArgsConstructor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class StartTest {
    
    private static SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private static DataSetAssertLoader dataSetAssertLoader = DataSetAssertLoader.getInstance();
    
    private static boolean isInitialized = IntegrateTestEnvironment.getInstance().isInitialized();
    
    private static boolean isCleaned = IntegrateTestEnvironment.getInstance().isInitialized();
    
    private final String sqlCaseId;
    
    private final String path;
    
    private final Object dataSetAssert;
    
    private final String shardingRuleType;
    
    private final DatabaseTypeEnvironment databaseTypeEnvironment;
    
    private final SQLCaseType caseType;
    
    @Parameters(name = "{0} -> Rule:{3} -> {4}")
    public static Collection<Object[]> getParameters() {
        Collection<Object[]> result = new LinkedList<>();
        for (Object[] each : sqlCasesLoader.getSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class)) {
            String sqlCaseId = each[0].toString();
            DatabaseType databaseType = (DatabaseType) each[1];
            SQLCaseType caseType = (SQLCaseType) each[2];
            DataSetAssert assertDefinition = dataSetAssertLoader.getDataSetAssert(sqlCaseId);
            // TODO remove when transfer finished
            if (null == assertDefinition) {
                continue;
            }
            if (!getDatabaseTypes(assertDefinition.getDatabaseTypes()).contains(databaseType)) {
                continue;
            }
            if (assertDefinition instanceof DQLDataSetAssert) {
                for (AssertSubDefinition assertSubDefinition : ((DQLDataSetAssert) assertDefinition).getSubAsserts()) {
                    Object[] data = new Object[6];
                    data[0] = assertDefinition.getId();
                    data[1] = assertDefinition.getPath();
                    data[2] = assertSubDefinition;
                    data[3] = assertSubDefinition.getShardingRuleTypes();
                    data[4] = new DatabaseTypeEnvironment(databaseType, IntegrateTestEnvironment.getInstance().getDatabaseTypes().contains(databaseType));
                    data[5] = caseType;
                    result.add(data);
                }
            } else {
                for (String shardingRuleType : assertDefinition.getShardingRuleTypes().split(",")) {
                    Object[] data = new Object[6];
                    data[0] = assertDefinition.getId();
                    data[1] = assertDefinition.getPath();
                    data[2] = assertDefinition;
                    data[3] = shardingRuleType;
                    data[4] = new DatabaseTypeEnvironment(databaseType, IntegrateTestEnvironment.getInstance().getDatabaseTypes().contains(databaseType));
                    data[5] = caseType;
                    result.add(data);
                }
            }
        }
        return result;
    }
    
    private static List<DatabaseType> getDatabaseTypes(final String databaseTypes) {
        List<DatabaseType> result = new LinkedList<>();
        for (String eachType : databaseTypes.split(",")) {
            result.add(DatabaseType.valueOf(eachType));
        }
        return result;
    }
    
    @BeforeClass
    // TODO ignore new test engine, because it is not completed yet, will continue to do it in 3.0.0.m2
    public static void setUp() throws JAXBException, IOException {
        if (isInitialized) {
            isInitialized = false;
        } else {
            for (String each : dataSetAssertLoader.getShardingRuleTypes()) {
                SchemaEnvironmentManager.dropDatabase(each);
            }
        }
        for (String each : dataSetAssertLoader.getShardingRuleTypes()) {
            SchemaEnvironmentManager.createDatabase(each);
        }
        for (String each : dataSetAssertLoader.getShardingRuleTypes()) {
            SchemaEnvironmentManager.createTable(each);
        }
    }
    
    @AfterClass
    // TODO add tearDown for temporary, will remove when original integrate test removed.
    public static void tearDown() throws JAXBException, IOException {
        if (isCleaned) {
            for (String each : dataSetAssertLoader.getShardingRuleTypes()) {
                SchemaEnvironmentManager.dropDatabase(each);
            }
            isCleaned = false;
        }
    }
    
    @Test
    // TODO ignore new test engine, because it is not completed yet, will continue to do it in 3.0.0.m2
    public void test() throws JAXBException, SAXException, ParseException, IOException, XPathExpressionException, SQLException, ParserConfigurationException {
        if (!databaseTypeEnvironment.isEnabled()) {
            return;
        }
        if (dataSetAssert instanceof AssertSubDefinition) {
            new DQLAssertEngine(sqlCaseId, path, (AssertSubDefinition) dataSetAssert, shardingRuleType, databaseTypeEnvironment, caseType).assertDQL();
        } else {
            new AssertEngine((DataSetAssert) dataSetAssert, shardingRuleType, databaseTypeEnvironment, caseType).run();
        }
    }
}
