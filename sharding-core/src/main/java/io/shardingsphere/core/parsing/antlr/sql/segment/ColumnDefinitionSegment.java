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

package io.shardingsphere.core.parsing.antlr.sql.segment;

import lombok.Getter;
import lombok.Setter;

/**
 * Add column segment.
 *
 * @author duhongjun
 */
@Getter
@Setter
public final class ColumnDefinitionSegment implements SQLSegment {
    
    private String name;
    
    private String type;
    
    private Integer length;
    
    private boolean primaryKey;
    
    private ColumnPositionSegment position;
    
    private String oldName;
    
    private boolean isAdd;
    
    public ColumnDefinitionSegment(final String name, final String type, final Integer length, final boolean primaryKey) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.primaryKey = primaryKey;
    }
    
    public ColumnDefinitionSegment(final String name, final String oldName) {
        this.name = name;
        this.oldName = oldName;
    }
}
