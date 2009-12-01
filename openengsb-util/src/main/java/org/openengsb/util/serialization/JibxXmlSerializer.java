/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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

package org.openengsb.util.serialization;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

public class JibxXmlSerializer implements Serializer {

    private Logger log = Logger.getLogger(JibxXmlSerializer.class);

    @Override
    public <T> void serialize(T object, Writer writer) throws SerializationException {

        try {
            // IBindingFactory unusedFactory = getFactory(object.getClass());
            IBindingFactory bfact = BindingDirectory.getFactory(object.getClass());

            IMarshallingContext mctx = bfact.createMarshallingContext();
            mctx.marshalDocument(object, "UTF-8", null, writer);
        } catch (JiBXException e) {
            throw new SerializationException(String.format("Error serializing object of type %s.", object.getClass()
                    .getName()), e);
        }
    }

    @Override
    public <T> T deserialize(Class<T> clazz, Reader reader) throws SerializationException {
        try {
            // IBindingFactory unusedFactory = getFactory(clazz);
            IBindingFactory bfact = BindingDirectory.getFactory(clazz);

            IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
            Object obj = uctx.unmarshalDocument(reader, null);
            return (T) obj;
        } catch (JiBXException e) {
            throw new SerializationException("Error deserializing from reader.", e);
        }
    }

    // JIBX DEBUG START

    // public static final String BINDINGLIST_NAME = "JiBX_bindingList";
    // public static final String FACTORY_INSTMETHOD = "getInstance";
    // public static final Class[] EMPTY_ARGS = new Class[0];
    //
    // private IBindingFactory getFactoryFromName(String name, Class clas,
    // ClassLoader loader) throws JiBXException {
    // log.info("Entering debug method");
    //
    // log.info(String.format("Listing parents of classloader %s:",
    // loader.toString()));
    // ClassLoader currentLoader = loader;
    // while ((currentLoader = currentLoader.getParent()) != null) {
    // log.info("* " + currentLoader.toString());
    // }
    //
    // Throwable ex = null;
    // Object result = null;
    // IBindingFactory ifact = null;
    // boolean incompat = false;
    // try {
    // Class factory = loader.loadClass(name);
    // Method method = factory.getMethod(FACTORY_INSTMETHOD, EMPTY_ARGS);
    // result = method.invoke(null, (Object[]) null);
    //
    // log.info(String.format("Retrieved result is %s", result));
    //
    // } catch (SecurityException e) {
    // ex = e;
    // } catch (ClassNotFoundException e) {
    // ex = e;
    // } catch (NoSuchMethodException e) {
    // ex = e;
    // } catch (IllegalAccessException e) {
    // ex = e;
    // } catch (InvocationTargetException e) {
    // ex = e;
    // incompat = true;
    // } finally {
    // if (ex == null) {
    // log.info("Checking result type ...");
    // log.info(String.format("Classloader of IBindingFactory is %s",
    // IBindingFactory.class.getClassLoader()
    // .toString()));
    //
    // if (result instanceof IBindingFactory) {
    // log.info("result is an instance of IBindingFactory");
    // } else {
    // log.info("result is NOT an instance of IBindingFactory");
    // }
    // }
    // }
    //
    // log.info("Leaving debug method");
    // return ifact;
    // }
    //
    // public IBindingFactory getFactory(Class clas) throws JiBXException {
    // String list = getBindingList(clas);
    // if (list != null && list.length() > 2) {
    // String fact = list.substring(1, list.length() - 1);
    // if (fact.indexOf('|') < 0) {
    // return getFactoryFromName(fact, clas, clas.getClassLoader());
    // }
    // }
    // throw new JiBXException("Multiple bindings defined for class " +
    // clas.getName());
    // }
    //
    // private String getBindingList(Class clas) throws JiBXException {
    // try {
    // Field field = clas.getDeclaredField(BINDINGLIST_NAME);
    // try {
    // // should be able to access field anyway, but just in case
    // field.setAccessible(true);
    // } catch (Exception e) { /* deliberately left empty */
    // }
    // return (String) field.get(null);
    // } catch (NoSuchFieldException e) {
    // throw new JiBXException("Unable to access binding information for class "
    // + clas.getName()
    // + "\nMake sure the binding has been compiled", e);
    // } catch (IllegalAccessException e) {
    // throw new JiBXException("Error in added code for class " + clas.getName()
    // + "Please report this to the JiBX developers", e);
    // }
    // }

    // JIBX DEBUG END

}
