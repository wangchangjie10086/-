package com.ingcore.generator.build;


import com.ingcore.generator.config.ConfigUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class BuilderTableMapping {

    List<String> _tables = null;
    static Connection conn = null;
    static String _schema = null;
    static String C_K = "ck";

    public static Connection getConnectionByJDBC() {
        String driver = ConfigUtils.readProperties("driverClass");
        String uid = ConfigUtils.readProperties("userId");
        String pid = ConfigUtils.readProperties("password");
        String conurl = ConfigUtils.readProperties("connectionURL");
        splitSchema(conurl);
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection(conurl, uid, pid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static void splitSchema(String connectionUrl) {
        int star = connectionUrl.indexOf('/');
        star = connectionUrl.indexOf('/', star + 1);
        star = connectionUrl.indexOf('/', star + 1);
        int end = connectionUrl.indexOf('?');

        _schema = connectionUrl.substring(star + 1, end);
    }

    public void queryTables() {

        _tables = new ArrayList<String>();
        String sql = "show tables";
        getConnectionByJDBC();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String n = rs.getString(1);
                _tables.add(n);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static HashMap<String, String> queryFileds(String tableName) {
        HashMap<String, String> hm = new HashMap<String, String>();

        BuilderTableMapping.getConnectionByJDBC();
        String sql = "select column_name,data_type,column_key from information_schema.columns  where table_schema='" + BuilderTableMapping._schema + "' and table_name='" + tableName + "'";

        try {
            Statement stmt = BuilderTableMapping.conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String n = rs.getString(1);
                String t = rs.getString(2);
                String ck = rs.getString(3);
                if (!hm.containsKey(n)) {
                    hm.put(n, t);
                }
                if (ck != null && ck.contains("PRI") && !hm.containsKey(ck)) {
                    hm.put(C_K, t);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (BuilderTableMapping.conn != null)
                    BuilderTableMapping.conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return hm;
    }


    public void printXml() {
        System.out.print("<!--copy the xml into generatorConfig.xml-->\n");
        for (int i = 0; i < _tables.size(); i++) {
            String n = _tables.get(i);
            String d = this.getLongTextDefine(n);
            System.out.print("<table schema=\"" + _schema + "\" tableName=\"" + n + "\"\n");
            System.out.print(" enableUpdateByExample=\"false\" enableDeleteByExample=\"false\" selectByExampleQueryId=\"false\" >\n");
            System.out.print(d);
            System.out.print("</table>\n");
        }
    }

    public String getLongTextDefine(String tableName) {
        String def = "";
        HashMap<String, String> _fileds = this.queryFileds(tableName);

        if (_fileds != null) {
            Iterator<Entry<String, String>> it = _fileds.entrySet().iterator();
            while (it.hasNext()) {

                Entry<String, String> entry = (Entry<String, String>) it.next();
                Object key = entry.getKey();
                Object value = entry.getValue();

                if (value.equals("text") || value.equals("longtext") || value.equals("mediumtext")) {
                    def += "<columnOverride column=\"" + key.toString() + "\" javaType=\"java.lang.String\" jdbcType=\"VARCHAR\" />\n";
                }
            }
        }
        return def;
    }

}