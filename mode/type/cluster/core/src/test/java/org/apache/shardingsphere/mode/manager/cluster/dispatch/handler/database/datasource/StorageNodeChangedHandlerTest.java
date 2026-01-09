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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.datasource;

import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.datasource.StorageNodeNodePath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class StorageNodeChangedHandlerTest {
    
    @Mock
    private ContextManager contextManager;
    
    @Test
    void assertGetSubscribedNodePath() {
        NodePath actual = new StorageNodeChangedHandler(contextManager).getSubscribedNodePath("foo_db");
        assertThat(NodePathGenerator.toPath(actual), is("/metadata/foo_db/data_sources/nodes/([\\w-]+)"));
    }
    
    @Test
    void assertHandleAdded() {
        DataChangedEvent event = new DataChangedEvent(NodePathGenerator.toPath(new StorageNodeNodePath("foo_db", "foo_node")), "", DataChangedEvent.Type.ADDED);
        assertDoesNotThrow(() -> new StorageNodeChangedHandler(contextManager).handle("foo_db", event));
    }
    
    @Test
    void assertHandleUpdated() {
        DataChangedEvent event = new DataChangedEvent(NodePathGenerator.toPath(new StorageNodeNodePath("foo_db", "foo_node")), "", DataChangedEvent.Type.UPDATED);
        assertDoesNotThrow(() -> new StorageNodeChangedHandler(contextManager).handle("foo_db", event));
    }
    
    @Test
    void assertHandleDeleted() {
        DataChangedEvent event = new DataChangedEvent(NodePathGenerator.toPath(new StorageNodeNodePath("foo_db", "foo_node")), "", DataChangedEvent.Type.DELETED);
        assertDoesNotThrow(() -> new StorageNodeChangedHandler(contextManager).handle("foo_db", event));
    }
    
    @Test
    void assertHandleIgnoredType() {
        DataChangedEvent event = new DataChangedEvent(NodePathGenerator.toPath(new StorageNodeNodePath("foo_db", "foo_node")), "", DataChangedEvent.Type.IGNORED);
        assertDoesNotThrow(() -> new StorageNodeChangedHandler(contextManager).handle("foo_db", event));
    }
}
