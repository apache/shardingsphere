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

package org.apache.shardingsphere.data.pipeline.yaml.metadata;

import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

/**
 * Yaml pipeline column meta data swapper.
 */
public final class YamlPipelineColumnMetaDataSwapper implements YamlConfigurationSwapper<YamlPipelineColumnMetaData, PipelineColumnMetaData> {
    
    @Override
    public YamlPipelineColumnMetaData swapToYamlConfiguration(final PipelineColumnMetaData data) {
        if (null == data) {
            return null;
        }
        YamlPipelineColumnMetaData result = new YamlPipelineColumnMetaData();
        result.setName(data.getName());
        result.setDataType(data.getDataType());
        result.setDataTypeName(data.getDataTypeName());
        result.setNullable(data.isNullable());
        result.setPrimaryKey(data.isPrimaryKey());
        result.setOrdinalPosition(data.getOrdinalPosition());
        result.setUniqueKey(data.isUniqueKey());
        return result;
    }
    
    @Override
    public PipelineColumnMetaData swapToObject(final YamlPipelineColumnMetaData yamlConfig) {
        if (null == yamlConfig) {
            return null;
        }
        return new PipelineColumnMetaData(yamlConfig.getOrdinalPosition(), yamlConfig.getName(), yamlConfig.getDataType(), yamlConfig.getDataTypeName(), yamlConfig.isNullable(),
                yamlConfig.isPrimaryKey(), yamlConfig.isUniqueKey());
    }
}
