package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.zookeeper.base.BaseClient;
import com.saaavsaaa.client.zookeeper.base.BaseProvider;
import org.apache.zookeeper.data.ACL;

import java.util.List;

/**
 * Created by aaa
 */
public class RetryProvider extends BaseProvider {
    RetryProvider(String rootNode, BaseClient client, boolean watched, List<ACL> authorities) {
        super(rootNode, client, watched, authorities);
    }
}
