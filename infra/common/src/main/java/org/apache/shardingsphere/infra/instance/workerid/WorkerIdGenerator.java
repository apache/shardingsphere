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

package org.apache.shardingsphere.infra.instance.workerid;

import java.util.Properties;

/**
 * Worker id generator.
 */
public interface WorkerIdGenerator {
    
    String WORKER_ID_KEY = "worker-id";
    
    long DEFAULT_WORKER_ID = 0L;
    
    long MAX_WORKER_ID = 1023L;
    
    /**
     * Generate worker id.
     *
     * @param props props
     * @return worker id
     */
    long generate(Properties props);
}
