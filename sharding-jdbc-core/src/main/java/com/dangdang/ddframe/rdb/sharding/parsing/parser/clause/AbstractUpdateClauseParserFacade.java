package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * UPDATE从句解析器门面类.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractUpdateClauseParserFacade {
    
    private final TableClauseParser tableClauseParser;
    
    private final SetItemsClauseParser setItemsClauseParser;
    
    private final WhereClauseParser whereClauseParser;
}
