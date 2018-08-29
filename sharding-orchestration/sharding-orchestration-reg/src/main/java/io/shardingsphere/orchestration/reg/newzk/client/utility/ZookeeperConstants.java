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

package io.shardingsphere.orchestration.reg.newzk.client.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.Charset;

/**
 * Zookeeper client constants.
 *
 * @author lidongbo
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZookeeperConstants {
    
    public static final int VERSION = -1;
    
    public static final int WAIT = 60 * 1000;
    
    public static final byte[] NOTHING_DATA = new byte[0];
    
    public static final String NOTHING_VALUE = "";
    
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    
    public static final String PATH_SEPARATOR = "/";
    
    public static final String GLOBAL_LISTENER_KEY = "globalListener";
    
    public static final String ROOT_INIT_PATH = "/InitValue";
    
    public static final byte[] CHANGING_VALUE = new byte[]{'c'};
    
    public static final byte[] RELEASE_VALUE = new byte[]{'r'};
    
    public static final String CHANGING_KEY = "CHANGING_KEY";
    
    public static final long THREAD_PERIOD = 3000L;
    
    public static final long THREAD_INITIAL_DELAY = 1000L;
    
    public static final int NODE_ELECTION_RETRY = 3;
    
    public static final String CLIENT_ID = "1";
    
    public static final boolean WATCHED = true;
}
