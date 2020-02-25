package org.apache.shardingsphere.example.proxy.hint.factory;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.example.proxy.hint.config.DatasourceConfig;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

public class YamlDataSourceFactory {
    
    public static DataSource createDataSource(File yamlFile) throws IOException {
        DatasourceConfig datasourceConfig = YamlEngine.unmarshal(yamlFile, DatasourceConfig.class);
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(datasourceConfig.getDriverClassName());
        dataSource.setJdbcUrl(datasourceConfig.getJdbcUrl());
        dataSource.setUsername(datasourceConfig.getUsername());
        dataSource.setPassword(datasourceConfig.getPassword());
        return dataSource;
    }
}

