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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.context;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 分页上下文.
 *
 * @author zhangliang
 */
@Getter
@Setter
public final class LimitContext {
    
    private int rowCount;
    
    private Optional<Integer> offset;
    
    private final int rowCountParameterIndex;
    
    private final int offsetParameterIndex;
    
    public LimitContext(final int rowCount, final int rowCountParameterIndex) {
        this.rowCount = rowCount;
        offset = Optional.absent();
        this.offsetParameterIndex = -1;
        this.rowCountParameterIndex = rowCountParameterIndex;
    }
    
    public LimitContext(final int offset, final int rowCount, final int offsetParameterIndex, final int rowCountParameterIndex) {
        this.offset = Optional.of(offset);
        this.rowCount = rowCount;
        this.offsetParameterIndex = offsetParameterIndex;
        this.rowCountParameterIndex = rowCountParameterIndex;
    }
    
    /**
     * 获取分页偏移量.
     * 
     * @return 分页偏移量
     */
    public int getOffset() {
        return offset.isPresent() ? offset.get() : 0;
    }
    
    /**
     * 填充改写分页参数.
     *
     * @param parameters 参数
     */
    public void processParameters(final List<Object> parameters) {
        fill(parameters);
        rewrite(parameters);
    }
    
    private void fill(final List<Object> parameters) {
        int offset = -1 == offsetParameterIndex ? getOffset() : (int) parameters.get(offsetParameterIndex);
        int rowCount = -1 == rowCountParameterIndex ? this.rowCount : (int) parameters.get(rowCountParameterIndex);
        this.offset = Optional.of(offset);
        this.rowCount = rowCount;
        if (offset < 0 || rowCount < 0) {
            throw new SQLParsingException("LIMIT offset and row count can not be a negative value.");
        }
    }
    
    private void rewrite(final List<Object> parameters) {
        int rewriteOffset = 0;
        int rewriteRowCount = getOffset() + rowCount;
        if (offsetParameterIndex > -1) {
            parameters.set(offsetParameterIndex, rewriteOffset);
        }
        if (rowCountParameterIndex > -1) {
            parameters.set(rowCountParameterIndex, rewriteRowCount);
        }
    }
}
