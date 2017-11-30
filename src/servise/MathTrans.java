/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servise;

import connectdbf.SqlTask;
import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;
import net.sf.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.*;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Всякие математические преобразования и кодировка
 *
 * @author 1
 */
public class MathTrans {

    public static int[] srCRCHi = {
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
            0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
            0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40
    };
    public static int[] srCRCLo = {
            0x00, 0xC0, 0xC1, 0x01, 0xC3, 0x03, 0x02, 0xC2, 0xC6, 0x06, 0x07, 0xC7, 0x05, 0xC5, 0xC4, 0x04, 0xCC, 0x0C, 0x0D, 0xCD,
            0x0F, 0xCF, 0xCE, 0x0E, 0x0A, 0xCA, 0xCB, 0x0B, 0xC9, 0x09, 0x08, 0xC8, 0xD8, 0x18, 0x19, 0xD9, 0x1B, 0xDB, 0xDA, 0x1A,
            0x1E, 0xDE, 0xDF, 0x1F, 0xDD, 0x1D, 0x1C, 0xDC, 0x14, 0xD4, 0xD5, 0x15, 0xD7, 0x17, 0x16, 0xD6, 0xD2, 0x12, 0x13, 0xD3,
            0x11, 0xD1, 0xD0, 0x10, 0xF0, 0x30, 0x31, 0xF1, 0x33, 0xF3, 0xF2, 0x32, 0x36, 0xF6, 0xF7, 0x37, 0xF5, 0x35, 0x34, 0xF4,
            0x3C, 0xFC, 0xFD, 0x3D, 0xFF, 0x3F, 0x3E, 0xFE, 0xFA, 0x3A, 0x3B, 0xFB, 0x39, 0xF9, 0xF8, 0x38, 0x28, 0xE8, 0xE9, 0x29,
            0xEB, 0x2B, 0x2A, 0xEA, 0xEE, 0x2E, 0x2F, 0xEF, 0x2D, 0xED, 0xEC, 0x2C, 0xE4, 0x24, 0x25, 0xE5, 0x27, 0xE7, 0xE6, 0x26,
            0x22, 0xE2, 0xE3, 0x23, 0xE1, 0x21, 0x20, 0xE0, 0xA0, 0x60, 0x61, 0xA1, 0x63, 0xA3, 0xA2, 0x62, 0x66, 0xA6, 0xA7, 0x67,
            0xA5, 0x65, 0x64, 0xA4, 0x6C, 0xAC, 0xAD, 0x6D, 0xAF, 0x6F, 0x6E, 0xAE, 0xAA, 0x6A, 0x6B, 0xAB, 0x69, 0xA9, 0xA8, 0x68,
            0x78, 0xB8, 0xB9, 0x79, 0xBB, 0x7B, 0x7A, 0xBA, 0xBE, 0x7E, 0x7F, 0xBF, 0x7D, 0xBD, 0xBC, 0x7C, 0xB4, 0x74, 0x75, 0xB5,
            0x77, 0xB7, 0xB6, 0x76, 0x72, 0xB2, 0xB3, 0x73, 0xB1, 0x71, 0x70, 0xB0, 0x50, 0x90, 0x91, 0x51, 0x93, 0x53, 0x52, 0x92,
            0x96, 0x56, 0x57, 0x97, 0x55, 0x95, 0x94, 0x54, 0x9C, 0x5C, 0x5D, 0x9D, 0x5F, 0x9F, 0x9E, 0x5E, 0x5A, 0x9A, 0x9B, 0x5B,
            0x99, 0x59, 0x58, 0x98, 0x88, 0x48, 0x49, 0x89, 0x4B, 0x8B, 0x8A, 0x4A, 0x4E, 0x8E, 0x8F, 0x4F, 0x8D, 0x4D, 0x4C, 0x8C,
            0x44, 0x84, 0x85, 0x45, 0x87, 0x47, 0x46, 0x86, 0x82, 0x42, 0x43, 0x83, 0x41, 0x81, 0x80, 0x40
    };
    public static long[] CRC24tab = {
            0x00000000, 0x00864CFB, 0x008AD50D, 0x000C99F6,
            0x0093E6E1, 0x0015AA1A, 0x001933EC, 0x009F7F17,
            0x00A18139, 0x0027CDC2, 0x002B5434, 0x00AD18CF,
            0x003267D8, 0x00B42B23, 0x00B8B2D5, 0x003EFE2E,
            0x00C54E89, 0x00430272, 0x004F9B84, 0x00C9D77F,
            0x0056A868, 0x00D0E493, 0x00DC7D65, 0x005A319E,
            0x0064CFB0, 0x00E2834B, 0x00EE1ABD, 0x00685646,
            0x00F72951, 0x007165AA, 0x007DFC5C, 0x00FBB0A7,
            0x000CD1E9, 0x008A9D12, 0x008604E4, 0x0000481F,
            0x009F3708, 0x00197BF3, 0x0015E205, 0x0093AEFE,
            0x00AD50D0, 0x002B1C2B, 0x002785DD, 0x00A1C926,
            0x003EB631, 0x00B8FACA, 0x00B4633C, 0x00322FC7,
            0x00C99F60, 0x004FD39B, 0x00434A6D, 0x00C50696,
            0x005A7981, 0x00DC357A, 0x00D0AC8C, 0x0056E077,
            0x00681E59, 0x00EE52A2, 0x00E2CB54, 0x006487AF,
            0x00FBF8B8, 0x007DB443, 0x00712DB5, 0x00F7614E,
            0x0019A3D2, 0x009FEF29, 0x009376DF, 0x00153A24,
            0x008A4533, 0x000C09C8, 0x0000903E, 0x0086DCC5,
            0x00B822EB, 0x003E6E10, 0x0032F7E6, 0x00B4BB1D,
            0x002BC40A, 0x00AD88F1, 0x00A11107, 0x00275DFC,
            0x00DCED5B, 0x005AA1A0, 0x00563856, 0x00D074AD,
            0x004F0BBA, 0x00C94741, 0x00C5DEB7, 0x0043924C,
            0x007D6C62, 0x00FB2099, 0x00F7B96F, 0x0071F594,
            0x00EE8A83, 0x0068C678, 0x00645F8E, 0x00E21375,
            0x0015723B, 0x00933EC0, 0x009FA736, 0x0019EBCD,
            0x008694DA, 0x0000D821, 0x000C41D7, 0x008A0D2C,
            0x00B4F302, 0x0032BFF9, 0x003E260F, 0x00B86AF4,
            0x002715E3, 0x00A15918, 0x00ADC0EE, 0x002B8C15,
            0x00D03CB2, 0x00567049, 0x005AE9BF, 0x00DCA544,
            0x0043DA53, 0x00C596A8, 0x00C90F5E, 0x004F43A5,
            0x0071BD8B, 0x00F7F170, 0x00FB6886, 0x007D247D,
            0x00E25B6A, 0x00641791, 0x00688E67, 0x00EEC29C,
            0x003347A4, 0x00B50B5F, 0x00B992A9, 0x003FDE52,
            0x00A0A145, 0x0026EDBE, 0x002A7448, 0x00AC38B3,
            0x0092C69D, 0x00148A66, 0x00181390, 0x009E5F6B,
            0x0001207C, 0x00876C87, 0x008BF571, 0x000DB98A,
            0x00F6092D, 0x007045D6, 0x007CDC20, 0x00FA90DB,
            0x0065EFCC, 0x00E3A337, 0x00EF3AC1, 0x0069763A,
            0x00578814, 0x00D1C4EF, 0x00DD5D19, 0x005B11E2,
            0x00C46EF5, 0x0042220E, 0x004EBBF8, 0x00C8F703,
            0x003F964D, 0x00B9DAB6, 0x00B54340, 0x00330FBB,
            0x00AC70AC, 0x002A3C57, 0x0026A5A1, 0x00A0E95A,
            0x009E1774, 0x00185B8F, 0x0014C279, 0x00928E82,
            0x000DF195, 0x008BBD6E, 0x00872498, 0x00016863,
            0x00FAD8C4, 0x007C943F, 0x00700DC9, 0x00F64132,
            0x00693E25, 0x00EF72DE, 0x00E3EB28, 0x0065A7D3,
            0x005B59FD, 0x00DD1506, 0x00D18CF0, 0x0057C00B,
            0x00C8BF1C, 0x004EF3E7, 0x00426A11, 0x00C426EA,
            0x002AE476, 0x00ACA88D, 0x00A0317B, 0x00267D80,
            0x00B90297, 0x003F4E6C, 0x0033D79A, 0x00B59B61,
            0x008B654F, 0x000D29B4, 0x0001B042, 0x0087FCB9,
            0x001883AE, 0x009ECF55, 0x009256A3, 0x00141A58,
            0x00EFAAFF, 0x0069E604, 0x00657FF2, 0x00E33309,
            0x007C4C1E, 0x00FA00E5, 0x00F69913, 0x0070D5E8,
            0x004E2BC6, 0x00C8673D, 0x00C4FECB, 0x0042B230,
            0x00DDCD27, 0x005B81DC, 0x0057182A, 0x00D154D1,
            0x0026359F, 0x00A07964, 0x00ACE092, 0x002AAC69,
            0x00B5D37E, 0x00339F85, 0x003F0673, 0x00B94A88,
            0x0087B4A6, 0x0001F85D, 0x000D61AB, 0x008B2D50,
            0x00145247, 0x00921EBC, 0x009E874A, 0x0018CBB1,
            0x00E37B16, 0x006537ED, 0x0069AE1B, 0x00EFE2E0,
            0x00709DF7, 0x00F6D10C, 0x00FA48FA, 0x007C0401,
            0x0042FA2F, 0x00C4B6D4, 0x00C82F22, 0x004E63D9,
            0x00D11CCE, 0x00575035, 0x005BC9C3, 0x00DD8538
    };
    private static int[] arrCRC = {255, 255};
    public static final int B_LITTLE_ENDIAN = 0;  // от Младшего к старшему
    public static final int B_BIG_ENDIAN = 1;  // от старшего  к младшему
    private static long CRC24_INIT = 0x00b704ce;

