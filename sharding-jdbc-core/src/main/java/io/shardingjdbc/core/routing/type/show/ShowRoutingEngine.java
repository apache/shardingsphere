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

package io.shardingjdbc.core.routing.type.show;

import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.ShowStatement;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.ShowType;
import io.shardingjdbc.core.routing.type.RoutingEngine;
import io.shardingjdbc.core.routing.type.RoutingResult;
import io.shardingjdbc.core.routing.type.TableUnit;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Show routing engine.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ShowRoutingEngine implements RoutingEngine {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShowStatement showStatement;
    
    @Override
    public RoutingResult route() {
        RoutingResult result = new RoutingResult();
        // TODO databases don't need route
        if (ShowType.DATABASES == showStatement.getShowType() || ShowType.TABLES == showStatement.getShowType()) {
            for (String each : dataSourceMap.keySet()) {
                result.getTableUnits().getTableUnits().add(new TableUnit(each, "", ""));
            }
        } else {
            result.getTableUnits().getTableUnits().add(new TableUnit(dataSourceMap.keySet().iterator().next(), "", ""));
        }
        return result;
    }
}
