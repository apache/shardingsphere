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

package org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.AddResourceStatementTestCase;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.SQLParserTestCase;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * SQL parser test cases.
 */
@XmlRootElement(name = "sql-parser-test-cases")
@Getter
public final class SQLParserTestCases {
    
    @XmlElement(name = "add-resource")
    private final List<AddResourceStatementTestCase> addResourceTestCase = new LinkedList<>();
    
    /**
     * Get all SQL parser test cases.
     * 
     * @return all SQL parser test cases
     */
    public Map<String, SQLParserTestCase> getAllSQLParserTestCases() {
        Map<String, SQLParserTestCase> result = new HashMap<>();
        putAll(addResourceTestCase, result);
        return result;
    }
    
    private void putAll(final List<? extends SQLParserTestCase> sqlParserTestCases, final Map<String, SQLParserTestCase> target) {
        Map<String, SQLParserTestCase> sqlParserTestCaseMap = getSQLParserTestCases(sqlParserTestCases);
        Collection<String> sqlParserTestCaseIds = new HashSet<>(sqlParserTestCaseMap.keySet());
        sqlParserTestCaseIds.retainAll(target.keySet());
        Preconditions.checkState(sqlParserTestCaseIds.isEmpty(), "Find duplicated SQL Case IDs: %s", sqlParserTestCaseIds);
        target.putAll(sqlParserTestCaseMap);
    }
    
    private Map<String, SQLParserTestCase> getSQLParserTestCases(final List<? extends SQLParserTestCase> sqlParserTestCases) {
        Map<String, SQLParserTestCase> result = new HashMap<>(sqlParserTestCases.size(), 1);
        for (SQLParserTestCase each : sqlParserTestCases) {
            Preconditions.checkState(!result.containsKey(each.getSqlCaseId()), "Find duplicated SQL Case ID: %s", each.getSqlCaseId());
            result.put(each.getSqlCaseId(), each);
        }
        return result;
    }
}