    private static long CRC24_POLY = 0x1864CFB;

    public static HashMap<String, String> stringToMap(String str, String del) {

        HashMap<String, String> result = new HashMap<>();

        String[] spar = str.split(del);

        for (int i = 0; i < spar.length; i++) {

            String[] parameter = spar[i].split("=");

            result.put(parameter[0], parameter[1]);

        }


        return result;
    }


    public static int[] getCRC24Tab(List<Integer> octets) {

        long crc;
        long temp;
        long arg;
        crc = CRC24_INIT;

        for (int i = 0; i < octets.size(); i++) {
            temp = crc;
            temp = temp >>> 8;
            temp = temp >>> 8;
            temp = temp ^ octets.get(i);
            arg = temp & 0x000000FF;

            crc = crc << 8;
            crc = crc ^ CRC24tab[(int) arg];
        }
        crc = crc & 0x00FFFFFF;

        return longToByteArray(crc, 3);
    }

    // Convert the 32-bit binary into the decimal  
    public static float GetFloat32(String Binary) {
        int intBits = Integer.parseInt(Binary, 2);
        float myFloat = Float.intBitsToFloat(intBits);
        return myFloat;
    }

    // Get 32-bit IEEE 754 format of the decimal value  
    public static String GetBinary32(float value) {
        int intBits = Float.floatToIntBits(value);
        String binary = Integer.toBinaryString(intBits);
        return binary;
    }

    public static long getCRC_PULSAR(List<Integer> octets, int len) {
        long w;
        int shift_cnt;
        int f;

        if (len == 0) {

            int a = octets.get(octets.size() - 2);
            int b = octets.get(octets.size() - 1);

            return (b * 256 + a);

        }

        int lengthSend;
        lengthSend = len;

        // short byte_cnt = size;
        //  ptrByte = Data.argValue;
        w = 0xffff;
        for (int byte_cnt = 0; byte_cnt < lengthSend; byte_cnt++) {

            // int d= octets.get(byte_cnt);
            w = w ^ octets.get(byte_cnt);
            for (shift_cnt = 0; shift_cnt < 8; shift_cnt++) {
                f = (int) ((w) & (0x1));
//C++ TO JAVA CONVERTER WARNING: The right shift operator was replaced by Java's logical right shift operator since the left operand was originally of an unsigned type, but you should confirm this replacement:
                w >>>= 1;
                if ((f) == 1) {
                    w = ((w) ^ 0xa001);
                }
            }
        }
        return w;
    }

    public static long getCRC2440(int[] octets) {

        long crc;
        long temp;
        long arg;
        crc = CRC24_INIT;

        for (int i = 0; i < octets.length; i++) {

            crc = crc ^ octets[i] << 16;

            for (int j = 0; j < 8; j++) {
                crc = crc << 1;

                if ((crc & 0x1000000) != 0) {

                    crc = crc ^ CRC24_POLY;
                }
            }
            return (crc & 0x00FFFFFF);

        }

        for (int i = 0; i < octets.length; i++) {
            temp = crc;
            temp = temp >>> 8;
            temp = temp >>> 8;
            temp = temp ^ octets[i];
            arg = temp & 0x000000FF;

            crc = crc << 8;
            crc = crc ^ CRC24tab[(int) arg];
        }
        crc = crc & 0x00FFFFFF;
        return crc;
    }

    public static long getCRC24Tab(int[] octets) {

        long crc;
        long temp;
        long arg;
        crc = CRC24_INIT;

        for (int i = 0; i < octets.length; i++) {
            temp = crc;
            temp = temp >>> 8;
            temp = temp >>> 8;
            temp = temp ^ octets[i];
            arg = temp & 0x000000FF;

            crc = crc << 8;
            crc = crc ^ CRC24tab[(int) arg];
        }
        crc = crc & 0x00FFFFFF;
        return crc;
    }

    public static long getCRC24Tab(byte[] octets) {

        long crc;
        long temp;
        long arg;
        crc = CRC24_INIT;

        for (int i = 0; i < octets.length; i++) {
            temp = crc;
            temp = temp >>> 8;
            temp = temp >>> 8;
            temp = temp ^ octets[i];
            arg = temp & 0x000000FF;

            crc = crc << 8;
            crc = crc ^ CRC24tab[(int) arg];
        }
        crc = crc & 0x00FFFFFF;
        return crc;
    }

    public static void sim900Encode(List<Integer> aSend) {

        for (int i = 0; i < aSend.size(); i++) {

            int kcnt = i;

            kcnt = (kcnt & 0x07) + 3;

            int bt = aSend.get(i);

            for (int j = 0; j < kcnt; j++) {
                bt = ~bt;
                int bt1 = bt & 0x0f;
                bt1 = bt1 << 4;
                bt1 = bt1 & 0xf0;
                int bt2 = bt & 0xf0;
                bt2 = bt2 >>> 4;
                bt2 = bt2 & 0x0f;
                bt = bt1 | bt2;

                if ((bt & 0x80) != 0) {

                    bt = bt << 1;
                    bt = bt | 0x01;

                } else {
                    bt = bt << 1;
                    bt = bt & 0xfe;
                }
            }
            aSend.set(i, bt);
        }
    }

    public static long getDaysByMonth(int iMonth) {

        DateTime dateTime = new DateTime().millisOfDay().setCopy(0);
        DateTime dateFirst;

        if (iMonth > 0) {
            dateFirst = dateTime.dayOfMonth().setCopy(1).monthOfYear().setCopy(iMonth);
        } else {

            int mon = Math.abs(iMonth);
            dateFirst = dateTime.dayOfMonth().setCopy(1).minusMonths(mon);

        }
        DateTime dateLast = dateFirst.plusMonths(1);

        org.joda.time.Interval interval = new Interval(dateFirst, dateLast);

        long days = interval.toDuration().getStandardDays();

        return days;

    }

