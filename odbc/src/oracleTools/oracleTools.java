/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package oracleTools;

import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import Yield.*;

/**
 *
 * @author D882758
 */

public class oracleTools {
    
    public Connection db;
    public Statement stm;
    public ResultSet rset;
    public ResultSetMetaData rsmd;
    
    public void Connect(String dbhost, 
                            String dbname, 
                            String dbuser, 
                            String dbpass, 
                            Integer dbport ) 
    throws ClassNotFoundException, SQLException {
       Class.forName("oracle.jdbc.OracleDriver"); 
       db = DriverManager.getConnection("jdbc:oracle:thin:@"+
                                        dbhost+":"+dbport.toString()+":"+
                                        dbname, dbuser, dbpass );
        stm = db.createStatement();                      
    } 
    
    public void Query( String query, String[] params )
    throws SQLException {
        PreparedStatement ps = db.prepareStatement(query, params);
        rset = ps.executeQuery();
        rsmd = rset.getMetaData();
    }
    
    public void Close() throws SQLException {
        db.close();
    }
    
    public Map<String,Object> ResultSetToMap(ResultSet rset)
    throws SQLException {
        Map<String, Object> ret = new HashMap<>();
        for (int i=1; i<=rsmd.getColumnCount();i++ ){
            ret.put(rsmd.getColumnName(i), rset.getObject(i));
        }
        return ret;
    }
    
    public Yielderable<Map<String,Object>> FetchAll() {
        return yield ->{  
            try {
                while (rset.next()) {               
                    yield.returning( ResultSetToMap(rset) ); 
                }
                rset.close();
            }
            catch (SQLException ex) { 
                //
            }
        };
    }
}        