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

package org.apache.shardingsphere.transaction.handler;

import org.hibernate.engine.spi.SessionImplementor;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.sql.Connection;
import java.util.Map;

/**
 * Jpa transaction manager handler.
 *
 * @author yangyi
 */
public final class JpaTransactionManagerHandler extends AbstractTransactionManagerHandler {
    
    private final JpaTransactionManager transactionManager;
    
    public JpaTransactionManagerHandler(final PlatformTransactionManager transactionManager) {
        this.transactionManager = (JpaTransactionManager) transactionManager;
    }
    
    @Override
    public void unbindResource() {
        EntityManagerHolder entityManagerHolder = (EntityManagerHolder) TransactionSynchronizationManager.unbindResourceIfPossible(transactionManager.getEntityManagerFactory());
        EntityManagerFactoryUtils.closeEntityManager(entityManagerHolder.getEntityManager());
    }
    
    @Override
    protected Connection getConnectionFromTransactionManager() {
        EntityManager entityManager = createEntityManager();
        Connection result = entityManager.unwrap(SessionImplementor.class).connection();
        TransactionSynchronizationManager.bindResource(transactionManager.getEntityManagerFactory(), new EntityManagerHolder(entityManager));
        return result;
    }
    
    private EntityManager createEntityManager() {
        EntityManagerFactory entityManagerFactory = transactionManager.getEntityManagerFactory();
        if (entityManagerFactory instanceof EntityManagerFactoryInfo) {
            entityManagerFactory = ((EntityManagerFactoryInfo) entityManagerFactory).getNativeEntityManagerFactory();
        }
        Map<String, Object> properties = transactionManager.getJpaPropertyMap();
        return !CollectionUtils.isEmpty(properties) ? entityManagerFactory.createEntityManager(properties) : entityManagerFactory.createEntityManager();
    }
}
