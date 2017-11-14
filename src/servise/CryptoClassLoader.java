package servise;

import Aladdin.Hasp;
import Aladdin.HaspApiVersion;
import Aladdin.HaspStatus;

import javax.imageio.IIOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Класс загрузки шифрованных классов
 *
 * @author 1
 */
public class CryptoClassLoader extends ClassLoader {

    Hasp hasp;
    private int status;
    public static final long HASP_FILEID_LICENSE = 0xfff2;

    public static final String vendor_code1 = "+p3UF624OLTK5eUfS5HED4eixe9ttDfw77r5TZuVf3AjCDg25zWBuggZET"
            + "Z1bsSQllo0kuaVi7jlC5t+Kh0Gy+23O2VT2OvGBmFoUnqZmrakfu7q2vzrogDFOHu6Zsg+7HjfEZfms0eCINLeS+wE"
            + "nlrz2VWFvfqB70EfKnoQpFLqh0nleDLuDX3zxpePrDE6JJMfLFe8klRmuyt6kZXjThvdqikwCGjQVkeXP9MgdrQdP50Z"
            + "kAMaVbLpUWTnsAom6VZO5aptwcPtAljCJDCCnyIHfobW0HgZvP/TfVQ/UYlQFRnBhy9OtNgcXt7tFej83fktQB9DDAPAP0t"
            + "GmIoDCSQf4i9xjN8e6GL8mpjmnM8WOCL2DULmVudwB5FomE7sqRSB8Hk4ILpD8jq6RuugVwGMaYnaWfGgA1y76R9K7YjD92L0t"
            + "lZgfRdEWfNAus9B2M+PnCqzMrwUOH6EfDpP3XDhGJZFy0RLjEIonUopM18SesBYFQQET0AmOLB6CYTKbEzTB59gJpLlTZgVYrsv"
            + "ShLQ4ZdGq0LNvM4tXKFe3aGoyU2NJ55Ib04citPTdHPWAwKZGAgNC0TO0uI/NKH4zG6gUpT6B+Bm+8LFOG7MlR7frlAEPW69J9"
            + "CkE1mlOa83+nf6TMkCZQriGxe7WpuCYQEhbUaC8aVO1gjt+kAbDHZObQwUapZ6GrGDIk/6Yv8IRZZ+S69lBNzjvzvg1OKTzqWa"
            + "r9UK2hQ607ZEYBs0TXDrwzt7ItIIUGL9p8sGIdH1Rv6HWwnIRKOT8Di6kAE2rG1Nx1KTKqxFV6x9wgwpO5ryFVzjoCppbvgspc"
            + "7EJ/LZQphvWCBNyxHEpHQ4Hnm0hdhW3Q1D7MKgwgrEL2ZWAtKeOmUhuC4Z1lHaQIgwa+IplfHR8H0f8odQgI/giQCiPPDdMYgtKC6gL9b0h/TA5rw=";

