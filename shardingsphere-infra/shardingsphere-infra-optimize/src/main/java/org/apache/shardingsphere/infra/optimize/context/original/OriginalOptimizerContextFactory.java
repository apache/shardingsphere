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

package org.apache.shardingsphere.infra.optimize.context.original;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.optimize.context.original.props.OptimizerPropertiesBuilderFactory;
import org.apache.shardingsphere.infra.optimize.core.metadata.FederationMetaData;

import java.util.Map;
import java.util.Properties;

/**
 * Original optimize context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OriginalOptimizerContextFactory {
    
    /**
     * Create original optimizer context.
     * 
     * @param metaDataMap meta data map
     * @return created original optimizer context
     */
    public static OriginalOptimizerContext create(final Map<String, ShardingSphereMetaData> metaDataMap) {
        DatabaseType databaseType = metaDataMap.isEmpty() ? null : metaDataMap.values().iterator().next().getResource().getDatabaseType();
        FederationMetaData metaData = new FederationMetaData(metaDataMap);
        Properties props = createOptimizerProperties(databaseType);
        return new OriginalOptimizerContext(databaseType, metaData, props);
    }
    
    private static Properties createOptimizerProperties(final DatabaseType databaseType) {
        Properties result = new Properties();
        result.setProperty(CalciteConnectionProperty.TIME_ZONE.camelName(), "UTC");
        result.putAll(OptimizerPropertiesBuilderFactory.build(databaseType, result));
        return result;
    }
}
