package io.shardingjdbc.orchestration.reg.stub;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.shardingjdbc.orchestration.reg.etcd.internal.*;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EtcdClientStub implements EtcdClient {

    private Map<String, Element> elements = Maps.newConcurrentMap();
    private Map<String, Watcher> watchers = Maps.newConcurrentMap();

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public Optional<String> get(String key) {
        Element element = elements.get(key);
        return Optional.fromNullable(element).transform(new Function<Element, String>() {
            @Override
            public String apply(Element element) { return element.getValue();
            }
        });
    }

    @Override
    public Optional<List<String>> list(String directory) {
        List<String> children = Lists.newArrayList();
        for (String key : elements.keySet()) {
            if (key.startsWith(directory)) {
                children.add(key);
            }
        }
        return Optional.of(children);
    }

    @Override
    public Optional<String> put(String key, String value) {
        return put(key, value, 0L);
    }

    @Override
    public Optional<String> put(String key, String value, long ttl) {
        Element element = new Element(key, value, 0L);
        elements.put(key, element);
        fireUpdatEvent(element);
        return Optional.fromNullable(elements.get(key)).transform(new Function<Element, String>() {
            @Override
            public String apply(Element input) {
                return input.getValue();
            }
        });
    }

    @Override
    public Optional<List<String>> delete(String keyOrDirectory) {
        List<String> keys = Lists.newArrayList();
        for (String key : elements.keySet()) {
            if (key.startsWith(keyOrDirectory)) {
                keys.add(key);
                fireDeleteEvent(elements.get(key));
                elements.remove(key);
            }
        }
        return Optional.of(keys);
    }

    private void fireEvent(final Element element, final WatchEvent.WatchEventType type) {
        for (String keyOrPath : watchers.keySet()) {
            if (element.getKey().startsWith(keyOrPath)) {
                final WatcherImpl watcher = (WatcherImpl) watchers.get(keyOrPath);
                for (final WatcherListener listener : watcher.getListeners()) {
                    scheduler.schedule(new Runnable() {
                        @Override
                        public void run() {
                            listener.onWatch(WatchEvent.builder()
                                    .watchEventType(type)
                                    .key(element.getKey())
                                    .value(element.getValue())
                                    .id(watcher.getId())
                                    .build());
                        }
                    }, 300, TimeUnit.MILLISECONDS);

                }
            }
        }
    }

    private void fireDeleteEvent(Element element) {
        fireEvent(element, WatchEvent.WatchEventType.DELETE);
    }

    private void fireUpdatEvent(Element element) {
        fireEvent(element, WatchEvent.WatchEventType.UPDATE);
    }

    @Override
    public Optional<Long> lease(long ttl) {
        return null;
    }

    @Override
    public Optional<Watcher> watch(String keyOrDirectory) {
        Watcher watcher = new WatcherImpl(keyOrDirectory);
        watchers.put(keyOrDirectory, watcher);
        return Optional.of(watcher);
    }

    @Value
    final class Element {
        String key, value;
        long ttl;
    }
}
