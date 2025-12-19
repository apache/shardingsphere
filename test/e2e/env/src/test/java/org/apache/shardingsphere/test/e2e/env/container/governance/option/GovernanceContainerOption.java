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

package org.apache.shardingsphere.test.e2e.env.container.governance.option;

import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

/**
 * Governance container option.
 */
@SingletonSPI
public interface GovernanceContainerOption extends TypedSPI {
    
    /**
     * Get container port.
     *
     * @return container port
     */
    int getPort();
    
    /**
     * Get default container image name.
     *
     * @return default container image name
     */
    String getDefaultImageName();
    
    /**
     * Get success log pattern.
     *
     * @return success log pattern
     */
    String getSuccessLogPattern();
    
    /**
     * Get abbreviation.
     *
     * @return abbreviation
     */
    String getAbbreviation();
}
