package com.saaavsaaa.client.cache;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.section.ClientTask;
import com.saaavsaaa.client.section.Listener;
import com.saaavsaaa.client.section.Properties;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.common.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/*
 * Created by aaa
 */
public final class PathTree {
    private static final Logger logger = LoggerFactory.getLogger(PathTree.class);
    private final AtomicReference<PathNode> rootNode = new AtomicReference<>();
    private boolean executorStart = false;
    private ScheduledExecutorService cacheService;
    private final IProvider provider;
    private PathStatus Status;
    
    public PathTree(final String root, final IProvider provider) {
        this.rootNode.set(new PathNode(root));
        this.Status = PathStatus.RELEASE;
        this.provider = provider;
    }
    
    public synchronized void loading() throws KeeperException, InterruptedException {
        if (Status == Status.RELEASE){
            logger.debug("loading Status:{}", Status);
            this.setStatus(PathStatus.CHANGING);
    
            PathNode newRoot = new PathNode(rootNode.get().getKey());
            List<String> children = provider.getChildren(rootNode.get().getKey());
            children.remove(provider.getRealPath(Constants.CHANGING_KEY));
            this.attechIntoNode(children, newRoot, provider);
            rootNode.set(newRoot);
    
            this.setStatus(PathStatus.RELEASE);
            logger.debug("loading release:{}", Status);
        } else {
            logger.info("loading but cache status not release");
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                logger.error("loading sleep error:{}", e.getMessage(), e);
            }
            loading();
        }
    }
    
    private void attechIntoNode(final List<String> children, final PathNode pathNode, final IProvider provider) throws KeeperException, InterruptedException {
        logger.debug("attechIntoNode children:{}", children);
        if (children.isEmpty()){
            logger.info("attechIntoNode there are no children");
            return;
        }
        for (String child : children) {
            String childPath = PathUtil.getRealPath(pathNode.getKey(), child);
            PathNode current = new PathNode(PathUtil.checkPath(child), provider.getData(childPath));
            pathNode.attachChild(current);
            List<String> subs = provider.getChildren(childPath);
            this.attechIntoNode(subs, current, provider);
        }
    }
    
    public void refreshPeriodic(final long period){
        if (executorStart){
            throw new IllegalArgumentException("period already set");
        }
        long threadPeriod = period;
        if (threadPeriod < 1){
            threadPeriod = Properties.INSTANCE.getThreadPeriod();
        }
        logger.debug("refreshPeriodic:{}", period);
        cacheService = Executors.newSingleThreadScheduledExecutor();
        cacheService.scheduleAtFixedRate(new ClientTask(provider) {
            @Override
            public void run(IProvider provider) throws KeeperException, InterruptedException {
                logger.debug("cacheService run:{}", getStatus());
                if (PathStatus.RELEASE == getStatus()) {
                    loading();
                }
            }
        }, Properties.INSTANCE.getThreadInitialDelay(), threadPeriod, TimeUnit.MILLISECONDS);
        executorStart = true;
    }
    
    public void stopRefresh(){
        cacheService.shutdown();
        executorStart = false;
        logger.debug("stopRefresh");
    }
    
    public void watch(Listener listener){
        if (listener == null){
            listener = new Listener() {
                @Override
                public void process(WatchedEvent event) {
                    String path = event.getPath();
                    logger.debug("PathTree Watch event:{}", event.toString());
                    switch (event.getType()) {
                        case NodeCreated:
                        case NodeDataChanged:
                        case NodeChildrenChanged: {
                            try {
                                String value = Constants.NOTHING_VALUE;
                                if (!path.equals(getRootNode().getKey())){
                                    value = provider.getDataString(path);
                                }
                                put(path, value);
                            } catch (Exception e) {
                                logger.error("PathTree put error : " + e.getMessage());
                            }
                            break;
                        }
                        case NodeDeleted: {
                            delete(path);
                            break;
                        }
                        default:
                            break;
                    }
                }
            };
        }
        logger.debug("PathTree Watch:{}", rootNode.get().getKey());
        provider.watch(rootNode.get().getKey(), listener);
    }
    
    public PathStatus getStatus() {
        return Status;
    }
    
    public void setStatus(final PathStatus status) {
        Status = status;
    }
    
    public PathNode getRootNode() {
        return rootNode.get();
    }
    
    public byte[] getValue(final String path){
        PathNode node = get(path);
        return null == node ? null : node.getValue();
    }
    
    private Iterator<String> keyIterator(final String path){
        List<String> nodes = PathUtil.getShortPathNodes(path);
        logger.debug("keyIterator path{},nodes:{}", path, nodes);
        Iterator<String> iterator = nodes.iterator();
        iterator.next(); // root
        return iterator;
    }
    
    public List<String> getChildren(String path) {
        PathNode node = get(path);
        List<String> result = new ArrayList<>();
        if (node == null){
            logger.info("getChildren null");
            return result;
        }
        if (node.getChildren().isEmpty()) {
            logger.info("getChildren no child");
            return result;
        }
        Iterator<PathNode> children = node.getChildren().values().iterator();
        while (children.hasNext()){
            result.add(new String(children.next().getValue()));
        }
        return result;
    }
    
    private PathNode get(final String path){
        logger.debug("PathTree get:{}", path);
        PathUtils.validatePath(path);
        if (path.equals(rootNode.get().getKey())){
            return rootNode.get();
        }
        Iterator<String> iterator = keyIterator(path);
        if (iterator.hasNext()) {
            return rootNode.get().get(iterator); //rootNode.get(1, path);
        }
        logger.debug("{} not exist", path);
        return null;
    }
    
    public synchronized void put(final String path, final String value) {
        logger.debug("cache put:{},value:{}", path, value);
        PathUtils.validatePath(path);
        logger.debug("put Status:{}", Status);
        if (Status == Status.RELEASE){
            if (path.equals(rootNode.get().getKey())){
                rootNode.set(new PathNode(rootNode.get().getKey(), value.getBytes(Constants.UTF_8)));
                return;
            }
            this.setStatus(PathStatus.CHANGING);
            rootNode.get().set(keyIterator(path), value);
            this.setStatus(PathStatus.RELEASE);
        } else {
            try {
                logger.debug("put but cache status not release");
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                logger.error("put sleep error:{}", e.getMessage(), e);
            }
            put(path, value);
        }
    }
    
    public synchronized void delete(String path) {
        PathUtils.validatePath(path);
        String prxpath = path.substring(0, path.lastIndexOf(Constants.PATH_SEPARATOR));
        PathNode node = get(prxpath);
        node.getChildren().remove(path);
        logger.debug("PathTree delete:{}", path);
    }
}
