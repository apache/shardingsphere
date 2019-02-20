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

package org.apache.shardingsphere.core.rewrite.placeholder;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.ShardingOperator;

import java.util.Collection;
import java.util.Map;

/**
 * Encrypt update item column placeholder for rewrite.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class EncryptUpdateItemColumnPlaceholder implements ShardingPlaceholder {
    
    private final String logicTableName;
    
    private final String columnName;
    
    private final Comparable<?> columnValue;
    
    private final String assistedColumnName;
    
    private final Comparable<?> assistedColumnValue;
    
    private final int placeholderIndex;
    
    private final ShardingOperator operator = ShardingOperator.EQUAL;
    
    @Override
    public String toString() {
        if (Strings.isNullOrEmpty(assistedColumnName)) {
            return -1 == placeholderIndex ? String.format("%s = \"%s\"", columnName, columnValue) : String.format("%s = ?", columnName);
        }
        
    }
}
