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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.position.yaml;

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.position.TableCheckRangePosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.UniqueKeyIngestPosition;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

/**
 * Yaml table check range position swapper.
 */
public final class YamlTableCheckRangePositionSwapper implements YamlConfigurationSwapper<YamlTableCheckRangePosition, TableCheckRangePosition> {
    
    @Override
    public YamlTableCheckRangePosition swapToYamlConfiguration(final TableCheckRangePosition data) {
        YamlTableCheckRangePosition result = new YamlTableCheckRangePosition();
        result.setSplittingItem(data.getSplittingItem());
        result.setSourceDataNode(data.getSourceDataNode());
        result.setLogicTableName(data.getLogicTableName());
        result.setSourceRange(data.getSourceRange().toString());
        result.setTargetRange(data.getTargetRange().toString());
        result.setQueryCondition(data.getQueryCondition());
        result.setSourcePosition(data.getSourcePosition());
        result.setTargetPosition(data.getTargetPosition());
        result.setFinished(data.isFinished());
        result.setMatched(data.getMatched());
        return result;
    }
    
    @Override
    public TableCheckRangePosition swapToObject(final YamlTableCheckRangePosition yamlConfig) {
        return new TableCheckRangePosition(yamlConfig.getSplittingItem(), yamlConfig.getSourceDataNode(), yamlConfig.getLogicTableName(),
                UniqueKeyIngestPosition.decode(yamlConfig.getSourceRange()), UniqueKeyIngestPosition.decode(yamlConfig.getTargetRange()),
                yamlConfig.getQueryCondition(), yamlConfig.getSourcePosition(), yamlConfig.getTargetPosition(), yamlConfig.isFinished(), yamlConfig.getMatched());
    }
}
