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

package org.apache.shardingsphere.transaction.xa.narayana.manager;

import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.arjuna.common.util.propertyservice.PropertiesFactory;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.util.reflection.ReflectionUtils;
import org.apache.shardingsphere.transaction.exception.CloseTransactionManagerFailedException;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManagerProvider;

import javax.sql.XADataSource;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.util.concurrent.ConcurrentMap;

/**
 * Narayana transaction manager provider.
 */
public final class NarayanaXATransactionManagerProvider implements XATransactionManagerProvider {
    
    @Getter
    private TransactionManager transactionManager;
    
    private XARecoveryModule xaRecoveryModule;
    
    private RecoveryManagerService recoveryManagerService;
    
    @Override
    public void init() {
        transactionManager = jtaPropertyManager.getJTAEnvironmentBean().getTransactionManager();
        xaRecoveryModule = XARecoveryModule.getRegisteredXARecoveryModule();
        recoveryManagerService = new RecoveryManagerService();
        recoveryManagerService.create();
        recoveryManagerService.start();
    }
    
    @Override
    public void registerRecoveryResource(final String dataSourceName, final XADataSource xaDataSource) {
        if (null != xaRecoveryModule) {
            xaRecoveryModule.addXAResourceRecoveryHelper(new DataSourceXAResourceRecoveryHelper(xaDataSource));
        }
    }
    
    @Override
    public void removeRecoveryResource(final String dataSourceName, final XADataSource xaDataSource) {
        if (null != xaRecoveryModule) {
            xaRecoveryModule.removeXAResourceRecoveryHelper(new DataSourceXAResourceRecoveryHelper(xaDataSource));
        }
    }
    
    @SneakyThrows({SystemException.class, RollbackException.class})
    @Override
    public void enlistResource(final SingleXAResource singleXAResource) {
        transactionManager.getTransaction().enlistResource(singleXAResource.getDelegate());
    }
    
    @Override
    public void close() {
        try {
            recoveryManagerService.stop();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new CloseTransactionManagerFailedException(ex);
        }
        recoveryManagerService.destroy();
        cleanPropertiesFactory();
        cleanBeanInstances();
        cleanAtomicActionRecovery();
        cleanXARecoveryModule();
        cleanStoreManager();
    }
    
    private void cleanPropertiesFactory() {
        ReflectionUtils.setStaticFieldValue(PropertiesFactory.class, "delegatePropertiesFactory", null);
    }
    
    private void cleanBeanInstances() {
        ReflectionUtils.<ConcurrentMap<String, Object>>getStaticFieldValue(BeanPopulator.class, "beanInstances").clear();
    }
    
    private void cleanAtomicActionRecovery() {
        ReflectionUtils.setStaticFieldValue(AtomicActionRecoveryModule.class, "_recoveryStore", null);
    }
    
    private void cleanXARecoveryModule() {
        ReflectionUtils.setStaticFieldValue(XARecoveryModule.class, "registeredXARecoveryModule", null);
    }
    
    private void cleanStoreManager() {
        ReflectionUtils.setStaticFieldValue(StoreManager.class, "actionStore", null);
        ReflectionUtils.setStaticFieldValue(StoreManager.class, "stateStore", null);
        ReflectionUtils.setStaticFieldValue(StoreManager.class, "communicationStore", null);
    }
    
    @Override
    public String getType() {
        return "Narayana";
    }
}
