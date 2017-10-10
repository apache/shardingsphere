package io.shardingjdbc.core.parsing.parser.clause.facade;

import io.shardingjdbc.core.parsing.parser.clause.DistinctClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.GroupByClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.HavingClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.OrderByClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.SelectListClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.SelectRestClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.WhereClauseParser;
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
