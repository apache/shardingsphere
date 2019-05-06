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

import io.shardingsphere.dbtest.cases.assertion.IntegrateTestCasesLoader;
import io.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.cases.assertion.root.SQLValue;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.IntegrateTestEnvironment;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import lombok.AccessLevel;
import lombok.Getter;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

@Getter(AccessLevel.PROTECTED)
public abstract class SingleIntegrateTest extends BaseIntegrateTest {
    
    private static IntegrateTestEnvironment integrateTestEnvironment = IntegrateTestEnvironment.getInstance();
    
    private static IntegrateTestCasesLoader integrateTestCasesLoader = IntegrateTestCasesLoader.getInstance();
    
    private final IntegrateTestCaseAssertion assertion;
    
    private final SQLCaseType caseType;
    
    private final String sql;
    
    private final String expectedDataFile;
    
    public SingleIntegrateTest(final String sqlCaseId, final String path, final IntegrateTestCaseAssertion assertion, final String shardingRuleType,
                               final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) 
            throws IOException, JAXBException, SQLException, ParseException {
        super(shardingRuleType, databaseTypeEnvironment);
        this.assertion = assertion;
        this.caseType = caseType;
        sql = getSQL(sqlCaseId);
        expectedDataFile = getExpectedDataFile(path, shardingRuleType, databaseTypeEnvironment.getDatabaseType(), assertion.getExpectedDataFile());
    }

    private String getSQL(final String sqlCaseId) throws ParseException {
        List<String> parameters = new LinkedList<>();
        for (SQLValue each : assertion.getSQLValues()) {
            parameters.add(each.toString());
        }
        return SQLCasesLoader.getInstance().getSupportedSQL(sqlCaseId, caseType, parameters);
    }
}
