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

package org.apache.shardingsphere.transaction.aspect;

import org.apache.shardingsphere.transaction.ShardingEnvironment;
import org.apache.shardingsphere.transaction.annotation.ShardingTransactionType;
import org.apache.shardingsphere.transaction.handler.DataSourceTransactionManagerHandler;
import org.apache.shardingsphere.transaction.handler.JpaTransactionManagerHandler;
import org.apache.shardingsphere.transaction.handler.TransactionManagerHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
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

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Sharding transaction aspect.
 *
 * @author yangyi
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public final class ShardingTransactionalAspect {
    
    private static final String PROXY_TAG = "Sharding-Proxy";
    
    private TransactionManagerHandler transactionManagerHandler;
    
    private ShardingEnvironment environment;
    
    /**
     * Inject spring transaction manager.
     * This transaction manager required when Switch transaction type for Sharding-Proxy.
     *
     * @param transactionManager spring transaction manager
     */
    @Autowired
    public void setTransactionManager(final PlatformTransactionManager transactionManager) {
        setTransactionManagerHandler(transactionManager);
    }
    
    /**
     * Analyze data source type to judge environment of sharding sphere.
     *
     * @param dataSources data sources array
     */
    @Autowired
    public void setEnvironment(final DataSource[] dataSources) {
        environment = null != dataSources && isConnectToProxy(dataSources) ? ShardingEnvironment.PROXY : ShardingEnvironment.JDBC;
    }
    
    /**
     * Sharding transactional AOP pointcut.
     */
    @Pointcut("@annotation(org.apache.shardingsphere.transaction.annotation.ShardingTransactionType) || @within(org.apache.shardingsphere.transaction.annotation.ShardingTransactionType)")
    public void shardingTransactionalPointCut() {
    
    }
    
    @Before(value = "shardingTransactionalPointCut()")
    public void setTransactionTypeBeforeTransaction(final JoinPoint joinPoint) {
        ShardingTransactionType shardingTransactionType = getAnnotation(joinPoint);
        switch (environment) {
            case JDBC:
                TransactionTypeHolder.set(shardingTransactionType.value());
                break;
            case PROXY:
                transactionManagerHandler.switchTransactionType(shardingTransactionType.value());
                break;
            default:
        }
    }
    
    @After(value = "shardingTransactionalPointCut()")
    public void cleanTransactionTypeAfterTransaction(final JoinPoint joinPoint) {
        switch (environment) {
            case JDBC:
                TransactionTypeHolder.clear();
                break;
            case PROXY:
                transactionManagerHandler.unbindResource();
                break;
            default:
        }
    }
    
    private void setTransactionManagerHandler(final PlatformTransactionManager transactionManager) {
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
    
    private boolean isConnectToProxy(final DataSource[] dataSources) {
        for (DataSource each : dataSources) {
            try (Connection connection = each.getConnection()) {
                DatabaseMetaData databaseMetaData = connection.getMetaData();
                if (databaseMetaData.getDatabaseProductVersion().contains(PROXY_TAG)) {
                    return true;
                }
            } catch (SQLException ex) {
                throw new ShardingException("Get databaseMetaData failed: ", ex);
            }
        }
        return false;
    }
    
    private ShardingTransactionType getAnnotation(final JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        ShardingTransactionType result = method.getAnnotation(ShardingTransactionType.class);
        if (null == result) {
            result = method.getDeclaringClass().getAnnotation(ShardingTransactionType.class);
        }
        return result;
    }
    
    @RequiredArgsConstructor
    private enum TransactionManagerType {
        
        /**
         * Spring {@code DataSourceTransactionManager}.
         */
        DATASOURCE("org.springframework.jdbc.datasource.DataSourceTransactionManager"),
    
        /**
         * Spring {@code JpaTransactionManager}.
         */
        JPA("org.springframework.orm.jpa.JpaTransactionManager"),
    
        /**
         * Other spring {@code PlatformTransactionManager}.
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
