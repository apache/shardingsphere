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
import lombok.SneakyThrows;
import org.apache.shardingsphere.dbtest.cases.assertion.IntegrateTestCasesLoader;
import org.apache.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCase;
import org.apache.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCaseAssertion;
import org.apache.shardingsphere.dbtest.cases.assertion.root.SQLValue;
import org.apache.shardingsphere.dbtest.cases.assertion.root.SQLCaseType;
import org.apache.shardingsphere.dbtest.engine.SQLType;
import org.apache.shardingsphere.dbtest.env.IntegrateTestEnvironment;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseType;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseTypes;

import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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
        getIntegrateTestCase(sqlType).forEach(integrateTestCase -> {
            getAvailableDatabaseTypes(integrateTestCase.getDbTypes()).forEach(databaseType -> {
                result.addAll(getParametersWithAssertion(databaseType, SQLCaseType.Literal, integrateTestCase));
                result.addAll(getParametersWithAssertion(databaseType, SQLCaseType.Placeholder, integrateTestCase));
            });
        });
        return result;
    }
    
    @SneakyThrows
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
            final IntegrateTestCase integrateTestCase, final IntegrateTestCaseAssertion assertion, final DatabaseType databaseType, final SQLCaseType caseType) throws ParseException {
        Collection<Object[]> result = new LinkedList<>();
        for (String each : integrateTestEnvironment.getRuleTypes()) {
            Object[] data = new Object[6];
            data[0] = integrateTestCase.getPath();
            data[1] = assertion;
            data[2] = each;
            data[3] = databaseType.getName();
            data[4] = caseType;
            data[5] = getSQL(integrateTestCase.getSql(), assertion, caseType);
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
        getIntegrateTestCase(sqlType).forEach(integrateTestCase ->
            getAvailableDatabaseTypes(integrateTestCase.getDbTypes()).forEach(databaseType -> result.addAll(getParametersWithCase(databaseType, integrateTestCase))));
        return result;
    }
    
    private static Collection<Object[]> getParametersWithCase(final DatabaseType databaseType, final IntegrateTestCase integrateTestCase) {
        Collection<Object[]> result = new LinkedList<>();
        for (String each : integrateTestEnvironment.getRuleTypes()) {
            Object[] data = new Object[4];
            data[0] = integrateTestCase;
            data[1] = each;
            data[2] = databaseType.getName();
            data[3] = integrateTestCase.getSql();
            result.add(data);
        }
        return result;
    }
    
    private static Collection<DatabaseType> getAvailableDatabaseTypes(final String databaseTypes) {
        return Strings.isNullOrEmpty(databaseTypes) ? IntegrateTestEnvironment.getInstance().getDatabaseTypes()
            : Splitter.on(',').trimResults().splitToList(databaseTypes).stream().map(DatabaseTypes::getActualDatabaseType)
            .filter(databaseType -> IntegrateTestEnvironment.getInstance().getDatabaseTypes().contains(databaseType)).collect(Collectors.toList());
    }
    
    
    private static String getSQL(final String sql, final IntegrateTestCaseAssertion assertion, final SQLCaseType sqlCaseType) throws ParseException {
        return sqlCaseType == SQLCaseType.Literal ? getLiteralSQL(sql, assertion) : sql;
    }
    
    private static String getLiteralSQL(final String sql, final IntegrateTestCaseAssertion assertion) throws ParseException {
        final List<Object> parameters = assertion.getSQLValues().stream().map(SQLValue::toString).collect(Collectors.toList());
        if (null == parameters || parameters.isEmpty()) {
            return sql;
        }
        return String.format(sql.replace("%", "$").replace("?", "%s"), parameters.toArray()).replace("$", "%")
            .replace("%%", "%").replace("'%'", "'%%'");
    }
    
    private static List<? extends IntegrateTestCase> getIntegrateTestCase(final SQLType sqlType) {
        switch (sqlType) {
            case DQL:
                return integrateTestCasesLoader.getDqlIntegrateTestCases();
            case DML:
                return integrateTestCasesLoader.getDmlIntegrateTestCases();
            case DDL:
                return integrateTestCasesLoader.getDdlIntegrateTestCases();
            case DCL:
                return integrateTestCasesLoader.getDclIntegrateTestCases();
            default:
                throw new UnsupportedOperationException(sqlType.name());
        }
    }
}
