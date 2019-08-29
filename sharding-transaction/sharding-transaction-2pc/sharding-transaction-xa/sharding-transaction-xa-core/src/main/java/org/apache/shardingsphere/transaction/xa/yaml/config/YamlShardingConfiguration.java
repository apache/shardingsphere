/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.transaction.xa.yaml.config;

import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.dbcp2.managed.BasicManagedDataSource;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlRootShardingConfiguration;
import org.apache.shardingsphere.transaction.xa.manager.XATransactionManagerLoader;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManager;

import lombok.Getter;
import lombok.Setter;

/**
 * Sharding configuration for YAML.
 *
 * @author liuyangming
 */
@Getter
@Setter
public class YamlShardingConfiguration extends YamlRootShardingConfiguration {

	public void afterPropertiesSet() throws Exception {
		XATransactionManagerLoader xaTransactionManagerLoader = XATransactionManagerLoader.getInstance();
		XATransactionManager xaTransactionManager = xaTransactionManagerLoader.getTransactionManager();
		TransactionManager transactionManager = xaTransactionManager.getTransactionManager();

		Map<String, DataSource> dataSourceMap = this.getDataSources();
		if (dataSourceMap != null) {
			for (Iterator<Map.Entry<String, DataSource>> itr = dataSourceMap.entrySet().iterator(); itr.hasNext();) {
				Map.Entry<String, DataSource> entry = itr.next();
				DataSource value = entry.getValue();

				if (value == null || BasicManagedDataSource.class.isInstance(value) == false) {
					continue;
				}

				((BasicManagedDataSource) value).setTransactionManager(transactionManager);
			}
		}
	}

}
