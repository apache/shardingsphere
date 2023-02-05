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

package org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.column;

import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Column revise engine.
 */
public final class ColumnReviseEngine {
    
    /**
     * Revise column meta data.
     * 
     * @param originalMetaDataList original column meta data list
     * @param revisers column revisers
     * @return revised column meta data
     */
    public Collection<ColumnMetaData> revise(final Collection<ColumnMetaData> originalMetaDataList, final Collection<ColumnReviser> revisers) {
        return originalMetaDataList.stream().map(each -> revise(each, revisers)).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    private Optional<ColumnMetaData> revise(final ColumnMetaData originalMetaData, final Collection<ColumnReviser> revisers) {
        ColumnMetaData result = originalMetaData;
        for (ColumnReviser each : revisers) {
            Optional<ColumnMetaData> revisedMetaData = each.revise(result);
            if (!revisedMetaData.isPresent()) {
                return Optional.empty();
            }
            result = revisedMetaData.get();
        }
        return Optional.of(result);
    }
}
