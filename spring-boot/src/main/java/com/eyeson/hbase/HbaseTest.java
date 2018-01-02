package com.eyeson.hbase;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;

import java.io.IOException;

/**
 * Created by Blues Zhao on 下午5:25 2018/1/16.
 */
public class HbaseTest {

    public static final String TABLE_NAME = "table1";
    public static final String FAMILY_NAME = "family1";
    public static final String ROW_KEY = "rowkey1";
//    public static void main(String[] args) throws Exception {
//        System.setProperty("hadoop.home.dir", "/Users/blues/dev/hbase/rootdir");
//        String createTableName = "mytable2";
//        Configuration configuration = HBaseConfiguration.create();;
//        configuration.set("hbase.zookeeper.quorum", "127.0.0.1:2181");
//        configuration.set("hadoop.home.dir", "/Users/blues/dev/hbase/rootdir");
//        //configuration.set("hbase.master", "10.10.2.66:600000");
//        System.out.println("start create table ......");
//        try {
//            HBaseAdmin hBaseAdmin = new HBaseAdmin(configuration);
//            HTableDescriptor tableDescriptor = new HTableDescriptor(createTableName);
//            tableDescriptor.addFamily(new HColumnDescriptor("column1"));
//            tableDescriptor.addFamily(new HColumnDescriptor("column2"));
//            tableDescriptor.addFamily(new HColumnDescriptor("column3"));
//            hBaseAdmin.createTable(tableDescriptor);
//            hBaseAdmin.close();
//        } catch (MasterNotRunningException e) {
//            e.printStackTrace();
//        } catch (ZooKeeperConnectionException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println("end create table ......");
//    }

    //创建表
    private static void createTable(final HBaseAdmin hBaseAdmin)
            throws IOException {
        if (!hBaseAdmin.tableExists(TABLE_NAME)) {
            HTableDescriptor tableDescriptor = new HTableDescriptor(TABLE_NAME);
            HColumnDescriptor family = new HColumnDescriptor(FAMILY_NAME);
            tableDescriptor.addFamily(family);
            hBaseAdmin.createTable(tableDescriptor);
        }
    }
}
