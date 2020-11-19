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
 *
 */

package org.apache.shardingsphere.agent.bootstrap;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import org.apache.shardingsphere.agent.core.config.AgentConfiguration;
import org.apache.shardingsphere.agent.core.config.AgentConfigurationLoader;
import org.apache.shardingsphere.agent.core.utils.SingletonHolder;

/**
 * ShardingSphere agent.
 */
public class ShardingSphereAgent {
    
    /**
     * Premain for instrumentation.
     *
     * @param agentArgs agent args
     * @param instrumentation instrumentation
     * @throws IOException IOException
     */
    public static void premain(final String agentArgs, final Instrumentation instrumentation) throws IOException {
        AgentConfiguration agentConfiguration = AgentConfigurationLoader.load();
        SingletonHolder.INSTANCE.put(agentConfiguration);
    }
}
