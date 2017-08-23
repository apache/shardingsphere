package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * SELECT从句解析器门面类.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class SelectClauseParserFacade {
    
    private final DistinctClauseParser distinctClauseParser;
    
    private final SelectListClauseParser selectListClauseParser;
    
    private final TableClauseParser tableClauseParser;
    
    private final WhereClauseParser whereClauseParser;
    
    private final GroupByClauseParser groupByClauseParser;
    
    private final HavingClauseParser havingClauseParser;
    
    private final OrderByClauseParser orderByClauseParser;
    
    private final SelectRestClauseParser selectRestClauseParser;
}
