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

package org.apache.shardingsphere.schedule.spi;

import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;

/**
 * Coordinator registry center provider.
 *
 * <p>Implementations must not retain invocation state or create resources in their constructor or SPI initialization method.</p>
 */
@SingletonSPI
public interface CoordinatorRegistryCenterProvider extends TypedSPI {
    
    /**
     * Create an initialized coordinator registry center.
     *
     * <p>The caller owns the returned registry center and must close it. If initialization fails, the provider must close any partially created registry center before propagating the failure.</p>
     *
     * @param repositoryConfig cluster persist repository configuration
     * @param namespaceRelativePath namespace relative path; {@code null} means that no relative path is appended
     * @return initialized coordinator registry center
     */
    CoordinatorRegistryCenter create(ClusterPersistRepositoryConfiguration repositoryConfig, String namespaceRelativePath);
}
