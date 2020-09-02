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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCaseAssertion;
import org.apache.shardingsphere.dbtest.cases.assertion.root.SQLCaseType;
import org.apache.shardingsphere.dbtest.cases.assertion.root.SQLValue;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class SingleIT extends BaseIT {
    
    private final IntegrateTestCaseAssertion assertion;
    
    private final SQLCaseType caseType;
    
    private final String expectedDataFile;
    
    private final String sql;
    
    private final String originalSQL;
    
    protected SingleIT(final String path, final IntegrateTestCaseAssertion assertion, final String ruleType, 
                       final DatabaseType databaseType, final SQLCaseType caseType, final String sql) throws IOException, JAXBException, SQLException, ParseException {
        super(ruleType, databaseType);
        this.assertion = assertion;
        this.caseType = caseType;
        originalSQL = sql;
        this.sql = convert(sql);
        expectedDataFile = getExpectedDataFile(path, ruleType, databaseType, null != assertion ? assertion.getExpectedDataFile() : null);
    }
    
    private String convert(final String sql) throws ParseException {
        return caseType == SQLCaseType.Literal ? getLiteralSQL(sql) : sql;
    }
    
    private String getLiteralSQL(final String sql) throws ParseException {
        List<Object> parameters = null != assertion ? assertion.getSQLValues().stream().map(SQLValue::toString).collect(Collectors.toList()) : null;
        if (null == parameters || parameters.isEmpty()) {
            return sql;
        }
        return String.format(sql.replace("%", "$").replace("?", "%s"), parameters.toArray()).replace("$", "%")
            .replace("%%", "%").replace("'%'", "'%%'");
    }
    
    protected void printExceptionContext(final Exception ex) {
        log.error(String.format("ruleType=%s, databaseType=%s, expectedDataFile=%s, sql=%s, ex.msg=%s", getRuleType(), getDatabaseType().getName(), expectedDataFile, sql, ex.getMessage()));
    }
}
