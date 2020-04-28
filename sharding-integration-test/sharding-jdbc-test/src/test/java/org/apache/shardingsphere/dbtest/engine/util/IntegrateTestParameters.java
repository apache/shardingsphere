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

package org.apache.shardingsphere.dbtest.engine.util;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.dbtest.cases.assertion.IntegrateTestCasesLoader;
import org.apache.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCase;
import org.apache.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCaseAssertion;
import org.apache.shardingsphere.dbtest.cases.sql.SQLCaseType;
import org.apache.shardingsphere.dbtest.engine.SQLType;
import org.apache.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import org.apache.shardingsphere.dbtest.env.IntegrateTestEnvironment;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseType;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseTypes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Integrate test parameters.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IntegrateTestParameters {
    
    private static IntegrateTestCasesLoader integrateTestCasesLoader = IntegrateTestCasesLoader.getInstance();
    
    private static IntegrateTestEnvironment integrateTestEnvironment = IntegrateTestEnvironment.getInstance();
    
    /**
     * Get parameters with assertions.
     * 
     * @param sqlType SQL type
     * @return integrate test parameters.
     */
    public static Collection<Object[]> getParametersWithAssertion(final SQLType sqlType) {
        Collection<Object[]> result = new LinkedList<>();
        getIntegrateTestCase(sqlType).forEach((sqlCaseId, integrateTestCase) -> {
            getDatabaseTypes(integrateTestCase.getDbTypes()).forEach(databaseType -> {
                result.addAll(getParametersWithAssertion(databaseType, SQLCaseType.Literal, integrateTestCase));
                result.addAll(getParametersWithAssertion(databaseType, SQLCaseType.Placeholder, integrateTestCase));
            });
        });
        return result;
    }
    
    private static Collection<Object[]> getParametersWithAssertion(final DatabaseType databaseType, final SQLCaseType caseType, final IntegrateTestCase integrateTestCase) {
        Collection<Object[]> result = new LinkedList<>();
        if (integrateTestCase.getIntegrateTestCaseAssertions().isEmpty()) {
            result.addAll(getParametersWithAssertion(integrateTestCase, null, databaseType, caseType));
            return result;
        }
        for (IntegrateTestCaseAssertion each : integrateTestCase.getIntegrateTestCaseAssertions()) {
            result.addAll(getParametersWithAssertion(integrateTestCase, each, databaseType, caseType));
        }
        return result;
    }
    
    private static Collection<Object[]> getParametersWithAssertion(
            final IntegrateTestCase integrateTestCase, final IntegrateTestCaseAssertion assertion, final DatabaseType databaseType, final SQLCaseType caseType) {
        Collection<Object[]> result = new LinkedList<>();
        for (String each : integrateTestEnvironment.getRuleTypes()) {
            Object[] data = new Object[7];
            data[0] = integrateTestCase.getSqlCaseId();
            data[1] = integrateTestCase.getPath();
            data[2] = assertion;
            data[3] = each;
            data[4] = new DatabaseTypeEnvironment(databaseType, IntegrateTestEnvironment.getInstance().getDatabaseTypes().contains(databaseType));
            data[5] = caseType;
            data[6] = integrateTestCase.getSql();
            result.add(data);
        }
        return result;
    }
    
    /**
     * Get parameters with test cases.
     *
     * @param sqlType SQL type
     * @return integrate test parameters.
     */
    public static Collection<Object[]> getParametersWithCase(final SQLType sqlType) {
        Collection<Object[]> result = new LinkedList<>();
        getIntegrateTestCase(sqlType).forEach((sqlCaseId, integrateTestCase) -> getDatabaseTypes(integrateTestCase.getDbTypes())
            .forEach(databaseType -> result.addAll(getParametersWithCase(databaseType, integrateTestCase))));
        return result;
    }
    
    private static Collection<Object[]> getParametersWithCase(final DatabaseType databaseType, final IntegrateTestCase integrateTestCase) {
        Collection<Object[]> result = new LinkedList<>();
        for (String each : integrateTestEnvironment.getRuleTypes()) {
            Object[] data = new Object[5];
            data[0] = integrateTestCase.getSqlCaseId();
            data[1] = integrateTestCase;
            data[2] = each;
            data[3] = new DatabaseTypeEnvironment(databaseType, IntegrateTestEnvironment.getInstance().getDatabaseTypes().contains(databaseType));
            data[4] = integrateTestCase.getSql();
            result.add(data);
        }
        return result;
    }
    
    private static Collection<DatabaseType> getDatabaseTypes(final String databaseTypes) {
        return Strings.isNullOrEmpty(databaseTypes) ? IntegrateTestEnvironment.getInstance().getDatabaseTypes()
            : Splitter.on(',').trimResults().splitToList(databaseTypes).stream().map(DatabaseTypes::getActualDatabaseType).collect(Collectors.toList());
    }
    
    private static Map<String, IntegrateTestCase> getIntegrateTestCase(final SQLType sqlType) {
        switch (sqlType) {
            case DQL:
                return integrateTestCasesLoader.getDqlIntegrateTestCaseMap();
            case DML:
                return integrateTestCasesLoader.getDmlIntegrateTestCaseMap();
            case DDL:
                return integrateTestCasesLoader.getDdlIntegrateTestCaseMap();
            case DCL:
                return integrateTestCasesLoader.getDclIntegrateTestCaseMap();
            default:
                throw new UnsupportedOperationException(sqlType.name());
        }
    }
}
