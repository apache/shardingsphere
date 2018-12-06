/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.annotation;

import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.transaction.ShardingEnvironment;
import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sharding transactional annotation.
 *
 * @author yangyi
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Transactional
@Inherited
public @interface ShardingTransactional {
    /**
     * Sharding transaction type, include LOCAL, XA, BASE.
     * default LOCAL.
     *
     * @return Sharding transaction type
     */
    TransactionType type() default TransactionType.LOCAL;
    
    /**
     * Sharding Sphere runtime environment, JDBC or PROXY.
     *
     * @return Sharding Sphere runtime environment
     */
    ShardingEnvironment environment() default ShardingEnvironment.JDBC;
    
    /**
     * Alias value attribute of Spring Transactional.
     *
     * @return value attribute of Transactional
     */
    @AliasFor(
        annotation = Transactional.class,
        attribute = "value"
    )
    String value() default "";
    
    /**
     * Alias propagation attribute of Spring Transactional.
     *
     * @return propagation attribute of Transactional
     */
    @AliasFor(
        annotation = Transactional.class,
        attribute = "propagation"
    )
    Propagation propagation() default Propagation.REQUIRED;
    
    /**
     * Alias isolation attribute of Spring Transactional.
     *
     * @return isolation attribute of Transactional
     */
    @AliasFor(
        annotation = Transactional.class,
        attribute = "isolation"
    )
    Isolation isolation() default Isolation.DEFAULT;
    
    /**
     * Alias timeout attribute of Spring Transactional.
     *
     * @return timeout attribute of Transactional
     */
    @AliasFor(
        annotation = Transactional.class,
        attribute = "timeout"
    )
    int timeout() default -1;
    
    /**
     * Alias readOnly attribute of Spring Transactional.
     *
     * @return readOnly attribute of Transactional
     */
    @AliasFor(
        annotation = Transactional.class,
        attribute = "readOnly"
    )
    boolean readOnly() default false;
    
    /**
     * Alias rollbackFor attribute of Spring Transactional.
     *
     * @return rollbackFor attribute of Transactional
     */
    @AliasFor(
        annotation = Transactional.class,
        attribute = "rollbackFor"
    )
    Class<? extends Throwable>[] rollbackFor() default {};
    
    /**
     * Alias rollbackForClassName attribute of Spring Transactional.
     *
     * @return rollbackForClassName attribute of Transactional
     */
    @AliasFor(
        annotation = Transactional.class,
        attribute = "rollbackForClassName"
    )
    String[] rollbackForClassName() default {};
    
    /**
     * Alias noRollbackFor attribute of Spring Transactional.
     *
     * @return noRollbackFor attribute of Transactional
     */
    @AliasFor(
        annotation = Transactional.class,
        attribute = "noRollbackFor"
    )
    Class<? extends Throwable>[] noRollbackFor() default {};
    
    /**
     * Alias noRollbackForClassName attribute of Spring Transactional.
     *
     * @return noRollbackForClassName attribute of Transactional
     */
    @AliasFor(
        annotation = Transactional.class,
        attribute = "noRollbackForClassName"
    )
    String[] noRollbackForClassName() default {};
}
