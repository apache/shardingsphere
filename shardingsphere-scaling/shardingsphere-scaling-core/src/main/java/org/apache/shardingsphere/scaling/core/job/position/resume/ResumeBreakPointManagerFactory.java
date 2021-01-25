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

package org.apache.shardingsphere.scaling.core.job.position.resume;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.scaling.core.service.RegistryRepositoryHolder;

/**
 * Resume from break-point manager factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResumeBreakPointManagerFactory {
    
    private static Class<? extends ResumeBreakPointManager> clazz = FileSystemResumeBreakPointManager.class;
    
    static {
        ShardingSphereServiceLoader.register(RegistryRepository.class);
        ShardingSphereServiceLoader.register(ConfigurationRepository.class);
        if (RegistryRepositoryHolder.isAvailable()) {
            clazz = RegistryRepositoryResumeBreakPointManager.class;
        }
    }
    
    /**
     * New resume from break-point manager instance.
     *
     * @param databaseType database type
     * @param taskPath task path for persist data.
     * @return resume from break-point manager
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static ResumeBreakPointManager newInstance(final String databaseType, final String taskPath) {
        return clazz.getConstructor(String.class, String.class).newInstance(databaseType, taskPath);
    }
}