    public static final String vendor_code = "hbQij0Y5hb+9AWgK1g+1vTx0kmBFdwa6nvGN5dBInyuzxVWku5ORG6hWM8zDr8imk/KrfctqDJfXzqYz"
            + "FX69nfIeOFHgEoFffaDrPBkAPvYXcdW3626gr1yLBsDG4YpfUEL84C4fe1XhJwpgNY2ibxYDjUu/SnhA"
            + "QhPV/dWvNqRx1sSEGcTbG+d34yjKXinzFQz/A3W8g2HVPmvxKmH1WBmN6pL6NzgW9pAbTCCkWqHeI/pc"
            + "xilUviovdkt3YWZs9syUPUC1KhDixpsPhQ35jzzz8RikVJO+PXy1tGE9hlNK0kjNcXfYW/KHxUmlSRYz"
            + "wA63iwB3BqrL4aCjwFTrAXsbxVSoYw3WFxTXLuA8tS0syObm6oyg82fSXFiqChN7zhMz3TznDrGRhZLB"
            + "oCPJqv11tmu71vQ8+CHbnxlhTsjyHH543Lk0DwoUoliiGJqz51QwC+GmRSqaNKuZ36Gc9XvpQv+AAWpU"
            + "Z5OIZSmtOZTQBRz+Axjg0rCYhuVTDkhhvMVPsH5SJNxdxrgQVsxfxLxJ/VSOYDgjVs1cZ6zAObXuPUER"
            + "Dz5xca8U7fP+SBXH2IcGfCQbUga5oTMOI5sAW90rr8JEbu4psRyyyfsURB9djDpGeb6xSVER+EUNt5uX"
            + "Ds4q73uOOq8vpiYYtonGJ+750mHm9HKenl0TvGCPdfXCYMCqM7TvzPmNN4ZJiQJzk/2i7K3RYYOiURnX"
            + "SwC3VdKcbZX6PEkP6RzCydCCAC5BbNKV4VE7+oRr5Jju/aoci69DBqZL+ciHOu5wPhlBfTizhSeaUvo1"
            + "mFEdWicZFpSEK46HKxvoV+GV3uilFXP/AHXhOeDtuyYmnoxjzucnDEDEK49m6wbk9CMMYj5QEbqesnRf"
            + "oPRsgcrSNrobekPXpdpqx6r0E6jlg2NqB3EDNpsbLkvKG42qU7o7OvxnnHzvhaLcSkPqMxwfdCF/UhhQ"
            + "Q5ywbMKlDKscjuO7vzGw+w==";

    public CryptoClassLoader() {

        hasp = new Hasp(Hasp.HASP_DEFAULT_FID);
        HaspApiVersion version = hasp.getVersion(vendor_code);
        status = version.getLastError();

        hasp.login(vendor_code);
        status = hasp.getLastError();

        switch (status) {
            case HaspStatus.HASP_STATUS_OK:
                System.out.println("OK");
                break;
            case HaspStatus.HASP_FEATURE_NOT_FOUND:
                System.out.println("no Sentinel DEMOMA key found");
                break;
            case HaspStatus.HASP_HASP_NOT_FOUND:
                System.out.println("Sentinel key not found");
                break;
            case HaspStatus.HASP_OLD_DRIVER:
                System.out.println("outdated driver version installed");
                break;
            case HaspStatus.HASP_NO_DRIVER:
                System.out.println("Sentinel driver not installed");
                break;
            case HaspStatus.HASP_INV_VCODE:
                System.out.println("invalid vendor code");
                break;
            default:
                System.out.println("login to default feature failed");
        }

    }

    public int getStatus() {
        return status;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        if (status != 0) {

            throw new ClassNotFoundException(name);

        }

        Class result = null;
        byte[] classBytes = null;

        try {
            classBytes = loadClassBytes(name);
        } catch (IOException ex) {

            throw new ClassNotFoundException(name);
        }

        name = "Constants";

        result = defineClass(name, classBytes, 0, classBytes.length);

        if (result == null) {
            throw new ClassNotFoundException(name);
        }

        return result;

    }

    private byte[] loadClassBytes(String name) throws IOException {

        byte[] bs;

        JarResourses jarResourses = new JarResourses(JarResourses.fileResourses);
        bs = jarResourses.getResoursesAsByte(name);

        if (bs == null || bs.length == 0) {

            throw new IOException(name);

        }

        hasp.decrypt(bs, bs.length);

        status = hasp.getLastError();

        if (status != HaspStatus.HASP_STATUS_OK) {

            throw new IOException(name);
        }
        return bs;

    }

    public byte[] getByte(File file) throws IOException {

        byte[] fileBArray = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);

        try {
            fis.read(fileBArray);
            if (!hasp.decrypt(fileBArray, fileBArray.length)) {
                throw new IIOException("");
            }
        } finally {
            fis.close();
        }
        return fileBArray;

    }

}
