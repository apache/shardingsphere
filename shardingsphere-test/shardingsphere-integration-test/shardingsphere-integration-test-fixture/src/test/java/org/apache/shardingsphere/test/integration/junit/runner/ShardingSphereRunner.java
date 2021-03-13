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
import org.apache.shardingsphere.test.integration.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.junit.annotation.ParameterFilter;
import org.apache.shardingsphere.test.integration.junit.annotation.TestCaseSpec;
import org.apache.shardingsphere.test.integration.junit.compose.ContainerCompose;
import org.apache.shardingsphere.test.integration.junit.compose.NotSupportedException;
import org.apache.shardingsphere.test.integration.junit.resolver.ConditionResolver;
import org.apache.shardingsphere.test.integration.junit.runner.parallel.ParallelRunnerScheduler;
import org.apache.shardingsphere.test.integration.junit.runner.parallel.annotaion.ParallelRuntimeStrategy;
import org.apache.shardingsphere.test.integration.param.ParameterizedArrayFactory;
import org.apache.shardingsphere.test.integration.param.model.AssertionParameterizedArray;
import org.apache.shardingsphere.test.integration.param.model.ParameterizedArray;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
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
    
    private ContainerCompose compose = null;
    
    private final TestCaseBeanContext beanContext = new TestCaseBeanContext();
    
    private final ConditionResolver resolver;
    
    public ShardingSphereRunner(final Class<?> klass) throws InitializationError {
        super(klass, Collections.emptyList());
        TestCaseSpec testCaseSpec = getTestClass().getAnnotation(TestCaseSpec.class);
        caseName = Strings.isNullOrEmpty(testCaseSpec.name()) ? klass.getSimpleName() : testCaseSpec.name();
        resolver = new ConditionResolver();
        
        if (Boolean.getBoolean("it.enable")) {
            // it process
            runners = createITRunners(testCaseSpec);
            compose.startup();
            compose.waitUntilReady();
        } else {
            // ci process
            runners = createCIRunners(testCaseSpec);
        }
    }
    
    private List<Runner> createITRunners(final TestCaseSpec testCaseSpec) {
        final Predicate<TestCaseParameters> predicate = createTestCaseParametersPredicate();
        TestCaseDescription description = TestCaseDescription.fromSystemProps(testCaseSpec).build();
        beanContext.registerBean(TestCaseDescription.class, description);
        compose = new ContainerCompose(caseName, getTestClass(), description, resolver, beanContext);
        return allParameters(description).stream()
                .filter(predicate)
                .map(e -> {
                    try {
                        TestCaseBeanContext context = beanContext.subContext();
                        
                        context.registerBeanByName("statement", e.getStatement());
                        context.registerBeanByName("parentPath", e.getParentPath());
                        context.registerBean(SQLExecuteType.class, e.getExecuteType());
                        context.registerBean(IntegrationTestCase.class, e.getTestCase());
                        context.registerBean(IntegrationTestCaseAssertion.class, e.getAssertion());
                        
                        return new ShardingSphereITSubRunner(getTestClass().getJavaClass(), context, resolver);
                    } catch (InitializationError ex) {
                        throw new RuntimeException("Initialization Error", ex);
                    }
                })
                .collect(Collectors.toList());
    }
    
    private List<Runner> createCIRunners(final TestCaseSpec testCaseSpec) {
        ParallelRuntimeStrategy parallelRuntimeStrategy = getTestClass().getAnnotation(ParallelRuntimeStrategy.class);
        if (null != parallelRuntimeStrategy) {
            setScheduler(new ParallelRunnerScheduler(parallelRuntimeStrategy.value()));
        }
        
        Predicate<TestCaseParameters> predicate = createTestCaseParametersPredicate();
        // there is only single IT runner;
        Collection<ParameterizedArray> parameterized = ParameterizedArrayFactory.getAssertionParameterized(testCaseSpec.sqlCommandType());
        return parameterized.stream()
                .flatMap(e -> {
                    final TestCaseDescription description = TestCaseDescription.builder()
                            .sqlCommandType(testCaseSpec.sqlCommandType())
                            .executionMode(testCaseSpec.executionMode())
                            .adapter(e.getAdapter())
                            .database(e.getDatabaseType().getName())
                            .scenario(e.getScenario())
                            .build();
                    
                    IntegrationTestCase testCase = e.getTestCaseContext().getTestCase();
                    return testCase.getAssertions().stream()
                            .map(ee -> {
                                TestCaseBeanContext context = beanContext.subContext();
                                context.registerBean(TestCaseDescription.class, description);
                                context.registerBeanByName("statement", testCase.getSql());
                                context.registerBeanByName("parentPath", e.getTestCaseContext().getParentPath());
                                context.registerBean(SQLExecuteType.class, ((AssertionParameterizedArray) e).getSqlExecuteType());
                                context.registerBean(IntegrationTestCase.class, testCase);
                                context.registerBean(IntegrationTestCaseAssertion.class, ee);
                                
                                try {
                                    return new ShardingSphereCISubRunner(getTestClass().getJavaClass(), context, resolver);
                                } catch (InitializationError initializationError) {
                                    initializationError.printStackTrace();
                                }
                                return null;
                            }).filter(Objects::nonNull);
                    
                })
                .collect(Collectors.toList());
    }
    
    private Predicate<TestCaseParameters> createTestCaseParametersPredicate() {
        ParameterFilter filter = getTestClass().getAnnotation(ParameterFilter.class);
        final Predicate<TestCaseParameters> predicate;
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
                .filter(e -> {
                    String dbTypes = e.getTestCase().getDbTypes();
                    if (Strings.isNullOrEmpty(dbTypes)) {
                        return true;
                    }
                    return dbTypes.contains(description.getDatabase());
                })
                .flatMap(e -> Arrays.stream(SQLExecuteType.values()).flatMap(type -> e.getTestCase().getAssertions().stream()
                        .map(a -> new TestCaseParameters(getCaseName(), e.getParentPath(), e.getTestCase().getSql(), type, klass, e.getTestCase(), a)))
                ).collect(Collectors.toList());
    }
    
    private Collection<TestCaseParameters> getCaseParameters(final Class<?> klass, final TestCaseDescription description) {
        IntegrationTestCasesLoader testCasesLoader = IntegrationTestCasesLoader.getInstance();
        return testCasesLoader.getTestCaseContexts(description.getSqlCommandType()).stream()
                .filter(e -> {
                    String dbTypes = e.getTestCase().getDbTypes();
                    if (Strings.isNullOrEmpty(dbTypes)) {
                        return true;
                    }
                    return dbTypes.contains(description.getDatabase());
                })
                .flatMap(e -> Arrays.stream(SQLExecuteType.values())
                        .map(type -> new TestCaseParameters(getCaseName(), e.getParentPath(), e.getTestCase().getSql(), type, klass, e.getTestCase(), null)))
                .collect(Collectors.toList());
    }
    
    @Override
    protected List<Runner> getChildren() {
        return runners;
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
            }
        });
    }
    
    private boolean isIgnoredCase() {
        return !resolver.filter(getTestClass());
    }
    
}
