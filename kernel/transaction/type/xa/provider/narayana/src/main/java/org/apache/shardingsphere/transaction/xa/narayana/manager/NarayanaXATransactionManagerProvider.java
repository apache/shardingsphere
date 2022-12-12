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
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.arjuna.common.util.propertyservice.PropertiesFactory;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManagerProvider;

import javax.sql.XADataSource;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.lang.reflect.Field;
import java.util.Objects;
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
        RecoveryManager.delayRecoveryManagerThread();
        recoveryManagerService.create();
        recoveryManagerService.start();
    }
    
    @Override
    public void registerRecoveryResource(final String dataSourceName, final XADataSource xaDataSource) {
        if (Objects.nonNull(xaRecoveryModule)) {
            xaRecoveryModule.addXAResourceRecoveryHelper(new DataSourceXAResourceRecoveryHelper(xaDataSource));
        }
    }
    
    @Override
    public void removeRecoveryResource(final String dataSourceName, final XADataSource xaDataSource) {
        if (Objects.nonNull(xaRecoveryModule)) {
            xaRecoveryModule.removeXAResourceRecoveryHelper(new DataSourceXAResourceRecoveryHelper(xaDataSource));
        }
    }
    
    @SneakyThrows({SystemException.class, RollbackException.class})
    @Override
    public void enlistResource(final SingleXAResource singleXAResource) {
        transactionManager.getTransaction().enlistResource(singleXAResource.getDelegate());
    }
    
    @Override
    public void close() throws Exception {
        recoveryManagerService.stop();
        recoveryManagerService.destroy();
        propertiesFactoryClean();
        beanPopulatorClean();
        atomicActionRecoveryClean();
        xaRecoveryModuleClean();
        storeManagerClean();
    }
    
    private void propertiesFactoryClean() throws NoSuchFieldException, IllegalAccessException {
        Field field = PropertiesFactory.class.getDeclaredField("delegatePropertiesFactory");
        field.setAccessible(true);
        field.set("delegatePropertiesFactory", null);
    }
    
    private void beanPopulatorClean() throws NoSuchFieldException, IllegalAccessException {
        Field field = BeanPopulator.class.getDeclaredField("beanInstances");
        field.setAccessible(true);
        ConcurrentMap map = (ConcurrentMap) field.get("beanInstances");
        map.clear();
    }
    
    private void atomicActionRecoveryClean() throws NoSuchFieldException, IllegalAccessException {
        Field field = AtomicActionRecoveryModule.class.getDeclaredField("_recoveryStore");
        field.setAccessible(true);
        field.set("_recoveryStore", null);
    }
    
    private void xaRecoveryModuleClean() throws NoSuchFieldException, IllegalAccessException {
        Field field = XARecoveryModule.class.getDeclaredField("registeredXARecoveryModule");
        field.setAccessible(true);
        field.set("registeredXARecoveryModule", null);
    }
    
    private void storeManagerClean() throws NoSuchFieldException, IllegalAccessException {
        Field actionStoreField = StoreManager.class.getDeclaredField("actionStore");
        actionStoreField.setAccessible(true);
        actionStoreField.set("actionStore", null);
        Field stateStoreField = StoreManager.class.getDeclaredField("stateStore");
        stateStoreField.setAccessible(true);
        stateStoreField.set("stateStore", null);
        Field communicationStoreField = StoreManager.class.getDeclaredField("communicationStore");
        communicationStoreField.setAccessible(true);
        communicationStoreField.set("communicationStore", null);
    }
    
    @Override
    public String getType() {
        return "Narayana";
    }
}
