package Hive;

/**
 * Created by Benny on 2017/5/16.
 */

import com.google.gson.JsonArray;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;
import java.util.ArrayList;

public class HiveJdbcClient {

    private static String driverName = "org.apache.hive.jdbc.HiveDriver";

    public static void main(String[] args) throws SQLException {
        /*
         * create connection table
         */
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
        Connection con = DriverManager.getConnection("jdbc:hive2://192.168.1.35:10000/xyzprinting", "neil", "neillin");
        Statement stmt = con.createStatement();
        String tableName = "testHiveDriverTable";
        //stmt.executeQuery("drop table " + tableName);

        ResultSet res;
        String sql;

        /*
         * create table
         */
//        res = stmt.executeQuery("create table " + tableName + " (key int, value string)");
//        System.out.println(res);

         /*
          * show table
          */
//        sql = "show tables '" + tableName + "'";
//        System.out.println("Running: " + sql);
//        res = stmt.executeQuery(sql);
//        if (res.next()) {
//            System.out.println(res.getString(1));
//        }

         /*
          * describe table
          */
//        sql = "describe " + tableName;
//        System.out.println("Running: " + sql);
//        res = stmt.executeQuery(sql);
//        while (res.next()) {
//            System.out.println(res.getString(1) + "\t" + res.getString(2));
//        }

        /*
         * load data into table
         * NOTE: filepath has to be local to the hive server
         * NOTE: /tmp/a.txt is a ctrl-A separated file with two fields per line
         */
//        String filepath = "/Users/Benny/Desktop/a.txt";
//        sql = "load data local inpath " + "'" + filepath + "'" +"into table " + tableName;
//        System.out.println("Running: " + sql);
//        res = stmt.executeQuery(sql);


        /*
         *insert data
         */
//        for (int i=0 ; i <= 100; i++) {
//            sql = "insert into table " + tableName  + " values ("+ i +",'aaa"+ i +"')";
//            System.out.println(sql);
//            res = stmt.executeQuery(sql);
//            while (res.next()){
//                System.out.println(String.valueOf(res.getInt(1))+ "\t" + res.getString(2));
//            }
//        }

        /*
         *select * query
         */
        JsonArray jsonArray = new JsonArray();

        sql = "select * from " + tableName;
        System.out.println("Running: " + sql);
        res = stmt.executeQuery(sql);
        String result;
        String result2;
        ArrayList arrayList = new ArrayList();
        while (res.next()) {
            //System.out.println(String.valueOf(res.getInt(1)) + "\t" + res.getString(2));
            result = res.getString(1);
            result2 = res.getString(2);
            System.out.println(result + ":" + result2);
            arrayList.add(result);
        }
        System.out.println(arrayList);
        String test = arrayList.get(0).toString();
        System.out.println(test);

        /*
         *regular hive query
         */
//        sql = "select count(1) from " + tableName;
//        System.out.println("Running: " + sql);
//        res = stmt.executeQuery(sql);
//        while (res.next()) {
//            System.out.println(res.getString(1));
//        }
    }
}