    /**
     * Делает строку равной all_len длине
     *
     * @param val     Строка
     * @param all_len необходимая длина строки
     * @return
     */
    public static String addSpace(String val, int all_len) {

        int len = val.length();

        String result = " ";
        if (len > all_len) {
            return val.substring(0, all_len);
        }

        int doplen = all_len - len;

        for (int i = 1; i < doplen; i++) {
            result = result + " ";

        }

        return val.concat(result);

    }


    /**
     * Возвращает дату по номеру часа в строковом формате
     * Первый час 1=1:00
     *
     * @param number
     * @param last   начальная дата
     * @return
     */

    public static String getDateStrProfHour(DateTime last, int number) {

        DateTime dateResult = new DateTime(last);

        dateResult = dateResult.plusHours(number);

        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.YY HH:mm");


        return dateResult.toString(dtf);


        //result = new Timestamp(dateResult.getMillis());


    }


    /**
     * Возвращает дату по номеру получасовки в строковом формате
     * Первая плучасовка 1=0:30
     *
     * @param number
     * @param last   начальная дата
     * @return
     */

    public static String getDateStrProf(DateTime last, int number) {

        DateTime dateResult = new DateTime(last);

        dateResult = dateResult.plusMinutes(number * 30);

        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd.MM.YY HH:mm");


        return dateResult.toString(dtf);


        //result = new Timestamp(dateResult.getMillis());


    }


    /**
     * Возвращает две даты по  номеру часа
     * Первый час 1=1:00
     *
     * @param number номер часа
     * @param last   начальная дата
     * @return
     */

    public static Timestamp[] getDateProfileHour(DateTime last, int number) {

        Timestamp[] result = new Timestamp[2];

        DateTime dateResult = new DateTime(last);

        dateResult = dateResult.plusHours(number);

        DateTime dateResult0 = new DateTime(dateResult).minusMinutes(30);


        result[0] = new Timestamp(dateResult0.getMillis());
        result[1] = new Timestamp(dateResult.getMillis());

        return result;
    }


    /**
     * Возвращает дату по номеру получасовки
     * Первая плучасовка 1=0:30
     *
     * @param number
     * @param last   начальная дата
     * @return
     */

    public static Timestamp getDateProfile(DateTime last, int number) {

        Timestamp result;

        DateTime dateResult = new DateTime(last);

        dateResult = dateResult.plusMinutes(number * 30);

        result = new Timestamp(dateResult.getMillis());


        return result;
    }


    public static DateTime getDateReport(int typ) {

        // начало текущих суток
        DateTime dateTime = new DateTime().millisOfDay().setCopy(0);

        if (typ == 0) {
            // на начало текущего месяца  
            dateTime = dateTime.dayOfMonth().setCopy(1);

        } else if (typ > 0 && typ < 13) {
            //на начало месяца 'typ'
            dateTime = dateTime.dayOfMonth().setCopy(1);
            dateTime = dateTime.monthOfYear().setCopy(typ);

        } else if (typ == 14) {
            //начало отчета

            dateTime = new DateTime(ScriptGroovy.dpFirst.getDate());
            dateTime = dateTime.millisOfDay().setCopy(0);

        } else if (typ == 15) {
            //конец отчета

            dateTime = new DateTime(ScriptGroovy.dpLast.getDate());
            dateTime = dateTime.millisOfDay().setCopy(0);

        } else if (typ == 16) {
            // начало текущих суток
            dateTime = new DateTime().millisOfDay().setCopy(0);

        } else if (typ == 17) {
            // начало предыдущих суток
            dateTime = new DateTime().millisOfDay().setCopy(0);
            dateTime = dateTime.minusDays(1);

        } else if (typ < 0) {
            // на начало  месяца  текущий -'typ' 
            dateTime = dateTime.dayOfMonth().setCopy(1);
            dateTime = dateTime.minusMonths(Math.abs(typ));

        } else if (typ == 18) {
            // на начало  месяца начальной даты отчета 
            dateTime = new DateTime(ScriptGroovy.dpFirst.getDate());
            dateTime = dateTime.dayOfMonth().setCopy(1);

        } else if (typ == 19) {
            // на начало  месяца конечной даты  отчета 
            dateTime = new DateTime(ScriptGroovy.dpLast.getDate());
            dateTime = dateTime.dayOfMonth().setCopy(1);

        }

        return dateTime;

    }

    public static ArrayList<String> getStringList(String str, String token) {

        ArrayList<String> result = new ArrayList<>();

        StringTokenizer tokenizer = new StringTokenizer(str, token);

        while (tokenizer.hasMoreTokens()) {
            result.add(tokenizer.nextToken());

        }

        return result;
    }


    /**
     * @param iMonth номер месяца или периуда
     * @return
     */

    public static ArrayList<DateTime> getDatesByMonth(int iMonth) {

        ArrayList<DateTime> result = new ArrayList<DateTime>();

        DateTime dtBegin = new DateTime();
        DateTime dtEnd;

        dtBegin = dtBegin.millisOfDay().setCopy(0);


        if (iMonth == -1) {
            // За предыдущий месяц
            dtEnd = dtBegin.dayOfMonth().setCopy(1);
            dtBegin = dtBegin.dayOfMonth().setCopy(1).minusMonths(1);

        } else if (iMonth == 0) {
            // За текущий месяц
            dtEnd = dtBegin.dayOfMonth().setCopy(1).plusMonths(1);
            dtBegin = dtBegin.dayOfMonth().setCopy(1);

        } else if (iMonth == 13) {
            // За даты отчета
            dtBegin = new DateTime(ScriptGroovy.dpFirst.getDate()).millisOfDay().setCopy(0); //начало
            dtEnd = new DateTime(ScriptGroovy.dpLast.getDate()).millisOfDay().setCopy(0);// конец


        } else if (iMonth == 14) {
            // За текущий год
            dtBegin = dtBegin.monthOfYear().setCopy(1).dayOfYear().setCopy(1);  // начало;
            dtEnd = new DateTime(); //конец


        } else {

            // за конкретный месяц (1-12)
            dtEnd = dtBegin.dayOfMonth().setCopy(1).monthOfYear().setCopy(iMonth + 1);
            dtBegin = dtBegin.dayOfMonth().setCopy(1).minusMonths(1);

        }

        result.add(dtBegin);
        result.add(dtEnd);

        return result;

    }

    /**
     * Список в байты
     *
     * @param list
     * @return
     */
    public static byte[] ListToByteArray(List<Integer> list) {

        byte[] bs = new byte[list.size()];

        for (int i = 0; i < list.size(); i++) {

            int par = list.get(i);

            bs[i] = (byte) par;
        }

        return bs;
    }

    public static int getCRC8(List<Integer> aSend) {

        int crc = 0;

        for (int i = 0; i < aSend.size(); i++) {

            crc = crc ^ aSend.get(i);

        }

        return crc;
    }

    public static void sim900Deccode(List<Integer> aSend) {

        for (int i = 0; i < aSend.size(); i++) {

            int kcnt = i;

            kcnt = (kcnt & 0x07) + 3;

            int bt = aSend.get(i);

            for (int j = 0; j < kcnt; j++) {
                bt = ~bt;
                int bt1 = bt & 0x0f;
                bt1 = bt1 << 4;
                bt1 = bt1 & 0xf0;
                int bt2 = bt & 0xf0;
                bt2 = bt2 >>> 4;
                bt2 = bt2 & 0x0f;
                bt = bt1 | bt2;

                if ((bt & 0x01) != 0) {

                    bt = bt >>> 1;
                    bt = bt | 0x80;

                } else {
                    bt = bt >>> 1;
                    bt = bt & 0x7f;
                }
            }
            aSend.set(i, bt);
        }

    }

    public static String ucs2ToUTF8(byte[] ucs2Bytes) throws UnsupportedEncodingException {

        String unicode = new String(ucs2Bytes, "UTF-16");

        String utf8 = new String(unicode.getBytes("UTF-8"), "Cp1252");

        return utf8;
    }

    public static String UCS2ToString(String inText) throws UnsupportedEncodingException {
        String res;
        if ((inText.length() == 0) || ((inText.length() % 2) != 0)) {
            return null;
        }
        int num = inText.length() / 2;
        byte[] buffer = new byte[num];
        for (int i = 0; i < num; i++) {
            buffer[i] = Byte.parseByte(inText.substring(i * 2, i * 2 + 2), 16);
        }
        res = new String(buffer, "UTF-16");
        return res;
    }


