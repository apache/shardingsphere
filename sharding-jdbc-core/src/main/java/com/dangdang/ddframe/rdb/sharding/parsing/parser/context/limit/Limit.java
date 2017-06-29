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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import static com.dangdang.ddframe.rdb.sharding.util.NumberUtil.roundHalfUp;

/**
 * 分页对象.
 *
 * @author zhangliang
 * @author caohao
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public final class Limit {
    
    private final boolean rowCountRewriteFlag;
    
    private LimitValue offsetLimit;
    
    private LimitValue rowCountLimit;
    
    /**
     * 获取分页偏移量.
     * 
     * @return 分页偏移量
     */
    public int getOffsetValue() {
        return null != offsetLimit ? offsetLimit.getValue() : 0;
    }
    
    /**
     * 获取分页行数.
     *
     * @return 分页行数
     */
    public int getRowCountValue() {
        return null != rowCountLimit ? rowCountLimit.getValue() : 0;
    }
    
    /**
     * 填充改写分页参数.
     *
     * @param parameters 参数
     * @param isRewrite 是否重写参数
     */
    public void processParameters(final List<Object> parameters, final boolean isRewrite) {
        fill(parameters);
        if (isRewrite) {
            rewrite(parameters);
        }
    }
    
    private void fill(final List<Object> parameters) {
        int offset = 0;
        if (null != offsetLimit) {
            offset = -1 == offsetLimit.getIndex() ? getOffsetValue() : roundHalfUp(parameters.get(offsetLimit.getIndex()));
            offsetLimit.setValue(offset);
        }
        int rowCount = 0;
        if (null != rowCountLimit) {
            rowCount = -1 == rowCountLimit.getIndex() ? getRowCountValue() : roundHalfUp(parameters.get(rowCountLimit.getIndex()));
            rowCountLimit.setValue(rowCount);
        }
        if (offset < 0 || rowCount < 0) {
            throw new SQLParsingException("LIMIT offset and row count can not be a negative value.");
        }
    }
    
    private void rewrite(final List<Object> parameters) {
        int rewriteOffset = 0;
        int rewriteRowCount;
        if (rowCountRewriteFlag) {
            rewriteRowCount = null == rowCountLimit ? -1 : getOffsetValue() + rowCountLimit.getValue();
        } else {
            rewriteRowCount = rowCountLimit.getValue();
        }
        if (null != offsetLimit && offsetLimit.getIndex() > -1) {
            parameters.set(offsetLimit.getIndex(), rewriteOffset);
        }
        if (null != rowCountLimit && rowCountLimit.getIndex() > -1) {
            parameters.set(rowCountLimit.getIndex(), rewriteRowCount);
        }
    }
}
