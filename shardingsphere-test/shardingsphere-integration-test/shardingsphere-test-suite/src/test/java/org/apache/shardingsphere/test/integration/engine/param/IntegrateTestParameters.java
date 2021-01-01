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
import org.apache.shardingsphere.test.integration.cases.IntegrateTestCaseType;
import org.apache.shardingsphere.test.integration.cases.IntegrateTestCaseContext;
import org.apache.shardingsphere.test.integration.cases.IntegrateTestCasesLoader;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrateTestCaseAssertion;
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
 * Integrate test parameters.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class IntegrateTestParameters {
    
    private static final IntegrateTestCasesLoader INTEGRATE_TEST_CASES_LOADER = IntegrateTestCasesLoader.getInstance();
    
    private static final IntegrateTestEnvironment INTEGRATE_TEST_ENVIRONMENT = IntegrateTestEnvironment.getInstance();
    
    /**
     * Get parameters of assertions.
     * 
     * @param caseType integrate test case type
     * @return integrate test parameters
     */
    public static Collection<Object[]> getAssertionParameters(final IntegrateTestCaseType caseType) {
        Map<DatabaseType, Collection<Object[]>> assertionParameters = new LinkedHashMap<>(10, 1);
        for (IntegrateTestCaseContext each : INTEGRATE_TEST_CASES_LOADER.getTestCaseContexts(caseType)) {
            assertionParameters.putAll(getAssertionParameters(each));
        }
        Map<DatabaseType, Collection<Object[]>> availableAssertionParameters = new LinkedHashMap<>(assertionParameters.size(), 1);
        Map<DatabaseType, Collection<Object[]>> disabledAssertionParameters = new LinkedHashMap<>(assertionParameters.size(), 1);
        for (Entry<DatabaseType, Collection<Object[]>> entry : assertionParameters.entrySet()) {
            if (IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().containsKey(entry.getKey())) {
                availableAssertionParameters.put(entry.getKey(), entry.getValue());
            } else {
                disabledAssertionParameters.put(entry.getKey(), entry.getValue());
            }
        }
        printTestPlan(availableAssertionParameters, disabledAssertionParameters, calculateRunnableTestAnnotation());
        return availableAssertionParameters.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private static Map<DatabaseType, Collection<Object[]>> getAssertionParameters(final IntegrateTestCaseContext testCaseContext) {
        Collection<DatabaseType> databaseTypes = getDatabaseTypes(testCaseContext.getTestCase().getDbTypes());
        Map<DatabaseType, Collection<Object[]>> result = new HashMap<>(databaseTypes.size(), 1);
        for (DatabaseType each : databaseTypes) {
            result.putIfAbsent(each, new LinkedList<>());
            result.get(each).addAll(getAssertionParameters(testCaseContext, each));
        }
        return result;
    }
    
    private static Collection<Object[]> getAssertionParameters(final IntegrateTestCaseContext testCaseContext, final DatabaseType databaseType) {
        Collection<Object[]> result = new LinkedList<>();
        for (SQLCaseType each : SQLCaseType.values()) {
            result.addAll(getAssertionParameters(testCaseContext, databaseType, each));
        }
        return result;
    }
    
    private static Collection<Object[]> getAssertionParameters(final IntegrateTestCaseContext testCaseContext, final DatabaseType databaseType, final SQLCaseType caseType) {
        Collection<Object[]> result = new LinkedList<>();
        if (testCaseContext.getTestCase().getAssertions().isEmpty()) {
            result.addAll(getAssertionParameters(testCaseContext, null, databaseType, caseType));
            return result;
        }
        for (IntegrateTestCaseAssertion each : testCaseContext.getTestCase().getAssertions()) {
            result.addAll(getAssertionParameters(testCaseContext, each, databaseType, caseType));
        }
        return result;
    }
    
    private static Collection<Object[]> getAssertionParameters(final IntegrateTestCaseContext testCaseContext,
                                                               final IntegrateTestCaseAssertion assertion, final DatabaseType databaseType, final SQLCaseType caseType) {
        Collection<Object[]> result = new LinkedList<>();
        for (String each : INTEGRATE_TEST_ENVIRONMENT.getScenarios()) {
            Object[] data = new Object[6];
            data[0] = testCaseContext.getParentPath();
            data[1] = assertion;
            data[2] = each;
            data[3] = databaseType.getName();
            data[4] = caseType;
            data[5] = testCaseContext.getTestCase().getSql();
            result.add(data);
        }
        return result;
    }
    
    /**
     * Get parameters with test cases.
     *
     * @param caseType integrate test case type
     * @return integrate test parameters
     */
    public static Collection<Object[]> getParametersWithCase(final IntegrateTestCaseType caseType) {
        Map<DatabaseType, Collection<Object[]>> availableCases = new LinkedHashMap<>();
        Map<DatabaseType, Collection<Object[]>> disabledCases = new LinkedHashMap<>();
        INTEGRATE_TEST_CASES_LOADER.getTestCaseContexts(caseType).forEach(testCaseContext -> getDatabaseTypes(testCaseContext.getTestCase().getDbTypes()).forEach(databaseType -> {
            if (IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().containsKey(databaseType)) {
                availableCases.putIfAbsent(databaseType, new LinkedList<>());
                availableCases.get(databaseType).addAll(getParametersWithCase(databaseType, testCaseContext));
            } else {
                disabledCases.putIfAbsent(databaseType, new LinkedList<>());
                disabledCases.get(databaseType).addAll(getParametersWithCase(databaseType, testCaseContext));
            }
        }));
        printTestPlan(availableCases, disabledCases, calculateRunnableTestAnnotation());
        return availableCases.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private static Collection<Object[]> getParametersWithCase(final DatabaseType databaseType, final IntegrateTestCaseContext testCaseContext) {
        Collection<Object[]> result = new LinkedList<>();
        for (String each : INTEGRATE_TEST_ENVIRONMENT.getScenarios()) {
            Object[] data = new Object[4];
            data[0] = testCaseContext;
            data[1] = each;
            data[2] = databaseType.getName();
            data[3] = testCaseContext.getTestCase().getSql();
            result.add(data);
        }
        return result;
    }
    
    private static Collection<DatabaseType> getDatabaseTypes(final String databaseTypes) {
        String candidates = Strings.isNullOrEmpty(databaseTypes) ? "H2,MySQL,Oracle,SQLServer,PostgreSQL" : databaseTypes;
        return Splitter.on(',').trimResults().splitToList(candidates).stream().map(DatabaseTypeRegistry::getActualDatabaseType).collect(Collectors.toList());
    }
    
    private static void printTestPlan(final Map<DatabaseType, Collection<Object[]>> availableCaseParameters, final Map<DatabaseType, Collection<Object[]>> disabledCaseParameters, final long factor) {
        Collection<String> activePlan = new LinkedList<>();
        for (Entry<DatabaseType, Collection<Object[]>> entry : availableCaseParameters.entrySet()) {
            activePlan.add(String.format("%s(%s)", entry.getKey().getName(), entry.getValue().size() * factor));
        }
        Collection<String> disabledPlan = new LinkedList<>();
        for (Entry<DatabaseType, Collection<Object[]>> entry : disabledCaseParameters.entrySet()) {
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
