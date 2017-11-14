package servise;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.BitSet;

/**
 *
 * @author user
 */
public class BitSetEx extends BitSet {

    public static final int B_LITTLE_ENDIAN = 0;  // от Младшего к старшему
    public static final int B_BIG_ENDIAN = 1;  // от старшего  к младшему

    public BitSetEx(int bytes) {

        init(bytes);

    }

    public static double getIntByBits(short bLittle, short bBig, int typ) {

        int res = 0;
        if (typ == B_LITTLE_ENDIAN) {

            res = bBig * 256 + bLittle;
        } else {

            res = bLittle * 256 + bBig;
        }



        return res;
    }

    public static int getIntByBits(short[] bits) {

        int res = 0;

        if (bits.length == 4) {

            if ((bits[0] == 0xff)
                    & (bits[1] == 0xff)
                    & (bits[2] == 0xff)
                    & (bits[3] == 0xff)) {
                res = 0;

            } else {
                res = (int) (bits[1] * Math.pow(256, 3) + bits[0] * Math.pow(256, 2) + bits[3] * 256 + bits[2]);
            }
        }


        if (bits.length == 3) {


            if ((bits[0] == 0xff)
                    & (bits[1] == 0xff)
                    & (bits[2] == 0xff)) {
                res = 0;
            } else {
                res = (int) (bits[0] * Math.pow(256, 2) + bits[2] * 256 + bits[1]);
            }
        }


        if (bits.length == 2) {



            if ((bits[0] == 0xff)
                    & (bits[1] == 0xff)) {

                res = 0;
            } else {
                res = bits[0] * 256 + bits[1];
            }





        }


        if (bits.length == 1) {
            if (bits[0] == 0xff) {
                res = 0;
            } else {
                res = bits[0];
            }
        }
        return res;

    }

    public static boolean isBitSet(int val, int bit) {

        boolean result = ((val & (1 << bit)) != 0);

        return result;

    }

    public static int setBit(int src, int bit) {

        return src | (1 << bit);

    }

    // function SetBit(Src, bit: Integer): Integer;
//begin
    //result := Src or (1 shl bit);
//end;
    public static boolean isBitSet(byte val, byte bit) {

        boolean result = ((val & (1 << bit)) != 0);

        return result;

    }

    private void init(int ir) {

        String bits = Integer.toBinaryString(ir);

        for (int i = bits.length() - 1, j = 0; i >= 0; i--, j++) {

            char c = bits.charAt(i);

            if (c == '1') {
                this.set(j);
            }

        }



    }

    public int getInt() {

        int r = 0;


        String bbits = new String();
        for (int j = this.length(); j >= 0; j--) {
            bbits += (this.get(j) ? "1" : "0");
        }
        r = Integer.parseInt(bbits, 2);
        return r;
    }
}
