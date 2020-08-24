package org.apache.shardingsphere.encrypt.merge.dql;

import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncryptDQLResultDecoratorTest{
    private  EncryptAlgorithmMetaData metaData;

    private  boolean queryWithCipherColumn;

    @Test
    public void assertDecorateQueryResult() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.next()).thenReturn(true);
        EncryptDQLResultDecorator decorator = new EncryptDQLResultDecorator(metaData,queryWithCipherColumn);
        MergedResult actual = decorator.decorate(queryResult, mock(SQLStatementContext.class), mock(SchemaMetaData.class));
        assertTrue(actual.next());
    }

    @Test
    public void assertDecorateMergedResult() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        when(mergedResult.next()).thenReturn(true);
        EncryptDQLResultDecorator decorator = new EncryptDQLResultDecorator(metaData,queryWithCipherColumn);
        MergedResult actual = decorator.decorate(mergedResult, mock(SQLStatementContext.class), mock(SchemaMetaData.class));
        assertTrue(actual.next());
    }
}
