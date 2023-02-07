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

package org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.index;

import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.IndexMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

/**
 * Index revise engine.
 * 
 * @param <T> type of rule
 */
public final class IndexReviseEngine<T extends ShardingSphereRule> {
    
    /**
     * Revise index meta data.
     * 
     * @param tableName table name
     * @param originalMetaDataList original index meta data list
     * @param rule rule
     * @return revised index meta data
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Collection<IndexMetaData> revise(final String tableName, final Collection<IndexMetaData> originalMetaDataList, final T rule) {
        Optional<IndexReviser> reviser = TypedSPILoader.findService(IndexReviser.class, rule.getClass().getSimpleName());
        if (!reviser.isPresent()) {
            return originalMetaDataList;
        }
        Collection<IndexMetaData> result = new LinkedHashSet<>();
        for (IndexMetaData each : originalMetaDataList) {
            Optional<IndexMetaData> indexMetaData = reviser.get().revise(tableName, each, rule);
            indexMetaData.ifPresent(result::add);
        }
        return result;
    }
}
