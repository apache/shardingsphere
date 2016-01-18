package com.dangdang.ddframe.rdb.sharding.api.rule.fixture;

import java.sql.Connection;
import java.sql.SQLException;

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractDataSourceAdapter;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
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
