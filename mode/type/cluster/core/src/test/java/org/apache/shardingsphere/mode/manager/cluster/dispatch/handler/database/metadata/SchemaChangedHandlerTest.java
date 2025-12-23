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

import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.manager.database.DatabaseMetaDataManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchemaChangedHandlerTest {
    
    private SchemaChangedHandler handler;
    
    @Mock
    private ContextManager contextManager;
    
    @Mock
    private DatabaseMetaDataManager databaseMetaDataManager;
    
    @BeforeEach
    void setUp() {
        MetaDataContextManager metaDataContextManager = mock(MetaDataContextManager.class);
        when(metaDataContextManager.getDatabaseMetaDataManager()).thenReturn(databaseMetaDataManager);
        when(contextManager.getMetaDataContextManager()).thenReturn(metaDataContextManager);
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(computeNodeInstanceContext);
        InstanceMetaData instanceMetaData = new InstanceMetaData() {
            
            @Override
            public String getId() {
                return "id";
            }
            
            @Override
            public InstanceType getType() {
                return InstanceType.JDBC;
            }
            
            @Override
            public String getIp() {
                return "127.0.0.1";
            }
            
            @Override
            public String getAttributes() {
                return "";
            }
            
            @Override
            public String getVersion() {
                return "version";
            }
        };
        when(computeNodeInstanceContext.getInstance()).thenReturn(new ComputeNodeInstance(instanceMetaData));
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
