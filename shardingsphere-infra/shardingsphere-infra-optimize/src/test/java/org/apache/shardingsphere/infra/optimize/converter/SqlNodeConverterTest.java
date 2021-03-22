package org.apache.shardingsphere.infra.optimize.converter;

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

/**
 * testcase of converting shardingshphere ast to calcite ast.
 *
 * after converting phrase finished, the next phrase is comparing  the converted result with the
 * result of calcite parser.
 *
 * //String sql = "select 10 + 30, o1.order_id, o1.user_id, o2.status from t_order o1 join t_order_item o2 on o1
 * .order_id = o2.order_id "
 *         //    + "where o1.status='FINISHED' and o2.order_item_id > 1024 and 1=1 order by o1.order_id desc";
 *         // String sql = "select 10 + 30, o1.order_id, o1.user_id from t_order o1 where o1.status='FINISHED' and o1
 *         .user_id > 1024 and 1=1 order by o1.order_id desc";
 *         String sql = "select '20 + 20', o1.order_id + 1, 10 + 30, o1.order_id, o1.user_id from t_order o1 where o1
 *         .status='FINISHED' and o1.user_id > 1024 and 1=1 order by o1.order_id desc";
 *
 */
public final class SqlNodeConverterTest extends AbstractSchemaTest {

    ShardingSphereSchema schema;

    ShardingSphereSQLParserEngine sqlStatementParserEngine;

    @Before
    public void init() {
        schema = buildSchema();
        sqlStatementParserEngine = new ShardingSphereSQLParserEngine(DatabaseTypeRegistry.getTrunkDatabaseTypeName(
                new MySQLDatabaseType()));
    }

    @Test
    public void testConvertSimpleSelect() throws SqlParseException {
        String sql = "select 10 + 30, o1.order_id + 10, o1.order_id, o1.user_id, o2.status from t_order o1 join t_order_item o2 on "
                + "o1.order_id = o2.order_id where o1.status='FINISHED' and o2.order_item_id > 1024 and 1=1 order by "
                + "o1.order_id desc";

        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(schema, Collections.emptyList(), sqlStatement);


        // compare ast from calcite parser and ast converted from ss ast if possible
        SqlParser parser = SqlParser.create(sql);
        SqlNode calciteSqlNode = parser.parseQuery();
        Assert.assertNotNull(calciteSqlNode);

        Optional<SqlNode> optional = SqlNodeConverter.convertSqlStatement(sqlStatementContext);
        Assert.assertTrue(optional.isPresent());
    }

}