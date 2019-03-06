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

package org.apache.shardingsphere.transaction.spring.boot;

import org.apache.shardingsphere.transaction.aspect.ShardingTransactionalAspect;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionMessage.Builder;
import org.springframework.boot.autoconfigure.condition.ConditionMessage.Style;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.util.Arrays;

/**
 * Spring boot sharding transaction configuration.
 *
 * @author yangyi
 */
@Configuration
@NoArgsConstructor
public class ShardingTransactionConfiguration {
    
    /**
     * Build sharding transaction aspect bean.
     *
     * @return sharding transaction aspect bean
     */
    @Bean
    public ShardingTransactionalAspect shardingTransactionalAspect() {
        return new ShardingTransactionalAspect();
    }
    
    /**
     * Build hibernate transaction manager.
     *
     * @param transactionManagerCustomizers transaction manager customizers
     * @return jpa transaction manager
     */
    @Bean
    @ConditionalOnMissingBean(PlatformTransactionManager.class)
    @Conditional(HibernateEntityManagerCondition.class)
    public PlatformTransactionManager jpaTransactionManager(final ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
        JpaTransactionManager result = new JpaTransactionManager();
        if (null != transactionManagerCustomizers.getIfAvailable()) {
            transactionManagerCustomizers.getIfAvailable().customize(result);
        }
        return result;
    }
    
    /**
     * Build datasource transaction manager.
     *
     * @param dataSource data source
     * @param transactionManagerCustomizers transaction manager customizers
     * @return datasource transaction manager
     */
    @Bean
    @ConditionalOnMissingBean(PlatformTransactionManager.class)
    public PlatformTransactionManager dataSourceTransactionManager(final DataSource dataSource, final ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
        DataSourceTransactionManager result = new DataSourceTransactionManager(dataSource);
        if (null != transactionManagerCustomizers.getIfAvailable()) {
            transactionManagerCustomizers.getIfAvailable().customize(result);
        }
        return result;
    }
    
    @NoArgsConstructor
    static class HibernateEntityManagerCondition extends SpringBootCondition {
        private static final String[] CLASS_NAMES = new String[]{"org.hibernate.ejb.HibernateEntityManager", "org.hibernate.jpa.HibernateEntityManager"};
        
        @Override
        public ConditionOutcome getMatchOutcome(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
            Builder message = ConditionMessage.forCondition("HibernateEntityManager", new Object[0]);
            for (String each : CLASS_NAMES) {
                if (ClassUtils.isPresent(each, context.getClassLoader())) {
                    return ConditionOutcome.match(message.found("class").items(Style.QUOTE, new Object[]{each}));
                }
            }
            
            return ConditionOutcome.noMatch(message.didNotFind("class", "classes").items(Style.QUOTE, Arrays.asList(CLASS_NAMES)));
        }
    }
}
