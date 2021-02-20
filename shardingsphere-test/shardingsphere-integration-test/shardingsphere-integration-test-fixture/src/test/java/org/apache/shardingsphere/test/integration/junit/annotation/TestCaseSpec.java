package org.apache.shardingsphere.test.integration.junit.annotation;

import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.common.ExecutionMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestCaseSpec {
    
    /**
     * Test case name.
     *
     * @return pattern
     */
    String name() default "";
    
    /**
     * Sql command type.
     *
     * @return type
     */
    SQLCommandType commandType();
    
    /**
     * Case execution mode.
     *
     * @return execution mode
     */
    ExecutionMode executionMode() default ExecutionMode.SINGLE;
    
}
