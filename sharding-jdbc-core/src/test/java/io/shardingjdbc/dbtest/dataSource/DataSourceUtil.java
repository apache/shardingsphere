package io.shardingjdbc.dbtest.datasource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.jdbc.core.ShardingContext;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.ShardingRule;

/**
 * 加载配置文件
 */
public class DataSourceUtil {

	public static DataSource getDataSource(String path) throws IOException, SQLException {
		return ShardingDataSourceFactory.createDataSource(new File(path));
	}

	public static Map<String, DataSource> getDataSourceMap(final ShardingDataSource shardingDataSource)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		ShardingContext shardingContext = getShardingContext(shardingDataSource);
		return shardingContext.getShardingRule().getDataSourceMap();
	}

	public static ShardingRule getShardingRule(final ShardingDataSource shardingDataSource)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		ShardingContext shardingContext = getShardingContext(shardingDataSource);
		return shardingContext.getShardingRule();
	}

	public static ShardingContext getShardingContext(final ShardingDataSource shardingDataSource)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = shardingDataSource.getClass().getDeclaredField("shardingContext");
		field.setAccessible(true);
		return (ShardingContext) field.get(shardingDataSource);
	}

	public static String getDatabaseName(final String dataSetFile) {
		String fileName = new File(dataSetFile).getName();
		if (-1 == fileName.lastIndexOf(".")) {
			return fileName;
		}
		return fileName.substring(0, fileName.lastIndexOf("."));
	}

}
