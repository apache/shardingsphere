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
import org.apache.shardingsphere.shadow.algorithm.shadow.validator.ShadowValueValidator;
import org.apache.shardingsphere.shadow.algorithm.shadow.validator.column.ShadowDateValueValidator;
import org.apache.shardingsphere.shadow.algorithm.shadow.validator.column.ShadowEnumValueValidator;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.column.PreciseColumnShadowValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;

/**
 * Abstract column match shadow algorithm.
 */
public abstract class AbstractColumnMatchShadowAlgorithm implements ColumnShadowAlgorithm<Comparable<?>> {
    
    private static final String COLUMN_PROPS_KEY = "column";
    
    private static final String OPERATION_PROPS_KEY = "operation";
    
    private static final Collection<ShadowValueValidator> SHADOW_VALUE_VALIDATORS = new LinkedList<>();
    
    @Getter
    private Properties props;
    
    private String shadowColumn;
    
    private ShadowOperationType shadowOperationType;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        shadowColumn = getShadowColumn(props);
        shadowOperationType = getShadowOperationType(props);
        initShadowValueValidator();
    }
    
    private String getShadowColumn(final Properties props) {
        String result = props.getProperty(COLUMN_PROPS_KEY);
        Preconditions.checkNotNull(result, "Column shadow algorithm column cannot be null.");
        return result;
    }
    
    private ShadowOperationType getShadowOperationType(final Properties props) {
        String operationType = props.getProperty(OPERATION_PROPS_KEY);
        Preconditions.checkNotNull(operationType, "Column shadow algorithm operation cannot be null.");
        Optional<ShadowOperationType> result = ShadowOperationType.contains(operationType);
        Preconditions.checkState(result.isPresent(), "Column shadow algorithm operation must be one of [select, insert, update, delete].");
        return result.get();
    }
    
    private void initShadowValueValidator() {
        SHADOW_VALUE_VALIDATORS.add(new ShadowDateValueValidator());
        SHADOW_VALUE_VALIDATORS.add(new ShadowEnumValueValidator());
    }
    
    @Override
    public boolean isShadow(final PreciseColumnShadowValue<Comparable<?>> shadowValue) {
        String table = shadowValue.getLogicTableName();
        String column = shadowValue.getColumnName();
        Comparable<?> value = shadowValue.getValue();
        if (shadowOperationType == shadowValue.getShadowOperationType() && shadowColumn.equals(column)) {
            SHADOW_VALUE_VALIDATORS.forEach(each -> each.preValidate(table, column, value));
            return isMatchValue(value);
        }
        return false;
    }
    
    protected abstract boolean isMatchValue(Comparable<?> value);
}
