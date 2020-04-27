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
import org.apache.shardingsphere.dbtest.cases.sql.SQLCaseType;
import org.apache.shardingsphere.dbtest.env.DatabaseTypeEnvironment;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

@Getter(AccessLevel.PROTECTED)
public abstract class SingleIT extends BaseIT {
    
    private final String sqlCaseId;
    
    private final IntegrateTestCaseAssertion assertion;
    
    private final SQLCaseType caseType;
    
    private final String expectedDataFile;
    
    private final String sql;
    
    public SingleIT(final String sqlCaseId, final String path, final IntegrateTestCaseAssertion assertion, final String ruleType,
                    final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType, final String sql) throws IOException, JAXBException, SQLException {
        super(ruleType, databaseTypeEnvironment);
        this.sqlCaseId = sqlCaseId;
        this.assertion = assertion;
        this.caseType = caseType;
        this.sql = sql;
        expectedDataFile = getExpectedDataFile(path, ruleType, databaseTypeEnvironment.getDatabaseType(), assertion.getExpectedDataFile());
    }
    
    protected final String getLiteralSQL() throws ParseException {
        final List<Object> parameters = assertion.getSQLValues().stream().map(SQLValue::getValue).collect(Collectors.toList());
        if (null == parameters || parameters.isEmpty()) {
            return sql;
        }
        return String.format(sql.replace("%", "$").replace("?", "%s"), parameters.toArray()).replace("$", "%")
            .replace("%%", "%").replace("'%'", "'%%'");
    }
}
