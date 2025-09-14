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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.type.docker.impl;

import com.google.common.base.Strings;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.type.docker.DockerStorageContainer;

/**
 * OpenGauss container.
 */
public final class OpenGaussContainer extends DockerStorageContainer {
    
    public static final int EXPOSED_PORT = 5432;
    
    public OpenGaussContainer(final String containerImage, final StorageContainerConfiguration storageContainerConfig) {
        super(TypedSPILoader.getService(DatabaseType.class, "openGauss"), Strings.isNullOrEmpty(containerImage) ? "opengauss/opengauss:3.1.0" : containerImage, storageContainerConfig);
    }
    
    @Override
    public int getExposedPort() {
        return EXPOSED_PORT;
    }
    
    @Override
    public int getMappedPort() {
        return getMappedPort(EXPOSED_PORT);
    }
}
