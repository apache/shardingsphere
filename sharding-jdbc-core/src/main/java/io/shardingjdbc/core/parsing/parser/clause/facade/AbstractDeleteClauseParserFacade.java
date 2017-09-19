package io.shardingjdbc.core.parsing.parser.clause.facade;

import io.shardingjdbc.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.WhereClauseParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Delete clause parser facade.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractDeleteClauseParserFacade {
    
    private final TableReferencesClauseParser tableReferencesClauseParser;
    
    private final WhereClauseParser whereClauseParser;
}
