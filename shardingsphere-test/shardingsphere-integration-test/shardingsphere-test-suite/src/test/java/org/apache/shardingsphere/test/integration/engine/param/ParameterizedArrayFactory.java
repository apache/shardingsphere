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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
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
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
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
        Map<DatabaseType, Collection<ParameterizedArray>> assertionParameters = new LinkedHashMap<>(10, 1);
        for (IntegrateTestCaseContext each : INTEGRATE_TEST_CASES_LOADER.getTestCaseContexts(sqlCommandType)) {
            Map<DatabaseType, Collection<ParameterizedArray>> eachAssertionParameters = getAssertionParameterizedArray(each);
            for (Entry<DatabaseType, Collection<ParameterizedArray>> entry : eachAssertionParameters.entrySet()) {
                assertionParameters.putIfAbsent(entry.getKey(), new LinkedList<>());
                assertionParameters.get(entry.getKey()).addAll(entry.getValue());
            }
        }
        Map<DatabaseType, Collection<ParameterizedArray>> availableAssertionParameters = new LinkedHashMap<>(assertionParameters.size(), 1);
        Map<DatabaseType, Collection<ParameterizedArray>> disabledAssertionParameters = new LinkedHashMap<>(assertionParameters.size(), 1);
        for (Entry<DatabaseType, Collection<ParameterizedArray>> entry : assertionParameters.entrySet()) {
            if (IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().containsKey(entry.getKey())) {
                availableAssertionParameters.put(entry.getKey(), entry.getValue());
            } else {
                disabledAssertionParameters.put(entry.getKey(), entry.getValue());
            }
        }
        printTestPlan(availableAssertionParameters, disabledAssertionParameters, calculateRunnableTestAnnotation());
        return availableAssertionParameters.values().stream().flatMap(Collection::stream).map(ParameterizedArray::toArrays).collect(Collectors.toList());
    }
    
    private static Map<DatabaseType, Collection<ParameterizedArray>> getAssertionParameterizedArray(final IntegrateTestCaseContext testCaseContext) {
        Collection<DatabaseType> databaseTypes = getDatabaseTypes(testCaseContext.getTestCase().getDbTypes());
        Map<DatabaseType, Collection<ParameterizedArray>> result = new HashMap<>(databaseTypes.size(), 1);
        for (DatabaseType each : databaseTypes) {
            result.putIfAbsent(each, new LinkedList<>());
            result.get(each).addAll(getAssertionParameterizedArray(testCaseContext, each));
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
    
    private static Collection<ParameterizedArray> getAssertionParameterizedArray(final IntegrateTestCaseContext testCaseContext, final DatabaseType databaseType, final SQLExecuteType sqlExecuteType) {
        Collection<ParameterizedArray> result = new LinkedList<>();
        if (testCaseContext.getTestCase().getAssertions().isEmpty()) {
            result.addAll(getAssertionParameterizedArray(testCaseContext, null, databaseType, sqlExecuteType));
            return result;
        }
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
        Map<DatabaseType, Collection<ParameterizedArray>> availableCases = new LinkedHashMap<>();
        Map<DatabaseType, Collection<ParameterizedArray>> disabledCases = new LinkedHashMap<>();
        INTEGRATE_TEST_CASES_LOADER.getTestCaseContexts(sqlCommandType).forEach(testCaseContext -> getDatabaseTypes(testCaseContext.getTestCase().getDbTypes()).forEach(databaseType -> {
            if (IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().containsKey(databaseType)) {
                availableCases.putIfAbsent(databaseType, new LinkedList<>());
                availableCases.get(databaseType).addAll(getCaseParameterizedArray(databaseType, testCaseContext));
            } else {
                disabledCases.putIfAbsent(databaseType, new LinkedList<>());
                disabledCases.get(databaseType).addAll(getCaseParameterizedArray(databaseType, testCaseContext));
            }
        }));
        printTestPlan(availableCases, disabledCases, calculateRunnableTestAnnotation());
        return availableCases.values().stream().flatMap(Collection::stream).map(ParameterizedArray::toArrays).collect(Collectors.toList());
    }
    
    private static Collection<ParameterizedArray> getCaseParameterizedArray(final DatabaseType databaseType, final IntegrateTestCaseContext testCaseContext) {
        return INTEGRATE_TEST_ENVIRONMENT.getScenarios().stream().map(each -> new CaseParameterizedArray(testCaseContext, each, databaseType)).collect(Collectors.toList());
    }
    
    private static Collection<DatabaseType> getDatabaseTypes(final String databaseTypes) {
        String candidates = Strings.isNullOrEmpty(databaseTypes) ? "H2,MySQL,Oracle,SQLServer,PostgreSQL" : databaseTypes;
        return Splitter.on(',').trimResults().splitToList(candidates).stream().map(DatabaseTypeRegistry::getActualDatabaseType).collect(Collectors.toList());
    }
    
    private static void printTestPlan(final Map<DatabaseType, 
            Collection<ParameterizedArray>> availableCaseParameters, final Map<DatabaseType, Collection<ParameterizedArray>> disabledCaseParameters, final long factor) {
        Collection<String> activePlan = new LinkedList<>();
        for (Entry<DatabaseType, Collection<ParameterizedArray>> entry : availableCaseParameters.entrySet()) {
            activePlan.add(String.format("%s(%s)", entry.getKey().getName(), entry.getValue().size() * factor));
        }
        Collection<String> disabledPlan = new LinkedList<>();
        for (Entry<DatabaseType, Collection<ParameterizedArray>> entry : disabledCaseParameters.entrySet()) {
            disabledPlan.add(String.format("%s(%s)", entry.getKey().getName(), entry.getValue().size() * factor));
        }
        log.info("[INFO] ======= Test Plan =======");
        String summary = String.format("[%s] Total: %s, Active: %s, Disabled: %s %s",
            disabledPlan.isEmpty() ? "INFO" : "WARN",
            (availableCaseParameters.values().stream().mapToLong(Collection::size).sum() + disabledCaseParameters.values().stream().mapToLong(Collection::size).sum()) * factor,
            activePlan.isEmpty() ? 0 : Joiner.on(", ").join(activePlan), disabledPlan.isEmpty() ? 0 : Joiner.on(", ").join(disabledPlan), System.lineSeparator());
        log.info(summary);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static long calculateRunnableTestAnnotation() {
        long result = 0;
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (int i = 3; i < stackTraceElements.length; i++) {
            Class<?> callerClazz = Class.forName(stackTraceElements[i].getClassName());
            result += Arrays.stream(callerClazz.getMethods()).filter(method -> method.isAnnotationPresent(Test.class)).count();
        }
        return result;
    }
}
