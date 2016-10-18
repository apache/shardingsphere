/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parser.result.merger;

import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * 分页对象.
 * 
 * @author gaohongtao
 */
@Getter
@ToString
public class Limit {
    
    public static final String OFFSET_NAME = "limit_offset";
    
    public static final String COUNT_NAME = "limit_count";
    
    private final int offset;
    
    private final int rowCount;
    
    private final int offsetParameterIndex;
    
    private final int rowCountParameterIndex;
    
    private final int multiShardingOffset;
    
    private final int multiShardingRowCount;
    
    
    public Limit(final int offset, final int rowCount, final int offsetParameterIndex, final int rowCountParameterIndex) {
        this.offset = offset;
        this.rowCount = rowCount;
        this.offsetParameterIndex = offsetParameterIndex;
        this.rowCountParameterIndex = rowCountParameterIndex;
        this.multiShardingOffset = 0;
        this.multiShardingRowCount = offset + rowCount;
    }
    
    public void replaceSQL(final SQLBuilder sqlBuilder, final boolean isVarious) {
        if (!isVarious) {
            return;
        }
        sqlBuilder.buildSQL(OFFSET_NAME, String.valueOf(multiShardingOffset));
        sqlBuilder.buildSQL(COUNT_NAME, String.valueOf(multiShardingRowCount));
    }
    
    public void replaceParameters(final List<Object> parameters, final boolean isVarious) {
        if (offsetParameterIndex > -1) {
            parameters.set(offsetParameterIndex, isVarious ? multiShardingOffset : offset);
        }
        if (rowCountParameterIndex > -1) {
            parameters.set(rowCountParameterIndex, isVarious ? multiShardingRowCount : rowCount);
        }
    }
}
