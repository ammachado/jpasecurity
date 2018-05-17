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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.jpasecurity.AccessManager;
import org.jpasecurity.AccessType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class CreationTagTest {

    private static final float FLOAT_VALUE = 2.3f;

    private PageContext pageContext = new MockPageContext();
    private CreationTag creationTag = new CreationTag();
    private Object param1 = new Object();
    private AccessManager accessManager;

    @Before
    public void setUp() {
        creationTag.setPageContext(pageContext);
        creationTag.setType("org.jpasecurity.model.TestEntity");
        creationTag.setParameters("param1, param2, 1, 2.3");
        accessManager = mock(AccessManager.class);
    }

    @After
    public void tearDown() {
        creationTag.release();
    }

    @Test
    public void testAccessiblePageScope() {
        initializeAccessManager(PageContext.PAGE_SCOPE, true);
        assertEquals(Tag.EVAL_BODY_INCLUDE, creationTag.doStartTag());
        verify(accessManager).isAccessible(AccessType.CREATE, creationTag.getType(), param1, "param2", 1, FLOAT_VALUE);
    }

    @Test
    public void testNotAccessiblePageScope() {
        initializeAccessManager(PageContext.PAGE_SCOPE, false);
        assertEquals(Tag.SKIP_BODY, creationTag.doStartTag());
        verify(accessManager).isAccessible(AccessType.CREATE, creationTag.getType(), param1, "param2", 1, FLOAT_VALUE);
    }

    @Test
    public void testAccessibleRequestScope() {
        initializeAccessManager(PageContext.REQUEST_SCOPE, true);
        assertEquals(Tag.EVAL_BODY_INCLUDE, creationTag.doStartTag());
        verify(accessManager).isAccessible(AccessType.CREATE, creationTag.getType(), param1, "param2", 1, FLOAT_VALUE);
    }

    @Test
    public void testNotAccessibleRequestScope() {
        initializeAccessManager(PageContext.REQUEST_SCOPE, false);
        assertEquals(Tag.SKIP_BODY, creationTag.doStartTag());
        verify(accessManager).isAccessible(AccessType.CREATE, creationTag.getType(), param1, "param2", 1, FLOAT_VALUE);
    }

    @Test
    public void testAccessibleSessionScope() {
        initializeAccessManager(PageContext.SESSION_SCOPE, true);
        assertEquals(Tag.EVAL_BODY_INCLUDE, creationTag.doStartTag());
        verify(accessManager).isAccessible(AccessType.CREATE, creationTag.getType(), param1, "param2", 1, FLOAT_VALUE);
    }

    @Test
    public void testNotAccessibleSessionScope() {
        initializeAccessManager(PageContext.SESSION_SCOPE, false);
        assertEquals(Tag.SKIP_BODY, creationTag.doStartTag());
        verify(accessManager).isAccessible(AccessType.CREATE, creationTag.getType(), param1, "param2", 1, FLOAT_VALUE);
    }

    @Test
    public void testAccessibleApplicationScope() {
        initializeAccessManager(PageContext.APPLICATION_SCOPE, true);
        assertEquals(Tag.EVAL_BODY_INCLUDE, creationTag.doStartTag());
        verify(accessManager).isAccessible(AccessType.CREATE, creationTag.getType(), param1, "param2", 1, FLOAT_VALUE);
    }

    @Test
    public void testNotAccessibleApplicationScope() {
        initializeAccessManager(PageContext.APPLICATION_SCOPE, false);
        assertEquals(Tag.SKIP_BODY, creationTag.doStartTag());
        verify(accessManager).isAccessible(AccessType.CREATE, creationTag.getType(), param1, "param2", 1, FLOAT_VALUE);
    }

    private void initializeAccessManager(int scope, boolean accessible) {
        pageContext.setAttribute("param1", param1, scope);
        pageContext.setAttribute("accessManager", accessManager, scope);
        when(accessManager.isAccessible(AccessType.CREATE, creationTag.getType(), param1, "param2", 1, FLOAT_VALUE))
            .thenReturn(accessible);
    }
}
