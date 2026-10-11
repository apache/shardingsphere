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

package org.apache.shardingsphere.data.pipeline.core.importer;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPI;

import java.util.Collection;
import java.util.Map;

/**
 * Pipeline required columns extractor.
 * 
 * @param <T> type of rule configuration
 */
@SingletonSPI
public interface PipelineRequiredColumnsExtractor<T extends RuleConfiguration> extends OrderedSPI<T> {
    
    /**
     * Get table and required columns map.
     *
     * @param ruleConfig rule configuration
     * @param logicTableNames logic table names
     * @return table and required columns map
     */
    Map<ShardingSphereIdentifier, Collection<String>> getTableAndRequiredColumnsMap(T ruleConfig, Collection<ShardingSphereIdentifier> logicTableNames);
}
