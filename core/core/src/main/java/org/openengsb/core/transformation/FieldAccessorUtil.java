/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.core.transformation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

class FieldAccessorUtil {

    private FieldAccessorUtil() {
        throw new AssertionError();
    }

    static Object getValue(Field field, Object obj) {
        boolean accessible = field.isAccessible();
        try {
            field.setAccessible(true);
            Object value = field.get(obj);
            return value;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(accessible);
        }
    }

    static void setValue(Field field, Object obj, Object value) {
        if (Modifier.isStatic(field.getModifiers())) {
            return;
        }
        boolean accessible = field.isAccessible();
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(accessible);
        }
    }

}
