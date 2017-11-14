/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 *
 * @author 1
 */
public class JarResourses {

    private ZipFile zipFile;
    private org.apache.log4j.Logger logger;
    public static File fileResourses; // Текущий ресурсный файл
    private FileInputStream fis;

    public JarResourses(File file) throws FileNotFoundException {

        init(file);
    }

    private void init(File file) throws FileNotFoundException {


        if (!file.exists()) {

            throw new FileNotFoundException(file.getAbsolutePath());


        }
        try {

            fis = new FileInputStream(file);

            zipFile = new ZipFile(file);
        } catch (ZipException ex) {
            logger.error("", ex);
        } catch (IOException ex) {
            logger.error("", ex);
        }

    }

    public JarResourses(String path) throws FileNotFoundException {

        logger = org.apache.log4j.Logger.getLogger("LogServer");


        File file = new File(path);

        init(file);



    }

    public void close() {
        try {
            zipFile.close();
        } catch (IOException ex) {
            logger.error("Закрытие", ex);
        }
    }

    public InputStream getResoursesAsStream(String nameResourse) throws IOException {

        ByteArrayInputStream is = null;
        Enumeration e = zipFile.entries();

        ZipInputStream zip = new ZipInputStream(fis);







        ZipEntry entry;
        try {
            while ((entry = zip.getNextEntry()) != null) {


                if (entry.isDirectory()) {
                    continue;
                }

                if (entry.getName().contains(nameResourse)) {

                    byte[] bs = new byte[(int) entry.getSize()];

                    zip.read(bs);

                    is = new ByteArrayInputStream(bs);

                    break;
                }




                zip.closeEntry();
            }

            zip.close();

        } catch (IOException ex) {
            logger.error("", ex);
        }

        return is;
    }

    public byte[] getResoursesAsByte(String nameResourse) throws IOException {

        byte[] bs = null;
        Enumeration e = zipFile.entries();

        ZipInputStream zip = new ZipInputStream(fis);







        ZipEntry entry;
        try {
            while ((entry = zip.getNextEntry()) != null) {


                if (entry.isDirectory()) {
                    continue;
                }

                if (entry.getName().contains(nameResourse)) {

                    bs = new byte[(int) entry.getSize()];

                    zip.read(bs);

                    break;
                }




                zip.closeEntry();
            }

            zip.close();

        } catch (IOException ex) {
            logger.error("", ex);
        }

        return bs;
    }
}
