/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mod.util;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.interfaces.world.IMixinWorld;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A wrapper array list because Forge specifically declares the
 * {@link Entity#capturedDrops} as an {@link ArrayList}. This
 * class is specifically wrapped to the underlying list, so that
 * Sponge's item capturing can be properly directed.
 */
public class WrappedArrayList extends ArrayList<EntityItem> {

    private List<EntityItem> wrappedList;
    private final WeakReference<Entity> container;
    private boolean isCapturingWithSponge = false;

    public WrappedArrayList(Entity entity, List<EntityItem> entityItems) {
        this.container = new WeakReference<>(entity);
        this.wrappedList = entityItems;
        entity.capturedDrops = this;
    }

    public boolean ifValid(Consumer<Entity> runnable) {
        final Entity entity = this.container.get();
        if (entity != null) {
            runnable.accept(entity);
            return true;
        }
        return false;
    }

    public <T> T perform(Function<Entity, T> function) throws UnsupportedOperationException {
        final Entity entity = this.container.get();
        if (entity != null) {
            return function.apply(entity);
        }
        throw new UnsupportedOperationException("Entity wrapper is empty!");
    }

    public Entity getContainer() {
        Entity entity = this.container.get();
        checkNotNull(entity, "Container entity is null! Reference expired!");
        return entity;
    }

    public boolean isValid() {
        return this.container.get() != null;
    }

    @Override
    public int size() {
        return this.wrappedList.size();
    }

    @Override
    public boolean isEmpty() {
        return this.wrappedList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.wrappedList.contains(o);
    }

    @Override
    public int indexOf(Object o) {
        return this.wrappedList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.wrappedList.lastIndexOf(o);
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Cannot clone a wrapped list");
    }

    @Override
    public Object[] toArray() {
        return this.wrappedList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.wrappedList.toArray(a);
    }

    @Override
    public EntityItem get(int index) {
        return this.wrappedList.get(index);
    }

    @Override
    public EntityItem set(int index, EntityItem element) {
        return this.wrappedList.set(index, element);
    }

    @Override
    public boolean add(EntityItem entityItem) {
        return this.wrappedList.add(entityItem);
    }

    @Override
    public void add(int index, EntityItem element) {
        this.wrappedList.add(index, element);
    }

    @Override
    public EntityItem remove(int index) {
        return this.wrappedList.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        return this.wrappedList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.wrappedList.containsAll(c);
    }

    @Override
    public void clear() {
        // In Forge Vanilla, this method is called AFTER capture drops is set to true,
        // we are injecting in here to "detect" that it is being cleared for the first
        // time. There are other areas where we will deterministically close the wrapper
        // as necessary.
        this.wrappedList.clear();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean addAll(Collection<? extends EntityItem> c) {
        return this.wrappedList.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends EntityItem> c) {
        return this.wrappedList.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.wrappedList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.wrappedList.retainAll(c);
    }

    @Override
    public ListIterator<EntityItem> listIterator(int index) {
        return this.wrappedList.listIterator(index);
    }

    @Override
    public ListIterator<EntityItem> listIterator() {
        return this.wrappedList.listIterator();
    }

    @Override
    public Iterator<EntityItem> iterator() {
        return this.wrappedList.iterator();
    }

    @Override
    public List<EntityItem> subList(int fromIndex, int toIndex) {
        return this.wrappedList.subList(fromIndex, toIndex);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public void forEach(Consumer<? super EntityItem> action) {
        this.wrappedList.forEach(action);
    }

    @Override
    public Spliterator<EntityItem> spliterator() {
        return this.wrappedList.spliterator();
    }

    @Override
    public Stream<EntityItem> stream() {
        return this.wrappedList.stream();
    }

    @Override
    public Stream<EntityItem> parallelStream() {
        return this.wrappedList.parallelStream();
    }

    @Override
    public boolean removeIf(Predicate<? super EntityItem> filter) {
        return this.wrappedList.removeIf(filter);
    }

    @Override
    public void replaceAll(UnaryOperator<EntityItem> operator) {
        this.wrappedList.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super EntityItem> c) {
        this.wrappedList.sort(c);
    }
}
