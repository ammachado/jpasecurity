/*
 * Copyright 2010 Arne Limburg
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
package org.jpasecurity.tags;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

/**
 * @author Arne Limburg
 */
@SuppressWarnings("deprecation")
public class MockPageContext extends PageContext {

    private Map<Integer, Map<String, Object>> attributes = new HashMap<>();

    public MockPageContext() {
        attributes.put(PageContext.PAGE_SCOPE, new HashMap<String, Object>());
        attributes.put(PageContext.REQUEST_SCOPE, new HashMap<String, Object>());
        attributes.put(PageContext.SESSION_SCOPE, new HashMap<String, Object>());
        attributes.put(PageContext.APPLICATION_SCOPE, new HashMap<String, Object>());
    }

    @Override
    public Object getAttribute(String name, int scope) {
        return attributes.get(scope).get(name);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.get(getAttributesScope(name)).remove(name);
    }

    @Override
    public void removeAttribute(String name, int scope) {
        attributes.get(scope).remove(name);
    }

    @Override
    public void setAttribute(String name, Object attribute) {
        setAttribute(name, attribute, PageContext.PAGE_SCOPE);
    }

    @Override
    public void setAttribute(String name, Object o, int scope) {
        attributes.get(scope).put(name, o);
    }

    @Override
    public Object getAttribute(String name) {
        return getAttribute(name, PageContext.PAGE_SCOPE);
    }

    @Override
    public Object findAttribute(String name) {
        Object attribute;
        attribute = getAttribute(name, PageContext.PAGE_SCOPE);
        if (attribute != null) {
            return attribute;
        }
        attribute = getAttribute(name, PageContext.REQUEST_SCOPE);
        if (attribute != null) {
            return attribute;
        }
        attribute = getAttribute(name, PageContext.SESSION_SCOPE);
        if (attribute != null) {
            return attribute;
        }
        return getAttribute(name, PageContext.APPLICATION_SCOPE);
    }

    @Override
    public Enumeration<String> getAttributeNamesInScope(final int scope) {
        return new Enumeration<String>() {

            private Iterator<String> iterator = attributes.get(scope).keySet().iterator();

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public String nextElement() {
                return iterator.next();
            }
        };
    }

    @Override
    public int getAttributesScope(String name) {
        for (Map.Entry<Integer, Map<String, Object>> scope: attributes.entrySet()) {
            if (scope.getValue().containsKey(name)) {
                return scope.getKey();
            }
        }
        return 0;
    }

    @Override
    public void forward(String relativeUrlPath) throws ServletException, IOException {
    }

    @Override
    public Exception getException() {
        return null;
    }

    @Override
    public JspWriter getOut() {
        return null;
    }

    @Override
    public Object getPage() {
        return null;
    }

    @Override
    public ServletRequest getRequest() {
        return null;
    }

    @Override
    public ServletResponse getResponse() {
        return null;
    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public void handlePageException(Exception e) throws ServletException, IOException {
    }

    @Override
    public void handlePageException(Throwable arg0) throws ServletException, IOException {
    }

    @Override
    public void include(String relativeUrlPath) throws ServletException, IOException {
    }

    @Override
    public void initialize(Servlet servlet,
                           ServletRequest request,
                           ServletResponse response,
                           String errorPageURL,
                           boolean needsSession,
                           int bufferSize,
                           boolean autoFlush) throws IOException {
    }

    @Override
    public void release() {
    }

    @Override
    public void include(String arg0, boolean arg1) throws ServletException, IOException {
    }

    @Override
    public ELContext getELContext() {
        return null;
    }

    @Override
    public ExpressionEvaluator getExpressionEvaluator() {
        return null;
    }

    @Override
    public VariableResolver getVariableResolver() {
        return null;
    }
}
