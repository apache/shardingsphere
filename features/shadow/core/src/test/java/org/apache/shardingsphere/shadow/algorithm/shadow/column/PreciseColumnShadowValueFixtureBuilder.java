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

package org.apache.shardingsphere.shadow.algorithm.shadow.column;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.column.PreciseColumnShadowValue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class PreciseColumnShadowValueFixtureBuilder {
    
    static Collection<PreciseColumnShadowValue<Comparable<?>>> createTrueCase(final String shadowTable, final String shadowColumn) {
        Collection<PreciseColumnShadowValue<Comparable<?>>> result = new LinkedList<>();
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.INSERT, shadowColumn, 1));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.INSERT, shadowColumn, 1L));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.INSERT, shadowColumn, "1"));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.INSERT, shadowColumn, '1'));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.INSERT, shadowColumn, new BigInteger("1")));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.INSERT, shadowColumn, new BigDecimal("1")));
        return result;
    }
    
    static Collection<PreciseColumnShadowValue<Comparable<?>>> createFalseCase(final String shadowTable, final String shadowColumn) {
        Collection<PreciseColumnShadowValue<Comparable<?>>> result = new LinkedList<>();
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.INSERT, shadowColumn, 2));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.INSERT, shadowColumn, 2L));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.INSERT, shadowColumn, "2"));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.INSERT, shadowColumn, '2'));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.INSERT, shadowColumn, new BigInteger("2")));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.INSERT, shadowColumn, new BigDecimal("2")));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.UPDATE, shadowColumn, 1));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.UPDATE, shadowColumn, 1L));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.UPDATE, shadowColumn, "1"));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.UPDATE, shadowColumn, '1'));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.UPDATE, shadowColumn, new BigInteger("2")));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.UPDATE, shadowColumn, new BigDecimal("1")));
        return result;
    }
    
    static Collection<PreciseColumnShadowValue<Comparable<?>>> createExceptionCase(final String shadowTable, final String shadowColumn) {
        Collection<PreciseColumnShadowValue<Comparable<?>>> result = new LinkedList<>();
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.INSERT, shadowColumn, new Date()));
        result.add(new PreciseColumnShadowValue<>(shadowTable, ShadowOperationType.INSERT, shadowColumn, ShadowEnum.SHADOW_VALUE));
        return result;
    }
    
    private enum ShadowEnum {
        
        SHADOW_VALUE
    }
}
