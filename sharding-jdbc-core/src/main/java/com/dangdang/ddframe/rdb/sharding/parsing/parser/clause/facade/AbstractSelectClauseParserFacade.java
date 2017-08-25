package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.DistinctClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.GroupByClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.HavingClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.OrderByClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SelectListClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SelectRestClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.TableReferencesClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.WhereClauseParser;
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
    
    private final DistinctClauseParser distinctClauseParser;
    
    private final SelectListClauseParser selectListClauseParser;
    
    private final TableReferencesClauseParser tableReferencesClauseParser;
    
    private final WhereClauseParser whereClauseParser;
    
    private final GroupByClauseParser groupByClauseParser;
    
    private final HavingClauseParser havingClauseParser;
    
    private final OrderByClauseParser orderByClauseParser;
    
    private final SelectRestClauseParser selectRestClauseParser;
}
