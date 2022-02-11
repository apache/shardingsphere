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

package org.apache.shardingsphere.transaction.xa.narayana.config;

import com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCStore;
import com.arjuna.ats.internal.arjuna.objectstore.jdbc.accessors.DynamicDataSourceJDBCAccess;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTAActionStatusServiceXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.spi.TransactionConfigurationFileGenerator;

import javax.xml.bind.JAXB;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Narayana transaction configuration file generator.
 */
public final class NarayanaConfigurationFileGenerator implements TransactionConfigurationFileGenerator {
    
    @Override
    public void generateFile(final TransactionRule transactionRule, final String instanceId) {
        NarayanaConfiguration config = createDefaultConfiguration(instanceId);
        if (null != transactionRule.getProps()) {
            appendJdbcStoreConfiguration(transactionRule, config);
        }
        JAXB.marshal(config, new File(ClassLoader.getSystemResource("").getPath(), "jbossts-properties.xml"));
    }
    
    private NarayanaConfiguration createDefaultConfiguration(final String instanceId) {
        NarayanaConfiguration result = new NarayanaConfiguration();
        result.getEntries().add(createEntry("CoordinatorEnvironmentBean.commitOnePhase", "YES"));
        result.getEntries().add(createEntry("ObjectStoreEnvironmentBean.transactionSync", "NO"));
        result.getEntries().add(createEntry("CoreEnvironmentBean.nodeIdentifier", null == instanceId ? "1" : instanceId));
        result.getEntries().add(createEntry("JTAEnvironmentBean.xaRecoveryNodes", null == instanceId ? "1" : instanceId));
        result.getEntries().add(createEntry("JTAEnvironmentBean.xaResourceOrphanFilterClassNames", createXAResourceOrphanFilterClassNames()));
        result.getEntries().add(createEntry("CoreEnvironmentBean.socketProcessIdPort", "0"));
        result.getEntries().add(createEntry("RecoveryEnvironmentBean.recoveryModuleClassNames", getRecoveryModuleClassNames()));
        result.getEntries().add(createEntry("RecoveryEnvironmentBean.expiryScannerClassNames", ExpiredTransactionStatusManagerScanner.class.getName()));
        result.getEntries().add(createEntry("RecoveryEnvironmentBean.recoveryPort", "4712"));
        result.getEntries().add(createEntry("RecoveryEnvironmentBean.recoveryAddress", ""));
        result.getEntries().add(createEntry("RecoveryEnvironmentBean.transactionStatusManagerPort", "0"));
        result.getEntries().add(createEntry("RecoveryEnvironmentBean.transactionStatusManagerAddress", ""));
        result.getEntries().add(createEntry("RecoveryEnvironmentBean.recoveryListener", "NO"));
        result.getEntries().add(createEntry("RecoveryEnvironmentBean.recoveryBackoffPeriod", "1"));
        return result;
    }
    
    private NarayanaConfigEntry createEntry(final String key, final String value) {
        NarayanaConfigEntry result = new NarayanaConfigEntry();
        result.setKey(key);
        result.getValue().add(value);
        return result;
    }
    
    private NarayanaConfigEntry createEntry(final String key, final Collection<String> values) {
        NarayanaConfigEntry result = new NarayanaConfigEntry();
        result.setKey(key);
        result.getValue().addAll(values);
        return result;
    }
    
    private Collection<String> createXAResourceOrphanFilterClassNames() {
        Collection<String> result = new LinkedList<>();
        result.add(JTATransactionLogXAResourceOrphanFilter.class.getName());
        result.add(JTANodeNameXAResourceOrphanFilter.class.getName());
        result.add(JTAActionStatusServiceXAResourceOrphanFilter.class.getName());
        return result;
    }
    
    private Collection<String> getRecoveryModuleClassNames() {
        Collection<String> result = new LinkedList<>();
        result.add(AtomicActionRecoveryModule.class.getName());
        result.add(XARecoveryModule.class.getName());
        return result;
    }
    
    private void appendJdbcStoreConfiguration(final TransactionRule transactionRule, final NarayanaConfiguration config) {
        String url = transactionRule.getProps().getProperty("recoveryStoreUrl");
        String user = transactionRule.getProps().getProperty("recoveryStoreUser");
        String password = String.valueOf(transactionRule.getProps().get("recoveryStorePassword"));
        String dataSourceClass = transactionRule.getProps().getProperty("recoveryStoreDataSource");
        if (null != url && null != user && null != password && null != dataSourceClass) {
            String jdbcAccessPatten = DynamicDataSourceJDBCAccess.class.getName() + ";ClassName=%s;URL=%s;User=%s;Password=%s";
            String jdbcAccess = String.format(jdbcAccessPatten, dataSourceClass, url, user, password);
            config.getEntries().add(createEntry("ObjectStoreEnvironmentBean.objectStoreType", JDBCStore.class.getName()));
            config.getEntries().add(createEntry("ObjectStoreEnvironmentBean.jdbcAccess", jdbcAccess));
            config.getEntries().add(createEntry("ObjectStoreEnvironmentBean.tablePrefix", "Action"));
            config.getEntries().add(createEntry("ObjectStoreEnvironmentBean.dropTable", "true"));
            config.getEntries().add(createEntry("ObjectStoreEnvironmentBean.stateStore.objectStoreType", JDBCStore.class.getName()));
            config.getEntries().add(createEntry("ObjectStoreEnvironmentBean.stateStore.jdbcAccess", jdbcAccess));
            config.getEntries().add(createEntry("ObjectStoreEnvironmentBean.stateStore.tablePrefix", "stateStore"));
            config.getEntries().add(createEntry("ObjectStoreEnvironmentBean.stateStore.dropTable", "true"));
            config.getEntries().add(createEntry("ObjectStoreEnvironmentBean.communicationStore.objectStoreType", JDBCStore.class.getName()));
            config.getEntries().add(createEntry("ObjectStoreEnvironmentBean.communicationStore.jdbcAccess", jdbcAccess));
            config.getEntries().add(createEntry("ObjectStoreEnvironmentBean.communicationStore.tablePrefix", "Communication"));
            config.getEntries().add(createEntry("ObjectStoreEnvironmentBean.communicationStore.dropTable", "true"));
        }
    }
    
    @Override
    public String getType() {
        return "Narayana";
    }
}
