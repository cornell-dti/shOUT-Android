package org.cornelldti.shout.util.function;

/**
 * A backport of Java 8's Consumer for Android
 * <p>
 * Created by Evan Welsh on 3/16/18.
 */
public interface Consumer<T> {

    void apply(T t);
}
