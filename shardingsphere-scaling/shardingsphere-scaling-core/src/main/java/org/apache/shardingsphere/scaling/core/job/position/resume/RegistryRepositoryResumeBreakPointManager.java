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

import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.scaling.core.service.RegistryRepositoryHolder;

/**
 * Registry repository resume from break-point manager.
 */
public final class RegistryRepositoryResumeBreakPointManager extends AbstractResumeBreakPointManager implements ResumeBreakPointManager {
    
    private static final RegistryRepository REGISTRY_REPOSITORY = RegistryRepositoryHolder.getInstance();
    
    public RegistryRepositoryResumeBreakPointManager(final String databaseType, final String taskPath) {
        super(databaseType, taskPath);
    }
    
    @Override
    public String getPosition(final String path) {
        return REGISTRY_REPOSITORY.get(path);
    }
    
    @Override
    public void persistPosition(final String path, final String data) {
        REGISTRY_REPOSITORY.persist(path, data);
    }
}