    private static String StringToUSC2(String text) throws Exception {
        byte[] textOnBytes = text.getBytes("UTF-16");
        String textInUSC = "";
        for (int i = 2; i < textOnBytes.length; i++) {
            String buff = Integer.toHexString((int) textOnBytes[i]);
            if (buff.length() % 2 == 1) {
                textInUSC += "0";
            }
            textInUSC += buff;
        }
        String msgTextLength = Integer.toHexString(textInUSC.length() / 2);
        // Если длина нечётная - добавляем в начале 0
        if (msgTextLength.length() % 2 == 1) {
            msgTextLength = "0" + msgTextLength;
        }
        return (msgTextLength + textInUSC).toUpperCase();
    }


    public static String reversePhone(String phone) {
        if (phone.length() % 2 == 1) phone += "F";
        String phoneRev = "";
        phoneRev += phone.charAt(1);
        phoneRev += phone.charAt(0);
        phoneRev += phone.charAt(3);
        phoneRev += phone.charAt(2);
        phoneRev += phone.charAt(5);
        phoneRev += phone.charAt(4);
        phoneRev += phone.charAt(7);
        phoneRev += phone.charAt(6);
        phoneRev += phone.charAt(9);
        phoneRev += phone.charAt(8);
        phoneRev += phone.charAt(11);
        phoneRev += phone.charAt(10);
        return phoneRev;
    }


    public static String convertToUCS2(String skonvert) throws UnsupportedEncodingException {

        byte[] bs1 = skonvert.getBytes("UTF-16BE");

        int len = bs1.length;

        String msg = "";

        for (int i = 0; i < len; i++) {

            byte b = bs1[i];

            String z = Integer.toHexString(b).toUpperCase();

            if (z.length() < 2) {
                z = "0" + z;
            }

            msg = msg + z;
        }

        return msg;
    }

    /**
     * Для GSM шлюза Меркурий 228
     *
     * @param arr
     * @return
     */
    public static int getIntCheckSum(List<Integer> arr) {

        int result = 0;
        for (int s : arr) {
            result = (result + s);
        }
        return (int) ((result & 0xFF) - 1);
    }

    public static int getWAIT(Integer wait) {

        int result = 0;
        int mant = wait;
        int por = 0;

        if (wait.toString().length() > 3) {
            mant = wait / 1000;
            por = 3;
        } else if (wait.toString().length() > 2) {
            mant = wait / 100;
            por = 2;
        } else if (wait.toString().length() > 1) {
            mant = wait / 10;
            por = 1;
        }

        String br = Integer.toBinaryString(mant);

        if (br.length() == 1) {

            br = "000" + br;
        } else if (br.length() == 2) {
            br = "00" + br;
        } else if (br.length() == 3) {
            br = "0" + br;
        }

        String sp = Integer.toBinaryString(por);
        result = Integer.parseInt(sp + br, 2);
        return result;
    }

    public static int getUART(int baud_rate, int byte_size, int stop_bits) {

        int result = 0;

        if (baud_rate == 300) {

            baud_rate = 1;
        } else if (baud_rate == 600) {
            baud_rate = 2;
        } else if (baud_rate == 1200) {
            baud_rate = 3;
        } else if (baud_rate == 2400) {
            baud_rate = 4;
        } else if (baud_rate == 4800) {
            baud_rate = 5;
        } else if (baud_rate == 9600) {
            baud_rate = 6;
        } else if (baud_rate == 14400) {
            baud_rate = 7;
        } else if (baud_rate == 19200) {
            baud_rate = 8;
        } else if (baud_rate == 28800) {
            baud_rate = 9;
        } else if (baud_rate == 38400) {
            baud_rate = 10;
        } else if (baud_rate == 57600) {
            baud_rate = 11;
        } else if (baud_rate == 115200) {
            baud_rate = 12;
        }

        String br = Integer.toBinaryString(baud_rate);

        if (br.length() == 1) {

            br = "000" + br;
        } else if (br.length() == 2) {
            br = "00" + br;
        } else if (br.length() == 3) {
            br = "0" + br;
        }

        if (byte_size == 8) {
            byte_size = 1;
        } else {
            byte_size = 0;
        }

        if (stop_bits == 1) {

            stop_bits = 0;
        } else {

            stop_bits = 1;
        }

        String bs = Integer.toBinaryString(byte_size);
        String sb = Integer.toBinaryString(stop_bits);

        result = Integer.parseInt(sb + bs + br, 2);

//      int ctr_baud_rate = (int) hmProperty.get("ctr_baud_rate");
        //   int ctr_byte_size = (int) hmProperty.get("ctr_byte_size");
        //  ctr_stop_bits
        //    int ctr_parity = (int) hmProperty.get("ctr_parity");
        //   int ctr_wait = (int) hmProperty.get("ctr_wait");
        //   int ctr_pause = (int) hmProperty.get("ctr_pause");
        return result;
    }

    /**
     * Для GSM шлюза Меркурий 228
     *
     * @param arr
     * @return
     */
    public static byte getCheckCum(byte[] arr) {

        int result = 0;
        for (byte s : arr) {
            result = (result + s);
        }
        return (byte) ((result & 0xFF) - 1);
    }

    /**
     * Расчет ВСС для счетчиков СЕ 303 304
     *
     * @param arr
     * @return
     */
    public static int getBBCSum(List<Integer> arr) {

        int LRC = 0;
        for (int b : arr) {

            LRC = LRC + b;

        }

        LRC = (LRC & 0xFF);

        return LRC;
    }

    public static int getSumM228(List<Integer> arr) {

        int result = 0;
        for (int s : arr) {
            result = (int) (result + s);
        }
        return (int) ((int) (result & 0xFF) - 1);
    }

    /**
     * Для GSM шлюза Меркурий 228
     *
     * @param arr
     * @return
     */
    public static int getSumM228(Integer[] arr) {

        int result = 0;
        for (int s : arr) {
            result = (int) (result + s);
        }
        return (int) ((int) (result & 0xFF) - 1);
    }

    public static int[] intToByteArray(int n, int byteCount) {
        int[] res = new int[byteCount];
        for (int i = 0; i < byteCount; i++) {
            res[byteCount - i - 1] = (int) ((n >> i * 8) & 255);
        }
        return res;
    }

    /**
     * Список Int из строки типа 12.\ 456 .444
     *
     * @param string
     * @return [12, 456, 444]
     */
    public static ArrayList<Integer> getListInteger(String string) {

        ArrayList<Integer> result = new ArrayList<>();

        String sval = "";

        Integer val;

        boolean ok = false;

        for (char c : string.toCharArray()) {

            String s = String.valueOf(c);

            val = getInteger(s);

            if (val == null) {
                ok = false;
            } else {

                ok = true;
            }

            if (ok) {
                sval = sval + s;

            } else {

                if (!sval.isEmpty()) {
                    val = getInteger(sval);
                    result.add(val);
                    sval = "";
                }
            }

        }

        if (ok && !sval.isEmpty()) {
            val = getInteger(sval);
            result.add(val);

        }

        return result;
    }

    /**
     * Позиция текущей получасовки с 1 00:30-1 01:00-2 и.т.д.
     *
     * @param time
     * @param step шаг в минутах
     * @return
     */
    public static Integer getPosicion(DateTime time, int step) {

        int result = 0;
        int min = time.minuteOfDay().get();

        result = min / step;

        if (result == 0) {

            result = 1440 / step;

        }

        return result;
    }

