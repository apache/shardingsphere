/**
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

package com.dangdang.ddframe.rdb.sharding.merger.groupby;

import java.lang.reflect.Method;
import java.sql.SQLException;

import com.dangdang.ddframe.rdb.sharding.merger.common.AbstractMergerInvokeHandler;
import com.dangdang.ddframe.rdb.sharding.merger.common.ResultSetQueryIndex;
import com.dangdang.ddframe.rdb.sharding.merger.common.ResultSetUtil;

/**
 * 分组函数动态代理.
 * 
 * @author gaohongtao
 */
public class GroupByInvokeHandler extends AbstractMergerInvokeHandler<GroupByResultSet> {
    
    public GroupByInvokeHandler(final GroupByResultSet groupByResultSet) {
        super(groupByResultSet);
    }
    
    @Override
    protected Object doMerge(final GroupByResultSet groupByResultSet, final Method method, final ResultSetQueryIndex resultSetQueryIndex) throws ReflectiveOperationException, SQLException {
        // TODO 更新文档：The column is not aggregation function, get first result set 
        return ResultSetUtil.convertValue(groupByResultSet.getCurrentGroupByResultSet().getValue(resultSetQueryIndex), method.getReturnType());
    }
}
