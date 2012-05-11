/*
 * Copyright 2012 Raffaela Ferrari
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
package net.sf.jpasecurity.mapping;

/**
 *
 * @author Raffaela Ferrari
 *
 */
public enum AccessState {
    NO_ACCESS_DEFINED, FIELD_ACCESS_PER_ID, PROPERTY_ACCESS_PER_ID, FIELD_ACCESS, PROPERTY_ACCESS,
    CLASS_PA_BUT_FA_PER_ID, CLASS_FA_BUT_PA_PER_ID;
}