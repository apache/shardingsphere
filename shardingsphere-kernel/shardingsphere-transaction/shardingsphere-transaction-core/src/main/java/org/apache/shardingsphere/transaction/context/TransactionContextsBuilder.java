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

package org.apache.shardingsphere.transaction.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.rule.builder.DefaultTransactionRuleConfigurationBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Transaction contexts builder.
 */
@Slf4j
@RequiredArgsConstructor
public final class TransactionContextsBuilder {

    private final Map<String, ShardingSphereMetaData> metaDataMap;
    
    private final Collection<ShardingSphereRule> globalRules;

    private final String instanceId;

    /**
     * Build transaction contexts.
     * 
     * @return transaction contexts
     */
    public TransactionContexts build() {
        Map<String, ShardingSphereTransactionManagerEngine> engines = new HashMap<>(metaDataMap.keySet().size(), 1);
        TransactionRule transactionRule = getTransactionRule();
        generateNarayanaConfig(transactionRule);
        for (String each : metaDataMap.keySet()) {
            ShardingSphereTransactionManagerEngine engine = new ShardingSphereTransactionManagerEngine();
            ShardingSphereResource resource = metaDataMap.get(each).getResource();
            engine.init(resource.getDatabaseType(), resource.getDataSources(), transactionRule);
            engines.put(each, engine);
        }
        return new TransactionContexts(engines);
    }
    
    private TransactionRule getTransactionRule() {
        Optional<TransactionRule> transactionRule = globalRules.stream().filter(each -> each instanceof TransactionRule).map(each -> (TransactionRule) each).findFirst();
        return transactionRule.orElseGet(() -> new TransactionRule(new DefaultTransactionRuleConfigurationBuilder().build()));
    }
    
    private void generateNarayanaConfig(final TransactionRule transactionRule) {
        Map<Object, Object> result;
        if (transactionRule.getDefaultType() == TransactionType.XA && transactionRule.getProviderType().equalsIgnoreCase("Narayana")) {
            result = generateDefaultNarayanaConfig();
            if (null != transactionRule.getProps()) {
                swapJdbcStore(transactionRule, result);
            }
        } else {
            return;
        }
        String value = narayanaConfigMapToXml((LinkedHashMap<Object, Object>) result);
        String path = ClassLoader.getSystemResource("").getPath();
        System.out.println(path);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(path, "jbossts-properties.xml"))) {
            bufferedWriter.write(value);
            bufferedWriter.flush();
        } catch (final IOException ex) {
            log.error("generate narayana config file failed.");
        }
    }

    private void swapJdbcStore(final TransactionRule transactionRule, final Map<Object, Object> config) {
        Object host = transactionRule.getProps().get("host");
        Object port = transactionRule.getProps().get("port");
        Object user = transactionRule.getProps().getProperty("user");
        Object password = transactionRule.getProps().getProperty("password");
        Object databaseName = transactionRule.getProps().getProperty("databaseName");
        if (null != host && null != port && null != user && null != password && null != databaseName) {
            String jdbcAccessPatten = "com.arjuna.ats.internal.arjuna.objectstore.jdbc.accessors.DynamicDataSourceJDBCAccess;"
                    + "ClassName=com.mysql.cj.jdbc.MysqlDataSource;URL=jdbc:mysql://%s:%d/%s;User=%s;Password=%s";
            String jdbcAccess = String.format(jdbcAccessPatten, host, port, databaseName, user, password);
            config.put("ObjectStoreEnvironmentBean.objectStoreType", "com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCStore");
            config.put("ObjectStoreEnvironmentBean.jdbcAccess", jdbcAccess);
            config.put("ObjectStoreEnvironmentBean.tablePrefix", "Action");
            config.put("ObjectStoreEnvironmentBean.dropTable", true);
            config.put("ObjectStoreEnvironmentBean.stateStore.objectStoreType", "com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCStore");
            config.put("ObjectStoreEnvironmentBean.stateStore.jdbcAccess", jdbcAccess);
            config.put("ObjectStoreEnvironmentBean.stateStore.tablePrefix", "stateStore");
            config.put("ObjectStoreEnvironmentBean.stateStore.dropTable", true);
            config.put("ObjectStoreEnvironmentBean.communicationStore.objectStoreType", "com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCStore");
            config.put("ObjectStoreEnvironmentBean.communicationStore.jdbcAccess", jdbcAccess);
            config.put("ObjectStoreEnvironmentBean.communicationStore.tablePrefix", "Communication");
            config.put("ObjectStoreEnvironmentBean.communicationStore.dropTable", true);
        }
    }

    private Map<Object, Object> generateDefaultNarayanaConfig() {
        Map<Object, Object> result = new LinkedHashMap<>();
        result.put("CoordinatorEnvironmentBean.commitOnePhase", "YES");
        result.put("ObjectStoreEnvironmentBean.transactionSync", "ON");
        result.put("CoreEnvironmentBean.nodeIdentifier", null == instanceId ? 1 : instanceId);
        result.put("JTAEnvironmentBean.xaRecoveryNodes", null == instanceId ? 1 : instanceId);
        List<String> xaResourceOrphanFilterClassNames = new LinkedList<>();
        xaResourceOrphanFilterClassNames.add("com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter");
        xaResourceOrphanFilterClassNames.add("com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter");
        xaResourceOrphanFilterClassNames.add("com.arjuna.ats.internal.jta.recovery.arjunacore.JTAActionStatusServiceXAResourceOrphanFilter");
        result.put("JTAEnvironmentBean.xaResourceOrphanFilterClassNames", xaResourceOrphanFilterClassNames);
        result.put("CoreEnvironmentBean.socketProcessIdPort", 0);
        List<String> recoveryModuleClassNames = new LinkedList<>();
        recoveryModuleClassNames.add("com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule");
        recoveryModuleClassNames.add("com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule");
        result.put("RecoveryEnvironmentBean.recoveryModuleClassNames", recoveryModuleClassNames);
        result.put("RecoveryEnvironmentBean.expiryScannerClassNames", "com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner");
        result.put("RecoveryEnvironmentBean.recoveryPort", 4712);
        result.put("RecoveryEnvironmentBean.recoveryAddress", null);
        result.put("RecoveryEnvironmentBean.transactionStatusManagerPort", 0);
        result.put("RecoveryEnvironmentBean.transactionStatusManagerAddress", null);
        result.put("RecoveryEnvironmentBean.recoveryListener", "NO");
        result.put("RecoveryEnvironmentBean.recoveryBackoffPeriod", 1);
        return result;
    }

    private String narayanaConfigMapToXml(final Map<Object, Object> sortedMap) {
        StringBuffer sb = new StringBuffer("<properties>");
        Iterator iterator = sortedMap.keySet().iterator();
        while (iterator.hasNext()) {
            sb.append("\n\t");
            Object key = iterator.next();
            Object value = sortedMap.get(key);
            sb.append(String.format("<entry key=\"%s\">", key));
            if (value instanceof List) {
                for (Object i : (List) value) {
                    sb.append("\n\t\t");
                    sb.append(i);
                }
                sb.append("\n\t</entry>");
            } else {
                if (null != value) {
                    sb.append(value);
                }
                sb.append("</entry>");
            }
        }
        sb.append("\n");
        sb.append("</properties>");
        return sb.toString();
    }
}
