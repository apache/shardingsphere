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
public class ShardingSphereITRunner extends Suite {
    
    private final List<Runner> runners;
    
    @Getter
    private final String caseName;
    
    private final ContainerCompose compose;
    
    private final TestCaseBeanContext beanContext = new TestCaseBeanContext();
    
    private final ConditionResolver resolver;
    
    public ShardingSphereITRunner(final Class<?> klass) throws InitializationError {
        super(klass, Collections.emptyList());
        TestCaseSpec testCaseSpec = getTestClass().getAnnotation(TestCaseSpec.class);
        caseName = Strings.isNullOrEmpty(testCaseSpec.name()) ? klass.getSimpleName() : testCaseSpec.name();
        TestCaseDescription description = TestCaseDescription.builder()
                .adapter(System.getProperty("it.adapter"))
                .database(System.getProperty("it.database"))
                .scenario(System.getProperty("it.scenario"))
                .sqlCommandType(testCaseSpec.sqlCommandType())
                .executionMode(testCaseSpec.executionMode())
                .build();
        beanContext.registerBean(TestCaseDescription.class, description);
        resolver = new ConditionResolver(getTestClass());
        if (isIgnoredCase(description)) {
            log.warn("The testcase[{}] was ignored.", klass);
            runners = Collections.emptyList();
            compose = null;
        } else {
            runners = createRunners(klass, description);
            compose = new ContainerCompose(caseName, getTestClass(), description, resolver);
            compose.startup();
            compose.waitUntilReady();
        }
    }
    
    private List<Runner> createRunners(final Class<?> klass, final TestCaseDescription description) {
        ParameterFilter filter = getTestClass().getAnnotation(ParameterFilter.class);
        final Predicate<TestCaseParameters> predicate;
        if (Objects.nonNull(filter)) {
            Class<? extends ParameterFilter.Filter> filtered = filter.filtered();
            try {
                final ParameterFilter.Filter instance = filtered.newInstance();
                predicate = instance::filter;
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            predicate = parameters -> true;
        }
        return allParameters(klass, description).stream()
                .filter(predicate)
                .map(e -> {
                    try {
                        TestCaseBeanContext context = beanContext.subContext();
                        context.registerBeanByName("statement", e.getStatement());
                        context.registerBeanByName("parentPath", e.getParentPath());
                        context.registerBean(SQLExecuteType.class, e.getExecuteType());
                        context.registerBean(IntegrationTestCase.class, e.getTestCaseContext().getTestCase());
                        context.registerBean(IntegrationTestCaseAssertion.class, e.getAssertion());
                        return new ShardingSphereITSubCaseRunner(klass, context, resolver);
                    } catch (InitializationError ex) {
                        throw new RuntimeException("Initialization Error", ex);
                    }
                })
                .collect(Collectors.toList());
    }
    
    @SneakyThrows
    private Collection<TestCaseParameters> allParameters(final Class<?> klass, final TestCaseDescription description) {
        switch (description.getExecutionMode()) {
            case ADDITIONAL:
                return IntegrationTestEnvironment.getInstance().isRunAdditionalTestCases() ? getAssertionParameters(klass, description) : Collections.emptyList();
            case BATCH:
                return getCaseParameters(klass, description);
            case SINGLE:
                return getAssertionParameters(klass, description);
            default:
                throw new NotSupportedException();
        }
    }
    
    private Collection<TestCaseParameters> getAssertionParameters(final Class<?> klass, final TestCaseDescription description) {
        IntegrationTestCasesLoader testCasesLoader = IntegrationTestCasesLoader.getInstance();
        return testCasesLoader.getTestCaseContexts(description.getSqlCommandType()).stream()
                .filter(e -> e.getTestCase().getDbTypes().contains(description.getDatabase()))
                .flatMap(e -> Arrays.stream(SQLExecuteType.values()).flatMap(type -> e.getTestCase().getAssertions().stream()
                        .map(a -> new TestCaseParameters(getCaseName(), e.getParentPath(), e.getTestCase().getSql(), type, klass, null, a)))
                ).collect(Collectors.toList());
    }
    
    private Collection<TestCaseParameters> getCaseParameters(final Class<?> klass, final TestCaseDescription description) {
        IntegrationTestCasesLoader testCasesLoader = IntegrationTestCasesLoader.getInstance();
        return testCasesLoader.getTestCaseContexts(description.getSqlCommandType()).stream()
                .filter(e -> e.getTestCase().getDbTypes().contains(description.getDatabase()))
                .flatMap(e -> Arrays.stream(SQLExecuteType.values())
                        .map(type -> new TestCaseParameters(getCaseName(), e.getParentPath(), e.getTestCase().getSql(), type, klass, e, null)))
                .collect(Collectors.toList());
    }
    
    @Override
    protected List<Runner> getChildren() {
        return runners;
    }
    
    @Override
    protected Statement withAfterClasses(final Statement statement) {
        super.withAfterClasses(statement);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                statement.evaluate();
                // shutdown docker containers.
                if (Objects.nonNull(compose)) {
                    compose.close();
                }
            }
        };
    }
    
    private boolean isIgnoredCase(final TestCaseDescription description) {
        return !resolver.filter(getTestClass());
    }
    
}
