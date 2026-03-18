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

import org.apache.shardingsphere.mode.repository.cluster.exception.ClusterRepositoryPersistException;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZookeeperExceptionHandlerTest {
    
    @Test
    void assertHandleNullException() {
        assertDoesNotThrow(() -> ZookeeperExceptionHandler.handleException(null));
    }
    
    @Test
    void assertHandleConnectionLossException() {
        assertDoesNotThrow(() -> ZookeeperExceptionHandler.handleException(new ConnectionLossException()));
    }
    
    @Test
    void assertHandleConnectionLossExceptionWithCause() {
        assertDoesNotThrow(() -> ZookeeperExceptionHandler.handleException(new Exception(new ConnectionLossException())));
    }
    
    @Test
    void assertHandleNoNodeException() {
        assertDoesNotThrow(() -> ZookeeperExceptionHandler.handleException(new NoNodeException()));
    }
    
    @Test
    void assertHandleNoNodeExceptionWithCause() {
        assertDoesNotThrow(() -> ZookeeperExceptionHandler.handleException(new Exception(new NoNodeException())));
    }
    
    @Test
    void assertHandleNodeExistsException() {
        assertDoesNotThrow(() -> ZookeeperExceptionHandler.handleException(new NodeExistsException()));
    }
    
    @Test
    void assertHandleNodeExistsExceptionWithCause() {
        assertDoesNotThrow(() -> ZookeeperExceptionHandler.handleException(new Exception(new NodeExistsException())));
    }
    
    @Test
    void assertHandleInterruptedException() {
        assertDoesNotThrow(() -> ZookeeperExceptionHandler.handleException(new InterruptedException()));
    }
    
    @Test
    void assertHandleUnIgnoredException() {
        assertThrows(ClusterRepositoryPersistException.class, () -> ZookeeperExceptionHandler.handleException(new Exception("")));
    }
    
    @Test
    void assertHandleUnIgnoredExceptionWithCause() {
        assertThrows(ClusterRepositoryPersistException.class, () -> ZookeeperExceptionHandler.handleException(new Exception(new RuntimeException(""))));
    }
}
