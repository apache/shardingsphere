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

package org.apache.shardingsphere.infra.yaml.config.swapper.mode;

import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlPersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

/**
 * YAML persist repository configuration swapper.
 * 
 * @param <T> type of persist repository configuration
 */
@SingletonSPI
public interface YamlPersistRepositoryConfigurationSwapper<T extends PersistRepositoryConfiguration> extends YamlConfigurationSwapper<YamlPersistRepositoryConfiguration, T>, TypedSPI {
}
