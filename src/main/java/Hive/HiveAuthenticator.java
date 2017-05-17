package Hive;

/**
 * Created by Benny on 2017/5/16.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.security.sasl.AuthenticationException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hive.service.auth.PasswdAuthenticationProvider;

public class HiveAuthenticator implements PasswdAuthenticationProvider  {

    @Override
    public void Authenticate(String username, String password)
            throws AuthenticationException {

        boolean ok = false;
        String passMd5 = new MD5().md5(password);
        HiveConf hiveConf = new HiveConf();
        Configuration conf = new Configuration(hiveConf);
        String filePath = conf.get("hive.server2.custom.authentication.file");
        System.out.println("hive.server2.custom.authentication.file [" + filePath + "] ..");
        File file = new File(filePath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String passwordData = null;
            while ((passwordData = reader.readLine()) != null) {
                String[] datas = passwordData.split(",", -1);
                if(datas.length != 2) continue;
                //check user account is OK
                if(datas[0].equals(username) && datas[1].equals(passMd5)) {
                    ok = true;
                    break;
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthenticationException("read authentication config file error, [" + filePath + "] ..", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {}
            }
        }
        if(ok) {
            System.out.println("user account [" + username + "] authentication check ok .. ");
        } else {
            System.out.println("user account [" + username + "] authentication check fail .. ");
            throw new AuthenticationException("user account [" + username + "] authentication check fail .. ");
        }
    }

    //MD5加密
    class MD5 {
        private MessageDigest digest;
        private char hexDigits[] = {'0','1','2','3','4','5','6','7','8','9','x','y','z','p','r','i','n','t'};//可自行修改
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
