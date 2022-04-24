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

package org.apache.shardingsphere.test.integration.framework.container.atomic.governance;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.integration.framework.container.atomic.governance.impl.ZookeeperContainer;

/**
 * Governance container factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GovernanceContainerFactory {
    
    /**
     * Create new instance of governance container.
     * 
     * @param type governance center type
     * @return new instance of governance container
     */
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public static GovernanceContainer newInstance(final String type) {
        switch (type) {
            case "ZooKeeper":
                return new ZookeeperContainer();
            default:
                throw new RuntimeException(String.format("Governance center [%s] is unknown.", type));
        }
    }
}
