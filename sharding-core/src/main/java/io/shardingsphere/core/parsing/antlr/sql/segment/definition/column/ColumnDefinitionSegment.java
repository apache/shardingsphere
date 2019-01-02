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

package io.shardingsphere.core.parsing.antlr.sql.segment.definition.column;

import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.util.SQLUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * Column definition segment.
 *
 * @author duhongjun
 */
@Getter
@Setter
public final class ColumnDefinitionSegment implements SQLSegment {
    
    private String columnName;
    
    private String dataType;
    
    private boolean primaryKey;
    
    public ColumnDefinitionSegment(final String columnName, final String dataType, final boolean primaryKey) {
        this.columnName = SQLUtil.getExactlyValue(columnName);
        this.dataType = dataType;
        this.primaryKey = primaryKey;
    }
}
