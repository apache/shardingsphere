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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * @param reviser index reviser
     * @param rule rule
     * @return revised index meta data
     */
    public Collection<IndexMetaData> revise(final String tableName, final Collection<IndexMetaData> originalMetaDataList, final IndexReviser<T> reviser, final T rule) {
        if (null == reviser) {
            return originalMetaDataList;
        }
        return originalMetaDataList.stream().map(each -> reviser.revise(tableName, each, rule)).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
