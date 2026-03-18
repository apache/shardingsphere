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

package org.apache.shardingsphere.infra.metadata.database.schema.reviser.constraint;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ConstraintMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Constraint revise engine.
 * 
 * @param <T> type of rule
 */
@RequiredArgsConstructor
public final class ConstraintReviseEngine<T extends ShardingSphereRule> {
    
    private final T rule;
    
    private final MetaDataReviseEntry<T> reviseEntry;
    
    /**
     * Revise constraint meta data.
     *
     * @param tableName table name
     * @param originalMetaDataList original constraint meta data list
     * @return revised constraint meta data
     */
    public Collection<ConstraintMetaData> revise(final String tableName, final Collection<ConstraintMetaData> originalMetaDataList) {
        Optional<? extends ConstraintReviser<T>> reviser = reviseEntry.getConstraintReviser(rule, tableName);
        return reviser.isPresent()
                ? originalMetaDataList.stream()
                        .map(each -> reviser.get().revise(tableName, each, rule)).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList())
                : originalMetaDataList;
    }
}
