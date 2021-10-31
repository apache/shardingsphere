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

package org.apache.shardingsphere.shadow.route.engine.determiner.algorithm;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.note.NoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.note.PreciseNoteShadowValue;
import org.apache.shardingsphere.shadow.condition.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.route.engine.determiner.ShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Note shadow algorithm determiner.
 */
@RequiredArgsConstructor
public final class NoteShadowAlgorithmDeterminer implements ShadowAlgorithmDeterminer {
    
    private final NoteShadowAlgorithm<Comparable<?>> noteShadowAlgorithm;
    
    @Override
    public boolean isShadow(final ShadowDetermineCondition shadowDetermineCondition, final ShadowRule shadowRule) {
        Collection<PreciseNoteShadowValue<Comparable<?>>> noteShadowValues = createNoteShadowValues(shadowDetermineCondition);
        for (PreciseNoteShadowValue<Comparable<?>> each : noteShadowValues) {
            if (noteShadowAlgorithm.isShadow(shadowRule.getAllShadowTableNames(), each)) {
                return true;
            }
        }
        return false;
    }
    
    private Collection<PreciseNoteShadowValue<Comparable<?>>> createNoteShadowValues(final ShadowDetermineCondition shadowDetermineCondition) {
        ShadowOperationType shadowOperationType = shadowDetermineCondition.getShadowOperationType();
        String tableName = shadowDetermineCondition.getTableName();
        return shadowDetermineCondition.getSqlNotes().stream().<PreciseNoteShadowValue<Comparable<?>>>map(each -> new PreciseNoteShadowValue<>(tableName, shadowOperationType, each))
                .collect(Collectors.toCollection(LinkedList::new));
    }
}
