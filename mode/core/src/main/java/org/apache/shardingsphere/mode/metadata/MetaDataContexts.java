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

package org.apache.shardingsphere.mode.metadata;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Meta data contexts.
 */
@Getter
public final class MetaDataContexts implements AutoCloseable {
    
    private final ShardingSphereMetaData metaData;
    
    private final ShardingSphereStatistics statistics;
    
    public MetaDataContexts(final ShardingSphereMetaData metaData, final ShardingSphereStatistics statistics) {
        this.metaData = metaData;
        this.statistics = statistics;
    }
    
    @SneakyThrows(Exception.class)
    @Override
    public void close() {
        for (ShardingSphereRule each : getAllRules()) {
            if (each instanceof AutoCloseable) {
                ((AutoCloseable) each).close();
            }
        }
    }
    
    private Collection<ShardingSphereRule> getAllRules() {
        Collection<ShardingSphereRule> result = new LinkedList<>(metaData.getGlobalRuleMetaData().getRules());
        metaData.getDatabases().values().stream().map(each -> each.getRuleMetaData().getRules()).forEach(result::addAll);
        return result;
    }
}
