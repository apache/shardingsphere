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

package org.apache.shardingsphere.orchestration.center.yaml.config;

import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.yaml.config.YamlConfiguration;

/**
 * Orchestration instance configuration for YAML.
 *
 * @author zhangliang
 * @author dongzonglei
 * @author wangguangyuan
 * @author sunbufu
 */
@Getter
@Setter
public class YamlInstanceConfiguration implements YamlConfiguration {
    
    private String centerType;
    
    private String instanceType;
    
    private String serverLists;
    
    private String namespace;
    
    private Properties props = new Properties();
}
