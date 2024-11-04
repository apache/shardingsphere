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
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.shadow.spi.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.column.PreciseColumnShadowValue;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Column shadow algorithm determiner.
 */
@HighFrequencyInvocation
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnShadowAlgorithmDeterminer {
    
    /**
     * Is shadow in column shadow algorithm.
     *
     * @param shadowAlgorithm column shadow algorithm
     * @param shadowCondition shadow determine condition
     * @return is shadow or not
     */
    public static boolean isShadow(final ColumnShadowAlgorithm<Comparable<?>> shadowAlgorithm, final ShadowCondition shadowCondition) {
        ShadowColumnCondition shadowColumnCondition = shadowCondition.getColumnCondition();
        String tableName = shadowCondition.getTableName();
        ShadowOperationType operationType = shadowCondition.getOperationType();
        for (PreciseColumnShadowValue<Comparable<?>> each : createColumnShadowValues(shadowColumnCondition.getColumn(), shadowColumnCondition.getValues(), tableName, operationType)) {
            if (!tableName.equals(shadowColumnCondition.getOwner()) || !shadowAlgorithm.isShadow(each)) {
                return false;
            }
        }
        return true;
    }
    
    private static Collection<PreciseColumnShadowValue<Comparable<?>>> createColumnShadowValues(final String columnName, final Collection<Comparable<?>> columnValues, final String tableName,
                                                                                                final ShadowOperationType operationType) {
        Collection<PreciseColumnShadowValue<Comparable<?>>> result = new LinkedList<>();
        for (Comparable<?> each : columnValues) {
            result.add(new PreciseColumnShadowValue<>(tableName, operationType, columnName, each));
        }
        return result;
    }
}
