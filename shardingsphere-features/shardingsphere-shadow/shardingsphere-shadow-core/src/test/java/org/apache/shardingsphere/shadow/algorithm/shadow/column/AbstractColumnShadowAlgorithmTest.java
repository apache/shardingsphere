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

import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.column.PreciseColumnShadowValue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

public abstract class AbstractColumnShadowAlgorithmTest {
    
    protected static final String SHADOW_TABLE = "t_user";
    
    protected static final String SHADOW_COLUMN = "shadow";
    
    enum ShadowEnum {
        SHADOW_VALUE
    }
    
    protected Collection<PreciseColumnShadowValue<Comparable<?>>> createPreciseColumnShadowValuesTrueCase() {
        Collection<PreciseColumnShadowValue<Comparable<?>>> result = new LinkedList<>();
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.INSERT, SHADOW_COLUMN, 1));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.INSERT, SHADOW_COLUMN, 1L));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.INSERT, SHADOW_COLUMN, "1"));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.INSERT, SHADOW_COLUMN, '1'));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.INSERT, SHADOW_COLUMN, new BigInteger("1")));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.INSERT, SHADOW_COLUMN, new BigDecimal("1")));
        return result;
    }
    
    protected Collection<PreciseColumnShadowValue<Comparable<?>>> createPreciseColumnShadowValuesFalseCase() {
        Collection<PreciseColumnShadowValue<Comparable<?>>> result = new LinkedList<>();
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.INSERT, SHADOW_COLUMN, 2));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.INSERT, SHADOW_COLUMN, 2L));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.INSERT, SHADOW_COLUMN, "2"));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.INSERT, SHADOW_COLUMN, '2'));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.INSERT, SHADOW_COLUMN, new BigInteger("2")));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.INSERT, SHADOW_COLUMN, new BigDecimal("2")));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.UPDATE, SHADOW_COLUMN, 1));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.UPDATE, SHADOW_COLUMN, 1L));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.UPDATE, SHADOW_COLUMN, "1"));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.UPDATE, SHADOW_COLUMN, '1'));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.UPDATE, SHADOW_COLUMN, new BigInteger("2")));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.UPDATE, SHADOW_COLUMN, new BigDecimal("1")));
        return result;
    }
    
    protected Collection<PreciseColumnShadowValue<Comparable<?>>> createPreciseColumnShadowValuesExceptionCase() {
        Collection<PreciseColumnShadowValue<Comparable<?>>> result = new LinkedList<>();
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.INSERT, SHADOW_COLUMN, new Date()));
        result.add(new PreciseColumnShadowValue<>(SHADOW_TABLE, ShadowOperationType.INSERT, SHADOW_COLUMN, ShadowEnum.SHADOW_VALUE));
        return result;
    }
}
