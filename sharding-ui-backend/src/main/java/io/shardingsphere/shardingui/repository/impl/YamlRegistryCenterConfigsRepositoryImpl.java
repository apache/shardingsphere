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

package io.shardingsphere.shardingui.repository.impl;

import io.shardingsphere.shardingui.common.domain.RegistryCenterConfigs;
import io.shardingsphere.shardingui.common.exception.ShardingUIException;
import io.shardingsphere.shardingui.repository.RegistryCenterConfigsRepository;
import org.springframework.stereotype.Repository;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Implementation of Registry center configs repository.
 *
 * @author chenqingyang
 */
@Repository
public final class YamlRegistryCenterConfigsRepositoryImpl implements RegistryCenterConfigsRepository {
    
    private final File file;
    
    public YamlRegistryCenterConfigsRepositoryImpl() {
        file = new File(new File(System.getProperty("user.home")), "sharding-ui-configs.yaml");
    }
    
    @Override
    public RegistryCenterConfigs load() {
        if (!file.exists()) {
            return new RegistryCenterConfigs();
        }
        
        try (FileInputStream fileInputStream = new FileInputStream(file);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")) {
            return new Yaml(new Constructor(RegistryCenterConfigs.class)).loadAs(inputStreamReader, RegistryCenterConfigs.class);
        } catch (IOException e) {
            throw new ShardingUIException(e);
        }
        
    }
    
    @Override
    public void save(final RegistryCenterConfigs registryCenterConfigs) {
        Yaml yaml = new Yaml();
        try (FileWriter fileWriter = new FileWriter(file)) {
            yaml.dump(registryCenterConfigs, fileWriter);
        } catch (IOException e) {
            throw new ShardingUIException(e);
        }
    }
    
}
