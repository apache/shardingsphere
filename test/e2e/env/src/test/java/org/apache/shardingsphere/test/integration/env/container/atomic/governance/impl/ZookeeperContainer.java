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

package org.apache.shardingsphere.test.integration.env.container.atomic.governance.impl;

import org.apache.shardingsphere.test.integration.env.container.atomic.governance.GovernanceContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

/**
 * Zookeeper container.
 */
public final class ZookeeperContainer extends GovernanceContainer {
    
    public ZookeeperContainer() {
        super("zookeeper", "zookeeper:3.6.2");
        setWaitStrategy(new LogMessageWaitStrategy().withRegEx(".*PrepRequestProcessor \\(sid:[0-9]+\\) started.*"));
        withExposedPorts(2181);
    }
    
    @Override
    public String getServerLists() {
        return getHost() + ":" + getMappedPort(2181);
    }
    
    @Override
    public String getAbbreviation() {
        return "zk";
    }
}
