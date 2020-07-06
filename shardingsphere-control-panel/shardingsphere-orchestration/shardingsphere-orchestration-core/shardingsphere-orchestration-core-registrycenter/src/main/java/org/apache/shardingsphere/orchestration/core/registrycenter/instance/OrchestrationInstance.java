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

package org.apache.shardingsphere.orchestration.core.registrycenter.instance;

import java.util.UUID;
import org.apache.shardingsphere.orchestration.core.common.utils.IpUtils;

/**
 * Orchestration instance.
 */
public final class OrchestrationInstance {
    
    private static final String DELIMITER = "@";

    private static OrchestrationInstance instance;
    
    private final String instanceId;

    private OrchestrationInstance(final String instanceTag) {
        this.instanceId = IpUtils.getIp() + DELIMITER + instanceTag + DELIMITER + UUID.randomUUID().toString();
    }

    /**
     * initializer method which takes in instance tag and initialises
     * the singleton object.
     *
     * @param   instanceTag     the orchestration instance tag
     * @return  instance        singleton instance
     */
    public static OrchestrationInstance init(final String instanceTag) {
        if (null == instance) {
            synchronized (OrchestrationInstance.class) {
                if (null == instance) {
                    instance = new OrchestrationInstance(instanceTag);
                }
            }
        }
        return instance;
    }

    /**
     * getter for instanceId.
     *
     * @return  instanceId    instance id of the singleton
     */
    public static String getInstanceId() {
        if (null == instance) {
            throw new IllegalStateException("OrchestrationInstance not initialized!");
        }
        return instance.instanceId;
    }

}
