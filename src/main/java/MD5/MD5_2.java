package MD5;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Benny on 2017/5/11.
 */
public class MD5_2 {


    class MD5 {
        private MessageDigest digest;
        private char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'x', 'y', 'z', 'p', 'r', 'i', 'n', 't'};

        public MD5() {
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }


        public String md5(String str) {
            byte[] btInput = str.getBytes();
            digest.reset();
            digest.update(btInput);
            byte[] md = digest.digest();
            // change password to 16 byte
            int j = md.length;
            char strChar[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                strChar[k++] = hexDigits[byte0 >>> 4 & 0xf];
                strChar[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(strChar);
        }

    }
}