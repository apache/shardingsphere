/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.context.condition;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Column.
 *
 * @author zhangliang
 * @author caohao
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class Column {
    
    private final String name;
    
    private final String tableName;
    
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
