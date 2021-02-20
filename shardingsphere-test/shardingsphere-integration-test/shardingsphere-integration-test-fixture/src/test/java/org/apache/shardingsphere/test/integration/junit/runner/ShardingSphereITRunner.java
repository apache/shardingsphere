package org.apache.shardingsphere.test.integration.junit.runner;

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.test.integration.cases.IntegrationTestCasesLoader;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.integration.common.SQLExecuteType;
import org.apache.shardingsphere.test.integration.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.junit.annotation.TestCaseSpec;
import org.apache.shardingsphere.test.integration.junit.compose.ContainerCompose;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ShardingSphereITRunner extends Suite {
    
    private final List<Runner> runners;
    
    @Getter
    private final String caseName;
    
    private final TestCaseBeanContext beanContext = new TestCaseBeanContext();
    
    public ShardingSphereITRunner(final Class<?> klass) throws InitializationError {
        super(klass, Collections.emptyList());
        TestCaseSpec testCaseSpec = getTestClass().getAnnotation(TestCaseSpec.class);
        caseName = Strings.isNullOrEmpty(testCaseSpec.name()) ? klass.getSimpleName() : testCaseSpec.name();
        TestCaseDescription description = TestCaseDescription.builder()
                .adapter(System.getProperty("it.adapter"))
                .database(System.getProperty("it.database"))
                .scenario(System.getProperty("it.scenario"))
                .commandType(testCaseSpec.commandType())
                .executionMode(testCaseSpec.executionMode())
                .build();
        beanContext.registerBean(TestCaseDescription.class, description);
        runners = createRunners(klass, description);
        ContainerCompose compose = new ContainerCompose(caseName, getTestClass());
        compose.startup();
        compose.waitUntilReady();
    }
    
    private List<Runner> createRunners(final Class<?> klass, final TestCaseDescription description) {
        return allParameters(klass, description).stream()
                .map(e -> {
                    try {
                        TestCaseBeanContext context = beanContext.subContext();
                        context.registerBeanByName("statement", e.getStatement());
                        context.registerBean(SQLExecuteType.class, e.getExecuteType());
                        context.registerBean(IntegrationTestCaseAssertion.class, e.getAssertion());
                        return new ShardingSphereITSubCaseRunner(klass, context);
                    } catch (InitializationError ex) {
                        throw new RuntimeException("Initialization Error", ex);
                    }
                })
                .collect(Collectors.toList());
    }
    
    Collection<TestCaseParameters> allParameters(final Class<?> klass, final TestCaseDescription description) {
//        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(ParameterFilter.class);
//        methods.stream().peek(e -> {
//            if (!e.isStatic()) {
//                throw new RuntimeException("");
//            }
//            if (!(e.getMethod().getParameterCount() == 1 &&
//                    e.getMethod().getParameterTypes()[0] == TestCaseParameters.class &&
//                    (e.getReturnType() == boolean.class || e.getReturnType() == Boolean.class))) {
//                throw new RuntimeException("The signature must be \"boolean methodName(TestCaseParameters parameters)\"");
//            }
//        }).map(e -> )
        switch (description.getExecutionMode()) {
            case ADDITIONAL:
                return IntegrationTestEnvironment.getInstance().isRunAdditionalTestCases() ? getAssertionParameters(klass, description) : Collections.emptyList();
            case BATCH:
                return getCaseParameters(klass, description);
            case SINGLE:
                return getAssertionParameters(klass, description);
            default:
                return null;
        }
    }
    
    Collection<TestCaseParameters> getAssertionParameters(final Class<?> klass, final TestCaseDescription description) {
        IntegrationTestCasesLoader testCasesLoader = IntegrationTestCasesLoader.getInstance();
        return testCasesLoader.getTestCaseContexts(description.getCommandType()).stream()
                .filter(e -> e.getTestCase().getDbTypes().contains(description.getDatabase()))
                .flatMap(e -> Arrays.stream(SQLExecuteType.values()).flatMap(type -> e.getTestCase().getAssertions().stream()
                        .map(a -> new TestCaseParameters(getCaseName(), e.getParentPath(), e.getTestCase().getSql(), type, klass, null, a)))
                ).collect(Collectors.toList());
    }
    
    Collection<TestCaseParameters> getCaseParameters(final Class<?> klass, final TestCaseDescription description) {
        IntegrationTestCasesLoader testCasesLoader = IntegrationTestCasesLoader.getInstance();
        return testCasesLoader.getTestCaseContexts(description.getCommandType()).stream()
                .filter(e -> e.getTestCase().getDbTypes().contains(description.getDatabase()))
                .flatMap(e -> Arrays.stream(SQLExecuteType.values())
                        .map(type -> new TestCaseParameters(getCaseName(), e.getParentPath(), e.getTestCase().getSql(), type, klass, e, null)))
                .collect(Collectors.toList());
    }
    
    @Override
    protected List<Runner> getChildren() {
        return runners;
    }
    
}
