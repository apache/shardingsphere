package org.apache.shardingsphere.test.integration.engine.it;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Case runtime strategy.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RuntimeStrategy {
    
    /**
     * Set/Get parallel or not.
     * @return boolean parallel or not
     */
    boolean parallel() default false;
    
    /**
     * Set/Get parallel level.
     * @return ParallelLevel parallel level
     */
    ParallelLevel parallelLevel() default ParallelLevel.SCENARIO;
    
    /**
     * Set/Get data isolation level.
     * @return DataIsolationLevel data isolation level
     */
    DataIsolationLevel dataIsolationLevel() default DataIsolationLevel.NON;
}
