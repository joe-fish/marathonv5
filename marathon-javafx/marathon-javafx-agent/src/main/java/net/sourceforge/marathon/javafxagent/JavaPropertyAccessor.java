/*******************************************************************************
 * Copyright 2016 Jalian Systems Pvt. Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.sourceforge.marathon.javafxagent;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaPropertyAccessor {

    private Object object;
    private static final Pattern arrayPattren = Pattern.compile("(.*)\\[([^\\]]*)\\]$");

    public JavaPropertyAccessor(Object stage) {
        this.object = stage;
    }

    public final String getAttribute(final String name) {
        return getAttribute(name, false);
    }

    public final String getAttribute(final String name, final boolean skipSelf) {
        return EventQueueWait.exec(new Callable<String>() {
            @Override public String call() throws Exception {
                return _getAttribute(name, skipSelf);
            }
        });
    }

    public final String _getAttribute(String name, boolean skipSelf) {
        String[] split = name.split("\\.");
        String first = split[0];
        Object attributeObject = null;
        try {
            if (!skipSelf) {
                attributeObject = getAttributeObject(this, first);
            }
        } catch (UnsupportedCommandException e) {
        }
        if (attributeObject == null) {
            Object o = object;
            if (this instanceof IPseudoElement) {
                o = ((IPseudoElement) this).getPseudoComponent();
            }
            attributeObject = getAttributeObject(o, first);
            if (attributeObject == null) {
                return null;
            }
        }
        for (int i = 1; i < split.length; i++) {
            attributeObject = getAttributeObject(attributeObject, split[i]);
            if (attributeObject == null) {
                return null;
            }
        }
        return toString(attributeObject);
    }

    public final Object getAttributeObject(Object component, String name) {
        Object o = null;
        Matcher matcher = arrayPattren.matcher(name);
        if (matcher.matches()) {
            try {
                o = getAttributeObject(component, getGetMethod(matcher.group(1)));
                if (o != null) {
                    if (o instanceof Map<?, ?>) {
                        o = ((Map<?, ?>) o).get(matcher.group(2));
                    } else
                        o = EventQueueWait.call(o, "get", Integer.parseInt(matcher.group(2)));
                }
            } catch (NoSuchMethodException e) {
            }
        }
        try {
            if (o == null) {
                String isMethod = getIsMethod(name);
                o = EventQueueWait.call(component, isMethod);
            }
        } catch (Throwable e) {
            if (!(e instanceof NoSuchMethodException)) {
                return null;
            }
        }
        try {
            if (o == null) {
                String getMethod = getGetMethod(name);
                o = EventQueueWait.call(component, getMethod);
            }
        } catch (Throwable e) {
        }
        try {
            if (o == null) {
                o = EventQueueWait.call(component, name);
            }
        } catch (Throwable e) {
        }
        try {
            if (o == null) {
                o = getFieldValue(component, name);
            }
        } catch (Throwable e) {
        }
        if (o != null && o.getClass().isArray() && !o.getClass().getComponentType().isPrimitive()) {
            ArrayList<Object> lo = new ArrayList<Object>();
            Object[] oa = (Object[]) o;
            for (Object object : oa) {
                lo.add(object);
            }
            o = lo;
        }
        if (o != null && o instanceof Map<?, ?>) {
            Map<String, Object> lm = new HashMap<String, Object>();
            Map<?, ?> om = (Map<?, ?>) o;
            Set<?> keySet = om.keySet();
            for (Object object : keySet) {
                lm.put(object.toString(), om.get(object));
            }
            o = lm;
        }
        if (o != null && o instanceof Collection<?>) {
            ArrayList<Object> lo = new ArrayList<Object>();
            Collection<?> oa = (Collection<?>) o;
            for (Object object : oa) {
                lo.add(object);
            }
            o = lo;
        }
        if (o == null && component instanceof Collection<?>) {
            ArrayList<Object> lo = new ArrayList<Object>();
            Collection<?> c = (Collection<?>) component;
            for (Object object : c) {
                lo.add(getAttributeObject(object, name));
            }
            return lo;
        }
        return o;
    }

    private String getIsMethod(String name) {
        return "is" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private String getGetMethod(String name) {
        return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private String toString(Object attributeObject) {
        return removeClassName(attributeObject);
    }

    public static String removeClassName(Object object) {
        if (object == null) {
            return "null";
        }
        if (object.getClass().isArray()) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("[");
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                buffer.append(removeClassName(Array.get(object, i)));
                if (i != length - 1) {
                    buffer.append(", ");
                }
            }
            buffer.append("]");
            return buffer.toString();
        }
        if (object.getClass().isPrimitive() || object instanceof String) {
            return object.toString();
        }
        try {
            return object.toString().replaceFirst(object.getClass().getName(), "");
        } catch (Throwable t) {
            return object.toString();
        }
    }

    private Object getFieldValue(Object component, String name) {
        Field f = null;
        Class<? extends Object> klass = component.getClass();
        while (f == null && klass != null) {
            try {
                f = klass.getDeclaredField(name);
            } catch (SecurityException e) {
                return null;
            } catch (NoSuchFieldException e) {
                klass = klass.getSuperclass();
            }
        }
        if (f == null) {
            return null;
        }
        boolean accessible = f.isAccessible();
        try {
            f.setAccessible(true);
            return f.get(component);
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } finally {
            f.setAccessible(accessible);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.sourceforge.marathon.javaagent.IJavaElement#hasAttribue(java.lang
     * .String)
     */
    public final boolean hasAttribue(String name) {
        return hasMethod(getIsMethod(name)) || hasMethod(getGetMethod(name));
    }

    private boolean hasMethod(String name) {
        try {
            object.getClass().getMethod(name);
            return true;
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        }
        return false;
    }

}
