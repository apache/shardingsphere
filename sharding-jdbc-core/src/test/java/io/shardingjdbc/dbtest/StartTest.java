/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.dbtest;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.dbtest.asserts.AssertEngine;
import io.shardingjdbc.dbtest.asserts.DataSetAssertLoader;
import io.shardingjdbc.dbtest.config.bean.DataSetAssert;
import io.shardingjdbc.dbtest.env.DatabaseTypeEnvironment;
import io.shardingjdbc.dbtest.env.IntegrateTestEnvironment;
import io.shardingjdbc.dbtest.env.schema.SchemaEnvironmentManager;
import io.shardingjdbc.test.sql.SQLCaseType;
import io.shardingjdbc.test.sql.SQLCasesLoader;
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
    
    private final DataSetAssert assertDefinition;
    
    private final String shardingRuleType;
    
    private final DatabaseTypeEnvironment databaseTypeEnvironment;
    
    private final SQLCaseType caseType;
    
    @Parameters(name = "{0} -> Rule:{1} -> {2}")
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
            for (String shardingRuleType : assertDefinition.getShardingRuleType().split(",")) {
                Object[] data = new Object[4];
                data[0] = assertDefinition;
                data[1] = shardingRuleType;
                data[2] = new DatabaseTypeEnvironment(databaseType, IntegrateTestEnvironment.getInstance().getDatabaseTypes().contains(databaseType));
                data[3] = caseType;
                result.add(data);
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
    public void test() throws JAXBException, SAXException, ParseException, IOException, XPathExpressionException, SQLException, ParserConfigurationException {
        AssertEngine.runAssert(assertDefinition, shardingRuleType, databaseTypeEnvironment, caseType);
    }
}
