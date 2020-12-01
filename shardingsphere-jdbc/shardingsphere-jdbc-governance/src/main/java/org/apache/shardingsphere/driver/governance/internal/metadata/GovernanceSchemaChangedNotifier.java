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

package org.apache.shardingsphere.driver.governance.internal.metadata;

import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.governance.core.event.model.schema.SchemaPersistEvent;
import org.apache.shardingsphere.infra.metadata.schema.refresher.spi.SchemaChangedNotifier;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;

/**
 * ShardingSphere schema changed notifier for governance.
 */
public final class GovernanceSchemaChangedNotifier implements SchemaChangedNotifier {
    
    @Override
    public void notify(final String name, final ShardingSphereSchema schema) {
        ShardingSphereEventBus.getInstance().post(new SchemaPersistEvent(name, schema));
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
    
    @Override
    public Class<ShardingSphereSchema> getTypeClass() {
        return ShardingSphereSchema.class;
    }
}
