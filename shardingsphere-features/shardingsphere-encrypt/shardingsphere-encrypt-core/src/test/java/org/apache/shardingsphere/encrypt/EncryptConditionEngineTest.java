package org.apache.shardingsphere.encrypt.rewrite.condition;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptConditionEngineTest {

    @InjectMocks
    private EncryptConditionEngine encryptConditionEngine;

    @Mock
    private EncryptRule encryptRule;

    @Mock
    private ShardingSphereSchema shardingSphereSchema;

    @Mock
    private SQLStatementContext sqlStatementContext;

    @Mock
    private InsertStatementContext insertStatementContext;

    @Mock
    private InsertSelectContext insertSelectContext;

    @Mock
    private SelectStatementContext selectStatementContext;

    @Mock
    private TablesContext tablesContext;


    /**
     * test encrypt conditions creation when sql statement context has no where clause or inserts.
     */
    @Test
    public void createEncryptConditionsWithEmptyContextTest() {
        final Collection<EncryptCondition> encryptConditions = encryptConditionEngine.createEncryptConditions(sqlStatementContext);
        assertEquals(0, encryptConditions.size());
    }

    /**
     * test encrypt conditions creation when sql statement context has no where clause but has inserts.
     */
    @Test
    public void createEncryptConditionsFromInsertStatementContextTest() {
        when(insertStatementContext.getInsertSelectContext()).thenReturn(insertSelectContext);
        final Collection<EncryptCondition> encryptConditions = encryptConditionEngine.createEncryptConditions(insertStatementContext);
        assertEquals(0, encryptConditions.size());
    }

    /**
     * test encrypt conditions creation when sql statement context has no where clause but with inserts without wheres.
     */
    @Test
    public void createEncryptConditionsFromInsertIncludingSelectWithoutWhereTest() {
        when(insertStatementContext.getInsertSelectContext()).thenReturn(insertSelectContext);
        when(insertSelectContext.getSelectStatementContext()).thenReturn(selectStatementContext);
        final Collection<EncryptCondition> encryptConditions = encryptConditionEngine.createEncryptConditions(insertStatementContext);
        assertEquals(0, encryptConditions.size());
    }

    /**
     * test encrypt conditions creation when sql statement context has no where clause but with inserts with wheres.
     */
    @Test
    public void createEncryptConditionsFromInsertIncludingSelectWhereTest() {
        when(insertStatementContext.getInsertSelectContext()).thenReturn(insertSelectContext);
        when(insertSelectContext.getSelectStatementContext()).thenReturn(selectStatementContext);
        ExpressionSegment expressionSegment = new InExpression(0, 0, null, null, false);
        WhereSegment whereSegment = new WhereSegment(0, 1, expressionSegment);
        when(selectStatementContext.getWhere()).thenReturn(Optional.of(whereSegment));
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        final Collection<EncryptCondition> encryptConditions = encryptConditionEngine.createEncryptConditions(insertStatementContext);
        assertEquals(0, encryptConditions.size());
    }
}