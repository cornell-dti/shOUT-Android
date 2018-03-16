package org.cornelldti.shout.util.functions;

/**
 * A backport of Java 8's BiConsumer for Android
 * <p>
 * Created by Evan Welsh on 3/16/18.
 */

public interface BiConsumer<T, R> {

    void apply(T t, R r);
}
