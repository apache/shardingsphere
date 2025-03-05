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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.metadata;

import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchemaChangedHandlerTest {
    
    private SchemaChangedHandler handler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        when(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()).thenReturn(InstanceType.PROXY);
        handler = new SchemaChangedHandler(contextManager);
    }
    
    @Test
    void assertHandleSchemaCreated() {
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema", "", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager()).addSchema("foo_db", "foo_schema");
    }
    
    @Test
    void assertHandleSchemaDropped() {
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema", "", Type.DELETED));
        verify(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager()).dropSchema("foo_db", "foo_schema");
    }
}
