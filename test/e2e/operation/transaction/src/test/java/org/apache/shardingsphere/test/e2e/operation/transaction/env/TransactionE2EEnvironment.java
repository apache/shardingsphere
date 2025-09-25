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

package org.apache.shardingsphere.test.e2e.operation.transaction.env;

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.test.e2e.operation.transaction.env.enums.TransactionTestCaseRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Getter
public final class TransactionE2EEnvironment {
    
    private static final TransactionE2EEnvironment INSTANCE = new TransactionE2EEnvironment();
    
    private final Properties props;
    
    private final Collection<String> cases;
    
    private final List<String> portBindings;
    
    private final List<String> allowTransactionTypes;
    
    private final List<String> allowXAProviders;
    
    private final Map<String, TransactionTestCaseRegistry> transactionTestCaseRegistryMap;
    
    private TransactionE2EEnvironment() {
        props = loadProperties();
        cases = splitProperty("e2e.cases");
        portBindings = splitProperty("e2e.artifact.proxy.port.bindings");
        allowTransactionTypes = splitProperty("transaction.e2e.env.transtypes");
        allowXAProviders = splitProperty("transaction.e2e.env.xa.providers");
        transactionTestCaseRegistryMap = initTransactionTestCaseRegistryMap();
    }
    
    private Properties loadProperties() {
        Properties result = new Properties();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("env/e2e-env.properties")) {
            result.load(inputStream);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
        for (String each : System.getProperties().stringPropertyNames()) {
            result.setProperty(each, System.getProperty(each));
        }
        return result;
    }
    
    private List<String> splitProperty(final String key) {
        return Arrays.stream(props.getOrDefault(key, "").toString().split(",")).filter(each -> !Strings.isNullOrEmpty(each)).map(String::trim).collect(Collectors.toList());
    }
    
    private Map<String, TransactionTestCaseRegistry> initTransactionTestCaseRegistryMap() {
        Map<String, TransactionTestCaseRegistry> result = new HashMap<>(TransactionTestCaseRegistry.values().length, 1F);
        for (TransactionTestCaseRegistry each : TransactionTestCaseRegistry.values()) {
            result.put(each.getTestCaseClass().getName(), each);
        }
        return result;
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static TransactionE2EEnvironment getInstance() {
        return INSTANCE;
    }
}
