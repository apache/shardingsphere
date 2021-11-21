package org.apache.shardingsphere.encrypt.rewrite.parameter.impl;

import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptAssignmentParameterRewriterTest {

    @InjectMocks
    private EncryptAssignmentParameterRewriter encryptAssignmentParameterRewriter;

    @Test
    public void isNeedRewriteForEncryptForUpdateContextTest() {
        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);
        final boolean result = encryptAssignmentParameterRewriter.isNeedRewriteForEncrypt(updateStatementContext);
        assertTrue(result);
    }

    @Test
    public void isNeedRewriteForEncryptForInsertContextTest() {
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class);
        final SetAssignmentSegment setAssignmentSegment = mock(SetAssignmentSegment.class);


        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getSetAssignment()).thenReturn(Optional.of(setAssignmentSegment));

        final boolean result = encryptAssignmentParameterRewriter.isNeedRewriteForEncrypt(insertStatementContext);
        assertTrue(result);
    }

    @Test
    public void isNeedRewriteForEncryptForDeleteContextTest() {
        final DeleteStatementContext deleteStatementContext = mock(DeleteStatementContext.class);
        final boolean result = encryptAssignmentParameterRewriter.isNeedRewriteForEncrypt(deleteStatementContext);
        assertFalse(result);
    }
}
