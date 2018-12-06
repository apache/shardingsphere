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

package io.shardingsphere.transaction.aspect;

import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.transaction.TransactionTypeHolder;
import io.shardingsphere.transaction.annotation.ShardingTransactional;
import io.shardingsphere.transaction.handler.DataSourceTransactionManagerHandler;
import io.shardingsphere.transaction.handler.JpaTransactionManagerHandler;
import io.shardingsphere.transaction.handler.TransactionManagerHandler;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Sharding transaction aspect.
 *
 * @author yangyi
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public final class ShardingTransactionalAspect {
    
    private PlatformTransactionManager transactionManager;
    
    private TransactionManagerHandler transactionManagerHandler;
    
    /**
     * Inject spring transactionManager.
     * This transactionManager required when Switch transaction type for Sharding-Proxy.
     *
     * @param transactionManager spring transactionManager
     */
    @Autowired
    public void setTransactionManager(final PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        setTransactionManagerHandler();
    }
    
    /**
     * Sharding transactional AOP pointcut.
     */
    @Pointcut("@annotation(io.shardingsphere.transaction.annotation.ShardingTransactional) || @within(io.shardingsphere.transaction.annotation.ShardingTransactional)")
    public void shardingTransactionalPointCut() {
    
    }
    
    @Before(value = "shardingTransactionalPointCut()")
    public void setTransactionTypeBeforeTransaction(final JoinPoint joinPoint) {
        ShardingTransactional shardingTransactional = getAnnotation(joinPoint);
        
        switch (shardingTransactional.environment()) {
            case JDBC:
                TransactionTypeHolder.set(shardingTransactional.type());
                break;
            case PROXY:
                transactionManagerHandler.switchTransactionType(shardingTransactional.type());
                break;
            default:
            
        }
    }
    
    @After(value = "shardingTransactionalPointCut()")
    public void cleanTransactionTypeAfterTransaction(final JoinPoint joinPoint) {
        ShardingTransactional shardingTransactional = getAnnotation(joinPoint);
        
        switch (shardingTransactional.environment()) {
            case JDBC:
                TransactionTypeHolder.clear();
                break;
            case PROXY:
                transactionManagerHandler.unbindResource();
                break;
            default:
            
        }
    }
    
    private void setTransactionManagerHandler() {
        switch (TransactionManagerType.getTransactionManagerTypeByClassName(transactionManager.getClass().getName())) {
            case DATASOURCE:
                transactionManagerHandler = new DataSourceTransactionManagerHandler(transactionManager);
                break;
            case JPA:
                transactionManagerHandler = new JpaTransactionManagerHandler(transactionManager);
                break;
            case UNSUPPORTED:
            default:
                throw new ShardingException(String.format("Switching transaction Type is unsupported for transaction manager %s", transactionManager.getClass().getName()));
        }
    }
    
    private ShardingTransactional getAnnotation(final JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        ShardingTransactional result = method.getAnnotation(ShardingTransactional.class);
        if (null == result) {
            result = method.getDeclaringClass().getAnnotation(ShardingTransactional.class);
        }
        return result;
    }
    
    @RequiredArgsConstructor
    private enum TransactionManagerType {
        
        /**
         * Spring DataSourceTransactionManager.
         */
        DATASOURCE("org.springframework.jdbc.datasource.DataSourceTransactionManager"),
    
        /**
         * Spring JpaTransactionManager.
         */
        JPA("org.springframework.orm.jpa.JpaTransactionManager"),
    
        /**
         * Other spring PlatformTransactionManager.
         */
        UNSUPPORTED("");
        
        @Getter
        private final String className;
        
        private static TransactionManagerType getTransactionManagerTypeByClassName(final String className) {
            for (TransactionManagerType each : TransactionManagerType.values()) {
                if (each.getClassName().equals(className)) {
                    return each;
                }
            }
            return UNSUPPORTED;
        }
    }
}
