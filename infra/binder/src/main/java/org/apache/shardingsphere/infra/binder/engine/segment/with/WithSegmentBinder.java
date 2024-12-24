package org.apache.shardingsphere.infra.binder.engine.segment.with;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.expression.type.CommonTableExpressionBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.syntax.UniqueCommonTableExpressionException;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor( access = AccessLevel.PRIVATE )
public final class WithSegmentBinder {

    public static WithSegment bind(final WithSegment segment , SQLStatementBinderContext binderContext, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts)
    {
        Collection<CommonTableExpressionSegment> commonTableExpressions = new ArrayList<>();

        segment.getCommonTableExpressions().forEach(optional -> {
            ShardingSpherePreconditions.checkNotContains( binderContext.getCommonTableExpressionsSegmentsUniqueAliases(), optional.getAliasName() , () -> new UniqueCommonTableExpressionException( optional.getAliasName().get() ));
            binderContext.getCommonTableExpressionsSegmentsUniqueAliases().add(new CaseInsensitiveString(optional.getAliasName().get()));
            commonTableExpressions.add( CommonTableExpressionBinder.bind( optional , binderContext , outerTableBinderContexts ) ); });
        return new WithSegment( segment.getStartIndex() , segment.getStopIndex() , commonTableExpressions , segment.isRecursive() );
    }
}
