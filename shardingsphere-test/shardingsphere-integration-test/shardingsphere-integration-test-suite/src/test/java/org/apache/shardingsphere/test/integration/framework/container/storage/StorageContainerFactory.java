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

package org.apache.shardingsphere.test.integration.framework.container.storage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.integration.framework.container.storage.impl.H2Container;
import org.apache.shardingsphere.test.integration.framework.container.storage.impl.MySQLContainer;
import org.apache.shardingsphere.test.integration.framework.container.storage.impl.PostgreSQLContainer;
import org.apache.shardingsphere.test.integration.framework.logging.ContainerLogs;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;
import org.testcontainers.containers.Network;

import java.util.Collections;

/**
 * Storage container factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageContainerFactory {
    
    /**
     * Create new instance of storage container.
     * 
     * @param parameterizedArray parameterized array
     * @param network network
     * @param suiteName suite name
     * @return new instance of storage container
     */
    public static StorageContainer newInstance(final ParameterizedArray parameterizedArray, final Network network, final String suiteName) {
        StorageContainer result;
        String databaseType = parameterizedArray.getDatabaseType().getName();
        switch (databaseType) {
            case "MySQL":
                result = new MySQLContainer(parameterizedArray);
                break;
            case "PostgreSQL" :
                result = new PostgreSQLContainer(parameterizedArray);
                break;
            case "H2":
                result = new H2Container(parameterizedArray);
                break;
            default:
                throw new RuntimeException("Unknown storage type " + parameterizedArray.getDatabaseType());
        }
        result.setNetwork(network);
        result.setNetworkAliases(Collections.singletonList(databaseType.toLowerCase() + "." + parameterizedArray.getScenario() + ".host"));
        result.withLogConsumer(ContainerLogs.newConsumer(String.join("-", suiteName, result.getName())));
        return result;
    }
}
