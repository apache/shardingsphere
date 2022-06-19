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
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class NarayanaConfigurationFileGeneratorTest {
    
    private final NarayanaConfigurationFileGenerator narayanaConfigFileGenerator = new NarayanaConfigurationFileGenerator();
    
    private TransactionRule transactionRule;
    
    private String jdbcAccess;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private InstanceContext instanceContext;
    
    @Before
    public void setUp() {
        transactionRule = new TransactionRule(new TransactionRuleConfiguration("XA", "Narayana", createProperties()), Collections.emptyMap());
        jdbcAccess = "com.arjuna.ats.internal.arjuna.objectstore.jdbc.accessors.DynamicDataSourceJDBCAccess;ClassName=com.mysql.jdbc.jdbc2.optional.MysqlDataSource;"
                + "URL=jdbc:mysql://127.0.0.1:3306/jbossts;User=root;Password=12345678";
        when(instanceContext.getInstance().getInstanceDefinition().getInstanceId()).thenReturn("127.0.0.1@3307");
        when(instanceContext.getInstance().getXaRecoveryIds()).thenReturn(Collections.singletonList("127.0.0.1@3307"));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("recoveryStoreUrl", "jdbc:mysql://127.0.0.1:3306/jbossts");
        result.setProperty("recoveryStoreDataSource", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        result.setProperty("recoveryStoreUser", "root");
        result.setProperty("recoveryStorePassword", "12345678");
        return result;
    }
    
    @Test
    public void assertNarayanaConfigurationFileGenerator() throws JAXBException, FileNotFoundException {
        narayanaConfigFileGenerator.generateFile(transactionRule.getProps(), instanceContext);
        JAXBContext jaxbContext = JAXBContext.newInstance(NarayanaConfiguration.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        InputStream inputStream = new FileInputStream(new File(ClassLoader.getSystemResource("").getPath(), "jbossts-properties.xml"));
        NarayanaConfiguration narayanaConfig = (NarayanaConfiguration) unmarshaller.unmarshal(inputStream);
        assertThat(narayanaConfig.getEntries().size(), is(32));
        assertCommitOnePhase(narayanaConfig);
        assertTransactionSync(narayanaConfig);
        assertNodeIdentifier(narayanaConfig);
        assertXaRecoveryNodes(narayanaConfig);
        assertXaResourceOrphanFilterClassNames(narayanaConfig);
        assertSocketProcessIdPort(narayanaConfig);
        assertRecoveryModuleClassNames(narayanaConfig);
        assertExpiryScannerClassNames(narayanaConfig);
        assertRecoveryPort(narayanaConfig);
        assertTransactionStatusManagerPort(narayanaConfig);
        assertRecoveryListener(narayanaConfig);
        assertRecoveryBackoffPeriod(narayanaConfig);
        assertDefaultTimeout(narayanaConfig);
        assertExpiryScanInterval(narayanaConfig);
        assertPeriodicRecoveryPeriod(narayanaConfig);
        assertObjectStoreType(narayanaConfig);
        assertJdbcAccess(narayanaConfig);
        assertTablePrefix(narayanaConfig);
        assertDropTable(narayanaConfig);
        assertCreateTable(narayanaConfig);
        assertStateStoreJdbcAccess(narayanaConfig);
        assertStateStoreObjectStoreType(narayanaConfig);
        assertStateStoreTablePrefix(narayanaConfig);
        assertStateStoreDropTable(narayanaConfig);
        assertStateStoreCreateTable(narayanaConfig);
        assertCommunicationStoreObjectStoreType(narayanaConfig);
        assertCommunicationStoreJdbcAccess(narayanaConfig);
        assertCommunicationStoreTablePrefix(narayanaConfig);
        assertCommunicationStoreCreateTable(narayanaConfig);
        assertCommunicationStoreDropTable(narayanaConfig);
    }
    
    private void assertCommitOnePhase(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "CoordinatorEnvironmentBean.commitOnePhase".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(Boolean.TRUE.toString()));
    }
    
    private void assertTransactionSync(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.transactionSync".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(Boolean.FALSE.toString()));
    }
    
    private void assertNodeIdentifier(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "CoreEnvironmentBean.nodeIdentifier".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("127.0.0.1@3307"));
    }
    
    private void assertXaRecoveryNodes(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "JTAEnvironmentBean.xaRecoveryNodes".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("127.0.0.1@3307"));
    }
    
    private void assertXaResourceOrphanFilterClassNames(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "JTAEnvironmentBean.xaResourceOrphanFilterClassNames".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(3));
        assertTrue(entry.get().getValue().contains(JTATransactionLogXAResourceOrphanFilter.class.getName()));
        assertTrue(entry.get().getValue().contains(JTANodeNameXAResourceOrphanFilter.class.getName()));
        assertTrue(entry.get().getValue().contains(JTAActionStatusServiceXAResourceOrphanFilter.class.getName()));
    }
    
    private void assertSocketProcessIdPort(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "CoreEnvironmentBean.socketProcessIdPort".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("0"));
    }
    
    private void assertRecoveryModuleClassNames(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "RecoveryEnvironmentBean.recoveryModuleClassNames".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(2));
        assertTrue(entry.get().getValue().contains(AtomicActionRecoveryModule.class.getName()));
        assertTrue(entry.get().getValue().contains(XARecoveryModule.class.getName()));
    }
    
    private void assertExpiryScannerClassNames(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "RecoveryEnvironmentBean.expiryScannerClassNames".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(ExpiredTransactionStatusManagerScanner.class.getName()));
    }
    
    private void assertRecoveryPort(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "RecoveryEnvironmentBean.recoveryPort".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("4712"));
    }
    
    private void assertTransactionStatusManagerPort(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "RecoveryEnvironmentBean.transactionStatusManagerPort".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("0"));
    }
    
    private void assertRecoveryListener(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "RecoveryEnvironmentBean.recoveryListener".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(Boolean.FALSE.toString()));
    }
    
    private void assertRecoveryBackoffPeriod(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "RecoveryEnvironmentBean.recoveryBackoffPeriod".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("1"));
    }
    
    private void assertDefaultTimeout(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "CoordinatorEnvironmentBean.defaultTimeout".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("180"));
    }
    
    private void assertExpiryScanInterval(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "RecoveryEnvironmentBean.expiryScanInterval".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("12"));
    }
    
    private void assertPeriodicRecoveryPeriod(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "RecoveryEnvironmentBean.periodicRecoveryPeriod".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("120"));
    }
    
    private void assertObjectStoreType(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.objectStoreType".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(JDBCStore.class.getName()));
    }
    
    private void assertJdbcAccess(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.jdbcAccess".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(jdbcAccess));
    }
    
    private void assertTablePrefix(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.tablePrefix".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("Action"));
    }
    
    private void assertCreateTable(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.createTable".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(Boolean.TRUE.toString()));
    }
    
    private void assertDropTable(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.dropTable".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(Boolean.FALSE.toString()));
    }
    
    private void assertStateStoreJdbcAccess(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.stateStore.jdbcAccess".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(jdbcAccess));
    }
    
    private void assertStateStoreObjectStoreType(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.stateStore.objectStoreType".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(JDBCStore.class.getName()));
    }
    
    private void assertStateStoreTablePrefix(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.stateStore.tablePrefix".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("stateStore"));
    }
    
    private void assertStateStoreCreateTable(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.stateStore.createTable".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(Boolean.TRUE.toString()));
    }
    
    private void assertStateStoreDropTable(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.stateStore.dropTable".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(Boolean.FALSE.toString()));
    }
    
    private void assertCommunicationStoreObjectStoreType(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.communicationStore.objectStoreType".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(JDBCStore.class.getName()));
    }
    
    private void assertCommunicationStoreJdbcAccess(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.communicationStore.jdbcAccess".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(jdbcAccess));
    }
    
    private void assertCommunicationStoreTablePrefix(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.communicationStore.tablePrefix".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains("Communication"));
    }
    
    private void assertCommunicationStoreCreateTable(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.communicationStore.createTable".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(Boolean.TRUE.toString()));
    }
    
    private void assertCommunicationStoreDropTable(final NarayanaConfiguration narayanaConfig) {
        Optional<NarayanaConfigEntry> entry = narayanaConfig.getEntries().stream().filter(each -> "ObjectStoreEnvironmentBean.communicationStore.dropTable".equals(each.getKey())).findFirst();
        assertTrue(entry.isPresent());
        assertThat(entry.get().getValue().size(), is(1));
        assertTrue(entry.get().getValue().contains(Boolean.FALSE.toString()));
    }
}
