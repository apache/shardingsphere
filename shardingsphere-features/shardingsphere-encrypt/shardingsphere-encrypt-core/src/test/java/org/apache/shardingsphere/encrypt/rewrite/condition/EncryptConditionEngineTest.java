package org.apache.shardingsphere.encrypt.rewrite.condition;

import org.apache.shardingsphere.encrypt.rule.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptConditionEngineTest {
    
    @Test
    public void assertCreateEncryptConditions() {
        new EncryptConditionEngine(mockEncryptRule(), new ShardingSphereSchema()).createEncryptConditions(mockSQLStatementContext());
    }
    
    private SQLStatementContext<?> mockSQLStatementContext() {
        SQLStatementContext<?> result = mock(SQLStatementContext.class);
        when(result instanceof WhereAvailable).thenReturn(true);
        when(((WhereAvailable) result).getWhereSegments()).thenReturn(Collections.singletonList(mockWhereSegment()));
        return result;
    }
    
    private WhereSegment mockWhereSegment() {
        WhereSegment result = mock(WhereSegment.class);
        return result;
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mockEncryptTable();
        when(result.findEncryptTable("test")).thenReturn(Optional.of(encryptTable));
        return result;
    }
    
    private EncryptTable mockEncryptTable() {
        EncryptTable result = mock(EncryptTable.class);
        when(result.findEncryptColumn("amount")).thenReturn(Optional.of(mockEncryptColumn()));
        return result;
    }
    
    private EncryptColumn mockEncryptColumn() {
        return new EncryptColumn(null, "cipher_amount", null, null, null, null, null, "ENCRYPT");
    }
}
