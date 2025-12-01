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

package org.apache.shardingsphere.mode.repository.cluster.zookeeper.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.mode.repository.cluster.exception.ClusterRepositoryPersistException;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;

/**
 * ZooKeeper exception handler.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ZookeeperExceptionHandler {
    
    /**
     * Handle exception.
     *
     * <p>Ignore interrupt and connection invalid exception.</p>
     *
     * @param cause to be handled exception
     * @throws ClusterRepositoryPersistException cluster persist repository exception
     */
    public static void handleException(final Exception cause) {
        if (null == cause) {
            log.info("cause is null");
            return;
        }
        if (isIgnoredException(cause) || null != cause.getCause() && isIgnoredException(cause.getCause())) {
            log.debug("Ignored exception for: {}", cause.getMessage());
        } else if (cause instanceof InterruptedException) {
            log.info("InterruptedException caught");
            Thread.currentThread().interrupt();
        } else {
            log.error("Zookeeper exception occured.", cause);
            throw new ClusterRepositoryPersistException(cause);
        }
    }
    
    private static boolean isIgnoredException(final Throwable cause) {
        return cause instanceof ConnectionLossException || cause instanceof NoNodeException || cause instanceof NodeExistsException;
    }
}
