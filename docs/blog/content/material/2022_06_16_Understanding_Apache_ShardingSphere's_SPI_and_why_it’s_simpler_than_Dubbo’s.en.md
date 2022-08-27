+++ 
title = "Understanding Apache ShardingSphere's SPI, and why it’s simpler than Dubbo’s"
weight = 61
chapter = true 
+++

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/phd62v4hi28k41td0gws.png)
 

## Why learn [ShardingSphere](https://shardingsphere.apache.org/)’s SPI?
You might already be familiar with [Java](https://www.java.com/en/) and [Dubbo](https://dubbo.apache.org/en/)’s SPI ([Service Provider Interface](https://en.wikipedia.org/wiki/Service_provider_interface)) mechanism, so you may wonder “why would I learn about [ShardingSphere](https://shardingsphere.apache.org/)’s SPI mechanism?” The reasons are quite simple:

1. ShardingSphere’s source code is simpler and easier to adapt.
2. The execution of ShardingSphere’s SPI mechanism is quite smooth, with less code required for day-to-day operations. Unlike Dubbo’s SPI mechanism and its additional features related to [IoC](https://medium.com/@amitkma/understanding-inversion-of-control-ioc-principle-163b1dc97454), the one in ShardingSphere only preserves the fundamental structure, making it effortless to use.

## Understanding ShardingSphere’s SPI
We also have to mention some shortcomings found in the [Java SPI](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) mechanism:

1. Instances of the [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) class with multiple concurrent threads are not safe to use.
2. Each time you get an element, you need to iterate through all the elements, and you can’t load them on demand.
3. When the implementation class fails to load, an exception is prompted without indicating the real reason, making the error difficult to locate.
4. The way to get an implementation class is not flexible enough. It can only be obtained through the [Iterator](https://docs.oracle.com/javase/8/docs/api/java/util/Iterator.html) form, not based on one parameter to get the corresponding implementation class.

In light of this, let’s see how [ShardingSphere](https://shardingsphere.apache.org/) solves these problems in a simple way.

## Loading SPI class
Dubbo is a direct rewrite of its own SPI, including the SPI file name and the way the file is configured, in stark contrast to [JDK](https://www.oracle.com/java/technologies/downloads/). Let’s briefly compare the differences between the uses of these two:

**Java SPI**

Add interface implementation class under the folder `META-INF/services`

```
optimusPrime = org.apache.spi.OptimusPrime
bumblebee = org.apache.spi.Bumblebee
```

**Dubbo SPI**

Add the implementation class of the interface to the folder `META-INF/services`, configure by means of `key`, `value` like the following example:

```
optimusPrime = org.apache.spi.OptimusPrime
bumblebee = org.apache.spi.Bumblebee
```

We can see now that Dubbo’s Java SPI is completely different from the JDK SPI.

## How does ShardingSphere easily extend the JDK SPI?

Unlike the Dubbo implementation concept, ShardingSphere extends the JDK SPI with less code.

1. The configuration is exactly the same as in the Java SPI.
Let’s take the `DialectTableMetaDataLoader` interface implementation class as an example:

`DialectTableMetaDataLoader.class`

```java
public interface DialectTableMetaDataLoader extends StatelessTypedSPI {
    /**
     * Load table meta data.
     *
     * @param dataSource data source
     * @param tables tables
     * @return table meta data map
     * @throws SQLException SQL exception
     */
    Map<String, TableMetaData> load(DataSource dataSource, Collection<String> tables) throws SQLException;
}
public interface TypedSPI {
    /**
     * Get type.
     * 
     * @return type
     */
    String getType();
    /**
     * Get type aliases.
     *
     * @return type aliases
     */
    default Collection<String> getTypeAliases() {
        return Collections.emptyList();
    }
}
```
`StatelessTypedSPI` interface takes it from `TypedSPI` and multiple interfaces are used to meet the principle of single interface responsibility. `TypedSPI` is the key of the `Map` where subclasses need to specify their own SPI.

Here you don’t need to care about what methods are defined by the `DialectTableMetaDataLoader` interface, you just have to focus on how the subclasses are loaded by SPI. If it is a Java SPI, to load the subclasses, you just define it by the full class name in `META-INF/services`.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/9nzzkyhn91dpzvbkapxy.png)
 

As you can see, it is exactly the same as the native java SPI configuration. So how about its shortcomings?

## Using the Factory Method Pattern
For every interface that needs to be extended and created by SPI, there usually is a similar `xxDataLoaderFactory` for creating and acquiring the specified SPI extension class.

`DialectTableMetaDataLoaderFactory`

```java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DialectTableMetaDataLoaderFactory {
    static {
        ShardingSphereServiceLoader.register(DialectTableMetaDataLoader.class);
    }
    /**
     * Create new instance of dialect table meta data loader.
     * 
     * @param databaseType database type
     * @return new instance of dialect table meta data loader
     */
    public static Optional<DialectTableMetaDataLoader> newInstance(final DatabaseType databaseType) {
        return TypedSPIRegistry.findRegisteredService(DialectTableMetaDataLoader.class, databaseType.getName());
    }
}
```
Here you can see that a static block is used, and all the `DialectTableMetaDataLoader` implementation classes are registered through `ShardingSphereServiceLoader.register` while class loading is in process. By using `TypedSPIRegistry.findRegisteredService`, we can get our specified spi extension class.

```
TypedSPIRegistry.findRegisteredService(final Class<T> spiClass, final String type)
```
So we just have to pay attention to `ShardingSphereServiceLoader.register` and `ypedSPIRegistry.findRegisteredService` approaches.

**`ShardingSphereServiceLoader`**

```java
@NoArgsConstructor(access =AccessLevel.PRIVATE)
public final class ShardingSphereServiceLoader {
    private static final Map<Class<?>, Collection<object>> SERVICES = new ConcurrentHashMap<>();
    /**
     *Register service.
     *
     *@param serviceInterface service interface
     */
    public static void register(final Class<?> serviceInterface){
        if (!SERVICES.containsKey(serviceInterface)) {
            SERVICES.put(serviceInterface, load(serviceInterface) ) ;
        }
    }
   
    private static <T> Collection<Object> load(final Class<T> serviceInterface) {
        Collection<Object> result = new LinkedList<>();
        for (T each: ServiceLoader. load(serviceInterface)) {
        result.add(each);
        }
        return result;
    }
    
    /**
     *Get singleton service instances.
     *
     *@param service service class
     * @param <T> type of service
     *@return service instances
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> getSingletonServiceInstances(final Class<T> service) {
        return (Collection<T>) SERVICES.getorDefault(service,Collections.emptyList());
    }
    
    /**
     *New service instances.
     *
     * eparam service service class
     *@param <T> type of service
     *@return service instances
     */
    @SuppressWarnings ("unchecked" )
    public static <T> Collection<T> newserviceInstances(final Class<T> service){
        if(!SERVICES.containskey(service)) {
           return Collections.emptyList();
        }
        Collection<object> services = SERVICES.get(service);
        if (services.isEmpty()){
            return Collections.emptyList();
        }
        Collection<T> result = new ArrayList<>(services.size());
        for (Object each: services) {
            result.add((T) newServiceInstance(each.getClass()));
        }
        return result;
    }
    
    private static Object newServiceInstance(final Class<?> clazz) {
        try{
           return clazz.getDeclaredConstructor( ) . newInstance( ) ;
        } catch (final ReflectiveOperationException ex) {
            throw new ServiceLoaderInstantiationException(clazz, ex);
        }
    }
}
```
We can see that all SPI classes are placed in this `SERVICES`property.

```java
private static final Map<Class<?>, Collection<Object>> SERVICES = new ConcurrentHashMap<>();
```


And registering is pretty simple too, just use the SPI api embedded in java.

```java
public static void register(final Class<?> serviceInterface) {
        if (!SERVICES.containsKey(serviceInterface)) {
            SERVICES.put(serviceInterface, load(serviceInterface));
        }
    }
private static <T> Collection<Object> load(final Class<T> serviceInterface) {
        Collection<Object> result = new LinkedList<>();
        for (T each : ServiceLoader.load(serviceInterface)) {
            result.add(each);
        }
        return result;
    }
```
**`TypedSPIRegistry`**

The `findRegisteredService` method in `TypedSPIRegistry` is essentially a call to the `getSingletonServiceInstancesmethod` of the `ShardingSphereServiceLoader`.

```java
public static <T extends StatelessTypedSPI> Optional<T> findRegisteredService(final Class<T> spiClass, final String type) {
        for (T each : ShardingSphereServiceLoader.getSingletonServiceInstances(spiClass)) {
            if (matchesType(type, each)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
private static boolean matchesType(final String type, final TypedSPI typedSPI) {
        return typedSPI.getType().equalsIgnoreCase(type) || typedSPI.getTypeAliases().contains(type);
    }
```
Here you can see that the class extension is using `getType` or `getTypeAliases` in `TypedSPI` to get a match, which is why each SPI needs to implement the `TypedSPI` interface.

Now let’s see the `newServiceInstances` method in `ShardingSphereServiceLoader`

```java
public static <T> Collection<T> newServiceInstances(final Class<T> service) {
        if (!SERVICES.containsKey(service)) {
            return Collections.emptyList();
        }
        Collection<Object> services = SERVICES.get(service);
        if (services.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<T> result = new ArrayList<>(services.size());
        for (Object each : services) {
            result.add((T) newServiceInstance(each.getClass()));
        }
        return result;
    }
```

You can see that it is also very simple to find all implementations class returns of the interface directly in `SERVICES` registered through the static code block.

Although short, this short walkthrough basically introduced ShardingSphere’s SPI source code. We’re sure that you have already noticed it’s much easier and simpler to work with ShardingSphere’s SPI than Dubbo's SPI mechanism.

## Summary

Both ShardingSphere and Dubbo’s SPIs meet the requirement of finding the specified implementation class by key, without having to reload all the implementation classes every time you use it, solving the concurrent loading problem. However, compared to Dubbo, the ShardingSphere SPI is more streamlined and easier to use.

You can refer to the ShardingSphere implementation later on when writing your own SPI extensions, as it is simpler to implement, and elegant to work with. You can write an expandable configuration file parser based on SPI so that we can understand what SPI is capable of as well as its application scenarios.

**Apache ShardingSphere Project Links:**

[ShardingSphere Github](https://github.com/apache/shardingsphere/issues?page=1&q=is%3Aopen+is%3Aissue+label%3A%22project%3A+OpenForce+2022%22)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)

[Contributor Guide](https://shardingsphere.apache.org/community/cn/involved/)
