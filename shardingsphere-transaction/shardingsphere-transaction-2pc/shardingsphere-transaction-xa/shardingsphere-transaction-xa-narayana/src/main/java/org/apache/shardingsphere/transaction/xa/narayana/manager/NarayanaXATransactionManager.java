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

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import lombok.SneakyThrows;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManager;

import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

/**
 * Narayana transaction manager.
 */
public final class NarayanaXATransactionManager implements XATransactionManager {
    
    private static final TransactionManager TRANSACTION_MANAGER = jtaPropertyManager.getJTAEnvironmentBean().getTransactionManager();
    
    private static final XARecoveryModule XA_RECOVERY_MODULE = XARecoveryModule.getRegisteredXARecoveryModule();
    
    private static final RecoveryManagerService RECOVERY_MANAGER_SERVICE = new RecoveryManagerService();
    
    @Override
    public void init() {
        RecoveryManager.delayRecoveryManagerThread();
        RECOVERY_MANAGER_SERVICE.create();
        RECOVERY_MANAGER_SERVICE.start();
    }
    
    @Override
    public void registerRecoveryResource(final String dataSourceName, final XADataSource xaDataSource) {
        XA_RECOVERY_MODULE.addXAResourceRecoveryHelper(new DataSourceXAResourceRecoveryHelper(xaDataSource));
    }
    
    @Override
    public void removeRecoveryResource(final String dataSourceName, final XADataSource xaDataSource) {
        XA_RECOVERY_MODULE.removeXAResourceRecoveryHelper(new DataSourceXAResourceRecoveryHelper(xaDataSource));
    }
    
    @SneakyThrows
    @Override
    public void enlistResource(final SingleXAResource singleXAResource) {
        TRANSACTION_MANAGER.getTransaction().enlistResource(singleXAResource.getDelegate());
    }
    
    @Override
    public TransactionManager getTransactionManager() {
        return TRANSACTION_MANAGER;
    }
    
    @Override
    public void close() throws Exception {
        RECOVERY_MANAGER_SERVICE.stop();
        RECOVERY_MANAGER_SERVICE.destroy();
    }
}
