package io.shardingsphere.core.routing.type.standard;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import io.shardingsphere.core.routing.SQLRouteResult;

public class SQLRouteTest extends AbstractSQLRouteTest {
    
    @Test
    public void assertDefaultDataSourceRoute() {
        String sql = "SELECT id,name from user where id = ?";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        SQLRouteResult result = route(sql, parameters);
        assertEquals("assert default datasource name", "main", result.getRouteUnits().iterator().next().getDataSourceName());
    }
    
    @Test
    public void assertWithBroadcastTable() {
        String sql = "SELECT id,name from t_order_item a join product b on a.product_id = b.product_id where user_id = ?";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        route(sql, parameters);
    }
    
    @Test
    public void assertAllBindingWithBroadcastTable() {
        String sql = "SELECT id,name from t_order a join t_order_item b on a.order_id = b.order_id join t_product c on b.product_id = c.product_id where a.user_id = ?";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        route(sql, parameters);
    }
    
    @Test
    public void assertComplexTableWithBroadcastTable() {
        String sql = "SELECT id,name from t_order a join t_user b on a.user_id = b.user_id join t_product c on a.product_id = c.product_id where a.user_id = ? and b.user_id =?";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(1);
        route(sql, parameters);
    }
}
