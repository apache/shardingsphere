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

package com.dangdang.ddframe.rdb.sharding.rewrite;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.LimitContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;

import java.util.List;

/**
 * 补列工具类.
 *
 * @author zhangliang
 */
public final class LimitUtils {
    
    /**
     * 追加分页.
     * 
     * @param sqlContext SQL上下文
     * @param parameters 参数
     */
    public static void appendLimit(final SQLContext sqlContext, final List<Object> parameters) {
        int offset = -1 == sqlContext.getLimitContext().getOffsetParameterIndex()
                ? sqlContext.getLimitContext().getOffset() : (int) parameters.get(sqlContext.getLimitContext().getOffsetParameterIndex());
        int rowCount = -1 == sqlContext.getLimitContext().getRowCountParameterIndex()
                ? sqlContext.getLimitContext().getRowCount() : (int) parameters.get(sqlContext.getLimitContext().getRowCountParameterIndex());
        sqlContext.setLimitContext(new LimitContext(offset, rowCount, sqlContext.getLimitContext().getOffsetParameterIndex(), sqlContext.getLimitContext().getRowCountParameterIndex()));
        if (offset < 0 || rowCount < 0) {
            throw new SQLParsingException("LIMIT offset and row count can not be a negative value.");
        }
    }
}
