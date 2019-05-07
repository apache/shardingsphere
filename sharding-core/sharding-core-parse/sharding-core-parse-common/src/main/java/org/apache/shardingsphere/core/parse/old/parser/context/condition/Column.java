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

package org.apache.shardingsphere.core.parse.old.parser.context.condition;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.core.exception.ShardingConfigurationException;

import java.util.List;

/**
 * Column.
 *
 * @author zhangliang
 * @author caohao
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class Column {
    
    private static final String DELIMITER = ".";
    
    private final String name;
    
    private final String tableName;
    
    public Column(final String column) {
        if (!isValidColumn(column)) {
            throw new ShardingConfigurationException("Invalid format for column: '%s'", column);
        }
        List<String> segments = Splitter.on(DELIMITER).splitToList(column);
        tableName = segments.get(0);
        name = segments.get(1);
    }
    
    private static boolean isValidColumn(final String columnNodeStr) {
        return columnNodeStr.contains(DELIMITER) && 2 == Splitter.on(DELIMITER).splitToList(columnNodeStr).size();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj || getClass() != obj.getClass()) {
            return false;
        }
        Column column = (Column) obj;
        return Objects.equal(this.name.toUpperCase(), column.name.toUpperCase()) && Objects.equal(this.tableName.toUpperCase(), column.tableName.toUpperCase()); 
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(name.toUpperCase(), tableName.toUpperCase()); 
    } 
}
