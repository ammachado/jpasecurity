/*
 * Copyright 2008, 2009 Arne Limburg
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
package org.jpasecurity.util;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

/**
 * This parser parses a specified <tt>Set</tt> of classes for annotations of
 * the specified type(s).
 * @param <A> the annotation to be parsed
 * @param <D> additional data that may be used by subclasses
 * @author Arne Limburg
 */
public abstract class AbstractAnnotationParser<A extends Annotation, D> {

    private final Collection<Class<A>> annotationTypes;

    /**
     * Creates an annotation parser for the specified annotation.
     */
    protected AbstractAnnotationParser(Class<A> annotationTypes) {
        this.annotationTypes = Collections.singletonList(annotationTypes);
    }

    /**
     * Parses the specified classes for the annotation(s).
     */
    protected D parse(Class<?>[] classes, D data) {
        for (Class<?> annotatedClass: classes) {
            parse(annotatedClass, data);
        }
        return data;
    }

    /**
     * Parses the specified classes for the annotation(s).
     */
    protected D parse(Collection<Class<?>> classes, D data) {
        for (Class<?> annotatedClass : classes) {
            parse(annotatedClass, data);
        }
        return data;
    }

    /**
     * Parses the specified class for the annotation(s).
     */
    protected D parse(Class<?> annotatedClass, D data) {
        if (annotatedClass == null) {
            return data;
        }
        parse(annotatedClass.getSuperclass(), data);
        for (Class<?> annotatedInterface: annotatedClass.getInterfaces()) {
            parse(annotatedInterface, data);
        }
        for (Class<A> annotationType: annotationTypes) {
            A annotation = annotatedClass.getAnnotation(annotationType);
            if (annotation != null) {
                process(annotatedClass, annotation, data);
            }
        }
        return data;
    }

    /**
     * Called during parsing, when an annotation is found.
     * Subclasses may override to process the annotation.
     * @param annotatedClass the annotated class
     * @param annotation the found annotation
     * @param data additional data that may be used by subclasses
     */
    protected void process(Class<?> annotatedClass, A annotation, D data) {
        process(annotation, data);
    }

    /**
     * Called during parsing, when an annotation is found.
     * Subclasses may override to process the annotation.
     * @param annotation the found annotation
     * @param data additional data that may be used by subclasses
     */
    protected void process(A annotation, D data) {
        process(annotation);
    }

    /**
     * Called during parsing, when an annotation is found.
     * Subclasses may override to process the annotation.
     * @param annotation the found annotation
     */
    protected void process(A annotation) {
    }
}
