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

import org.apache.shardingsphere.governance.core.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.governance.core.event.persist.MetaDataPersistEvent;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.spi.RuleMetaDataNotifier;

import java.util.Properties;

/**
 * Rule schema meta data notifier.
 */
public final class RuleSchemaMetaDataNotifier implements RuleMetaDataNotifier {
    
    @Override
    public void notify(final String schemaName, final RuleSchemaMetaData metaData) {
        ShardingSphereEventBus.getInstance().post(new MetaDataPersistEvent(schemaName, metaData));
    }
    
    @Override
    public String getType() {
        return "metadata-notifier";
    }
    
    @Override
    public Properties getProps() {
        return null;
    }
    
    @Override
    public void setProps(final Properties props) {
    }
}
