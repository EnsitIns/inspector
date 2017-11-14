/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author 1
 */
public class ZipCreator {

    private File zipFile;
    private ZipOutputStream zos;

    public ZipCreator(String sPath, String sNameZipFile) throws FileNotFoundException {


        zipFile = new File(sPath, sNameZipFile);
        zos = new ZipOutputStream(
                new FileOutputStream(zipFile));

        zos.setLevel(Deflater.DEFAULT_COMPRESSION);


    }

    public File getZipFile() throws IOException {

        zos.close();
        return zipFile;

    }

    public void addFileToZip(File f) throws IOException {

        ZipEntry ze;

        ze = new ZipEntry(f.getName());

        zos.putNextEntry(ze);

        FileInputStream fis =
                new FileInputStream(f);

        byte[] buf = new byte[8000];
        int nLength;
        while (true) {
            nLength = fis.read(buf);
            if (nLength < 0) {
                break;
            }
            zos.write(buf, 0, nLength);
        }

//После завершения цикла необходимо закрыть поток, связанный с добавляемым файлом и новый элемент оглавления:

        fis.close();
        zos.closeEntry();


    }
}
