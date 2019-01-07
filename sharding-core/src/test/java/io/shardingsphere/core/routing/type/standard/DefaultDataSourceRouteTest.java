package io.shardingsphere.core.routing.type.standard;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import io.shardingsphere.core.routing.SQLRouteResult;

public class DefaultDataSourceRouteTest extends AbstractSQLRouteTest {
    
    @Test
    public void assertDefaultRoute() {
        String sql = "SELECT id,name from user where id = ?";
        List<Object> parameters = new LinkedList<>();
        parameters.add(1);
        SQLRouteResult result = route(sql, parameters);
        assertEquals("assert default datasource name", "main", result.getRouteUnits().iterator().next().getDataSourceName());
    }
}
