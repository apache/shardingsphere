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

package org.apache.shardingsphere.infra.yaml.schema.swapper;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereConstraint;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereConstraint;

/**
 * YAML constraint swapper.
 */
public final class YamlConstraintSwapper implements YamlConfigurationSwapper<YamlShardingSphereConstraint, ShardingSphereConstraint> {
    
    @Override
    public YamlShardingSphereConstraint swapToYamlConfiguration(final ShardingSphereConstraint data) {
        YamlShardingSphereConstraint result = new YamlShardingSphereConstraint();
        result.setName(data.getName());
        result.setReferencedTableName(data.getReferencedTableName());
        return result;
    }
    
    @Override
    public ShardingSphereConstraint swapToObject(final YamlShardingSphereConstraint yamlConfig) {
        return new ShardingSphereConstraint(yamlConfig.getName(), yamlConfig.getReferencedTableName());
    }
}
