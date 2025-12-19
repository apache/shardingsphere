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

import lombok.Getter;
import org.apache.shardingsphere.test.e2e.env.runtime.EnvironmentPropertiesLoader;
import org.apache.shardingsphere.test.e2e.operation.transaction.env.enums.TransactionTestCaseRegistry;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Getter
public final class TransactionE2EEnvironment {
    
    private static final TransactionE2EEnvironment INSTANCE = new TransactionE2EEnvironment();
    
    private final Collection<String> cases;
    
    private final List<String> transactionTypes;
    
    private final List<String> xaProviders;
    
    private final Map<String, TransactionTestCaseRegistry> transactionTestCaseRegistryMap;
    
    private TransactionE2EEnvironment() {
        Properties props = EnvironmentPropertiesLoader.loadProperties();
        cases = EnvironmentPropertiesLoader.getListValue(props, "e2e.transaction.cases");
        transactionTypes = EnvironmentPropertiesLoader.getListValue(props, "e2e.transaction.types");
        xaProviders = EnvironmentPropertiesLoader.getListValue(props, "e2e.transaction.xa.providers");
        transactionTestCaseRegistryMap = Arrays.stream(TransactionTestCaseRegistry.values()).collect(Collectors.toMap(each -> each.getTestCaseClass().getName(), each -> each));
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
