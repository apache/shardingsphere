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
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * Sharding transaction aspect.
 *
 * @author yangyi
 */
@Aspect
@Component
@Order(2147483646)
public final class ShardingTransactionalAspect {
    
    private static final String SET_TRANSACTION_TYPE_SQL = "SET TRANSACTION_TYPE=%s";
    
    private DataSource dataSource;
    
    /**
     * Inject dataSource of Sharding-Proxy.
     * This dataSource required when Switch transaction type for Sharding-Proxy.
     *
     * @param dataSources Sharding-Proxy datasource.
     */
    @Autowired(required = false)
    public void setDataSource(final DataSource[] dataSources) {
        this.dataSource = dataSources[0];
    }
    
    /**
     * ShardingTransationnal AOP pointcut.
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
                if (null == dataSource) {
                    throw new ShardingException(String.format("No DataSource be injected while executing transactional method %s.%s. Please make sure there are a data source bean in Spring",
                        joinPoint.getTarget().getClass().getName(), joinPoint.getSignature().getName()));
                }
                try (Connection connection = dataSource.getConnection();
                    Statement statement = connection.createStatement()) {
                    statement.execute(String.format(SET_TRANSACTION_TYPE_SQL, shardingTransactional.type().name()));
                } catch (SQLException e) {
                    throw new ShardingException("Switch transaction type for sharding-proxy failed: ", e);
                }
                break;
            default:
            
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
    
}
