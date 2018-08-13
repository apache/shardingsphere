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

package io.shardingsphere.transaction.job;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.quartz.SchedulerException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * B.A.S.E transaction job main entrance.
 *
 * @author zhangliang
 * @author caohao
 * @author wangkai
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BASETransactionJobBootStrap {
    
    /**
     * Main entrance.
     *
     * @param args arguments
     * @throws IOException IO exception
     * @throws SchedulerException scheduler exception
     */
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws IOException, SchedulerException {
        // CHECKSTYLE:ON
        try (InputStreamReader inputStreamReader = new InputStreamReader(BASETransactionJobBootStrap.class.getResourceAsStream("/conf/config.yaml"), "UTF-8")) {
            BASETransactionJobConfiguration config = new Yaml(new Constructor(BASETransactionJobConfiguration.class)).loadAs(inputStreamReader, BASETransactionJobConfiguration.class);
            new BASETransactionJobFactory(config).start();
        }
    }
}
