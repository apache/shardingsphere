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

package org.apache.shardingsphere.test.integration.junit.runner;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.integration.cases.IntegrationTestCasesLoader;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCase;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.integration.common.SQLExecuteType;
import org.apache.shardingsphere.test.integration.env.EnvironmentType;
import org.apache.shardingsphere.test.integration.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.junit.annotation.BeforeAllCases;
import org.apache.shardingsphere.test.integration.junit.annotation.ParameterFilter;
import org.apache.shardingsphere.test.integration.junit.annotation.TestCaseSpec;
import org.apache.shardingsphere.test.integration.junit.compose.ContainerCompose;
import org.apache.shardingsphere.test.integration.junit.compose.NotSupportedException;
import org.apache.shardingsphere.test.integration.junit.param.ParameterizedArrayFactory;
import org.apache.shardingsphere.test.integration.junit.param.TestCaseParameters;
import org.apache.shardingsphere.test.integration.junit.param.model.AssertionParameterizedArray;
import org.apache.shardingsphere.test.integration.junit.param.model.CaseParameterizedArray;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;
import org.apache.shardingsphere.test.integration.junit.runner.parallel.ParallelRunnerScheduler;
import org.apache.shardingsphere.test.integration.junit.runner.parallel.annotaion.ParallelRuntimeStrategy;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class ShardingSphereRunner extends Suite {
    
    private final List<Runner> runners;
    
    @Getter
    private final String caseName;
    
    private ContainerCompose compose;
    
    private final TestCaseBeanContext beanContext = new TestCaseBeanContext();
    
    public ShardingSphereRunner(final Class<?> klass) throws InitializationError {
        super(klass, Collections.emptyList());
        TestCaseSpec testCaseSpec = getTestClass().getAnnotation(TestCaseSpec.class);
        caseName = Strings.isNullOrEmpty(testCaseSpec.name()) ? klass.getSimpleName() : testCaseSpec.name();
        if (EnvironmentType.NATIVE == IntegrationTestEnvironment.getInstance().getEnvType()) {
            ParallelRuntimeStrategy parallelRuntimeStrategy = getTestClass().getAnnotation(ParallelRuntimeStrategy.class);
            if (null != parallelRuntimeStrategy) {
                setScheduler(new ParallelRunnerScheduler(parallelRuntimeStrategy.value()));
            }
            runners = createCIRunners(testCaseSpec);
        } else {
            runners = createITRunners(testCaseSpec);
        }
    }
    
    private List<Runner> createITRunners(final TestCaseSpec testCaseSpec) {
        final Predicate<TestCaseBeanContext> predicate = createTestCaseParametersPredicate();
        TestCaseDescription description = TestCaseDescription.fromSystemProps(testCaseSpec).build();
        beanContext.registerBean(TestCaseDescription.class, description);
        compose = new ContainerCompose(caseName, getTestClass(), description, beanContext);
        return allParameters(description).stream()
                .map(e -> {
                    TestCaseBeanContext context = beanContext.subContext();
                    register(e, context);
                    return context;
                })
                .filter(predicate)
                .map(e -> {
                    try {
                        return new ShardingSphereITSubRunner(getTestClass().getJavaClass(), e);
                    } catch (InitializationError ex) {
                        throw new RuntimeException("Initialization Error", ex);
                    }
                })
                .collect(Collectors.toList());
    }
    
    private List<Runner> createCIRunners(final TestCaseSpec testCaseSpec) {
        final Predicate<TestCaseBeanContext> predicate = createTestCaseParametersPredicate();
        return allCIParameters(testCaseSpec).stream()
                .map(e -> {
                    final TestCaseDescription description = TestCaseDescription.builder()
                            .sqlCommandType(testCaseSpec.sqlCommandType())
                            .executionMode(testCaseSpec.executionMode())
                            .adapter(e.getAdapter())
                            .database(e.getDatabaseType().getName())
                            .scenario(e.getScenario())
                            .build();
                    IntegrationTestCase testCase = e.getTestCaseContext().getTestCase();
                    final TestCaseBeanContext context = beanContext.subContext();
                    context.registerBean(ParameterizedArray.class, e);
                    context.registerBean(TestCaseDescription.class, description);
                    TestCaseParameters testCaseParameters;
                    if (e instanceof CaseParameterizedArray) {
                        testCaseParameters = new TestCaseParameters(e.toString(), e.getTestCaseContext().getParentPath(),
                                testCase.getSql(), null, getTestClass().getJavaClass(), testCase, null);
                        context.registerBean(CaseParameterizedArray.class, (CaseParameterizedArray) e);
                    } else {
                        AssertionParameterizedArray assertion = (AssertionParameterizedArray) e;
                        SQLExecuteType executeType = assertion.getSqlExecuteType();
                        testCaseParameters = new TestCaseParameters(e.toString(), e.getTestCaseContext().getParentPath(),
                                testCase.getSql(), executeType, getTestClass().getJavaClass(), testCase, assertion.getAssertion());
                        context.registerBean(AssertionParameterizedArray.class, assertion);
                    }
                    register(testCaseParameters, context);
                    return context;
                })
                .filter(predicate)
                .map(e -> {
                    try {
                        return new ShardingSphereCISubRunner(getTestClass().getJavaClass(), e);
                    } catch (InitializationError initializationError) {
                        throw new RuntimeException(initializationError);
                    }
                })
                .collect(Collectors.toList());
    }
    
    private void register(final TestCaseParameters parameters, final TestCaseBeanContext context) {
        context.registerBean(TestCaseParameters.class, parameters);
        context.registerBeanByName("statement", parameters.getStatement());
        context.registerBeanByName("parentPath", parameters.getParentPath());
        context.registerBean(SQLExecuteType.class, parameters.getExecuteType());
        context.registerBean(IntegrationTestCase.class, parameters.getTestCase());
        context.registerBean(IntegrationTestCaseAssertion.class, parameters.getAssertion());
    }
    
    private Predicate<TestCaseBeanContext> createTestCaseParametersPredicate() {
        ParameterFilter filter = getTestClass().getAnnotation(ParameterFilter.class);
        final Predicate<TestCaseBeanContext> predicate;
        if (Objects.nonNull(filter)) {
            Class<? extends ParameterFilter.Filter> filtered = filter.filter();
            try {
                final ParameterFilter.Filter instance = filtered.newInstance();
                predicate = instance::filter;
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            predicate = parameters -> true;
        }
        return predicate;
    }
    
    private Collection<ParameterizedArray> allCIParameters(final TestCaseSpec testCaseSpec) {
        switch (testCaseSpec.executionMode()) {
            case ADDITIONAL:
                return IntegrationTestEnvironment.getInstance().isRunAdditionalTestCases()
                        ? ParameterizedArrayFactory.getAssertionParameterized(testCaseSpec.sqlCommandType())
                        : Collections.emptyList();
            case BATCH:
                return ParameterizedArrayFactory.getCaseParameterized(testCaseSpec.sqlCommandType());
            default:
                return ParameterizedArrayFactory.getAssertionParameterized(testCaseSpec.sqlCommandType());
        }
    }
    
    @SneakyThrows
    private Collection<TestCaseParameters> allParameters(final TestCaseDescription description) {
        switch (description.getExecutionMode()) {
            case ADDITIONAL:
                return IntegrationTestEnvironment.getInstance().isRunAdditionalTestCases()
                        ? getAssertionParameters(getTestClass().getJavaClass(), description)
                        : Collections.emptyList();
            case BATCH:
                return getCaseParameters(getTestClass().getJavaClass(), description);
            case SINGLE:
                return getAssertionParameters(getTestClass().getJavaClass(), description);
            default:
                throw new NotSupportedException();
        }
    }
    
    private Collection<TestCaseParameters> getAssertionParameters(final Class<?> klass, final TestCaseDescription description) {
        final IntegrationTestCasesLoader testCasesLoader = IntegrationTestCasesLoader.getInstance();
        return testCasesLoader.getTestCaseContexts(description.getSqlCommandType()).stream()
                .filter(e -> assertThan(e.getTestCase().getDbTypes(), description.getDatabase()))
                .filter(e -> assertThan(e.getTestCase().getScenarioTypes(), description.getScenario()))
                .flatMap(e -> Arrays.stream(SQLExecuteType.values()).flatMap(type -> e.getTestCase().getAssertions().stream()
                        .map(a -> new TestCaseParameters(getCaseName(), e.getParentPath(), e.getTestCase().getSql(), type, klass, e.getTestCase(), a)))
                ).collect(Collectors.toList());
    }
    
    private static boolean assertThan(final String target, final String expected) {
        if (Strings.isNullOrEmpty(target)) {
            return true;
        }
        if (target.indexOf(',') < 0) {
            return target.equals(expected);
        }
        String[] scenarioTypes = target.split("\\s*,\\s*");
        for (String each : scenarioTypes) {
            if (each.equals(expected)) {
                return true;
            }
        }
        return false;
    }
    
    private Collection<TestCaseParameters> getCaseParameters(final Class<?> klass, final TestCaseDescription description) {
        IntegrationTestCasesLoader testCasesLoader = IntegrationTestCasesLoader.getInstance();
        return testCasesLoader.getTestCaseContexts(description.getSqlCommandType()).stream()
                .filter(e -> assertThan(e.getTestCase().getDbTypes(), description.getDatabase()))
                .filter(e -> assertThan(e.getTestCase().getScenarioTypes(), description.getScenario()))
                .flatMap(e -> Arrays.stream(SQLExecuteType.values())
                        .map(type -> new TestCaseParameters(getCaseName(), e.getParentPath(), e.getTestCase().getSql(), type, klass, e.getTestCase(), null)))
                .collect(Collectors.toList());
    }
    
    @Override
    protected List<Runner> getChildren() {
        return runners;
    }
    
    protected Statement withBeforeClasses(final Statement statement) {
        if (getChildren().isEmpty()) {
            return super.withBeforeClasses(statement);
        }
        return super.withBeforeClasses(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                beforeAllClasses();
                statement.evaluate();
            }
        });
    }
    
    private void beforeAllClasses() throws Exception {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(BeforeAllCases.class);
        List<Runner> children = getChildren();
        if (!children.isEmpty()) {
            final Runner runner = children.get(0);
            final Object testInstance;
            if (runner instanceof ShardingSphereITSubRunner) {
                ShardingSphereITSubRunner itRunner = ((ShardingSphereITSubRunner) runner).copySelf();
                testInstance = itRunner.createTestInstance();
                compose.setInstance(testInstance);
                compose.createContainers();
                ((ShardingSphereITSubRunner) runner).autowired(testInstance);
                compose.createInitializerAndExecute();
                compose.start();
                compose.waitUntilReady();
            } else {
                testInstance = ((ShardingSphereCISubRunner) runner).createTest();
            }
            methods.forEach(e -> {
                e.getMethod().setAccessible(true);
                try {
                    e.invokeExplosively(testInstance);
                    // CHECKSTYLE:OFF
                } catch (Throwable throwable) {
                    // CHECKSTYLE:ON
                    throw new RuntimeException(throwable);
                }
            });
        }
    }
    
    @Override
    protected Statement withAfterClasses(final Statement statement) {
        return super.withAfterClasses(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                statement.evaluate();
                // shutdown docker containers.
                if (Objects.nonNull(compose)) {
                    compose.close();
                }
                // missing @AfterAllCases
            }
        });
    }
    
}