    public static Integer getInteger(String string) {

        Integer result = null;

        try {
            result = Integer.parseInt(string);
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    public static int getIntByString(String s) throws NumberFormatException {
        int r = 0;

        if (s.indexOf("0x") != -1) {
            String a = s.substring(2);
            r = Integer.parseInt(a.trim(), 16);
        } else {
            r = Integer.parseInt(s.trim());
        }

        return r;
    }

    public static String getBetweenVal(String value, String lp, String rp) {

        String result = null;
        int idxl = value.indexOf(lp);
        int idxr = value.indexOf(rp);

        if (idxl >= 0 && idxr >= 0) {

            result = value.substring(idxl + 1, idxr);
        }

        return result;
    }

    public static String getBetweenVal(String value, char lp, char rp) {

        String result = null;
        int idxl = value.indexOf(lp);
        int idxr = value.indexOf(rp);

        if (idxl >= 0 && idxr >= 0) {

            result = value.substring(idxl + 1, idxr);
        }

        return result;
    }

    public static Boolean compareValue(Double value, String scompare) throws NumberFormatException {

        Boolean result = false;

        String[] sparam = new String[2];
        Double[] dparam = new Double[2];

        if (scompare.contains(">val>")) {

            sparam = scompare.split(">val>");
            dparam[0] = Double.parseDouble(sparam[0]);
            dparam[1] = Double.parseDouble(sparam[1]);

            if (value < dparam[0] && value > dparam[1]) {
                result = true;
            }

        } else if (scompare.contains("<val<")) {

            sparam = scompare.split("<val<");
            dparam[0] = Double.parseDouble(sparam[0]);
            dparam[1] = Double.parseDouble(sparam[1]);

            if (value > dparam[0] && value < dparam[1]) {
                result = true;
            }

        } else if (scompare.contains("<=val<=")) {

            sparam = scompare.split("<=val<=");
            dparam[0] = Double.parseDouble(sparam[0]);
            dparam[1] = Double.parseDouble(sparam[1]);

            if (value >= dparam[0] && value <= dparam[1]) {
                result = true;
            }

        } else if (scompare.contains(">=val>=")) {

            sparam = scompare.split(">=val>=");
            dparam[0] = Double.parseDouble(sparam[0]);
            dparam[1] = Double.parseDouble(sparam[1]);

            if (value <= dparam[0] && value >= dparam[1]) {
                result = true;
            }

        } else if (scompare.contains(">=val>")) {

            sparam = scompare.split(">=val>");
            dparam[0] = Double.parseDouble(sparam[0]);
            dparam[1] = Double.parseDouble(sparam[1]);

            if (value <= dparam[0] && value > dparam[1]) {
                result = true;
            }

        } else if (scompare.contains(">val>=")) {

            sparam = scompare.split(">val>=");
            dparam[0] = Double.parseDouble(sparam[0]);
            dparam[1] = Double.parseDouble(sparam[1]);

            if (value < dparam[0] && value >= dparam[1]) {
                result = true;
            }

        } else if (scompare.contains("<val<=")) {

            sparam = scompare.split("<=val<=");
            dparam[0] = Double.parseDouble(sparam[0]);
            dparam[1] = Double.parseDouble(sparam[1]);

            if (value > dparam[0] && value <= dparam[1]) {
                result = true;
            }

        } else if (scompare.contains("<=val<")) {

            sparam = scompare.split("<=val<");
            dparam[0] = Double.parseDouble(sparam[0]);
            dparam[1] = Double.parseDouble(sparam[1]);

            if (value >= dparam[0] && value < dparam[1]) {
                result = true;
            }

        } else if (scompare.contains("val>=")) {

            sparam[0] = scompare.replace("val>=", "");
            dparam[0] = Double.parseDouble(sparam[0]);

            if (value >= dparam[0]) {
                result = true;
            }

        } else if (scompare.contains("val<=")) {

            sparam[0] = scompare.replace("val<=", "");
            dparam[0] = Double.parseDouble(sparam[0]);

            if (value <= dparam[0]) {
                result = true;
            }

        } else if (scompare.contains("val==")) {

            sparam[0] = scompare.replace("val==", "");
            dparam[0] = Double.parseDouble(sparam[0]);

            if (value == dparam[0]) {
                result = true;
            }
        } else if (scompare.contains("val>")) {

            sparam[0] = scompare.replace("val>", "");
            dparam[0] = Double.parseDouble(sparam[0]);

            if (value > dparam[0]) {
                result = true;
            }

        } else if (scompare.contains("val<")) {

            sparam[0] = scompare.replace("val<", "");
            dparam[0] = Double.parseDouble(sparam[0]);

            if (value < dparam[0]) {
                result = true;
            }

        }

        return result;
    }

    public static synchronized void setObjectParameter(int idObject, String param, Object value) throws SQLException {

        String sql = "SELECT json_info FROM objects WHERE id_object=" + idObject;

        String json;
        JSONObject jSONObject = null;
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {

            if (rs.next()) {

                json = rs.getString("json_info");

                if (json == null || json.isEmpty()) {
                    jSONObject = new JSONObject();

                } else {

                    jSONObject = JSONObject.fromObject(json);

                }
            }
        } finally {
            rs.close();
        }

        jSONObject.put(param, value);

        json = jSONObject.toString();

        HashMap<String, Object> hmParam = new HashMap<String, Object>();

        hmParam.put("json_info", json);

        sql = " WHERE id_object=" + idObject;
        SqlTask.updateRecInTable(null, "objects", sql, hmParam);

    }

    public static synchronized Object getObjectParameter(int idObject, String nameparam) throws SQLException {

        Object result = null;

        String sql = "SELECT json_info FROM objects WHERE id_object=" + idObject;

        String json;
        JSONObject jSONObject;
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {
            if (rs.next()) {
                json = rs.getString("json_info");

                if (json == null || json.isEmpty()) {
                    jSONObject = new JSONObject();

                } else {

                    jSONObject = JSONObject.fromObject(json);
                    result = jSONObject.get(nameparam);

                }
            }
        } finally {
            rs.close();
        }

        return result;
    }

    public static synchronized void setUserParameter(int idObject, String param, Object value) throws SQLException {

        String sql = "SELECT dop_info_xml FROM object7 WHERE c_tree_id=" + idObject;

        String json;
        JSONObject jSONObject = null;
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {

            if (rs.next()) {

                json = rs.getString("dop_info_xml");

                if (json == null || json.isEmpty()) {
                    jSONObject = new JSONObject();

                } else {

                    jSONObject = JSONObject.fromObject(json);

                }
            }
        } finally {
            rs.close();
        }

        jSONObject.put(param, value);

        json = jSONObject.toString();

        HashMap<String, Object> hmParam = new HashMap<String, Object>();

        hmParam.put("dop_info_xml", json);

        sql = " WHERE c_tree_id=" + idObject;
        SqlTask.updateRecInTable(null, "object7", sql, hmParam);

    }

    public static synchronized Object getUserParameter(int idObject, String nameparam) throws SQLException {

        Object result = null;

        String sql = "SELECT dop_info_xml FROM object7 WHERE c_tree_id=" + idObject;

        String json;
        JSONObject jSONObject;
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {
            if (rs.next()) {
                json = rs.getString("dop_info_xml");

                if (json == null || json.isEmpty()) {
                    jSONObject = new JSONObject();

                } else {

                    jSONObject = JSONObject.fromObject(json);
                    result = jSONObject.get(nameparam);

                }
            }
        } finally {
            rs.close();
        }

        return result;
    }

    public static synchronized void setCtrlParameter(int idObject, String param, Object value) throws SQLException {

        String sql = "SELECT dopinfo_ctrl FROM controllers WHERE id_controller=" + idObject;

        String json;
        JSONObject jSONObject = null;
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {

            if (rs.next()) {

                json = rs.getString("dopinfo_ctrl");

                if (json == null || json.isEmpty()) {
                    jSONObject = new JSONObject();

                } else {

                    jSONObject = JSONObject.fromObject(json);

                }
            }
        } finally {
            rs.close();
        }

        jSONObject.put(param, value);

        json = jSONObject.toString();

        HashMap<String, Object> hmParam = new HashMap<String, Object>();

        hmParam.put("dopinfo_ctrl", json);

        sql = " WHERE id_controller=" + idObject;
        SqlTask.updateRecInTable(null, "controllers", sql, hmParam);

    }

    public static synchronized Object getCtrlParameter(int idObject, String nameparam) throws SQLException {

        Object result = null;

        String sql = "SELECT dopinfo_ctrl FROM controllers WHERE id_controller=" + idObject;

        String json;
        JSONObject jSONObject;
        ResultSet rs = SqlTask.getResultSet(null, sql);

        try {
            if (rs.next()) {
                json = rs.getString("dopinfo_ctrl");

                if (json == null || json.isEmpty()) {
                    jSONObject = new JSONObject();

                } else {

                    jSONObject = JSONObject.fromObject(json);
                    result = jSONObject.get(nameparam);

                }
            }
        } finally {
            rs.close();
        }

        return result;
    }

    /**
     * @param values -массив байт
     * @param kfc    делитель
     * @return
     */
    public static Double getValByNexString(List<Integer> values, int kfc) {

        String s = "";

        Double result = 0.0;
        for (Integer i : values) {

            String sv = Integer.toHexString(i);

            sv = sv.length() > 1 ? sv : "0" + sv;

            s = s + sv;

        }

        result = Double.parseDouble(s);

        result = result / kfc;

        return result;
    }

    /**
     * @param mon номер месяца
     * @param rod падеж 0-именительный другое -родительный
     * @return
     */
    public static String getStringMon(int mon, int rod) {

        String result = "Ошибка !";

        switch (mon) {

            case 1:
                result = (rod == 0 ? "Январь" : "Января");
                break;
            case 2:
                result = (rod == 0 ? "Февраль" : "Февраля");
                break;
            case 3:
                result = (rod == 0 ? "Март" : "Марта");
                break;
            case 4:
                result = (rod == 0 ? "Апрель" : "Апреля");
                break;
            case 5:
                result = (rod == 0 ? "Май" : "Мая");
                break;
            case 6:
                result = (rod == 0 ? "Июнь" : "Июня");
                break;
            case 7:
                result = (rod == 0 ? "Июль" : "Июля");
                break;
            case 8:
                result = (rod == 0 ? "Август" : "Августа");
                break;
            case 9:
                result = (rod == 0 ? "Сетябрь" : "Сентября");
                break;
            case 10:
                result = (rod == 0 ? "Октябрь" : "Октября");
                break;
            case 11:
                result = (rod == 0 ? "Ноябрь" : "Ноября");
                break;
            case 12:
                result = (rod == 0 ? "Декабрь" : "Декабря");
                break;
        }

        return result;
    }

    /**
     * @param dow день недели 0-Воскресенье 1-6 понедельник -суббота 7-праздник
     * @return строковое представление
     */
    public static String getStringDow(int dow) {
        String result = "Ошибка !";

        switch (dow) {

            case 0:
                result = "Воскресенье";
                break;
            case 1:
                result = "Понедельник";
                break;
            case 2:
                result = "Вторник";
                break;
            case 3:
                result = "Среда";
                break;
            case 4:
                result = "Четверг";
                break;
            case 5:
                result = "Пятница";
                break;
            case 6:
                result = "Суббота";
                break;
            case 7:
                result = "Праздник";
                break;

        }

        return result;

    }

    public static boolean isCheck(int[] a, int[] b) {
        return Arrays.equals(a, b);

    }

    public static int[] getArraySrc(List<Integer> list, int typ, int order) {

        int[] result = null;

        if (typ == 16) {

            result = new int[2];

            if (list.size() < 2) {

                result[0] = 0;
                result[1] = 0;
                return result;
            }

            if (order == B_BIG_ENDIAN) {
                result[0] = list.get(list.size() - 1);
                result[1] = list.get(list.size() - 2);
            } else {
                result[1] = list.get(list.size() - 1);
                result[0] = list.get(list.size() - 2);

            }

        } else {

            result = new int[3];

            if (list.size() < 3) {

                result[0] = 0;
                result[1] = 0;
                result[2] = 0;

                return result;
            }

            if (order == B_BIG_ENDIAN) {
                result[0] = list.get(0);
                result[1] = list.get(1);
                result[2] = list.get(2);
            } else {

                result[2] = list.get(0);
                result[1] = list.get(1);
                result[0] = list.get(2);

            }
        }

        return result;

    }

    public static int[] getCrc16Polin(List<Integer> send) {

        int[] result = new int[2];

        int sl = 0;  //Младший байт
        int sh = 0;  // Старший байт

        for (int d : send) {

            for (int w = 0; w < 8; w++) {
                int q = 0;

                if ((d & 1) != 0) {
                    ++q;
                }
                d >>= 1;

                if ((sl & 1) != 0) {
                    ++q;
                }

                sl >>= 1;

                if ((sl & 8) != 0) {
                    ++q;
                }

                if ((sl & 64) != 0) {
                    ++q;
                }

                if ((sh & 2) != 0) {
                    ++q;
                }

                if ((sh & 1) != 0) {
                    sl |= 128;
                }

                sh >>= 1;

                if ((q & 1) != 0) {
                    sh |= 128;
                }

            }
        }

        result[0] = sh;//Старший
        result[1] = sl;//Младший

        return result;

    }

    public static boolean checkCRC24(byte[] crc, byte[] crc1) {

        return Arrays.equals(crc, crc1);
    }

    public static ArrayList<Object> getResultMsg(byte[] answer) {

        if (answer.length < 8) {

            return null;
        }

        ArrayList<Object> result = null;

        byte[] crc = Arrays.copyOf(answer, 3);
        byte[] cap = Arrays.copyOfRange(answer, 3, 8);

        long lcrc = getCRC24Tab(cap);

        byte[] crcr = longTo3Byte(lcrc);

        byte[] crc1 = new byte[3];

        crc1[0] = crcr[2];
        crc1[1] = crcr[1];
        crc1[2] = crcr[0];

        if (!Arrays.equals(crc, crc1)) {

            // Не совпала контрольная сумма
            return null;
        }

        // Номер пакета
        byte[] num = Arrays.copyOf(cap, 2);
        int numpak = getIntByBits(num, B_LITTLE_ENDIAN);

// Длина полезной нагрузки
        byte[] len = Arrays.copyOfRange(cap, 2, 4);
        int msglen = getIntByBits(len, B_LITTLE_ENDIAN);

        // Тип команды
        int typcmd = (cap[4] < 0 ? 256 + cap[4] : cap[4]);

        // Позиция следующей команды
        if (msglen == 0) {

            // Пустой пакет
            result = new ArrayList<Object>();
            result.add(null);
            result.add(numpak);
            result.add(typcmd);
            return result;

        } else if (answer.length < 8 + msglen + 1) {
            return null;
        }

        byte[] ar = Arrays.copyOfRange(answer, 8, 8 + msglen);

        // Контрольная сумма
        byte csum = getCheckCum(ar);

        byte rsum = answer[8 + msglen];

        if (csum != rsum) {

            return null;

        }

        result = new ArrayList<Object>();
        result.add(ar);
        result.add(numpak);
        result.add(typcmd);

        return result;
    }

    public static ArrayList<byte[]> createSendMsg(int num, int port, byte[] send) {
        byte[] crc = new byte[3];
        byte[] cap = new byte[5];
        byte[] sum = new byte[1];
        ArrayList<byte[]> listResult = new ArrayList<byte[]>();

        if (send != null) {
            byte checksum = getCheckCum(send);
            sum[0] = checksum;
        } else {
            send = new byte[0];
            sum = null;
        }

        int[] bNum = MathTrans.intToByteArray(num, 2);
        cap[0] = (byte) bNum[1];
        cap[1] = (byte) bNum[0];
        int[] bLen = MathTrans.intToByteArray(send.length, 2);
        cap[2] = (byte) bLen[1];
        cap[3] = (byte) bLen[0];
        cap[4] = (byte) port;
        long l = MathTrans.getCRC24Tab(cap);
        byte[] crcr = MathTrans.longTo3Byte(l);
        crc[0] = crcr[2];
        crc[1] = crcr[1];
        crc[2] = crcr[0];
        listResult.add(crc);
        listResult.add(cap);

        // Не пустой пакет
        if (send.length > 0) {
            listResult.add(send);
            listResult.add(sum);
        }

        return listResult;
    }

    public static ArrayList<Integer> getUnPackMsg(List<Integer> send) {

        // Проверяем  размер данных
        List l = send.subList(5, 7);
        int iLen = MathTrans.getIntByList(l, MathTrans.B_LITTLE_ENDIAN, false);

        // вытаскиваем данные
        List<Integer> resList = send.subList(8, 8 + iLen);

        ArrayList<Integer> alResult = new ArrayList<>();

        for (Integer i : resList) {
            alResult.add(i);
        }

        return alResult;

    }

    public static LinkedList<Integer> getPackMsg(int num, int port, List<Integer> send) {
        int[] crc = new int[3];
        int[] cap = new int[5];
        int[] sum = new int[1];
        int checksum = 0;

        LinkedList<Integer> listResult = new LinkedList<>();

        if (send != null) {
            checksum = getIntCheckSum(send);
            sum[0] = checksum;
        } else {
            //send = new int[0];
            sum = null;
        }

        int[] bNum = MathTrans.intToByteArray(num, 2);
        cap[0] = bNum[1];
        cap[1] = bNum[0];
        int[] bLen = MathTrans.intToByteArray(send.size(), 2);
        cap[2] = bLen[1];
        cap[3] = bLen[0];
        cap[4] = port;
        long l = MathTrans.getCRC24Tab(cap);
        int[] crcr = MathTrans.longToIntArray(l);
        crc[0] = crcr[2];
        crc[1] = crcr[1];
        crc[2] = crcr[0];

        //crc24
        listResult.add(crc[0]);
        listResult.add(crc[1]);
        listResult.add(crc[2]);

        // заголовок
        listResult.add(cap[0]);
        listResult.add(cap[1]);
        listResult.add(cap[2]);
        listResult.add(cap[3]);
        listResult.add(cap[4]);

        int sendLen = send.size();

        // Не пустой пакет
        if (sendLen > 0) {
            for (int i = 0; i < sendLen; i++) {

                listResult.add(send.get(i));

            }

            listResult.add(checksum);

        }

        return listResult;
    }

    /**
     * @param start  Начальная позиция считывания
     * @param count  количество байт
     * @param result Откуда
     * @return
     */
    public static List getListResult(int start, int count, List result) {

        ArrayList<ArrayList<Integer>> alResult = new ArrayList<>();

        for (int i = start; i < result.size(); i = i + count) {

            int end = i + count;

            if (end <= result.size()) {

                List list = result.subList(i, end);
                ArrayList<Integer> al = new ArrayList<>(list);
                alResult.add(al);
            }

        }

        return alResult;

    }

    /**
     * @param start     Начальная позиция считывания
     * @param countByte количество байт
     * @param result    Откуда
     * @param countSize размер возвращаемого листинга
     * @return
     */
    public static List getListResult(int start, int countByte, List result, int countSize) {

        ArrayList<ArrayList<Integer>> alResult = new ArrayList<>();

        for (int i = start; i < result.size(); i = i + countByte) {

            int end = i + countByte;

            if (end <= result.size()) {

                List list = result.subList(i, end);
                ArrayList<Integer> al = new ArrayList<>(list);

                if (alResult.size() < countSize) {
                    alResult.add(al);
                }
            }

        }

        return alResult;

    }

    public static byte[] getAllMsg(int num, int port, byte[] send) {

        byte[] bs = null;

        ArrayList<Byte> alresult = new ArrayList<Byte>();

        ArrayList<byte[]> al = createSendMsg(num, port, send);

        for (byte[] bs1 : al) {
            for (byte b : bs1) {
                alresult.add(b);
            }
        }
        bs = new byte[alresult.size()];
        for (int i = 0; i < alresult.size(); i++) {
            bs[i] = alresult.get(i);
        }
        return bs;
    }

    public static ArrayList<Integer> getListByNexString(String strNext) {

        ArrayList<Integer> alResult = new ArrayList<>();

        if (strNext.length() % 2 > 0) {
            strNext = "0" + strNext;

        }

        char[] cs = new char[2];

        for (int i = 0; i < strNext.length() - 1; i = i + 2) {

            cs[0] = strNext.charAt(i);
            cs[1] = strNext.charAt(i + 1);
            String sr = String.copyValueOf(cs);
            Integer res = Integer.parseInt(sr, 16);
            alResult.add(res);

        }

        return alResult;
    }

    public static String getNexStrByList(List<Integer> list, String delim) {

        String result = "";

        if (list == null || list.isEmpty()) {
            return "Пустой пакет.";
        }


        for (int i : list) {
            String r = Integer.toHexString(i).toUpperCase();
            if (r.length() < 2) {
                r = "0" + r;
            }
            result = result + r + delim;

        }

        result = result.substring(0, result.length() - 1);

        return result;
    }

    /**
     * Преобразование байтов в строку
     *
     * @param list
     * @param delim -разделитель байтов
     * @return
     */
    public static String getStringByList(List<Integer> list, String delim, boolean bShort) {

        String s = null;

        if (list == null) {
            return s;
        }

        StringBuilder sb = new StringBuilder();

        for (int c : list) {

            if (bShort) {
                sb.append(c);

            } else {
                sb.append((char) c);
            }

            if (delim != null && !delim.isEmpty()) {
                sb.append(delim);
            }

        }

        if (delim != null) {

            sb.deleteCharAt(sb.lastIndexOf(delim));
        }

        s = sb.toString();

        return s;

    }

    // формируем контрольную сумму  CRC24
    public static long getJackSum(List<Integer> arr, String typSum) throws NoSuchAlgorithmException {

        byte[] bs;
        bs = new byte[arr.size()];

        for (int i = 0; i < arr.size(); i++) {
            bs[i] = (byte) (arr.get(i) <= 127 ? arr.get(i) : (arr.get(i) - 256));

        }

        AbstractChecksum checksum = null;
        // select an algorithm (md5 in this case)
        checksum = JacksumAPI.getChecksumInstance(typSum);
        checksum.update(bs);
        long lSum = checksum.getValue();
        return lSum;

    }

    // формируем контрольную сумму  CRC24
    public static long getCRC24Jack(int[] arr) throws NoSuchAlgorithmException {

        byte[] bs;
        bs = new byte[arr.length];

        for (int i = 0; i < arr.length; i++) {
            bs[i] = (byte) (arr[i] <= 127 ? arr[i] : arr[i] - 256);

        }

        AbstractChecksum checksum = null;
        // select an algorithm (md5 in this case)
        checksum = JacksumAPI.getChecksumInstance("crc24");
        checksum.update(bs);
        long lSum = checksum.getValue();
        return lSum;

    }

    // формируем контрольную сумму  CRC24
    public static int[] getCRC24(List<Integer> arr) throws NoSuchAlgorithmException {

        byte[] bs = new byte[arr.size()];

        for (int i = 0; i < arr.size(); i++) {
            bs[i] = (byte) (arr.get(i) <= 127 ? arr.get(i) : arr.get(i) - 256);

        }

        AbstractChecksum checksum = null;
        // select an algorithm (md5 in this case)
        checksum = JacksumAPI.getChecksumInstance("crc24");
        checksum.update(bs);
        long lSum = checksum.getValue();

        return longToByteArray(lSum, 3);
    }

    private static void updCRC(int C) {
        int i;

        i = (int) (arrCRC[1] ^ C);

        arrCRC[1] = (int) (arrCRC[0] ^ srCRCHi[i]);
        arrCRC[0] = srCRCLo[i];

    }

    /**
     * Преобразует строку вида 1=Значение1;2=значение2 в карту с ключем
     * String(Правое)
     *
     * @param param
     * @return
     */
    public static HashMap<String, Integer> parsingToMapKeyString(String param) {

        HashMap<String, Integer> result = new HashMap<>();

        String[] values = param.split(";");

        for (String s : values) {

            String[] m = s.split("=");

            Integer iv = Integer.parseInt(m[0]);

            result.put(m[1], iv);

        }
        return result;
    }

    /**
     * Преобразует строку вида 1=Значение1;2=значение2 в карту с ключем
     * Interer(Левое)
     *
     * @param param
     * @return
     */
    public static HashMap<Integer, String> parsingToMapKeyInteger(String param) {

        HashMap<Integer, String> result = new HashMap<>();

        String[] values = param.split(";");

        for (String s : values) {

            String[] m = s.split("=");

            Integer iv = Integer.parseInt(m[0]);

            result.put(iv, m[1]);

        }
        return result;
    }

    /**
     * @param byte_array
     * @param typByte    -1 от старшего к младшему 0 от младшего к старшему
     * @param zero       если все FF то возвращаем 0
     * @return
     */
    public static int getIntByList(List<Integer> byte_array, int typByte, boolean zero) {

        int result = 0;

        boolean isFull = true;
        if (zero) {

            for (int i : byte_array) {
                if (i != 255) {
                    isFull = false;
                    break;
                }

            }

            if (isFull) {
                return result;
            }
        }

        int len = byte_array.size();
        int sm = 8 * (len - 1);

        //  от старшего к младшему
        if (typByte == B_BIG_ENDIAN) {

            for (int i = 0; i < len; i++) {

                int iB = sm - (i * 8);

                int b = byte_array.get(i);

                result = result + ((b & 0xFF) << iB);
            }

            return result;
        } else {

            //от младшего к старшему
            for (int i = 0; i < len; i++) {

                int iB = sm - (i * 8);

                int b = byte_array.get(len - 1 - i);

                result = result + ((b & 0xFF) << iB);
            }

            return result;
        }
    }

    /**
     * @param byte_array
     * @param typByte    -1 от старшего к младшему 0 от младшего к старшему
     * @param zero       если все FF то возвращаем 0
     * @return
     */
    public static long getLongByList(List<Integer> byte_array, int typByte, boolean zero) {

        long result = 0;

        boolean isFull = true;
        if (zero) {

            for (int i : byte_array) {
                if (i != 255) {
                    isFull = false;
                    break;
                }

            }

            if (isFull) {
                return result;
            }
        }

        int len = byte_array.size();

        String bs;
        String nexlong = "";

        //  от старшего к младшему
        if (typByte == B_BIG_ENDIAN) {

            for (int i = 0; i < len; i++) {

                int b = byte_array.get(i);

                bs = Integer.toHexString(b);

                bs = (bs.length() > 1 ? bs : "0" + bs);

                nexlong = nexlong + bs;

            }

        } else {

            //от младшего к старшему
            for (int i = len - 1; i >= 0; i--) {

                int b = byte_array.get(i);

                bs = Integer.toHexString(b);

                bs = (bs.length() > 1 ? bs : "0" + bs);

                nexlong = nexlong + bs;

            }

        }

        //  nexlong = "0x" + nexlong;

        result = Long.parseLong(nexlong, 16);
        return result;

    }

    /**
     * @param val ,байт 1 2 3 4 5 6 7 8
     * @param poz позиция байта ( с единицы, слева направо) 7 6 5 4 3 2 1 0
     * @param len количество байт
     * @return число из выделенных байт
     */
    public static int getIntbyBits(int val, int poz, int len) {

        int result = val;

        int v = 256 | val;

        String s = Integer.toBinaryString(v);

        String d = s.substring(poz, poz + len);

        result = Integer.parseInt(d, 2);

        return result;
    }

    public static int getIntByBits(int[] byte_array, int typByte) {

        //  от старшего к младшему
        int result = 0;
        int len = byte_array.length;
        int sm = 8 * (len - 1);

        if (typByte == B_BIG_ENDIAN) {

            for (int i = 0; i < len; i++) {

                int iB = sm - (i * 8);

                int b = byte_array[i];

                result = result + ((b & 0xFF) << iB);
            }

            return result;
        } else {

            //от младшего к старшему
            for (int i = 0; i < len; i++) {

                int iB = sm - (i * 8);

                int b = byte_array[len - 1 - i];

                result = result + ((b & 0xFF) << iB);
            }

            return result;
        }
    }

    public static int getIntByBits(byte[] byte_array, int typByte) {

        //  от старшего к младшему
        int result = 0;
        int len = byte_array.length;
        int sm = 8 * (len - 1);

        if (typByte == B_BIG_ENDIAN) {

            for (int i = 0; i < len; i++) {

                int iB = sm - (i * 8);

                int b = byte_array[i];

                result = result + ((b & 0xFF) << iB);
            }

            return result;
        } else {

            //от младшего к старшему
            for (int i = 0; i < len; i++) {

                int iB = sm - (i * 8);

                int b = byte_array[len - 1 - i];

                result = result + ((b & 0xFF) << iB);
            }

            return result;
        }
    }

    /**
     * @param n
     * @param byteCount
     * @return
     */
    public static int[] longToByteArray(long n, int byteCount) {
        int[] res = new int[byteCount];
        for (int i = 0; i < byteCount; i++) {
            res[byteCount - i - 1] = (int) ((n >> i * 8) & 255);
        }
        return res;
    }

    public static int[] longToIntArray(long n) {
        int[] res = new int[3];
        for (int i = 0; i < 3; i++) {
            res[3 - i - 1] = (int) ((n >> i * 8) & 255);
        }
        return res;
    }

    public static byte[] longTo3Byte(long n) {
        byte[] res = new byte[3];
        for (int i = 0; i < 3; i++) {
            res[3 - i - 1] = (byte) (int) ((n >> i * 8) & 255);
        }
        return res;
    }

    /**
     * Контрольная сумма пульсар
     *
     * @param Data
     * @param size
     * @return
     */
    private short WordCrc16(byte[] Data, short size) {
        short w;

        byte shift_cnt;
        byte f;

//C++ TO JAVA CONVERTER TODO TASK: Pointer arithmetic is detected on this variable, so pointers on this variable are left unchanged:
        byte ptrByte;

        short byte_cnt = size;

//ptrByte = Data.argValue;
        w = (short) 0xffff;

        for (; byte_cnt > 0; byte_cnt--) {
//w = (short)(w ^ (short)(*ptrByte++));
            for (shift_cnt = 0; shift_cnt < 8; shift_cnt++) {
                f = (byte) ((w) & (0x1));
//C++ TO JAVA CONVERTER WARNING: The right shift operator was replaced by Java's logical right shift operator since the left operand was originally of an unsigned type, but you should confirm this replacement:
                w >>>= 1;
                if ((f) == 1) {
                    w = (short) ((w) ^ 0xa001);
                }
            }
        }
        return w;
    }

    // формируем контрольную сумму
    public static int getCRC16(Integer[] arr, int len) {

        int a = 0;
        int b = 0;
        int CRCHi = 0xff;
        int CRCLo = 0xff;
        int index = 0;

        // Если 0 то проверяем
        if (len == 0) {

            a = arr[arr.length - 2];
            b = arr[arr.length - 1];

            return (a * 256 + b);

        } else {

            int lengthSend;
            lengthSend = len;
            updCRC(arr[0]);

            for (int i = 0; i < lengthSend; i++) {

                index = (int) (CRCHi ^ arr[i]);
                CRCHi = (int) (CRCLo ^ srCRCHi[index]);
                CRCLo = srCRCLo[index];
            }

            return (CRCHi << 8 | CRCLo);

            //   a = arrCRC[1];
            // b = arrCRC[0];
            // return  (a * 256 + b);
        }

    }
// формируем контрольную сумму

    public static int getCRC16(List<Integer> arr, int len) {

        int a = 0;
        int b = 0;
        int CRCHi = 0xff;
        int CRCLo = 0xff;
        int index = 0;

        // Если 0 то проверяем
        if (len == 0) {

            a = arr.get(arr.size() - 2);
            b = arr.get(arr.size() - 1);

            return (a * 256 + b);

        } else {

            int lengthSend;
            lengthSend = len;
            updCRC(arr.get(0));

            for (int i = 0; i < lengthSend; i++) {

                index = (int) (CRCHi ^ arr.get(i));
                CRCHi = (int) (CRCLo ^ srCRCHi[index]);
                CRCLo = srCRCLo[index];
            }

            return (CRCHi << 8 | CRCLo);
        }

    }
}
