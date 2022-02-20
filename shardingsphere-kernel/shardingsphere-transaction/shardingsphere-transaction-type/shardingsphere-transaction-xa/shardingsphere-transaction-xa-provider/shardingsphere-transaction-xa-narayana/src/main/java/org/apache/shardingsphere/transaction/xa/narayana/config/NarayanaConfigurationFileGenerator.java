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
import org.apache.shardingsphere.infra.config.schema.SchemaConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaDataFactory;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.spi.TransactionConfigurationFileGenerator;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Narayana transaction configuration file generator.
 */
public final class NarayanaConfigurationFileGenerator implements TransactionConfigurationFileGenerator {
    
    @Override
    public void generateFile(final TransactionRule transactionRule, final InstanceContext instanceContext) {
        String instanceId = instanceContext.getInstance().getInstanceDefinition().getInstanceId().getId();
        String recoveryId = null == instanceContext.getInstance().getXaRecoveryId() ? instanceId : instanceContext.getInstance().getXaRecoveryId();
        NarayanaConfiguration config = createDefaultConfiguration(instanceId, recoveryId);
        if (null != transactionRule.getProps()) {
            appendUserDefinedJdbcStoreConfiguration(transactionRule, config);
        }
        JAXB.marshal(config, new File(ClassLoader.getSystemResource("").getPath(), "jbossts-properties.xml"));
    }
    
    private NarayanaConfiguration createDefaultConfiguration(final String instanceId, final String recoveryId) {
        NarayanaConfiguration result = new NarayanaConfiguration();
        result.getEntries().add(createEntry("CoordinatorEnvironmentBean.commitOnePhase", "YES"));
        result.getEntries().add(createEntry("ObjectStoreEnvironmentBean.transactionSync", "NO"));
        result.getEntries().add(createEntry("CoreEnvironmentBean.nodeIdentifier", instanceId));
        result.getEntries().add(createEntry("JTAEnvironmentBean.xaRecoveryNodes", recoveryId));
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
    
    private void appendUserDefinedJdbcStoreConfiguration(final TransactionRule transactionRule, final NarayanaConfiguration config) {
        String url = transactionRule.getProps().getProperty("recoveryStoreUrl");
        String user = transactionRule.getProps().getProperty("recoveryStoreUser");
        String password = String.valueOf(transactionRule.getProps().get("recoveryStorePassword"));
        String dataSourceClass = transactionRule.getProps().getProperty("recoveryStoreDataSource");
        if (null != url && null != user && null != password && null != dataSourceClass) {
            appendJdbcStoreConfiguration(url, user, password, dataSourceClass, config);
        }
    }
    
    private void appendJdbcStoreConfiguration(final String jdbcUrl, final String user, final String password, final String dataSourceClassName, final NarayanaConfiguration config) {
        String jdbcAccessPatten = DynamicDataSourceJDBCAccess.class.getName() + ";ClassName=%s;URL=%s;User=%s;Password=%s";
        String jdbcAccess = String.format(jdbcAccessPatten, dataSourceClassName, jdbcUrl, user, password);
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
    
    @Override
    public Properties getTransactionProps(final TransactionRuleConfiguration transactionRuleConfiguration, final SchemaConfiguration schemaConfiguration) {
        Properties result = new Properties();
        if (!transactionRuleConfiguration.getProps().isEmpty()) {
            generateUserDefinedJdbcStoreConfiguration(transactionRuleConfiguration, result);
        } else {
            generateDefaultJdbcStoreConfiguration(schemaConfiguration, result);
        }
        return result;
    }
    
    private void generateUserDefinedJdbcStoreConfiguration(final TransactionRuleConfiguration transactionRuleConfiguration, final Properties props) {
        String url = transactionRuleConfiguration.getProps().getProperty("recoveryStoreUrl");
        String user = transactionRuleConfiguration.getProps().getProperty("recoveryStoreUser");
        String password = String.valueOf(transactionRuleConfiguration.getProps().get("recoveryStorePassword"));
        String dataSourceClass = transactionRuleConfiguration.getProps().getProperty("recoveryStoreDataSource");
        generateTransactionProps(url, user, password, dataSourceClass, props);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void generateDefaultJdbcStoreConfiguration(final SchemaConfiguration schemaConfiguration, final Properties props) {
        Map<String, DataSource> datasourceMap = schemaConfiguration.getDataSources();
        Optional<DataSource> dataSource = datasourceMap.values().stream().findFirst();
        if (dataSource.isPresent()) {
            Optional<DataSourcePoolMetaData> poolMetaData = DataSourcePoolMetaDataFactory.newInstance(dataSource.get().getClass().getName());
            if (poolMetaData.isPresent()) {
                String jdbcUrl = poolMetaData.get().getJdbcUrlMetaData().getJdbcUrl(dataSource.get());
                int endIndex = jdbcUrl.indexOf("?");
                jdbcUrl = jdbcUrl.substring(0, endIndex);
                String user = poolMetaData.get().getJdbcUrlMetaData().getUsername(dataSource.get());
                String password = poolMetaData.get().getJdbcUrlMetaData().getPassword(dataSource.get());
                String dataSourceClassName = getDataSourceClassNameByJdbcUrl(jdbcUrl);
                generateTransactionProps(jdbcUrl, user, password, dataSourceClassName, props);
            }
        }
    }
    
    private String getDataSourceClassNameByJdbcUrl(final String jdbcUrl) {
        DatabaseType type = DatabaseTypeRegistry.getDatabaseTypeByURL(jdbcUrl);
        if (type instanceof MySQLDatabaseType || type instanceof OpenGaussDatabaseType || type instanceof PostgreSQLDatabaseType) {
            if (type.getDataSourceClassName().isPresent()) {
                return type.getDataSourceClassName().get();
            }
        }
        throw new UnsupportedOperationException(String.format("Cannot support database type: `%s` as narayana recovery store", type));
    }
    
    private void generateTransactionProps(final String recoveryStoreUrl, final String recoveryStoreUser, final String recoveryStorePassword, final String recoveryStoreDataSource,
                                          final Properties props) {
        props.setProperty("recoveryStoreUrl", recoveryStoreUrl);
        props.setProperty("recoveryStoreUser", recoveryStoreUser);
        props.setProperty("recoveryStorePassword", recoveryStorePassword);
        props.setProperty("recoveryStoreDataSource", recoveryStoreDataSource);
    }
    
    @Override
    public String getType() {
        return "Narayana";
    }
}
