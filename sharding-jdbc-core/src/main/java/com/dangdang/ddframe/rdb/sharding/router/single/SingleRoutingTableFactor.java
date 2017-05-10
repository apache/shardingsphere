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

package com.dangdang.ddframe.rdb.sharding.router.single;

import com.dangdang.ddframe.rdb.sharding.rewrite.SQLBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 单表路由表单元.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
@ToString(exclude = "builder")
public class SingleRoutingTableFactor {
    
    @Getter
    private final String logicTable;
    
    @Getter
    private final String actualTable;
    
    private SQLBuilder builder;
    
    /**
     * 修改SQL.
     * 
     * @param builder SQL构建器
     * @return 单表路由表单元
     */
    public SingleRoutingTableFactor replaceSQL(final SQLBuilder builder) {
        builder.recordNewToken(logicTable, actualTable);
        this.builder = builder;
        return this;
    }
    
    /**
     * 构建SQL.
     * 
     * @return SQL构建器
     */
    SQLBuilder buildSQL() {
        return builder.buildSQLWithNewToken();
    }
}
