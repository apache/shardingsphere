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

package org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.constraint;

import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ConstraintMetaData;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Constraint revise engine.
 */
public final class ConstraintReviseEngine {
    
    /**
     * Revise constraint meta data.
     * 
     * @param tableName table name
     * @param originalMetaDataList original constraint meta data list
     * @param revisers constraint revisers
     * @return revised constraint meta data
     */
    public Collection<ConstraintMetaData> revise(final String tableName, final Collection<ConstraintMetaData> originalMetaDataList, final Collection<ConstraintReviser> revisers) {
        return originalMetaDataList.stream().map(each -> revise(tableName, each, revisers)).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    private Optional<ConstraintMetaData> revise(final String tableName, final ConstraintMetaData originalMetaData, final Collection<ConstraintReviser> revisers) {
        ConstraintMetaData result = originalMetaData;
        for (ConstraintReviser each : revisers) {
            Optional<ConstraintMetaData> revisedMetaData = each.revise(tableName, result);
            if (!revisedMetaData.isPresent()) {
                return Optional.empty();
            }
            result = revisedMetaData.get();
        }
        return Optional.of(result);
    }
}
