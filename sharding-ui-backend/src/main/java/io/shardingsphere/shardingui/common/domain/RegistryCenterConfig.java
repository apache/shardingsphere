/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingui.common.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Properties;

/**
 * Registry center config.
 *
 * @author chenqingyang
 */
@Getter
@Setter
public class RegistryCenterConfig {
    
    private String name;
    
    private String registryCenterType;
    
    private String serverLists;
    
    private String namespace;
    
    private String orchestrationName;
    
    private String digest;
    
    private boolean activated;

    private Properties props = new Properties();
}
