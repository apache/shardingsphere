/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.transaction.soft.bed;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStreamReader;

/**
 * 最大努力送达型异步作业启动入口.
 * 
 * @author zhangliang
 * @author caohao
 */
public final class BestEffortsDeliveryJobMain {
    
    /**
     * 启动入口.
     * 
     * @param args 启动参数
     */
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws Exception {
    // CHECKSTYLE:ON
        try (InputStreamReader inputStreamReader = new InputStreamReader(BestEffortsDeliveryJobMain.class.getResourceAsStream("/conf/config.yaml"), "UTF-8")) {
            BestEffortsDeliveryConfiguration config = new Yaml(new Constructor(BestEffortsDeliveryConfiguration.class)).loadAs(inputStreamReader, BestEffortsDeliveryConfiguration.class);
            new BestEffortsDeliveryJobFactory(config).init();
        }
    }
}
