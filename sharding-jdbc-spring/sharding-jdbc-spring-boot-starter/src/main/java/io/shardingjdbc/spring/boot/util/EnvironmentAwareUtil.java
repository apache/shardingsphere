package io.shardingjdbc.spring.boot.util;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Sharding jdbc spring boot environment aware util.
 *
 * @author caohao
 */
public class EnvironmentAwareUtil {
    
    public static void setDataSourceMap(final Map<String, DataSource> dataSourceMap, final Environment environment) {
        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, "sharding.jdbc.datasource.");
        String dataSources = propertyResolver.getProperty("names");
        for (String each : dataSources.split(",")) {
            try {
                Map<String, Object> dataSourceProps = propertyResolver.getSubProperties(each + ".");
                if (dataSourceProps.isEmpty()) {
                    throw new RuntimeException("Wrong datasource properties!");
                }
                DataSource dataSource = DataSourceBuilder.create().driverClassName(dataSourceProps.get("driver-class-name").toString())
                        .username(dataSourceProps.get("username").toString()).password(dataSourceProps.get("password").toString())
                        .url(dataSourceProps.get("url").toString()).type((Class<? extends DataSource>) Class.forName(dataSourceProps.get("type").toString())).build();
                dataSourceMap.put(each, dataSource);
            } catch (final ClassNotFoundException ex) {
                throw new RuntimeException("Can't find datasource type!", ex);
            }
        }
    }
}
