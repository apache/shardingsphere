package com.dangdang.ddframe.rdb.sharding.jdbc.util;

public class JDBCTestSQL {
    
    public static final String SELECT_GROUP_BY_USER_ID_SQL = "SELECT user_id FROM t_order GROUP BY user_id";
    
    public static final String SELECT_ORDER_BY_USER_ID_SQL = "SELECT user_id FROM t_order WHERE status = 'init' ORDER BY user_id";
    
    public static final String SELECT_COUNT_ALIAS_SQL = "SELECT COUNT(*) AS orders_count FROM t_order";
    
    public static final String INSERT_WITH_PARTIAL_PLACEHOLDERS_SQL = "INSERT INTO t_order (order_id, user_id, status) VALUES (%s, %s, ?)";
    
    public static final String INSERT_WITHOUT_PLACEHOLDER_SQL = "INSERT INTO t_order (order_id, user_id, status) VALUES (%s, %s, 'insert')";
    
    public static final String UPDATE_WITHOUT_ALIAS_SQL = "UPDATE t_order SET status = %s WHERE order_id = %s AND user_id = %s";
    
    public static final String UPDATE_WITH_ALIAS_SQL = "UPDATE t_order AS o SET o.status = ? WHERE o.order_id = ? AND o.user_id = ?";
    
    public static final String DELETE_WITHOUT_ALIAS_SQL = "DELETE FROM t_order WHERE order_id = %s AND user_id = %s AND status = %s";
}
