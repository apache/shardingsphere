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

package io.shardingsphere.dbtest.engine.ddl;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.dbtest.cases.assertion.ddl.DDLIntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.engine.util.IntegrateTestParameters;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.test.sql.SQLCaseType;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;

public final class GeneralDDLIntegrateTest extends BaseDDLIntegrateTest {
    
    private final DDLIntegrateTestCaseAssertion assertion;
    
    public GeneralDDLIntegrateTest(final String sqlCaseId, final String path, final DDLIntegrateTestCaseAssertion assertion, final String shardingRuleType,
                                   final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) throws IOException, JAXBException, SQLException, ParseException {
        super(sqlCaseId, path, assertion, shardingRuleType, databaseTypeEnvironment, caseType);
        this.assertion = assertion;
    }
    
    @Parameters(name = "{0} -> Rule:{3} -> {4} -> {5}")
    public static Collection<Object[]> getParameters() {
        return IntegrateTestParameters.getParametersWithAssertion(SQLType.DDL);
    }
    
    @Test
    public void assertExecuteUpdate() throws JAXBException, IOException, SQLException {
        assertExecuteByType(true);
    }
    
    @Test
    public void assertExecute() throws JAXBException, IOException, SQLException {
        assertExecuteByType(false);
    }

    private void assertExecuteByType(final boolean isExecuteUpdate) throws JAXBException, IOException, SQLException {
        if (!getDatabaseTypeEnvironment().isEnabled()) {
            return;
        }
        try (Connection connection = getDataSource().getConnection()) {
            dropTableIfExisted(connection);
            if (!Strings.isNullOrEmpty(assertion.getInitSql())) {
                for (String sql : Splitter.on(";").trimResults().splitToList(assertion.getInitSql())) {
                    connection.prepareStatement(sql).executeUpdate();
                }
            }
            if (isExecuteUpdate) {
                if (SQLCaseType.Literal == getCaseType()) {
                    connection.createStatement().executeUpdate(getSql());
                } else {
                    connection.prepareStatement(getSql()).executeUpdate();
                }
            } else {
                if (SQLCaseType.Literal == getCaseType()) {
                    connection.createStatement().execute(getSql());
                } else {
                    connection.prepareStatement(getSql()).execute();
                }
            }
            assertMetadata(connection);
            dropTableIfExisted(connection);
        }
    }
}
