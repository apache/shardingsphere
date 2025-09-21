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

package org.apache.shardingsphere.test.e2e.env.container.governance;

import org.apache.shardingsphere.test.e2e.env.container.DockerE2EContainer;
import org.apache.shardingsphere.test.e2e.env.container.governance.option.GovernanceContainerOption;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

/**
 * Governance container.
 */
public final class GovernanceContainer extends DockerE2EContainer {
    
    private final GovernanceContainerOption option;
    
    public GovernanceContainer(final GovernanceContainerOption option) {
        super(option.getType().toString().toLowerCase(), option.getDefaultImageName());
        this.option = option;
        setWaitStrategy(new LogMessageWaitStrategy().withRegEx(option.getSuccessLogPattern()));
        withExposedPorts(option.getPort());
    }
    
    /**
     * Get server list.
     *
     * @return server list
     */
    public String getServerLists() {
        return getHost() + ":" + getMappedPort(option.getPort());
    }
    
    @Override
    public String getAbbreviation() {
        return option.getAbbreviation();
    }
}
