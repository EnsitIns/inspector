package files;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author 1
 */
public class MD5 {

//    java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
//md.update(code.getBytes() , 0,  code.length());
//byte[] res = new byte[32];
//md.digest(res,0,32);
    public String getHash(byte[] bytes) {

        MessageDigest md5;
        StringBuilder hexString = new StringBuilder();

        try {

            md5 = MessageDigest.getInstance("md5");

            md5.reset();
            md5.update(bytes);


            byte messageDigest[] = md5.digest();

            for (int i = 0; i < messageDigest.length; i++) {
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }

        } catch (NoSuchAlgorithmException e) {
            return e.toString();
        }

        return hexString.toString();
    }

    public String getHash(String str) {

        MessageDigest md5;
        StringBuilder hexString = new StringBuilder();

        try {

            md5 = MessageDigest.getInstance("md5");

            md5.reset();
            md5.update(str.getBytes());


            byte messageDigest[] = md5.digest();

            for (int i = 0; i < messageDigest.length; i++) {
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }

        } catch (NoSuchAlgorithmException e) {
            return e.toString();
        }

        return hexString.toString();
    }

    
}
