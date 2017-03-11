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

package com.dangdang.ddframe.rdb.sharding.parser.sql.context;

import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLStatementType;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Select SQL上下文.
 *
 * @author zhangliang
 */
@Getter
@Setter
public final class SelectSQLContext extends AbstractSQLContext {
    
    private boolean distinct;
    
    private boolean containStar;
    
    private int selectListLastPosition;
    
    private final List<SelectItemContext> itemContexts = new LinkedList<>();
    
    private final List<GroupByContext>  groupByContexts = new LinkedList<>();
    
    private final List<OrderByContext>  orderByContexts = new LinkedList<>();
    
    private LimitContext limitContext;
    
    private SelectSQLContext parent;
    
    private List<SelectSQLContext> children = new LinkedList<>();
    
    public SelectSQLContext(final String originalSQL) {
        super(originalSQL);
    }
    
    @Override
    public SQLStatementType getType() {
        return SQLStatementType.SELECT;
    }
}
