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

package org.apache.shardingsphere.mode.manager.standalone.workerid.generator;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;

import java.util.Properties;

/**
 * Worker id generator for standalone mode.
 */
public final class StandaloneWorkerIdGenerator implements WorkerIdGenerator {
    
    @Override
    public int generate(final Properties props) {
        if (null == props) {
            return DEFAULT_WORKER_ID;
        }
        Object workerId = props.get(WORKER_ID_KEY);
        if (null == workerId) {
            return DEFAULT_WORKER_ID;
        }
        int result = Integer.parseInt(workerId.toString());
        Preconditions.checkState(result <= MAX_WORKER_ID, "%s can not exceed %s", WORKER_ID_KEY, MAX_WORKER_ID);
        return result;
    }
}
