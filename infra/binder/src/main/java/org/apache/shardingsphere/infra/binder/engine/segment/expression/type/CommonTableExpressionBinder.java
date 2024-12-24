package org.apache.shardingsphere.infra.binder.engine.segment.expression.type;

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;

@NoArgsConstructor( access = AccessLevel.PRIVATE )
public final class CommonTableExpressionBinder {
    public static CommonTableExpressionSegment bind(CommonTableExpressionSegment segment , SQLStatementBinderContext binderContext , final Multimap<CaseInsensitiveMap.CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts)
    {
        SubquerySegment subquerySegment = SubquerySegmentBinder.bind( segment.getSubquery() , binderContext , outerTableBinderContexts );
        return new CommonTableExpressionSegment( segment.getStartIndex() , segment.getStopIndex(), segment.getAliasSegment() , subquerySegment );
    }
}
