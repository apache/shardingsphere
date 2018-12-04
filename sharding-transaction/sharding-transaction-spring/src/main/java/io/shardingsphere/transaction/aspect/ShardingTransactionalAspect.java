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

import io.shardingsphere.core.transaction.TransactionTypeHolder;
import io.shardingsphere.transaction.annotation.ShardingTransactional;
import io.shardingsphere.transaction.annotation.ShardingTransactional.ShardingEnvironment;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Sharding transaction aspect.
 *
 * @author yangyi
 */
@Aspect
@Component
@Order(2147483646)
public final class ShardingTransactionalAspect {
    
    /**
     * Sharding transactional AOP pointcut.
     */
    @Pointcut("@annotation(io.shardingsphere.transaction.annotation.ShardingTransactional) || @within(io.shardingsphere.transaction.annotation.ShardingTransactional)")
    public void shardingTransactionalPointCut() {
    
    }
    
    @Before(value = "shardingTransactionalPointCut()")
    public void setTransactionTypeBeforeTransaction(final JoinPoint joinPoint) {
        ShardingTransactional shardingTransactional = getAnnotation(joinPoint);
        if (ShardingEnvironment.JDBC == shardingTransactional.environment()) {
            TransactionTypeHolder.set(shardingTransactional.type());
        }
        // TODO :yangyi send set transaction type command/SQL to sharding-proxy
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
