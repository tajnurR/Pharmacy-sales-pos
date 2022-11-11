
package possystem.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Config {
    Connection con;
    String username = "root";
    String pass = "12345";
    String url = "jdbc:mysql://localhost:3306/polinpharmacyerp";
    String driver = "com.mysql.cj.jdbc.Driver";
    
    public Connection getCon() throws ClassNotFoundException, SQLException{
        Class.forName(driver);
        con = DriverManager.getConnection(url, username, pass);
        
    return con;
    }
    
    
}
