/*
 * Copyright 2008 - 2016 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.jpasecurity.collection;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A sorted set-implementation of secure collection.
 * @author Arne Limburg
 */
public class SecureSortedSet<E> extends AbstractSecureCollection<E, SortedSet<E>> implements SortedSet<E> {

    public SecureSortedSet(SortedSet<E> set) {
        super(set);
    }

    SecureSortedSet(SortedSet<E> original, SortedSet<E> filtered) {
        super(original, filtered);
    }

    @Override
    protected SortedSet<E> createFiltered() {
        if (getOriginal().comparator() == null) {
            return new TreeSet<>();
        } else {
            return new TreeSet<>(getOriginal().comparator());
        }
    }

    @Override
    public Comparator<? super E> comparator() {
        return getFiltered().comparator();
    }

    @Override
    public E first() {
        return getFiltered().first();
    }

    @Override
    public E last() {
        return getFiltered().last();
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return new SecureSubSet(first(), toElement);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return new SecureSubSet(fromElement, toElement);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return new SecureSubSet(fromElement, last(), true);
    }

    private int compare(E object1, E object2) {
        if (comparator() != null) {
            return comparator().compare(object1, object2);
        }
        Comparable<E> comparable1 = (Comparable<E>)object1;
        return comparable1.compareTo(object2);
    }

    private class SecureSubSet extends AbstractSet<E> implements SortedSet<E> {

        private E from;
        private E to;
        boolean tailSet = false;

        SecureSubSet(E from, E to) {
            this(from, to, false);
        }

        SecureSubSet(E from, E to, boolean tailSet) {
            this.from = from;
            this.to = to;
            this.tailSet = tailSet;
        }

        @Override
        public Iterator<E> iterator() {
            if (tailSet) {
                return new SecureSubSetIterator(getFiltered().tailSet(from).iterator());
            } else {
                return new SecureSubSetIterator(getFiltered().subSet(from, to).iterator());
            }
        }

        @Override
        public boolean add(E entry) {
            checkRange(entry);
            return SecureSortedSet.this.add(entry);
        }

        @Override
        public boolean addAll(Collection<? extends E> collection) {
            for (E entry: collection) {
                checkRange(entry);
            }
            return SecureSortedSet.this.addAll(collection);
        }

        @Override
        public boolean remove(Object entry) {
            checkRange((E)entry);
            return SecureSortedSet.this.remove(entry);
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            for (E entry: (Collection<E>)collection) {
                checkRange(entry);
            }
            return SecureSortedSet.this.removeAll(collection);
        }

        @Override
        public boolean retainAll(final Collection<?> collection) {
            for (E entry: (Collection<E>)collection) {
                checkRange(entry);
            }
            return SecureSortedSet.this.retainAll(collection);
        }

        @Override
        public void clear() {
            for (Iterator<E> i = iterator(); i.hasNext();) {
                i.next();
                i.remove();
            }
        }

        @Override
        public int size() {
            if (tailSet) {
                return getFiltered().tailSet(from).size();
            } else {
                return getFiltered().subSet(from, to).size();
            }
        }

        @Override
        public Comparator<? super E> comparator() {
            return SecureSortedSet.this.comparator();
        }

        @Override
        public E first() {
            return getFiltered().subSet(from, to).first();
        }

        @Override
        public E last() {
            if (tailSet) {
                return getFiltered().tailSet(from).last();
            } else {
                return getFiltered().subSet(from, to).last();
            }
        }

        @Override
        public SortedSet<E> headSet(E to) {
            checkRange(to);
            return new SecureSubSet(from, to);
        }

        @Override
        public SortedSet<E> subSet(E from, E to) {
            checkRange(from);
            checkRange(to);
            return new SecureSubSet(from, to);
        }

        @Override
        public SortedSet<E> tailSet(E from) {
            checkRange(from);
            return new SecureSubSet(from, to, true);
        }

        protected void checkRange(E entry) {
            if (compare(from, entry) > 0
                || compare(entry, to) > 0
                || (!tailSet && compare(entry, to) == 0)) {
                throw new IllegalArgumentException("entry out of range");
            }
        }

        private class SecureSubSetIterator extends FilteredIterator {

            SecureSubSetIterator(Iterator<E> iterator) {
                super(iterator);
            }

            @Override
            protected void checkRange(E entry) {
                SecureSubSet.this.checkRange(entry);
            }
        }
    }
}
