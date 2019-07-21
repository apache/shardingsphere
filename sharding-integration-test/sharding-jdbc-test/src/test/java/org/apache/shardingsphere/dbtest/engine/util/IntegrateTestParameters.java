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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.parse.core.SQLParseKernel;
import org.apache.shardingsphere.core.parse.core.rule.registry.ParseRuleRegistry;
import org.apache.shardingsphere.dbtest.cases.assertion.IntegrateTestCasesLoader;
import org.apache.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCase;
import org.apache.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCaseAssertion;
import org.apache.shardingsphere.dbtest.engine.SQLType;
import org.apache.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import org.apache.shardingsphere.dbtest.env.IntegrateTestEnvironment;
import org.apache.shardingsphere.spi.database.DatabaseType;
import org.apache.shardingsphere.test.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.loader.SQLCasesLoader;
import org.apache.shardingsphere.test.sql.loader.sharding.ShardingSQLCasesRegistry;

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
    
    private static SQLCasesLoader sqlCasesLoader = ShardingSQLCasesRegistry.getInstance().getSqlCasesLoader();
    
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
        for (Object[] each : sqlCasesLoader.getSQLTestParameters()) {
            String sqlCaseId = each[0].toString();
            String databaseType = each[1].toString();
            SQLCaseType caseType = (SQLCaseType) each[2];
            Class<?> sqlStatementClass = new SQLParseKernel(ParseRuleRegistry.getInstance(), 
                    DatabaseTypes.getTrunkDatabaseType(databaseType), sqlCasesLoader.getSQL(sqlCaseId, SQLCaseType.Placeholder, Collections.emptyList())).parse().getClass();
            if (!sqlType.getSqlStatementClass().isAssignableFrom(sqlStatementClass)) {
                continue;
            }
            IntegrateTestCase integrateTestCase = getIntegrateTestCase(sqlCaseId, sqlType);
            // TODO remove when transfer finished
            if (null == integrateTestCase) {
                continue;
            }
            result.addAll(getParametersWithAssertion(DatabaseTypes.getActualDatabaseType(databaseType), caseType, integrateTestCase));
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
        for (Object[] each : sqlCasesLoader.getSQLTestParameters()) {
            String sqlCaseId = each[0].toString();
            String databaseType = each[1].toString();
            SQLCaseType caseType = (SQLCaseType) each[2];
            Class<?> sqlStatementClass = new SQLParseKernel(ParseRuleRegistry.getInstance(), 
                    DatabaseTypes.getTrunkDatabaseType(databaseType), sqlCasesLoader.getSQL(sqlCaseId, SQLCaseType.Placeholder, Collections.emptyList())).parse().getClass();
            if (!sqlType.getSqlStatementClass().isAssignableFrom(sqlStatementClass)) {
                continue;
            }
            // TODO only for prepared statement for now
            if (SQLCaseType.Literal == caseType) {
                continue;
            }
            IntegrateTestCase integrateTestCase = getIntegrateTestCase(sqlCaseId, sqlType);
            // TODO remove when transfer finished
            if (null == integrateTestCase) {
                continue;
            }
            result.addAll(getParametersWithCase(DatabaseTypes.getActualDatabaseType(databaseType), integrateTestCase));
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
