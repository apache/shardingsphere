package org.apache.shardingsphere.infra.optimize.converter;

import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlExplainFormat;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.optimize.schema.AbstractSchemaTest;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;


public class RelNodeConverterTest extends AbstractSchemaTest {

    RelNodeConverter relNodeConverter;

    ShardingSphereSQLParserEngine sqlStatementParserEngine;
    
    ShardingSphereSchema schema;

    @Before
    public void init() {
        schema = buildSchema();

        sqlStatementParserEngine = new ShardingSphereSQLParserEngine(DatabaseTypeRegistry.getTrunkDatabaseTypeName(
                new MySQLDatabaseType()));
        
        relNodeConverter = new RelNodeConverter("logical_db", schema);
    }

    @Test
    public void testValidateAndConvert() {

        String sql = "select 10 + 30, o1.order_id + 10, o1.order_id, sum(o1.user_id), o2.status from t_order o1 join t_order_item o2 on "
                + "o1.order_id = o2.order_id where o1.status='FINISHED' and o2.order_item_id > 1024 and 1=1 order by "
                + "o1.order_id desc";

        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(schema, Collections.emptyList(), sqlStatement);
        Optional<SqlNode> optional = SqlNodeConverter.convertSqlStatement(sqlStatementContext);
        Assert.assertTrue(optional.isPresent());
        RelNode relNode = relNodeConverter.validateAndConvert(optional.get());
        Assert.assertNotNull(relNode);
        String desc = RelOptUtil.dumpPlan("", relNode, SqlExplainFormat.TEXT, SqlExplainLevel.EXPPLAN_ATTRIBUTES);
        Assert.assertNotNull(desc);
    }

    @Test
    public void testCalciteGroupByRel() throws SqlParseException {
        String sql = "select 10 + 30, sum(o1.user_id) from t_order o1 join t_order_item o2 on "
                + "o1.order_id = o2.order_id where o1.status='FINISHED' and o2.order_item_id > 1024 and 1=1 group by o1.user_id having  o1.user_id > 13" +
                "  order by o1.user_id desc";

        // compare ast from calcite parser and ast converted from ss ast if possible
        SqlParser parser = SqlParser.create(sql, SqlParser.config().withCaseSensitive(false).withQuotedCasing(Casing.UNCHANGED).withUnquotedCasing(Casing.UNCHANGED));
        SqlNode calciteSqlNode = parser.parseQuery();
        Assert.assertNotNull(calciteSqlNode);

        RelNode relNode = relNodeConverter.validateAndConvert(calciteSqlNode);
        Assert.assertNotNull(relNode);
        String desc = RelOptUtil.dumpPlan("", relNode, SqlExplainFormat.TEXT, SqlExplainLevel.EXPPLAN_ATTRIBUTES);
        Assert.assertNotNull(desc);



    }

    @Test
    public void testSum() throws SqlParseException {
        String sql = "select 10 + 30, sum(o1.order_id) from t_order o1 join t_order_item o2 on "
                + "o1.order_id = o2.order_id where o1.status='FINISHED' and o2.order_item_id > 1024 and 1=1 ";
        SqlParser parser = SqlParser.create(sql, SqlParser.config().withCaseSensitive(false).withQuotedCasing(Casing.UNCHANGED).withUnquotedCasing(Casing.UNCHANGED));
        SqlNode calciteSqlNode = parser.parseQuery();
        Assert.assertNotNull(calciteSqlNode);

        RelNode relNode = relNodeConverter.validateAndConvert(calciteSqlNode);
        String desc = RelOptUtil.dumpPlan("", relNode, SqlExplainFormat.TEXT, SqlExplainLevel.EXPPLAN_ATTRIBUTES);
        Assert.assertNotNull(desc);
    }
}