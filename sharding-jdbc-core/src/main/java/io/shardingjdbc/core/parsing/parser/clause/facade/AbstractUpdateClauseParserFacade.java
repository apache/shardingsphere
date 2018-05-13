package io.shardingjdbc.core.parsing.parser.clause.facade;

import io.shardingjdbc.core.parsing.parser.clause.UpdateSetItemsClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.WhereClauseParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Update clause parser facade.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractUpdateClauseParserFacade {
    
    private final TableReferencesClauseParser tableReferencesClauseParser;
    
    private final UpdateSetItemsClauseParser updateSetItemsClauseParser;
    
    private final WhereClauseParser whereClauseParser;
}
