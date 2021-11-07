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

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.column.PreciseColumnShadowValue;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Column value match shadow algorithm.
 */
@Getter
@Setter
public final class ColumnValueMatchShadowAlgorithm implements ColumnShadowAlgorithm<Comparable<?>> {
    
    private static final String COLUMN = "column";
    
    private static final String OPERATION = "operation";
    
    private static final String VALUE = "value";
    
    private Properties props = new Properties();
    
    private ShadowOperationType shadowOperationType;
    
    @Override
    public void init() {
        checkProps();
    }
    
    private void checkProps() {
        checkOperation();
        checkColumn();
        checkValue();
    }
    
    private void checkValue() {
        Object value = props.get(VALUE);
        Preconditions.checkNotNull(value, "Column value match shadow algorithm value cannot be null.");
    }
    
    private void checkColumn() {
        String column = props.getProperty(COLUMN);
        Preconditions.checkNotNull(column, "Column value match shadow algorithm column cannot be null.");
    }
    
    private void checkOperation() {
        String operationType = props.getProperty(OPERATION);
        Preconditions.checkNotNull(operationType, "Column value match shadow algorithm operation cannot be null.");
        Optional<ShadowOperationType> shadowOperationType = ShadowOperationType.contains(operationType);
        Preconditions.checkState(shadowOperationType.isPresent(), "Column value match shadow algorithm operation must be one of select insert update delete.");
        shadowOperationType.ifPresent(type -> this.shadowOperationType = type);
    }
    
    @Override
    public boolean isShadow(final Collection<String> shadowTableNames, final PreciseColumnShadowValue<Comparable<?>> shadowValue) {
        boolean containTable = shadowTableNames.contains(shadowValue.getLogicTableName());
        boolean isSameOperation = shadowOperationType == shadowValue.getShadowOperationType();
        boolean isSameColumnName = Objects.equals(props.get(COLUMN), shadowValue.getColumnName());
        boolean isSameColumnValue = props.get(VALUE).toString().equals(String.valueOf(shadowValue.getValue()));
        return containTable && isSameOperation && isSameColumnName && isSameColumnValue;
    }
    
    @Override
    public String getType() {
        return "VALUE_MATCH";
    }
}
