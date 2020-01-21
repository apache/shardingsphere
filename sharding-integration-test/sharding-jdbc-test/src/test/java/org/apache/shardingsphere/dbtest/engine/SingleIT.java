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

package org.apache.shardingsphere.dbtest.engine;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCaseAssertion;
import org.apache.shardingsphere.dbtest.cases.assertion.root.SQLValue;
import org.apache.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import org.apache.shardingsphere.test.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.loader.SQLCasesRegistry;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

@Getter(AccessLevel.PROTECTED)
public abstract class SingleIT extends BaseIT {
    
    private final IntegrateTestCaseAssertion assertion;
    
    private final SQLCaseType caseType;
    
    private final String sql;
    
    private final String expectedDataFile;
    
    public SingleIT(final String sqlCaseId, final String path, final IntegrateTestCaseAssertion assertion, 
                    final String shardingRuleType, final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) throws IOException, JAXBException, SQLException, ParseException {
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
        return SQLCasesRegistry.getInstance().getSqlCasesLoader().getSQL(sqlCaseId, caseType, parameters);
    }
}
