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
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.IntegrateTestEnvironment;
import io.shardingsphere.dbtest.env.schema.SchemaEnvironmentManager;
import io.shardingsphere.dbtest.jaxb.assertion.IntegrateTestCasesLoader;
import io.shardingsphere.dbtest.jaxb.assertion.root.IntegrateTestCase;
import io.shardingsphere.dbtest.jaxb.assertion.root.IntegrateTestCaseAssertion;
import io.shardingsphere.test.sql.SQLCaseType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@RunWith(Parameterized.class)
public abstract class BaseIntegrateTest {
    
    private static IntegrateTestCasesLoader integrateTestCasesLoader = IntegrateTestCasesLoader.getInstance();
    
    private static boolean isInitialized = IntegrateTestEnvironment.getInstance().isInitialized();
    
    private static boolean isCleaned = IntegrateTestEnvironment.getInstance().isInitialized();
    
    protected static Collection<Object[]> getParameters(final DatabaseType databaseType, final SQLCaseType caseType, final IntegrateTestCase integrateTestCase) {
        Collection<Object[]> result = new LinkedList<>();
        for (IntegrateTestCaseAssertion assertion : integrateTestCase.getIntegrateTestCaseAssertions()) {
            Object[] data = new Object[5];
            data[0] = integrateTestCase.getSqlCaseId();
            data[1] = integrateTestCase.getPath();
            data[2] = assertion;
            data[3] = new DatabaseTypeEnvironment(databaseType, IntegrateTestEnvironment.getInstance().getDatabaseTypes().contains(databaseType));
            data[4] = caseType;
            result.add(data);
        }
        return result;
    }
    
    protected static List<DatabaseType> getDatabaseTypes(final String databaseTypes) {
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
    
    @AfterClass
    public static void dropDatabases() throws JAXBException, IOException {
        if (isCleaned) {
            for (String each : integrateTestCasesLoader.getShardingRuleTypes()) {
                SchemaEnvironmentManager.dropDatabase(each);
            }
            isCleaned = false;
        }
    }
}
