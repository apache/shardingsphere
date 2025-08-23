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

import lombok.Getter;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.shadow.spi.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.column.PreciseColumnShadowValue;

import java.util.Optional;
import java.util.Properties;

/**
 * Abstract column matched shadow algorithm.
 */
@Getter
public abstract class AbstractColumnMatchedShadowAlgorithm implements ColumnShadowAlgorithm<Comparable<?>> {
    
    private static final String COLUMN_PROPS_KEY = "column";
    
    private static final String OPERATION_PROPS_KEY = "operation";
    
    private String shadowColumn;
    
    private ShadowOperationType shadowOperationType;
    
    @Override
    public void init(final Properties props) {
        shadowColumn = getShadowColumn(props);
        shadowOperationType = getShadowOperationType(props);
    }
    
    private String getShadowColumn(final Properties props) {
        String result = props.getProperty(COLUMN_PROPS_KEY);
        ShardingSpherePreconditions.checkNotNull(result, () -> new AlgorithmInitializationException(this, "Column shadow algorithm column cannot be null"));
        return result;
    }
    
    private ShadowOperationType getShadowOperationType(final Properties props) {
        String operationType = props.getProperty(OPERATION_PROPS_KEY);
        ShardingSpherePreconditions.checkNotNull(operationType, () -> new AlgorithmInitializationException(this, "Column shadow algorithm operation cannot be null"));
        Optional<ShadowOperationType> result = ShadowOperationType.valueFrom(operationType);
        ShardingSpherePreconditions.checkState(result.isPresent(),
                () -> new AlgorithmInitializationException(this, "Column shadow algorithm operation must be one of [select, insert, update, delete]"));
        return result.get();
    }
    
    @Override
    public final boolean isShadow(final PreciseColumnShadowValue<Comparable<?>> shadowValue) {
        String table = shadowValue.getTableName();
        String column = shadowValue.getColumnName();
        Comparable<?> value = shadowValue.getValue();
        if (shadowOperationType == shadowValue.getOperationType() && shadowColumn.equals(column)) {
            ColumnShadowValueValidator.validate(table, column, value);
            return matchesShadowValue(value);
        }
        return false;
    }
    
    protected abstract boolean matchesShadowValue(Comparable<?> value);
}
