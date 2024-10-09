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

package org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch;

import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereRowData;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.DatabaseDataAddedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.DatabaseDataDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.SchemaDataAddedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.SchemaDataDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.ShardingSphereRowDataChangedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.ShardingSphereRowDataDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.TableDataChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DatabaseDataChangedSubscriberTest {
    
    private DatabaseDataChangedSubscriber subscriber;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        subscriber = new DatabaseDataChangedSubscriber(contextManager);
    }
    
    @Test
    void assertRenewWithDatabaseDataAddedEvent() {
        subscriber.renew(new DatabaseDataAddedEvent("foo_db"));
        verify(contextManager.getMetaDataContextManager().getDatabaseManager()).addShardingSphereDatabaseData("foo_db");
    }
    
    @Test
    void assertRenewWithDatabaseDataDeletedEvent() {
        subscriber.renew(new DatabaseDataDeletedEvent("foo_db"));
        verify(contextManager.getMetaDataContextManager().getDatabaseManager()).dropShardingSphereDatabaseData("foo_db");
    }
    
    @Test
    void assertRenewWithSchemaDataAddedEvent() {
        subscriber.renew(new SchemaDataAddedEvent("foo_db", "foo_schema"));
        verify(contextManager.getMetaDataContextManager().getDatabaseManager()).addShardingSphereSchemaData("foo_db", "foo_schema");
    }
    
    @Test
    void assertRenewWithSchemaDataDeletedEvent() {
        subscriber.renew(new SchemaDataDeletedEvent("foo_db", "foo_schema"));
        verify(contextManager.getMetaDataContextManager().getDatabaseManager()).dropShardingSphereSchemaData("foo_db", "foo_schema");
    }
    
    @Test
    void assertRenewWithTableDataChangedEvent() {
        subscriber.renew(new TableDataChangedEvent("foo_db", "foo_schema", "add_tbl", "del_tbl"));
        verify(contextManager.getMetaDataContextManager().getDatabaseManager()).addShardingSphereTableData("foo_db", "foo_schema", "add_tbl");
        verify(contextManager.getMetaDataContextManager().getDatabaseManager()).dropShardingSphereTableData("foo_db", "foo_schema", "del_tbl");
    }
    
    @Test
    void assertRenewWithShardingSphereRowDataChangedEvent() {
        YamlShardingSphereRowData rowData = new YamlShardingSphereRowData();
        subscriber.renew(new ShardingSphereRowDataChangedEvent("foo_db", "foo_schema", "foo_tbl", rowData));
        verify(contextManager.getMetaDataContextManager().getDatabaseManager()).alterShardingSphereRowData("foo_db", "foo_schema", "foo_tbl", rowData);
    }
    
    @Test
    void assertRenewWithShardingSphereRowDataDeletedEvent() {
        subscriber.renew(new ShardingSphereRowDataDeletedEvent("foo_db", "foo_schema", "foo_tbl", "id"));
        verify(contextManager.getMetaDataContextManager().getDatabaseManager()).deleteShardingSphereRowData("foo_db", "foo_schema", "foo_tbl", "id");
    }
}
