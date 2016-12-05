/*
 * Copyright 2008 Arne Limburg
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
package org.jpasecurity.persistence;

import static org.jpasecurity.util.Types.isSimplePropertyType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.FlushModeType;
import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.persistence.TypedQuery;

import org.jpasecurity.AccessManager;
import org.jpasecurity.AccessType;
import org.jpasecurity.entity.FetchManager;
import org.jpasecurity.entity.SecureObjectManager;
import org.jpasecurity.jpa.JpaParameter;
import org.jpasecurity.jpa.JpaQuery;
import org.jpasecurity.mapping.Path;
import org.jpasecurity.util.ReflectionUtils;

/**
 * This class handles invocations on queries.
 *
 * @author Arne Limburg
 */
public class SecureQuery<T> extends DelegatingQuery<T> {

    private SecureObjectManager objectManager;
    private FetchManager fetchManager;
    private Class<T> constructorArgReturnType;
    private List<Path> selectedPaths;
    private FlushModeType flushMode;

    public SecureQuery(SecureObjectManager objectManager,
                       FetchManager fetchManager,
                       Query query,
                       Class<T> constructorReturnType,
                       List<Path> selectedPaths,
                       FlushModeType flushMode) {
        super(query);
        this.objectManager = objectManager;
        this.fetchManager = fetchManager;
        this.constructorArgReturnType = constructorReturnType;
        this.selectedPaths = selectedPaths;
        this.flushMode = flushMode;
    }

    public TypedQuery<T> setFlushMode(FlushModeType flushMode) {
        this.flushMode = flushMode;
        return super.setFlushMode(flushMode);
    }

    public TypedQuery<T> setParameter(int index, Object parameter) {
        objectManager.setParameter(new JpaQuery(getDelegate()), index, parameter);
        return this;
    }

    public TypedQuery<T> setParameter(String name, Object parameter) {
        objectManager.setParameter(new JpaQuery(getDelegate()), name, parameter);
        return this;
    }

    public <P> TypedQuery<T> setParameter(Parameter<P> parameter, P value) {
        objectManager.setParameter(new JpaQuery(getDelegate()), new JpaParameter<P>(parameter), value);
        return this;
    }

    public T getSingleResult() {
        preFlush();
        T result;
        AccessManager.Instance.get().delayChecks();
        if (constructorArgReturnType != null) {
            Object parameters = super.getSingleResult();
            try {
                result = handleConstructorReturnType(parameters);
            } catch (InvocationTargetException e) {
                result = ReflectionUtils.throwThrowable(e.getTargetException());
            } catch (Exception e) {
                result = ReflectionUtils.throwThrowable(e);
            }
        } else {
            result = getSecureResult(super.getSingleResult());
        }
        AccessManager.Instance.get().ignoreChecks(AccessType.READ, Collections.singleton(result));
        postFlush();
        return result;
    }

    public List<T> getResultList() {
        preFlush();
        AccessManager.Instance.get().delayChecks();
        List<T> targetResult = super.getResultList();
        postFlush();
        List<T> proxyResult = new ArrayList<T>();
        if (constructorArgReturnType != null) {
            for (Object parameter : (List<Object>)targetResult) {
                try {
                    proxyResult.add(handleConstructorReturnType(parameter));
                } catch (InvocationTargetException e) {
                    ReflectionUtils.throwThrowable(e.getTargetException());
                } catch (Exception e) {
                    ReflectionUtils.throwThrowable(e);
                }
            }
        } else {
            for (T entity : targetResult) {
                proxyResult.add(getSecureResult(entity));
            }
        }
        AccessManager.Instance.get().ignoreChecks(AccessType.READ, targetResult);
        return proxyResult;
    }

    T handleConstructorReturnType(Object parameter)
        throws InstantiationException, IllegalAccessException, InvocationTargetException {
        try {
            final T result;
            if (constructorArgReturnType.isAssignableFrom(parameter.getClass())
                && parameter.getClass().isAssignableFrom(constructorArgReturnType)) {
                result = (T)parameter;
            } else if (parameter instanceof Object[]) {
                Object[] parameters = (Object[])parameter;
                Constructor<T> constructor = ReflectionUtils.getConstructor(constructorArgReturnType, parameters);
                result = constructor.newInstance(parameters);
            } else {
                Constructor<T> constructor = ReflectionUtils.getConstructor(constructorArgReturnType, parameter);
                result = constructor.newInstance(parameter);
            }
            return result;
        } catch (NoSuchMethodException e) {
            throw new PersistenceException("No constructor for result type of query", e);
        }
    }

    private void preFlush() {
        if (flushMode == FlushModeType.AUTO) {
            objectManager.preFlush();
        }
    }

    private void postFlush() {
        if (flushMode == FlushModeType.AUTO) {
            objectManager.postFlush();
        }
    }

    private <R> R getSecureResult(R result) {
        if (result == null) {
            return null;
        }
        if (isSimplePropertyType(result.getClass())) {
            return result;
        }
        if (result instanceof Tuple) {
            return (R)new SecureTuple((Tuple)result);
        }
        if (!(result instanceof Object[])) {
            result = objectManager.getSecureObject(result);
            fetchManager.fetch(result);
            return result;
        }
        Object[] scalarResult = (Object[])result;
        for (int i = 0; i < scalarResult.length; i++) {
            if (scalarResult[i] != null && !isSimplePropertyType(scalarResult[i].getClass())) {
                scalarResult[i] = objectManager.getSecureObject(scalarResult[i]);
                if (selectedPaths != null) {
                    fetchManager.fetch(scalarResult[i]);
                }
            }
        }
        return (R)scalarResult;
    }

    private final class SecureTuple implements Tuple {

        private Tuple tuple;

        private SecureTuple(Tuple tuple) {
            this.tuple = tuple;
        }

        public List<TupleElement<?>> getElements() {
            return tuple.getElements();
        }

        public <X> X get(TupleElement<X> tupleElement) {
            return getSecureResult(tuple.get(tupleElement));
        }

        public Object get(String alias) {
            return getSecureResult(tuple.get(alias));
        }

        public Object get(int index) {
            return getSecureResult(tuple.get(index));
        }

        public <X> X get(String alias, Class<X> type) {
            return getSecureResult(tuple.get(alias, type));
        }

        public <X> X get(int index, Class<X> type) {
            return getSecureResult(tuple.get(index, type));
        }

        public Object[] toArray() {
            return getSecureResult(tuple.toArray());
        }
    }
}
