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

package org.apache.shardingsphere.mode.repository.cluster.lock;

import org.apache.shardingsphere.infra.util.props.TypedProperties;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

/**
 * Distributed lock creator.
 * 
 * @param <C> type of distributed lock client
 * @param <P> type of typed properties
 */
@SingletonSPI
public interface DistributedLockCreator<C, P extends TypedProperties<?>> extends TypedSPI {
    
    /**
     * Create distributed lock.
     * 
     * @param lockKey lock key
     * @param client client
     * @param props props
     * @return created distributed lock
     */
    DistributedLock create(String lockKey, C client, P props);
}
