/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.orchestration.internal.state.instance;

import io.shardingjdbc.orchestration.internal.util.IpUtils;
import lombok.Getter;

import java.lang.management.ManagementFactory;
import java.util.UUID;

/**
 * Orchestration instance.
 * 
 * @author zhangliang
 */
@Getter
public final class OrchestrationInstance {
    
    private static final OrchestrationInstance INSTANCE = new OrchestrationInstance();
    
    /**
     * Orchestration instance id.
     */
    private String instanceId;
    
    private OrchestrationInstance() {
        String splitFlag = "@";
        instanceId = IpUtils.getIp() + splitFlag + ManagementFactory.getRuntimeMXBean().getName().split(splitFlag)[0] + splitFlag + UUID.randomUUID().toString();
    }
    
    public static OrchestrationInstance getInstance() {
        return INSTANCE;
    }
}
