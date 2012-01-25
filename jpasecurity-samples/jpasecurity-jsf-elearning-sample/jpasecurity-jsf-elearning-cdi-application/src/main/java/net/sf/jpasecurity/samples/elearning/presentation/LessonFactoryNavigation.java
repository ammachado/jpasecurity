/*
 * Copyright 2011 Raffaela Ferrari
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
package net.sf.jpasecurity.samples.elearning.presentation;

import javax.inject.Inject;
import javax.inject.Named;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.samples.elearning.view.Navigation;
/**
 * @author Raffaela Ferrari
 */
@Named
public class LessonFactoryNavigation extends Navigation {
    @Inject
    private Course course;

    public String getOutcome() {
        return new Navigation("course.xhtml").facesRedirect().includeViewParams()
            .withParameter("course", course.getId()).toString();
    }
}