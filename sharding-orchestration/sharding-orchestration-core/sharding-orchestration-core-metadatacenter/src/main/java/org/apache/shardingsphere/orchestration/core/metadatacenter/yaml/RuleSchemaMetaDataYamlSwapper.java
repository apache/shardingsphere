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

package org.apache.shardingsphere.orchestration.core.metadatacenter.yaml;

import org.apache.shardingsphere.underlying.common.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.underlying.common.yaml.swapper.YamlSwapper;

/**
 * Rule Schema Metadata configuration YAML swapper.
 */
public class RuleSchemaMetaDataYamlSwapper implements YamlSwapper<YamlRuleSchemaMetaData, RuleSchemaMetaData> {

    @Override
    public YamlRuleSchemaMetaData swap(final RuleSchemaMetaData metaData) {
        YamlRuleSchemaMetaData result = new YamlRuleSchemaMetaData();
        result.setConfiguredSchemaMetaData(metaData.getConfiguredSchemaMetaData());
        result.setUnconfiguredSchemaMetaDataMap(metaData.getUnconfiguredSchemaMetaDataMap());
        return result;
    }

    @Override
    public RuleSchemaMetaData swap(final YamlRuleSchemaMetaData yaml) {
        return new RuleSchemaMetaData(yaml.getConfiguredSchemaMetaData(), yaml.getUnconfiguredSchemaMetaDataMap());
    }
}
