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
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public final class NarayanaConfigurationFileGeneratorTest {
    
    private final NarayanaConfigurationFileGenerator narayanaConfigurationFileGenerator = new NarayanaConfigurationFileGenerator();
    
    private TransactionRule transactionRule;
    
    private String jdbcAccess;
    
    @Before
    public void setUp() {
        TransactionRuleConfiguration transactionRuleConfiguration = createTransactionRuleConfiguration();
        transactionRule = new TransactionRule(transactionRuleConfiguration);
        jdbcAccess = "com.arjuna.ats.internal.arjuna.objectstore.jdbc.accessors.DynamicDataSourceJDBCAccess;ClassName=com.mysql.jdbc.jdbc2.optional.MysqlDataSource;"
                + "URL=jdbc:mysql://127.0.0.1:3306/jbossts;User=root;Password=12345678";
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
        narayanaConfigurationFileGenerator.generateFile(transactionRule, "1");
        JAXBContext jaxbContext = JAXBContext.newInstance(NarayanaConfiguration.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        InputStream inputStream = new FileInputStream(new File(ClassLoader.getSystemResource("").getPath(), "jbossts-properties.xml"));
        NarayanaConfiguration narayanaConfiguration = (NarayanaConfiguration) unmarshaller.unmarshal(inputStream);
        assertEquals(26, narayanaConfiguration.getEntries().size());
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
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("CoordinatorEnvironmentBean.commitOnePhase")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains("YES"));
    }
    
    private void assertTransactionSync(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("ObjectStoreEnvironmentBean.transactionSync")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains("NO"));
    }
    
    private void assertNodeIdentifier(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("CoreEnvironmentBean.nodeIdentifier")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains("1"));
    }
    
    private void assertXaRecoveryNodes(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("JTAEnvironmentBean.xaRecoveryNodes")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains("1"));
    }
    
    private void assertXaResourceOrphanFilterClassNames(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("JTAEnvironmentBean.xaResourceOrphanFilterClassNames")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(3, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains(JTATransactionLogXAResourceOrphanFilter.class.getName()));
        assertTrue(entry.get().getValue().contains(JTANodeNameXAResourceOrphanFilter.class.getName()));
        assertTrue(entry.get().getValue().contains(JTAActionStatusServiceXAResourceOrphanFilter.class.getName()));
    }
    
    private void assertSocketProcessIdPort(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("CoreEnvironmentBean.socketProcessIdPort")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains("0"));
    }
    
    private void assertRecoveryModuleClassNames(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("RecoveryEnvironmentBean.recoveryModuleClassNames")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(2, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains(AtomicActionRecoveryModule.class.getName()));
        assertTrue(entry.get().getValue().contains(XARecoveryModule.class.getName()));
    }
    
    private void assertExpiryScannerClassNames(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("RecoveryEnvironmentBean.expiryScannerClassNames")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains(ExpiredTransactionStatusManagerScanner.class.getName()));
    }
    
    private void assertRecoveryPort(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("RecoveryEnvironmentBean.recoveryPort")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains("4712"));
    }
    
    private void assertTransactionStatusManagerPort(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("RecoveryEnvironmentBean.transactionStatusManagerPort")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains("0"));
    }
    
    private void assertRecoveryListener(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("RecoveryEnvironmentBean.recoveryListener")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains("NO"));
    }
    
    private void assertRecoveryBackoffPeriod(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("RecoveryEnvironmentBean.recoveryBackoffPeriod")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains("1"));
    }
    
    private void assertObjectStoreType(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("ObjectStoreEnvironmentBean.objectStoreType")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains(JDBCStore.class.getName()));
    }
    
    private void assertJdbcAccess(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("ObjectStoreEnvironmentBean.jdbcAccess")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains(jdbcAccess));
    }
    
    private void assertTablePrefix(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("ObjectStoreEnvironmentBean.tablePrefix")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains("Action"));
    }
    
    private void assertDropTable(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("ObjectStoreEnvironmentBean.dropTable")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains("true"));
    }
    
    private void assertStateStoreJdbcAccess(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("ObjectStoreEnvironmentBean.stateStore.jdbcAccess")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains(jdbcAccess));
    }
    
    private void assertStateStoreObjectStoreType(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("ObjectStoreEnvironmentBean.stateStore.objectStoreType")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains(JDBCStore.class.getName()));
    }
    
    private void assertStateStoreTablePrefix(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("ObjectStoreEnvironmentBean.stateStore.tablePrefix")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains("stateStore"));
    }
    
    private void assertStateStoreDropTable(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("ObjectStoreEnvironmentBean.stateStore.dropTable")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains("true"));
    }
    
    private void assertCommunicationStoreObjectStoreType(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("ObjectStoreEnvironmentBean.communicationStore.objectStoreType")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains(JDBCStore.class.getName()));
    }
    
    private void assertCommunicationStoreJdbcAccess(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("ObjectStoreEnvironmentBean.communicationStore.jdbcAccess")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains(jdbcAccess));
    }
    
    private void assertCommunicationStoreTablePrefix(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("ObjectStoreEnvironmentBean.communicationStore.tablePrefix")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains("Communication"));
    }
    
    private void assertCommunicationStoreDropTable(final NarayanaConfiguration configuration) {
        Optional<Entry> entry = configuration.getEntries().stream().filter(each -> each.getKey().equals("ObjectStoreEnvironmentBean.communicationStore.dropTable")).findFirst();
        assertTrue(entry.isPresent());
        assertEquals(1, entry.get().getValue().size());
        assertTrue(entry.get().getValue().contains("true"));
    }
}
