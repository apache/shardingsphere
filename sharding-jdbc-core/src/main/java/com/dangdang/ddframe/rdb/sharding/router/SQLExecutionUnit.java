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

package com.dangdang.ddframe.rdb.sharding.router;

import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * SQL最小执行单元.
 * 
 * @author gaohongtao
 */
@Getter
@Slf4j
@ToString
@EqualsAndHashCode(exclude = "sqlBuilder")
public class SQLExecutionUnit {
    
    private final String dataSource;
    
    @Setter
    private String sql;
    
    private final SQLBuilder sqlBuilder;
    
    public SQLExecutionUnit(final String dataSource, final SQLBuilder sqlBuilder) {
        this.dataSource = dataSource;
        this.sqlBuilder = sqlBuilder.cloneBuilder();
        sql = sqlBuilder.toSQL();
        log.trace("route sql to db: [{}] sql: [{}]", dataSource, sql);
    }
}
