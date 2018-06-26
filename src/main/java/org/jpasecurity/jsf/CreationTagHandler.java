/*
 * Copyright 2011 Arne Limburg
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
package org.jpasecurity.jsf;

import javax.faces.view.facelets.TagConfig;

import org.jpasecurity.AccessType;

/**
 * @author Arne Limburg
 */
public class CreationTagHandler extends AbstractBeanTagHandler {

    public CreationTagHandler(TagConfig config) {
        super(config);
    }

    @Override
    protected boolean isAccessible(Object object) {
        SecureBeanDefinition bean = (SecureBeanDefinition)object;
        return JsfAccessContext.getAccessManager().isAccessible(getAccessType(), bean.getName(), bean.getParameters());
    }

    @Override
    protected AccessType getAccessType() {
        return AccessType.CREATE;
    }
}
