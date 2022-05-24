package org.apache.shardingsphere.transaction.xa.fixture;

import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XADataSourceDefinition;

import java.util.Arrays;
import java.util.Collection;

public class XADataSourceDefinitionFixTure implements XADataSourceDefinition {

	@Override
	public Collection<String> getXADriverClassName() {
		return Arrays.asList("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource", "com.mysql.cj.jdbc.MysqlXADataSource");
	}
}
