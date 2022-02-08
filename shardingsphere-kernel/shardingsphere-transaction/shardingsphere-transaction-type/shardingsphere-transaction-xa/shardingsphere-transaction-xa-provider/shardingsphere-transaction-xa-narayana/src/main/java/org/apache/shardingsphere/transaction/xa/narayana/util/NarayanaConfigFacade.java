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

package org.apache.shardingsphere.transaction.xa.narayana.util;

import com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCStore;
import com.arjuna.ats.internal.arjuna.objectstore.jdbc.accessors.DynamicDataSourceJDBCAccess;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTAActionStatusServiceXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.spi.TransactionConfigFacade;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public class NarayanaConfigFacade implements TransactionConfigFacade {

    @Override
    public void generate(final TransactionRule transactionRule, final String instanceId) {
        if (null == transactionRule) {
            return;
        }
        Map<Object, Object> content;
        content = generateDefaultNarayanaConfig(instanceId);
        if (null != transactionRule.getProps()) {
            swapJdbcStore(transactionRule, content);
        }
        String value = narayanaConfigMapToXml(content);
        String path = ClassLoader.getSystemResource("").getPath();
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(path, "jbossts-properties.xml"))) {
            bufferedWriter.write(value);
            bufferedWriter.flush();
        } catch (final IOException ex) {
            log.error("generate narayana config file failed.");
        }
    }

    @Override
    public String getTransactionType() {
        return "Narayana";
    }

    private static void swapJdbcStore(final TransactionRule transactionRule, final Map<Object, Object> config) {
        Object host = transactionRule.getProps().get("host");
        Object port = transactionRule.getProps().get("port");
        Object user = transactionRule.getProps().getProperty("user");
        Object password = transactionRule.getProps().getProperty("password");
        Object databaseName = transactionRule.getProps().getProperty("databaseName");
        if (null != host && null != port && null != user && null != password && null != databaseName) {
            String jdbcAccessPatten = DynamicDataSourceJDBCAccess.class.getName()
                    + ";ClassName=com.mysql.cj.jdbc.MysqlDataSource;URL=jdbc:mysql://%s:%d/%s;User=%s;Password=%s";
            String jdbcAccess = String.format(jdbcAccessPatten, host, port, databaseName, user, password);
            config.put("ObjectStoreEnvironmentBean.objectStoreType", JDBCStore.class.getName());
            config.put("ObjectStoreEnvironmentBean.jdbcAccess", jdbcAccess);
            config.put("ObjectStoreEnvironmentBean.tablePrefix", "Action");
            config.put("ObjectStoreEnvironmentBean.dropTable", true);
            config.put("ObjectStoreEnvironmentBean.stateStore.objectStoreType", JDBCStore.class.getName());
            config.put("ObjectStoreEnvironmentBean.stateStore.jdbcAccess", jdbcAccess);
            config.put("ObjectStoreEnvironmentBean.stateStore.tablePrefix", "stateStore");
            config.put("ObjectStoreEnvironmentBean.stateStore.dropTable", true);
            config.put("ObjectStoreEnvironmentBean.communicationStore.objectStoreType", JDBCStore.class.getName());
            config.put("ObjectStoreEnvironmentBean.communicationStore.jdbcAccess", jdbcAccess);
            config.put("ObjectStoreEnvironmentBean.communicationStore.tablePrefix", "Communication");
            config.put("ObjectStoreEnvironmentBean.communicationStore.dropTable", true);
        }
    }

    private static Map<Object, Object> generateDefaultNarayanaConfig(final String instanceId) {
        Map<Object, Object> result = new LinkedHashMap<>(32, 1);
        result.put("CoordinatorEnvironmentBean.commitOnePhase", "YES");
        result.put("ObjectStoreEnvironmentBean.transactionSync", "ON");
        result.put("CoreEnvironmentBean.nodeIdentifier", null == instanceId ? 1 : instanceId);
        result.put("JTAEnvironmentBean.xaRecoveryNodes", null == instanceId ? 1 : instanceId);
        Collection<String> xaResourceOrphanFilterClassNames = new LinkedList<>();
        xaResourceOrphanFilterClassNames.add(JTATransactionLogXAResourceOrphanFilter.class.getName());
        xaResourceOrphanFilterClassNames.add(JTANodeNameXAResourceOrphanFilter.class.getName());
        xaResourceOrphanFilterClassNames.add(JTAActionStatusServiceXAResourceOrphanFilter.class.getName());
        result.put("JTAEnvironmentBean.xaResourceOrphanFilterClassNames", xaResourceOrphanFilterClassNames);
        result.put("CoreEnvironmentBean.socketProcessIdPort", 0);
        Collection<String> recoveryModuleClassNames = new LinkedList<>();
        recoveryModuleClassNames.add(AtomicActionRecoveryModule.class.getName());
        recoveryModuleClassNames.add(XARecoveryModule.class.getName());
        result.put("RecoveryEnvironmentBean.recoveryModuleClassNames", recoveryModuleClassNames);
        result.put("RecoveryEnvironmentBean.expiryScannerClassNames", ExpiredTransactionStatusManagerScanner.class.getName());
        result.put("RecoveryEnvironmentBean.recoveryPort", 4712);
        result.put("RecoveryEnvironmentBean.recoveryAddress", null);
        result.put("RecoveryEnvironmentBean.transactionStatusManagerPort", 0);
        result.put("RecoveryEnvironmentBean.transactionStatusManagerAddress", null);
        result.put("RecoveryEnvironmentBean.recoveryListener", "NO");
        result.put("RecoveryEnvironmentBean.recoveryBackoffPeriod", 1);
        return result;
    }

    private static String narayanaConfigMapToXml(final Map<Object, Object> config) {
        StringBuilder result = new StringBuilder("<properties>");
        for (Object each : config.keySet()) {
            result.append("\n\t");
            Object value = config.get(each);
            result.append(String.format("<entry key=\"%s\">", each));
            if (value instanceof List) {
                for (Object i : (List) value) {
                    result.append("\n\t\t");
                    result.append(i);
                }
                result.append("\n\t</entry>");
            } else {
                if (null != value) {
                    result.append(value);
                }
                result.append("</entry>");
            }
        }
        result.append("\n");
        result.append("</properties>");
        return result.toString();
    }
}
