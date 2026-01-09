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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.it.sql.parser.internal.InternalSQLParserTestParameter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.jaxb.SQLCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.type.CaseTypedSQLBuilderFactory;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.type.SQLCaseType;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL cases.
 */
@RequiredArgsConstructor
public final class SQLCases {
    
    private final Map<String, SQLCase> cases;
    
    /**
     * Generate test parameters.
     *
     * @param databaseTypes database types to be generated
     * @return generated test parameters
     */
    public Collection<InternalSQLParserTestParameter> generateTestParameters(final Collection<String> databaseTypes) {
        Collection<InternalSQLParserTestParameter> result = new LinkedList<>();
        for (SQLCase each : cases.values()) {
            result.addAll(generateTestParameters(databaseTypes, each));
        }
        return result;
    }
    
    private Collection<InternalSQLParserTestParameter> generateTestParameters(final Collection<String> databaseTypes, final SQLCase sqlCase) {
        Collection<InternalSQLParserTestParameter> result = new LinkedList<>();
        for (SQLCaseType each : SQLCaseType.values()) {
            result.addAll(generateTestParameters(databaseTypes, sqlCase, each));
        }
        return result;
    }
    
    private Collection<InternalSQLParserTestParameter> generateTestParameters(final Collection<String> databaseTypes, final SQLCase sqlCase, final SQLCaseType caseType) {
        Collection<InternalSQLParserTestParameter> result = new LinkedList<>();
        for (String each : getDatabaseTypes(sqlCase.getDatabaseTypes())) {
            if (databaseTypes.contains(each) && containsSQLCaseType(sqlCase, caseType)) {
                result.add(new InternalSQLParserTestParameter(sqlCase.getId(), caseType, each));
            }
        }
        return result;
    }
    
    private Collection<String> getDatabaseTypes(final String databaseTypes) {
        return null == databaseTypes ? getAllDatabaseTypes() : Splitter.on(',').trimResults().splitToList(databaseTypes);
    }
    
    private Collection<String> getAllDatabaseTypes() {
        // TODO "Presto" need to be fixed
        return Arrays.asList("H2", "MySQL", "PostgreSQL", "Oracle", "SQLServer", "openGauss", "Doris", "Firebird", "SQL92");
    }
    
    private boolean containsSQLCaseType(final SQLCase sqlCase, final SQLCaseType caseType) {
        return null == sqlCase.getCaseTypes() || Splitter.on(',').trimResults().splitToList(sqlCase.getCaseTypes().toUpperCase()).contains(caseType.name());
    }
    
    /**
     * Get SQL.
     *
     * @param caseId SQL case ID
     * @param caseType SQL case type
     * @param params parameters
     * @return got SQL
     */
    public String getSQL(final String caseId, final SQLCaseType caseType, final List<?> params) {
        Preconditions.checkState(cases.containsKey(caseId), "Can not find SQL of ID: %s.", caseId);
        return CaseTypedSQLBuilderFactory.newInstance(caseType).build(cases.get(caseId).getValue(), params);
    }
}
