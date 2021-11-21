package org.apache.shardingsphere.encrypt.rewrite.context;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.SQLTokenGenerators;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptSQLRewriteContextDecoratorTest {

    @InjectMocks
    private EncryptSQLRewriteContextDecorator encryptSQLRewriteContextDecorator;

    @Mock
    private EncryptRule encryptRule;

    @Mock
    private ConfigurationProperties configurationProperties;

    @Mock
    private RouteContext routeContext;

    @Mock
    private SQLTokenGenerators sqlTokenGenerators;

    @Test
    public void decorateWithoutRewritingTest() {
        final ShardingSphereSchema shardingSphereSchema = Mockito.mock(ShardingSphereSchema.class);
        final DeleteStatementContext deleteStatementContext = Mockito.mock(DeleteStatementContext.class);

        SQLRewriteContext rewriteContext = new SQLRewriteContext(shardingSphereSchema, deleteStatementContext, "query", new ArrayList<>());
        encryptSQLRewriteContextDecorator.decorate(encryptRule, configurationProperties, rewriteContext, routeContext);
        assertEquals(0, rewriteContext.getSqlTokens().size());
    }

    @Test
    public void decorateWithRewritingTest() {
        final ShardingSphereSchema shardingSphereSchema = Mockito.mock(ShardingSphereSchema.class);
        final UpdateStatementContext updateStatementContext = Mockito.mock(UpdateStatementContext.class);
        final TablesContext tablesContext = mock(TablesContext.class);
        final EncryptTable encryptTable = mock(EncryptTable.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final UpdateStatement updateStatement = mock(UpdateStatement.class);
        final SetAssignmentSegment setAssignmentSegment = mock(SetAssignmentSegment.class);

        when(updateStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.getTableNames()).thenReturn(Collections.singletonList("table1"));
        when(encryptRule.findEncryptTable(anyString())).thenReturn(Optional.of(encryptTable));
        when(updateStatementContext.getAllTables()).thenReturn(Collections.singletonList(simpleTableSegment));
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        IdentifierValue identifierValue = new IdentifierValue("table1");
        when(tableNameSegment.getIdentifier()).thenReturn(identifierValue);
        when(updateStatementContext.getSchemaName()).thenReturn("schema1");
        when(updateStatementContext.getSqlStatement()).thenReturn(updateStatement);
        when(updateStatement.getSetAssignment()).thenReturn(setAssignmentSegment);

        List<Object> parameters = new ArrayList<>();
        parameters.add("abc");

        SQLRewriteContext rewriteContext = new SQLRewriteContext(shardingSphereSchema, updateStatementContext, "query", parameters);
        encryptSQLRewriteContextDecorator.decorate(encryptRule, configurationProperties, rewriteContext, routeContext);
        assertEquals(0, rewriteContext.getSqlTokens().size());
    }

    @Test
    public void getOrderTest() {
        final int order = encryptSQLRewriteContextDecorator.getOrder();
        assertEquals(10, order);
    }

    @Test
    public void getTypeClassTest() {
        final Class<EncryptRule> typeClass = encryptSQLRewriteContextDecorator.getTypeClass();
        assertEquals("EncryptRule", typeClass.getSimpleName());
    }
}
