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

package io.shardingsphere.dbtest.engine.dcl;

import io.shardingsphere.dbtest.cases.assertion.dcl.DCLIntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.engine.SingleIntegrateTest;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.dbtest.env.authority.AuthorityEnvironmentManager;
import io.shardingsphere.dbtest.env.dataset.DataSetEnvironmentManager;
import io.shardingsphere.test.sql.SQLCaseType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

public abstract class BaseDCLIntegrateTest extends SingleIntegrateTest {
    
    private final AuthorityEnvironmentManager authorityEnvironmentManager;
    
    public BaseDCLIntegrateTest(final String sqlCaseId, final String path, final DCLIntegrateTestCaseAssertion assertion, final String shardingRuleType,
                                final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) throws IOException, JAXBException, SQLException, ParseException {
        super(sqlCaseId, path, assertion, shardingRuleType, databaseTypeEnvironment, caseType);
        authorityEnvironmentManager = new AuthorityEnvironmentManager(
                EnvironmentPath.getAuthorityResourcesPath(shardingRuleType), getInstanceDataSourceMap(), getDatabaseTypeEnvironment().getDatabaseType());
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
    public void insertData() throws SQLException, ParseException, IOException, JAXBException {
        if (getDatabaseTypeEnvironment().isEnabled()) {
            new DataSetEnvironmentManager(EnvironmentPath.getDataInitializeResourceFile(getShardingRuleType()), getDataSourceMap()).initialize();
            authorityEnvironmentManager.initialize();
        }
    }
    
    @After
    public void cleanData() throws SQLException {
        if (getDatabaseTypeEnvironment().isEnabled()) {
            authorityEnvironmentManager.clean();
        }
    }
}
