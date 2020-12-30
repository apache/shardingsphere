/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.integration.engine.ddl;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.cases.assertion.ddl.DDLIntegrateTestCaseAssertion;
import org.apache.shardingsphere.test.integration.cases.assertion.root.SQLCaseType;
import org.apache.shardingsphere.test.integration.engine.SingleIT;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.env.dataset.DataSetEnvironmentManager;
import org.apache.shardingsphere.test.integration.env.schema.SchemaEnvironmentManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;

@Slf4j
public abstract class BaseDDLIT extends SingleIT {
    
    private final DataSetEnvironmentManager dataSetEnvironmentManager;
    
    protected BaseDDLIT(final String parentPath, final DDLIntegrateTestCaseAssertion assertion, final String ruleType, 
                        final DatabaseType databaseType, final SQLCaseType caseType, final String sql) throws IOException, JAXBException, SQLException, ParseException {
        super(parentPath, assertion, ruleType, databaseType, caseType, sql);
        dataSetEnvironmentManager = new DataSetEnvironmentManager(EnvironmentPath.getDataSetFile(ruleType), getActualDataSources());
    }
    
    @BeforeClass
    public static void initDatabases() throws IOException, JAXBException {
        SchemaEnvironmentManager.createDatabases();
    }
    
    @AfterClass
    public static void destroyDatabases() throws IOException, JAXBException {
        SchemaEnvironmentManager.dropDatabases();
    }
    
    @Before
    public final void initTables() throws SQLException, ParseException, IOException, JAXBException {
        SchemaEnvironmentManager.createTables();
        resetTargetDataSource();
        dataSetEnvironmentManager.load();
    }
    
    @After
    public final void destroyTables() throws JAXBException, IOException {
        SchemaEnvironmentManager.dropTables();
    }
    
    protected final void executeInitSQLs(final Connection connection) throws SQLException {
        dropTableIfExisted(connection);
        if (!Strings.isNullOrEmpty(((DDLIntegrateTestCaseAssertion) getAssertion()).getInitSQL())) {
            for (String each : Splitter.on(";").trimResults().splitToList(((DDLIntegrateTestCaseAssertion) getAssertion()).getInitSQL())) {
                connection.prepareStatement(each).executeUpdate();
            }
        }
    }
    
    private void dropTableIfExisted(final Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("DROP TABLE %s", ((DDLIntegrateTestCaseAssertion) getAssertion()).getTable()))) {
            preparedStatement.executeUpdate();
        } catch (final SQLException ignored) {
        }
    }
}
