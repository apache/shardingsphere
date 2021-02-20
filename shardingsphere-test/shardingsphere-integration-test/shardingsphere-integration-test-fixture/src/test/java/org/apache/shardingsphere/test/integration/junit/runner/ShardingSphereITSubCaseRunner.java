package org.apache.shardingsphere.test.integration.junit.runner;

import org.apache.shardingsphere.test.integration.common.SQLExecuteType;
import org.apache.shardingsphere.test.integration.junit.annotation.Inject;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;

public class ShardingSphereITSubCaseRunner extends BlockJUnit4ClassRunner {
    
    private final TestCaseBeanContext context;
    
    public ShardingSphereITSubCaseRunner(final Class<?> testClass, final TestCaseBeanContext context) throws InitializationError {
        super(testClass);
        this.context = context;
    }
    
    @Override
    protected Object createTest() throws Exception {
        final Object testInstance = super.createTest();
        getTestClass().getAnnotatedFields(Inject.class)
                .forEach(e -> {
                    try {
                        Field field = e.getField();
                        field.setAccessible(true);
                        if (field.getType() == String.class) {
                            field.set(testInstance, context.getBeanByName(field.getName()));
                        } else {
                            field.set(testInstance, context.getBean(e.getType()));
                        }
                    } catch (IllegalAccessException ex) {
                        throw new RuntimeException(ex.getMessage(), ex);
                    }
                });
        return testInstance;
    }
    
    @Override
    protected Statement withBeforeClasses(Statement statement) {
        return statement;
    }
    
    @Override
    protected Statement withAfterClasses(Statement statement) {
        return statement;
    }
    
    @Override
    protected String getName() {
        // {2}: {3} -> {4} -> {5} -> {6}
        TestCaseDescription description = context.getBean(TestCaseDescription.class);
        return String.format("[%s: %s -> %s -> %s -> %s]",
                description.getAdapter(),
                description.getScenario(),
                description.getDatabase(),
                context.getBean(SQLExecuteType.class),
                context.getBeanByName("statement")
        );
    }
    
}
