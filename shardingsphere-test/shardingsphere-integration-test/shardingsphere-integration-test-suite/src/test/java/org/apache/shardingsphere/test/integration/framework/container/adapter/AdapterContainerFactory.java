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

package org.apache.shardingsphere.test.integration.framework.container.adapter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.integration.framework.container.adapter.impl.ShardingSphereJDBCContainer;
import org.apache.shardingsphere.test.integration.framework.container.adapter.impl.ShardingSphereProxyContainer;
import org.apache.shardingsphere.test.integration.framework.logging.ContainerLogs;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;
import org.testcontainers.containers.Network;

import java.util.Collections;

/**
 * Adapter container factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AdapterContainerFactory {
    
    /**
     * Create new instance of adapter container.
     *
     * @param parameterizedArray parameterized array
     * @param network network
     * @param suiteName suite name
     * @return new instance of adapter container
     */
    public static AdapterContainer newInstance(final ParameterizedArray parameterizedArray, final Network network, final String suiteName) {
        AdapterContainer result;
        switch (parameterizedArray.getAdapter()) {
            case "proxy":
                result = new ShardingSphereProxyContainer(parameterizedArray);
                break;
            case "jdbc":
                result = new ShardingSphereJDBCContainer(parameterizedArray);
                break;
            default:
                throw new RuntimeException(String.format("Adapter [%s] is unknown.", parameterizedArray.getAdapter()));
        }
        result.setNetwork(network);
        result.setNetworkAliases(Collections.singletonList("adapter"));
        result.withLogConsumer(ContainerLogs.newConsumer(String.join("-", suiteName, result.getName())));
        return result;
    }
}
