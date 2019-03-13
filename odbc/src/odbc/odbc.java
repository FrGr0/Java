/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package odbc;

import java.sql.*;
import java.util.Map;

import oracleTools.*;

/**
 *
 * @author D882758
 */
public class odbc {

    /**
     * @param args the command line arguments
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        int errcode = 0;
        System.out.println( "*** test odbc java ***" );
        
        try {
            
            oracleTools db = new oracleTools();       
            db.Connect( "M3B8X001", "GLDDF008", "******", "******", 1521);       
            System.out.println( "ouverture de la connexion a la base de donnees" );

            db.Query( "select aracexr, lpad(to_char(aracexvl), 3, '0' ) as aracexvl "+
                      "from artuc where rownum<=10" , null );
           
            for ( Map<String, Object> row : db.FetchAll() ) {
                row.entrySet().stream().forEach((field) -> {
                    System.out.println(field.getKey()+" : "+ field.getValue().toString() );
                });
                System.out.println( "**************************************************" );
            }

            db.Close();
            System.out.println( "fermeture de connexion a la base de donnees" );
            System.out.println( "*** test odbc java OK ***" );
        }
        catch(SQLException ex) {
            System.out.println( "erreur : "+ex.getMessage());
            errcode=101;
        }
        finally {
            System.exit(errcode);
        }
    }
}
