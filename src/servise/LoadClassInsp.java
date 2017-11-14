/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author 1
 */
public class LoadClassInsp extends ClassLoader {

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {



        Class result = null;
        byte[] classBytes = null;

        try {
            classBytes = loadClassBytes(name);
        } catch (IOException ex) {

            throw new ClassNotFoundException(name);
        }

        result = defineClass(name, classBytes, 0, classBytes.length);

        if (result == null) {
            throw new ClassNotFoundException(name);
        }


        return result;

    }

    private byte[] loadClassBytes(String name) throws IOException {

        //    String cname = name.replace('.', '/') + ".caesar";

        //    String path = "/cls/";

        //  InputStream in = Begin.class.getResourceAsStream(path + name + ".srg");




        InputStream in = this.getClass().getResourceAsStream(name);


        if (in == null) {

            throw new IOException(name);

        }

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int ch;

            while ((ch = in.read()) != -1) {
                byte b = (byte) ch;
                buffer.write(b);
            }


            byte[] bs = buffer.toByteArray();






            return bs;

        } finally {

            in.close();
        }
    }
}
