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

package org.apache.shardingsphere.shadow.route.determiner;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.shadow.condition.ShadowCondition;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.hint.HintShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.hint.PreciseHintShadowValue;

/**
 * Hint shadow algorithm determiner.
 */
@HighFrequencyInvocation
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HintShadowAlgorithmDeterminer {
    
    /**
     * Is shadow in hint shadow algorithm.
     *
     * @param shadowAlgorithm hint shadow algorithm
     * @param shadowCondition shadow determine condition
     * @param shadowRule shadow rule
     * @param useShadow use shadow or not
     * @return is shadow or not
     */
    public static boolean isShadow(final HintShadowAlgorithm<Comparable<?>> shadowAlgorithm, final ShadowCondition shadowCondition, final ShadowRule shadowRule, final boolean useShadow) {
        PreciseHintShadowValue<Comparable<?>> shadowValue = new PreciseHintShadowValue<>(shadowCondition.getTableName(), shadowCondition.getOperationType(), useShadow);
        return shadowAlgorithm.isShadow(shadowRule.getAllShadowTableNames(), shadowValue);
    }
}
