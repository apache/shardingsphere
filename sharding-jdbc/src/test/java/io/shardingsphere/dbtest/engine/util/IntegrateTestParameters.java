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

package io.shardingsphere.dbtest.engine.util;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.parsing.SQLJudgeEngine;
import io.shardingsphere.dbtest.cases.assertion.IntegrateTestCasesLoader;
import io.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCase;
import io.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.IntegrateTestEnvironment;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Integrate test parameters.
 * 
 * @author zhangliang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IntegrateTestParameters {
    
    private static SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private static IntegrateTestCasesLoader integrateTestCasesLoader = IntegrateTestCasesLoader.getInstance();
    
    private static IntegrateTestEnvironment integrateTestEnvironment = IntegrateTestEnvironment.getInstance();
    
    /**
     * Get parameters with assertions.
     * 
     * @param sqlType SQL type
     * @return integrate test parameters.
     */
    public static Collection<Object[]> getParametersWithAssertion(final SQLType sqlType) {
        // TODO sqlCasesLoader size should eq integrateTestCasesLoader size
        // assertThat(sqlCasesLoader.countAllSupportedSQLCases(), is(integrateTestCasesLoader.countAllDataSetTestCases()));
        Collection<Object[]> result = new LinkedList<>();
        for (Object[] each : sqlCasesLoader.getSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class)) {
            String sqlCaseId = each[0].toString();
            if (sqlType != new SQLJudgeEngine(sqlCasesLoader.getSupportedSQL(sqlCaseId, SQLCaseType.Placeholder, Collections.emptyList())).judge().getType()) {
                continue;
            }
            DatabaseType databaseType = (DatabaseType) each[1];
            SQLCaseType caseType = (SQLCaseType) each[2];
            IntegrateTestCase integrateTestCase = getIntegrateTestCase(sqlCaseId, sqlType);
            // TODO remove when transfer finished
            if (null == integrateTestCase) {
                continue;
            }
            result.addAll(getParametersWithAssertion(databaseType, caseType, integrateTestCase));
        }
        return result;
    }
    
    private static Collection<Object[]> getParametersWithAssertion(final DatabaseType databaseType, final SQLCaseType caseType, final IntegrateTestCase integrateTestCase) {
        Collection<Object[]> result = new LinkedList<>();
        for (IntegrateTestCaseAssertion each : integrateTestCase.getIntegrateTestCaseAssertions()) {
            result.addAll(getParametersWithAssertion(integrateTestCase, each, databaseType, caseType));
        }
        return result;
    }
    
    private static Collection<Object[]> getParametersWithAssertion(
            final IntegrateTestCase integrateTestCase, final IntegrateTestCaseAssertion assertion, final DatabaseType databaseType, final SQLCaseType caseType) {
        Collection<Object[]> result = new LinkedList<>();
        for (String each : integrateTestEnvironment.getShardingRuleTypes()) {
            Object[] data = new Object[6];
            data[0] = integrateTestCase.getSqlCaseId();
            data[1] = integrateTestCase.getPath();
            data[2] = assertion;
            data[3] = each;
            data[4] = new DatabaseTypeEnvironment(databaseType, IntegrateTestEnvironment.getInstance().getDatabaseTypes().contains(databaseType));
            data[5] = caseType;
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
        // TODO sqlCasesLoader size should eq integrateTestCasesLoader size
        // assertThat(sqlCasesLoader.countAllSupportedSQLCases(), is(integrateTestCasesLoader.countAllDataSetTestCases()));
        Collection<Object[]> result = new LinkedList<>();
        for (Object[] each : sqlCasesLoader.getSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class)) {
            String sqlCaseId = each[0].toString();
            if (sqlType != new SQLJudgeEngine(sqlCasesLoader.getSupportedSQL(sqlCaseId, SQLCaseType.Placeholder, Collections.emptyList())).judge().getType()) {
                continue;
            }
            DatabaseType databaseType = (DatabaseType) each[1];
            SQLCaseType caseType = (SQLCaseType) each[2];
            // TODO only for prepared statement for now
            if (SQLCaseType.Literal == caseType) {
                continue;
            }
            IntegrateTestCase integrateTestCase = getIntegrateTestCase(sqlCaseId, sqlType);
            // TODO remove when transfer finished
            if (null == integrateTestCase) {
                continue;
            }
            result.addAll(getParametersWithCase(databaseType, integrateTestCase));
        }
        return result;
    }
    
    private static Collection<Object[]> getParametersWithCase(final DatabaseType databaseType, final IntegrateTestCase integrateTestCase) {
        Collection<Object[]> result = new LinkedList<>();
        for (String each : integrateTestEnvironment.getShardingRuleTypes()) {
            Object[] data = new Object[4];
            data[0] = integrateTestCase.getSqlCaseId();
            data[1] = integrateTestCase;
            data[2] = each;
            data[3] = new DatabaseTypeEnvironment(databaseType, IntegrateTestEnvironment.getInstance().getDatabaseTypes().contains(databaseType));
            result.add(data);
        }
        return result;
    }
    
    private static IntegrateTestCase getIntegrateTestCase(final String sqlCaseId, final SQLType sqlType) {
        switch (sqlType) {
            case DQL:
                return integrateTestCasesLoader.getDQLIntegrateTestCase(sqlCaseId);
            case DML:
                return integrateTestCasesLoader.getDMLIntegrateTestCase(sqlCaseId);
            case DDL:
                return integrateTestCasesLoader.getDDLIntegrateTestCase(sqlCaseId);
            case DCL:
                return integrateTestCasesLoader.getDCLIntegrateTestCase(sqlCaseId);
            default:
                throw new UnsupportedOperationException(sqlType.name());
        }
    }
}
