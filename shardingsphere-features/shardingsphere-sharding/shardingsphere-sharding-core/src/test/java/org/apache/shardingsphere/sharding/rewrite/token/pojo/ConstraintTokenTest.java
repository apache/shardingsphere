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

import static org.mockito.Mockito.mock;

public class ConstraintTokenTest {

    @Test
    public void assertConstraintToken() {
        ConstraintToken constraintToken = new ConstraintToken(0, 1, new IdentifierValue("uc"), mock(SQLStatementContext.class), mock(ShardingRule.class));
        assertThat(constraintToken.toString(getRouteUnit()), is("uc_t_order_0"));
        assertTokenGrid(constraintToken);
    }

    private void assertTokenGrid(ConstraintToken constraintToken) {
        assertThat(constraintToken.getStopIndex(), is(1));
        assertThat(constraintToken.getStartIndex(), is(0));
    }

    private RouteUnit getRouteUnit() {
        return new RouteUnit(new RouteMapper("logic_db", "logic_db"), Collections.singletonList(new RouteMapper("t_order", "t_order_0")));
    }

}