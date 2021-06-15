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

package org.apache.shardingsphere.spring.transaction;

import org.aopalliance.aop.Advice;
import org.apache.shardingsphere.transaction.annotation.ShardingTransactionType;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.core.Ordered;

/**
 * Sharding transaction type advisor.
 */
public final class ShardingTransactionTypeAdvisor extends AbstractPointcutAdvisor {
    
    private static final long serialVersionUID = 8776494474108508276L;
    
    private final Pointcut transactionTypePointcut;
    
    private final Advice transactionTypeInterceptor;
    
    ShardingTransactionTypeAdvisor() {
        Pointcut classPointcut = new ComposablePointcut(AnnotationMatchingPointcut.forClassAnnotation(ShardingTransactionType.class));
        Pointcut methodPointcut = new ComposablePointcut(AnnotationMatchingPointcut.forMethodAnnotation(ShardingTransactionType.class));
        transactionTypePointcut = new ComposablePointcut(classPointcut).union(methodPointcut);
        transactionTypeInterceptor = new ShardingTransactionTypeInterceptor();
        setOrder(Ordered.LOWEST_PRECEDENCE - 1);
    }
    
    @Override
    public Pointcut getPointcut() {
        return transactionTypePointcut;
    }
    
    @Override
    public Advice getAdvice() {
        return transactionTypeInterceptor;
    }
}
