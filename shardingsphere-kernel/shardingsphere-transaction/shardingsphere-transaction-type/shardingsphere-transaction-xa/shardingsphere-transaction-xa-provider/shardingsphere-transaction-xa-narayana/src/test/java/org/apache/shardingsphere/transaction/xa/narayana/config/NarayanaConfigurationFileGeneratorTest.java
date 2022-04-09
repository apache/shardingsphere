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
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTAActionStatusServiceXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceId;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class NarayanaConfigurationFileGeneratorTest {
    
    private final NarayanaConfigurationFileGenerator narayanaConfigurationFileGenerator = new NarayanaConfigurationFileGenerator();
    
    private TransactionRule transactionRule;
    
    private String jdbcAccess;
    
    @Mock
    private InstanceContext instanceContext;
    
    @Before
    public void setUp() {
        TransactionRuleConfiguration transactionRuleConfiguration = createTransactionRuleConfiguration();
        transactionRule = new TransactionRule(transactionRuleConfiguration);
        jdbcAccess = "com.arjuna.ats.internal.arjuna.objectstore.jdbc.accessors.DynamicDataSourceJDBCAccess;ClassName=com.mysql.jdbc.jdbc2.optional.MysqlDataSource;"
                + "URL=jdbc:mysql://127.0.0.1:3306/jbossts;User=root;Password=12345678";
        InstanceId instanceId = mock(InstanceId.class);
        when(instanceId.getId()).thenReturn("127.0.0.1@3307");
        InstanceDefinition instanceDefinition = mock(InstanceDefinition.class);
        when(instanceDefinition.getInstanceId()).thenReturn(instanceId);
        ComputeNodeInstance computeNodeInstance = mock(ComputeNodeInstance.class);
        when(computeNodeInstance.getInstanceDefinition()).thenReturn(instanceDefinition);
        when(computeNodeInstance.getXaRecoveryId()).thenReturn("127.0.0.1@3307");
        when(instanceContext.getInstance()).thenReturn(computeNodeInstance);
    }
    
    private TransactionRuleConfiguration createTransactionRuleConfiguration() {
        Properties props = new Properties();
        props.setProperty("recoveryStoreUrl", "jdbc:mysql://127.0.0.1:3306/jbossts");
        props.setProperty("recoveryStoreDataSource", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        props.setProperty("recoveryStoreUser", "root");
        props.setProperty("recoveryStorePassword", "12345678");
        return new TransactionRuleConfiguration("XA", "Narayana", props);
    }
    
    @Test
    public void assertNarayanaConfigurationFileGenerator() throws JAXBException, FileNotFoundException {
        narayanaConfigurationFileGenerator.generateFile(transactionRule.getProps(), instanceContext);
        JAXBContext jaxbContext = JAXBContext.newInstance(NarayanaConfiguration.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        InputStream inputStream = new FileInputStream(new File(ClassLoader.getSystemResource("").getPath(), "jbossts-properties.xml"));
        NarayanaConfiguration narayanaConfiguration = (NarayanaConfiguration) unmarshaller.unmarshal(inputStream);
        assertThat(narayanaConfiguration.getEntries().size(), is(27));
        assertCommitOnePhase(narayanaConfiguration);
        assertTransactionSync(narayanaConfiguration);
        assertNodeIdentifier(narayanaConfiguration);
        assertXaRecoveryNodes(narayanaConfiguration);
        assertXaResourceOrphanFilterClassNames(narayanaConfiguration);
        assertSocketProcessIdPort(narayanaConfiguration);
        assertRecoveryModuleClassNames(narayanaConfiguration);
        assertExpiryScannerClassNames(narayanaConfiguration);
        assertRecoveryPort(narayanaConfiguration);
        assertTransactionStatusManagerPort(narayanaConfiguration);
        assertRecoveryListener(narayanaConfiguration);
        assertRecoveryBackoffPeriod(narayanaConfiguration);
        assertObjectStoreType(narayanaConfiguration);
        assertJdbcAccess(narayanaConfiguration);
        assertTablePrefix(narayanaConfiguration);
        assertDropTable(narayanaConfiguration);
        assertStateStoreJdbcAccess(narayanaConfiguration);
        assertStateStoreObjectStoreType(narayanaConfiguration);
        assertStateStoreTablePrefix(narayanaConfiguration);
        assertStateStoreDropTable(narayanaConfiguration);
        assertCommunicationStoreObjectStoreType(narayanaConfiguration);
        assertCommunicationStoreJdbcAccess(narayanaConfiguration);
        assertCommunicationStoreTablePrefix(narayanaConfiguration);
        assertCommunicationStoreDropTable(narayanaConfiguration);
    }
    
    private void assertCommitOnePhase(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "CoordinatorEnvironmentBean.commitOnePhase".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("YES"));
    }
    
    private void assertTransactionSync(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.transactionSync".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("NO"));
    }
    
    private void assertNodeIdentifier(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "CoreEnvironmentBean.nodeIdentifier".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("127.0.0.1@3307"));
    }
    
    private void assertXaRecoveryNodes(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "JTAEnvironmentBean.xaRecoveryNodes".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("127.0.0.1@3307"));
    }
    
    private void assertXaResourceOrphanFilterClassNames(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "JTAEnvironmentBean.xaResourceOrphanFilterClassNames".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(3));
        assertTrue(entry.get().getValue().contains(JTATransactionLogXAResourceOrphanFilter.class.getName()));
        assertTrue(entry.get().getValue().contains(JTANodeNameXAResourceOrphanFilter.class.getName()));
        assertTrue(entry.get().getValue().contains(JTAActionStatusServiceXAResourceOrphanFilter.class.getName()));
    }
    
    private void assertSocketProcessIdPort(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "CoreEnvironmentBean.socketProcessIdPort".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("0"));
    }
    
    private void assertRecoveryModuleClassNames(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "RecoveryEnvironmentBean.recoveryModuleClassNames".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(2));
        assertTrue(entry.get().getValue().contains(AtomicActionRecoveryModule.class.getName()));
        assertTrue(entry.get().getValue().contains(XARecoveryModule.class.getName()));
    }
    
    private void assertExpiryScannerClassNames(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "RecoveryEnvironmentBean.expiryScannerClassNames".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(ExpiredTransactionStatusManagerScanner.class.getName()));
    }
    
    private void assertRecoveryPort(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "RecoveryEnvironmentBean.recoveryPort".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("4712"));
    }
    
    private void assertTransactionStatusManagerPort(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "RecoveryEnvironmentBean.transactionStatusManagerPort".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("0"));
    }
    
    private void assertRecoveryListener(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "RecoveryEnvironmentBean.recoveryListener".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("NO"));
    }
    
    private void assertRecoveryBackoffPeriod(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "RecoveryEnvironmentBean.recoveryBackoffPeriod".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("1"));
    }
    
    private void assertObjectStoreType(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.objectStoreType".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(JDBCStore.class.getName()));
    }
    
    private void assertJdbcAccess(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.jdbcAccess".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(jdbcAccess));
    }
    
    private void assertTablePrefix(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.tablePrefix".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("Action"));
    }
    
    private void assertDropTable(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.dropTable".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("true"));
    }
    
    private void assertStateStoreJdbcAccess(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.stateStore.jdbcAccess".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(jdbcAccess));
    }
    
    private void assertStateStoreObjectStoreType(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.stateStore.objectStoreType".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(JDBCStore.class.getName()));
    }
    
    private void assertStateStoreTablePrefix(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.stateStore.tablePrefix".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("stateStore"));
    }
    
    private void assertStateStoreDropTable(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.stateStore.dropTable".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("true"));
    }
    
    private void assertCommunicationStoreObjectStoreType(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.communicationStore.objectStoreType".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(JDBCStore.class.getName()));
    }
    
    private void assertCommunicationStoreJdbcAccess(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.communicationStore.jdbcAccess".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(jdbcAccess));
    }
    
    private void assertCommunicationStoreTablePrefix(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.communicationStore.tablePrefix".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("Communication"));
    }
    
    private void assertCommunicationStoreDropTable(final NarayanaConfiguration configuration) {
        Optional<NarayanaConfigEntry> entry = configuration.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.communicationStore.dropTable".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("true"));
    }
}
