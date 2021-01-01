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

package org.apache.shardingsphere.test.integration.engine.param;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.cases.IntegrateTestCaseContext;
import org.apache.shardingsphere.test.integration.cases.IntegrateTestCasesLoader;
import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrateTestCaseAssertion;
import org.apache.shardingsphere.test.integration.engine.param.domain.AssertionParameterizedArray;
import org.apache.shardingsphere.test.integration.engine.param.domain.CaseParameterizedArray;
import org.apache.shardingsphere.test.integration.engine.param.domain.ParameterizedArray;
import org.apache.shardingsphere.test.integration.env.IntegrateTestEnvironment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parameterized array factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ParameterizedArrayFactory {
    
    private static final IntegrateTestCasesLoader INTEGRATE_TEST_CASES_LOADER = IntegrateTestCasesLoader.getInstance();
    
    private static final IntegrateTestEnvironment INTEGRATE_TEST_ENVIRONMENT = IntegrateTestEnvironment.getInstance();
    
    /**
     * Get assertion parameterized array.
     * 
     * @param sqlCommandType SQL command type
     * @return assertion parameterized array
     */
    public static Collection<Object[]> getAssertionParameterizedArray(final SQLCommandType sqlCommandType) {
        Collection<ParameterizedArray> result = new LinkedList<>();
        for (IntegrateTestCaseContext each : INTEGRATE_TEST_CASES_LOADER.getTestCaseContexts(sqlCommandType)) {
            result.addAll(getAssertionParameterizedArray(each));
        }
        return toArrays(result);
    }
    
    private static Collection<ParameterizedArray> getAssertionParameterizedArray(final IntegrateTestCaseContext testCaseContext) {
        Collection<ParameterizedArray> result = new LinkedList<>();
        for (DatabaseType each : getDatabaseTypes(testCaseContext.getTestCase().getDbTypes())) {
            if (IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().containsKey(each)) {
                result.addAll(getAssertionParameterizedArray(testCaseContext, each));
            }
        }
        return result;
    }
    
    private static Collection<ParameterizedArray> getAssertionParameterizedArray(final IntegrateTestCaseContext testCaseContext, final DatabaseType databaseType) {
        Collection<ParameterizedArray> result = new LinkedList<>();
        for (SQLExecuteType each : SQLExecuteType.values()) {
            result.addAll(getAssertionParameterizedArray(testCaseContext, databaseType, each));
        }
        return result;
    }
    
    private static Collection<ParameterizedArray> getAssertionParameterizedArray(final IntegrateTestCaseContext testCaseContext,
                                                                                 final DatabaseType databaseType, final SQLExecuteType sqlExecuteType) {
        Collection<ParameterizedArray> result = new LinkedList<>();
        for (IntegrateTestCaseAssertion each : testCaseContext.getTestCase().getAssertions()) {
            result.addAll(getAssertionParameterizedArray(testCaseContext, each, databaseType, sqlExecuteType));
        }
        return result;
    }
    
    private static Collection<ParameterizedArray> getAssertionParameterizedArray(final IntegrateTestCaseContext testCaseContext,
                                                                                 final IntegrateTestCaseAssertion assertion, final DatabaseType databaseType, final SQLExecuteType sqlExecuteType) {
        return INTEGRATE_TEST_ENVIRONMENT.getScenarios().stream().map(
            each -> new AssertionParameterizedArray(testCaseContext, assertion, each, databaseType, sqlExecuteType)).collect(Collectors.toList());
    }
    
    /**
     * Get case parameterized array.
     *
     * @param sqlCommandType SQL command type
     * @return case parameterized array
     */
    public static Collection<Object[]> getCaseParameterizedArray(final SQLCommandType sqlCommandType) {
        Collection<ParameterizedArray> result = new LinkedList<>();
        for (IntegrateTestCaseContext each : INTEGRATE_TEST_CASES_LOADER.getTestCaseContexts(sqlCommandType)) {
            result.addAll(getCaseParameterizedArray(each));
        }
        return toArrays(result);
    }
    
    private static Collection<ParameterizedArray> getCaseParameterizedArray(final IntegrateTestCaseContext testCaseContext) {
        Collection<ParameterizedArray> result = new LinkedList<>();
        for (DatabaseType each : getDatabaseTypes(testCaseContext.getTestCase().getDbTypes())) {
            if (IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().containsKey(each)) {
                result.addAll(getCaseParameterizedArray(testCaseContext, each));
            }
        }
        return result;
    }
    
    private static Collection<ParameterizedArray> getCaseParameterizedArray(final IntegrateTestCaseContext testCaseContext, final DatabaseType databaseType) {
        return INTEGRATE_TEST_ENVIRONMENT.getScenarios().stream().map(each -> new CaseParameterizedArray(testCaseContext, each, databaseType)).collect(Collectors.toList());
    }
    
    private static Collection<DatabaseType> getDatabaseTypes(final String databaseTypes) {
        String candidates = Strings.isNullOrEmpty(databaseTypes) ? "H2,MySQL,Oracle,SQLServer,PostgreSQL" : databaseTypes;
        return Splitter.on(',').trimResults().splitToList(candidates).stream().map(DatabaseTypeRegistry::getActualDatabaseType).collect(Collectors.toList());
    }
    
    private static List<Object[]> toArrays(final Collection<ParameterizedArray> parameterizedArrays) {
        return parameterizedArrays.stream().map(ParameterizedArray::toArrays).collect(Collectors.toList());
    }
}
