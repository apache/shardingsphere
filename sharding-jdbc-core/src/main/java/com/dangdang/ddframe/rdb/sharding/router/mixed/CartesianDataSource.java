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

package com.dangdang.ddframe.rdb.sharding.router.mixed;

import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.router.SQLExecutionUnit;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 笛卡尔积路由数据源.
 * 
 * @author zhangliang
 */
@Getter
@ToString
final class CartesianDataSource {
    
    private final String dataSource;
    
    private final List<CartesianTableReference> routingTableReferences;
    
    CartesianDataSource(final String dataSource, final CartesianTableReference routingTableReference) {
        this.dataSource = dataSource;
        routingTableReferences = new ArrayList<>(Collections.singletonList(routingTableReference));
    }
    
    Collection<SQLExecutionUnit> getSQLExecutionUnits(final SQLBuilder sqlBuilder) {
        Collection<SQLExecutionUnit> result = new ArrayList<>();
        for (CartesianTableReference each : routingTableReferences) {
            each.buildSQL(sqlBuilder);
            result.add(new SQLExecutionUnit(dataSource, sqlBuilder.toSQL()));
        }
        return result;
    }
}
