package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


class ShardingInPredicateTokenTest {

    private ShardingInPredicateToken shardingInPredicateToken;
    private RouteUnit routeUnit1;
    private RouteUnit routeUnit2;

    @BeforeEach
    void setUp() {
        routeUnit1 = createRouteUnit("t_order_0");
        routeUnit2 = createRouteUnit("t_order_1");

        Map<RouteUnit, Map<String, List<ShardingInPredicateValue>>> columnParameterMap = new HashMap<>();

        Map<String, List<ShardingInPredicateValue>> params1 = new LinkedHashMap<>();
        List<ShardingInPredicateValue> orderIdParams1 = Arrays.asList(
                new ShardingInPredicateValue(-1, 2L, false),
                new ShardingInPredicateValue(-1, 4L, false)
        );
        params1.put("order_id", orderIdParams1);
        columnParameterMap.put(routeUnit1, params1);

        Map<String, List<ShardingInPredicateValue>> params2 = new LinkedHashMap<>();
        List<ShardingInPredicateValue> orderIdParams2 = Arrays.asList(
                new ShardingInPredicateValue(-1, 1L, false),
                new ShardingInPredicateValue(-1, 3L, false)
        );
        params2.put("order_id", orderIdParams2);
        columnParameterMap.put(routeUnit2, params2);

        shardingInPredicateToken = new ShardingInPredicateToken(10, 30, columnParameterMap);
    }

    @Test
    void assertToStringWithOptimizedValues() {
        String result = shardingInPredicateToken.toString(routeUnit1);
        assertThat(result, is("order_id IN (2, 4)"));
    }

    @Test
    void assertToStringWithDifferentValues() {
        String result = shardingInPredicateToken.toString(routeUnit2);
        assertThat(result, is("order_id IN (1, 3)"));
    }

    @Test
    void assertToStringWithEmptyParameters() {
        Map<RouteUnit, Map<String, List<ShardingInPredicateValue>>> emptyMap = new HashMap<>();
        Map<String, List<ShardingInPredicateValue>> emptyParams = new LinkedHashMap<>();
        emptyParams.put("order_id", Collections.emptyList());
        emptyMap.put(routeUnit1, emptyParams);

        ShardingInPredicateToken emptyToken = new ShardingInPredicateToken(10, 30, emptyMap);
        String result = emptyToken.toString(routeUnit1);

        assertThat(result, is("order_id IN (NULL) AND 1 = 0"));
    }

    @Test
    void assertGetStartAndStopIndex() {
        assertThat(shardingInPredicateToken.getStartIndex(), is(10));
        assertThat(shardingInPredicateToken.getStopIndex(), is(30));
    }

    private RouteUnit createRouteUnit(final String actualTableName) {
        RouteMapper dataSourceMapper = new RouteMapper("ds", "ds_0");
        RouteMapper tableMapper = new RouteMapper("t_order", actualTableName);
        return new RouteUnit(dataSourceMapper, Collections.singletonList(tableMapper));
    }
}