# ZookeeperClient

-----

    ClientFactory creator = new ClientFactory();   
    IClient testClient = creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes()).newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();   
    
    Reference unit test

-----

Client:   
    CacheClient   
    UsualClient
    
ExecStrategy:   
    public enum StrategyType {   
        USUAL,   
        CONTENTION   
    }   
    
    String getDataString(final String key) throws KeeperException, InterruptedException;
    byte[] getData(final String key) throws KeeperException, InterruptedException;
    void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException;
    boolean checkExists(final String key) throws KeeperException, InterruptedException;
    boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException;
    List<String> getChildren(final String key) throws KeeperException, InterruptedException;
    void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException;
    void update(final String key, final String value) throws KeeperException, InterruptedException;
    void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException;
    void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException;
    ZKTransaction transaction();
    void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException ;
    void deleteAllChildren(final String key) throws KeeperException, InterruptedException ;
    
    /*
    * delete the current node with force and delete the super node whose only child node is current node recursively
    */
    void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException ;
