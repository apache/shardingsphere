/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.clause.facade;

import io.shardingsphere.core.parsing.parser.clause.GroupByClauseParser;
import io.shardingsphere.core.parsing.parser.clause.HavingClauseParser;
import io.shardingsphere.core.parsing.parser.clause.OrderByClauseParser;
import io.shardingsphere.core.parsing.parser.clause.SelectListClauseParser;
import io.shardingsphere.core.parsing.parser.clause.SelectRestClauseParser;
import io.shardingsphere.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingsphere.core.parsing.parser.clause.WhereClauseParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Select clause parser facade.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractSelectClauseParserFacade {
    
    private final SelectListClauseParser selectListClauseParser;
    
    private final TableReferencesClauseParser tableReferencesClauseParser;
    
    private final WhereClauseParser whereClauseParser;
    
    private final GroupByClauseParser groupByClauseParser;
    
    private final HavingClauseParser havingClauseParser;
    
    private final OrderByClauseParser orderByClauseParser;
    
    private final SelectRestClauseParser selectRestClauseParser;
}
