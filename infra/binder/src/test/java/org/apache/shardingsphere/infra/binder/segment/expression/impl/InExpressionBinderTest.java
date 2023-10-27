package org.apache.shardingsphere.infra.binder.segment.expression.impl;

import org.apache.shardingsphere.infra.binder.enums.SegmentType;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class InExpressionBinderTest {

    @InjectMocks
    InExpressionBinder subject;

    @Test
    void bindTest() {
        Map<String, TableSegmentBinderContext> tableBinderContexts = new LinkedHashMap<>();
        InExpression actual = subject.bind(mock(InExpression.class), mock(SegmentType.class), mock(SQLStatementBinderContext.class), tableBinderContexts);

        Assertions.assertInstanceOf(InExpression.class, actual);
    }
}
