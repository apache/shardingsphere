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

package org.apache.shardingsphere.governance.core.registry.instance;

import com.google.common.base.Joiner;
import org.apache.shardingsphere.governance.core.utils.IpUtils;

import java.lang.management.ManagementFactory;
import java.util.UUID;

/**
 * Governance instance.
 */
public final class GovernanceInstance {
    
    private static final String DELIMITER = "@";
    
    private static final GovernanceInstance INSTANCE = new GovernanceInstance();
    
    private final String instanceId;
    
    private GovernanceInstance() {
        instanceId = Joiner.on(DELIMITER).join(IpUtils.getIp(), ManagementFactory.getRuntimeMXBean().getName().split(DELIMITER)[0], UUID.randomUUID().toString());
    }
    
    /**
     * Getter for instance ID.
     *
     * @return  instance ID
     */
    public String getInstanceId() {
        return instanceId;
    }
    
    /**
     * Get instance.
     *
     * @return  singleton instance
     */
    public static GovernanceInstance getInstance() {
        return INSTANCE;
    }
}
