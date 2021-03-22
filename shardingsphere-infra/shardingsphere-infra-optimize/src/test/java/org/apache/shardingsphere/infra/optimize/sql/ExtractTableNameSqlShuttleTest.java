package org.apache.shardingsphere.infra.optimize.sql;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ExtractTableNameSqlShuttleTest {
    
    @Test
    public void testVisitFromWithIdentifier() throws SqlParseException {
        String sql = "select 10 + 30 as prefix, t_order.order_id + 10, t_order.order_id, t_order.user_id from t_order where t_order.status='FINISHED' " +
                "and 1=1 order by t_order.order_id desc";
        
        SqlParser parser = SqlParser.create(sql);
        SqlNode calciteSqlNode = parser.parseQuery();
        Assert.assertNotNull(calciteSqlNode);
    
        ExtractTableNameSqlShuttle extractTableNameSqlShuttle = new ExtractTableNameSqlShuttle();
        SqlNode newNode = calciteSqlNode.accept(extractTableNameSqlShuttle);
        Assert.assertEquals(Arrays.asList("T_ORDER"), extractTableNameSqlShuttle.getTableNames());
        // TODO check result SqlNode
    }
    
    @Test
    public void testVisitFromWithAsBasicCall() throws SqlParseException {
        String sql = "select 10 + 30 as prefix, o1.order_id + 10, o1.order_id, o1.user_id from t_order as o1 where o1.status='FINISHED' " +
                "and 1=1 order by o1.order_id desc";
        
        SqlParser parser = SqlParser.create(sql);
        SqlNode calciteSqlNode = parser.parseQuery();
        Assert.assertNotNull(calciteSqlNode);
        
        ExtractTableNameSqlShuttle extractTableNameSqlShuttle = new ExtractTableNameSqlShuttle();
        SqlNode newNode = calciteSqlNode.accept(extractTableNameSqlShuttle);
        Assert.assertEquals(Arrays.asList("T_ORDER"), extractTableNameSqlShuttle.getTableNames());
        // TODO check result SqlNode
    }
    
    @Test
    public void testVisitFromWithJoinIdentifier() throws SqlParseException {
        // TODO 
        // TODO check result SqlNode
    }
    
    @Test
    public void testVisitFromWithJoinAsBasicCall() throws SqlParseException {
        String sql = "select 10 + 30, o1.order_id + 10, o1.order_id, o1.user_id, o2.status from t_order o1 join t_order_item o2 on "
                + "o1.order_id = o2.order_id where o1.status='FINISHED' and o2.order_item_id > 1024 and 1=1 order by "
                + "o1.order_id desc";
        
        SqlParser parser = SqlParser.create(sql);
        SqlNode calciteSqlNode = parser.parseQuery();
        Assert.assertNotNull(calciteSqlNode);
        
        ExtractTableNameSqlShuttle extractTableNameSqlShuttle = new ExtractTableNameSqlShuttle();
        SqlNode newNode = calciteSqlNode.accept(extractTableNameSqlShuttle);
        Assert.assertEquals(Arrays.asList("T_ORDER", "T_ORDER_ITEM"), extractTableNameSqlShuttle.getTableNames());
        // TODO check result SqlNode
    }
}
