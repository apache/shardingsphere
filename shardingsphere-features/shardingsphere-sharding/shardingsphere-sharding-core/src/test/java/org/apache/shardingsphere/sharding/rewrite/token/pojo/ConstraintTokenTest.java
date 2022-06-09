package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class ConstraintTokenTest {
    
    @Test
    public void assertToString() {
        assertThat(new ConstraintToken(0, 1, new IdentifierValue("uc"), mock(SQLStatementContext.class, RETURNS_DEEP_STUBS), mock(ShardingRule.class)).toString(getRouteUnit()), is("uc"));
    }
    
    private RouteUnit getRouteUnit() {
        return new RouteUnit(new RouteMapper("logic_db", "logic_db"), Collections.singletonList(new RouteMapper("t_order", "t_order_0")));
    }
}
