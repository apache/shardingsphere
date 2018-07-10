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

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.dbtest.cases.assertion.ddl.DDLIntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.engine.IntegrateTestParameters;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.IntegrateTestEnvironment;
import io.shardingsphere.test.sql.SQLCaseType;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;

@RunWith(Parameterized.class)
public final class AdditionalDDLIntegrateTest extends BaseDDLIntegrateTest {
    
    private static IntegrateTestEnvironment integrateTestEnvironment = IntegrateTestEnvironment.getInstance();
    
    public AdditionalDDLIntegrateTest(final String sqlCaseId, final String path, final DDLIntegrateTestCaseAssertion assertion, final String shardingRuleType,
                                      final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) throws IOException, JAXBException, SQLException, ParseException {
        super(sqlCaseId, path, assertion, shardingRuleType, databaseTypeEnvironment, caseType);
    }
    
    @Parameters(name = "{0} -> Rule:{3} -> {4} -> {5}")
    public static Collection<Object[]> getParameters() {
        return integrateTestEnvironment.isRunAdditionalTestCases() ? IntegrateTestParameters.getParameters(SQLType.DDL) : Collections.<Object[]>emptyList();
    }
}
