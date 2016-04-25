package com.dangdang.ddframe.rdb.sharding.api.rule.fixture;

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractDataSourceAdapter;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public final class TestDataSource extends AbstractDataSourceAdapter {
    
    private final String name;
    
    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }
    
    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return getConnection();
    }
}
