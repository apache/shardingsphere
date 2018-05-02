package com.saaavsaaa.client.zookeeper;

/**
 * Created by aaa on 18-5-2.
 *  todo log
 */
public final class CacheClient extends UsualClient {
    CacheClient(String servers, int sessionTimeoutMilliseconds) {
        super(servers, sessionTimeoutMilliseconds);
    }
}
