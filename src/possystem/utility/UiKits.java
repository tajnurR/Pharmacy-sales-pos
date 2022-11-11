
package possystem.utility;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import possystem.view.Dashboard;


public class UiKits {
    
    public static String uName;
    public static String uRole;

   
   
   
   
    Config config = new Config();
    PreparedStatement pst;
    ResultSet rs;
    
    DecimalFormat df = new DecimalFormat("0.00");
    
  public void qtyTable(DefaultTableModel dtm, JTable qtyTable){
      String[] col = {"ID", "Medicine Name","Form","Company", "Group", "Purchase Price","MRP", "Qty"};
      dtm = new DefaultTableModel();
      dtm.setColumnIdentifiers(col);
      qtyTable.setModel(dtm);
      qtyTable.getColumnModel().getColumn(0).setPreferredWidth(0);
      qtyTable.getColumnModel().getColumn(0).setMinWidth(0);
      qtyTable.getColumnModel().getColumn(0).setMaxWidth(0);
      
      String qtyLowQ = "SELECT m.medicine_list_id, m.medicine_name, c.company_name, m.physical_type, m.mrp_price, m.group, m.size_mg, m.w_price, m.qty FROM ppm_medicine_list m INNER JOIN ppm_company_name c ON m.company_name_ml = c.company_id ORDER BY qty ASC;";
      
        try {
            pst = config.getCon().prepareStatement(qtyLowQ);
            rs = pst.executeQuery();
            
            while(rs.next()){
            int pId = rs.getInt("medicine_list_id");
            String pName = rs.getString("medicine_name");
            String pCompanyName = rs.getString("company_name");
            String pTyp = rs.getString("physical_type");
            double pMRP = rs.getDouble("mrp_price");
            double pPursPrice = rs.getDouble("w_price");
            String pGroup = rs.getString("group");
            String pSize = rs.getString("size_mg");
            int pQty = rs.getInt("qty");
            
            dtm.addRow(new Object[] {pId, (pName+ " "+pSize),pTyp, pCompanyName, pGroup, pPursPrice,pMRP,pQty});
            
            }
            
            stopDataase();
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        }
  }
    
    public void stopDataase(){
        try {
            pst.close();
            config.getCon().close();
        } catch (SQLException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void salesUpdate(Object[][] table, String cName, String cPhone){
       

            java.util.Date javaDate = new java.util.Date();
            java.sql.Date mySQLDate = new java.sql.Date(javaDate.getTime());
        
        String customarID = null;
        int randomID = (int) Math.round(Math.random()*1000000);
        customarID = "ppm"+Integer.toString(randomID)+"";
        
       
        String salesQ = "INSERT INTO ppm_sales_inventory (sales_customar_id , sales_product_id, sales_mrp_price, sales_qty, sales_s_price, sales_date, sales_customer_n,sales_custonar_phone,sale_p_price) values (?,?,?,?,?,?,?,?,?)";
        try {
            pst = config.getCon().prepareStatement(salesQ);
            
            int prodID = 0;
            int salesQty =0;
            for (int i = 0; i < table.length; i++) {
                for (int j = 0; j < 1; j++) {
//                    
                    prodID = (int) table[i][j];
                    double mrpP = (double) table[i][j+1];
                    salesQty = (int) table[i][j+2];
                    double saleP = (double) table[i][j+3];
                    double pPrice =  (double) table[i][j+4];
                   
                    
                    pst.setString(1, customarID);
                    pst.setInt(2, prodID);
                    pst.setDouble(3, mrpP);
                    pst.setInt(4, salesQty);
                    pst.setDouble(5, saleP);
                    pst.setDate(6, mySQLDate);
                    pst.setString(7, cName);
                    pst.setString(8, cPhone);
                     pst.setDouble(9, pPrice);

                    
                }
                    pst.executeUpdate();
//                    stockUpdate(salesQty, prodID);
                    
            }
            pst.close();
            config.getCon().close();
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    // User in show exsiting qty when click in selected table row
    public int getQtry(int pId){
    
    String stockQ = "SELECT qty FROM ppm_medicine_list WHERE medicine_list_id = ?;";
    int avStock = 0;
        try {
            pst = config.getCon().prepareStatement(stockQ);
            pst.setInt(1, pId);
            rs = pst.executeQuery();
            if (rs.next()) {
               avStock = rs.getInt("qty"); 
            }
            pst.close();
            config.getCon().close();
           
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return avStock;
    }
    
    public void stockUpdate(int stock, int id){
        String StockUpQ = "UPDATE ppm_medicine_list SET qty =(qty-"+stock+") WHERE medicine_list_id = "+id+";";
        try {
            pst = config.getCon().prepareStatement(StockUpQ);
//            pst.setInt(1, stock);
//            pst.setInt(id, id);
            
            pst.executeUpdate();
//            pst.close();
//            config.getCon().close();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void setImage(JLabel jLabel, String imgPath){
         Dimension d = jLabel.getSize();
        try {
            Image img = ImageIO.read(new File("files/"+imgPath+"")).getScaledInstance(d.width-5, d.height-5, Image.SCALE_SMOOTH);
            jLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
        } 
    }
    
    public void setComboBoxItem( JComboBox box, String[] items ){
    
        for (String item : items) {
            box.addItem(item);
        }
        
    }
    
    public void setPopupMenues(DefaultListModel dlm, String keywors, String Query, String dbCol){
        
        dlm.removeAllElements();
        try {
            pst = config.getCon().prepareStatement(Query);
            pst.setString(1, keywors+"%");
            
            rs = pst.executeQuery();
            while(rs.next()){
                dlm.addElement(rs.getString(dbCol));
                
            }
            
            pst.close();
            config.getCon().close();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void setPopupMenues(DefaultListModel dlm, String keywors, String Query){
        
        dlm.removeAllElements();
        try {
            pst = config.getCon().prepareStatement(Query);
            pst.setString(1, keywors+"%");
            
            rs = pst.executeQuery();
            while(rs.next()){
                dlm.addElement(rs.getString("medicine_list_id") + "   " + rs.getString("medicine_name")+" "+rs.getString("size_mg"));
                
            }
            
            pst.close();
            config.getCon().close();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void InsertOrUpdateItem(String Queary, String mName, String mCompany, String type, double mMRP, double mPprice, String mGroup, String mSize, int mQty){
        try {
            pst = config.getCon().prepareStatement(Queary);
            pst.setString(1, mName);
            pst.setString(2, mCompany);
            pst.setString(3, type);
            pst.setDouble(4, mMRP);
            pst.setDouble(5, mPprice);
            pst.setString(6, mGroup);
            pst.setString(7, mSize);
            pst.setInt(8, mQty);
            
            pst.executeUpdate();
            
            pst.close();
            config.getCon().close();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void InsertOrUpdateItem(String Queary, String mName, String mCompany, String type, double mMRP, double mPprice, String mGroup, String mSize, int mQty, int mId){
//       int currentQty = getQtry(mId);
        try {
            
            pst = config.getCon().prepareStatement(Queary);
            pst.setString(1, mName);
            pst.setString(2, mCompany);
            pst.setString(3, type);
            pst.setDouble(4, mMRP);
            pst.setDouble(5, mPprice);
            pst.setString(6, mGroup);
            pst.setString(7, mSize);
            pst.setInt(8, (mQty));
            pst.setInt(9, mId);
            
            pst.executeUpdate();
            
            pst.close();
            config.getCon().close();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void deleteItem(String query, int id){
        try {
            pst = config.getCon().prepareStatement(query);
            pst.setInt(1, id);
            
            pst.executeUpdate();
            
            pst.close();
            config.getCon().close();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
   public String todaySales(String Quary){
       String tTotal = null;
        try {
            pst = config.getCon().prepareStatement(Quary);
            rs = pst.executeQuery();
            
            if (rs.next()) {
                double x = rs.getDouble(1);
                x = x/1000;
                tTotal = df.format(x);
            }
            stopDataase();
            
//            pst.close();
//            config.getCon().close();
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tTotal;
   }
   
   public double todayExpense(String expensQ){
//       java.util.Date javaDate = new java.util.Date();
//         java.sql.Date mySQLDate = new java.sql.Date(javaDate.getTime());
//       String expensQ = "SELECT sum(ex_p_price*ex_qty) FROM polinpharmacyerp.ppm_expenses_table WHERE ex_date = '"+mySQLDate+"';";
       double ex = 0;
        try {
            pst = config.getCon().prepareStatement(expensQ);
            rs = pst.executeQuery();
            if (rs.next()) {
                ex = rs.getDouble(1);
                ex = ex/1000;
                
            }
            stopDataase();
        } catch (ClassNotFoundException ex1) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex1);
        } catch (SQLException ex1) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex1);
        }
            
        
   return ex;
   }
   
   public void expenseInsert(double price, int qty){
       String expenTable = "INSERT INTO ppm_expenses_table (ex_product_id, ex_p_price,ex_qty,ex_date) VALUES ((SELECT medicine_list_id FROM polinpharmacyerp.ppm_medicine_list order by medicine_list_id DESC LIMIT 1),?,?,?);";
       java.util.Date javaDate = new java.util.Date();
       java.sql.Date mySQLDate = new java.sql.Date(javaDate.getTime());
       System.out.println(price);
       System.out.println(qty);
        try {
            pst = config.getCon().prepareStatement(expenTable);
            pst.setDouble(1, price);
            pst.setInt(2, qty);
            pst.setDate(3, mySQLDate);
            
            pst.executeUpdate();
            
            stopDataase();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       
   }
   
   public void expenseInsert(double price, int qty, int id){
       String expenTable = "INSERT INTO ppm_expenses_table (ex_product_id, ex_p_price,ex_qty,ex_date) VALUES (?,?,?,?);";
       java.util.Date javaDate = new java.util.Date();
       java.sql.Date mySQLDate = new java.sql.Date(javaDate.getTime());
//       System.out.println(price);
//       System.out.println(qty);
        try {
            pst = config.getCon().prepareStatement(expenTable);
            pst.setInt(1, id);
            pst.setDouble(2, price);
            pst.setInt(3, qty);
            pst.setDate(4, mySQLDate);
            
            pst.executeUpdate();
            
            stopDataase();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       
   }
   
   public double OtherExpenseTotal(String exQueary){
//       java.util.Date javaDate = new java.util.Date();
//       java.sql.Date mySQLDate = new java.sql.Date(javaDate.getTime());
       
       double othExp = 0;
        try {
            pst = config.getCon().prepareStatement(exQueary);
            rs = pst.executeQuery();
            
            if (rs.next()) {
               othExp = rs.getDouble(1);
               othExp /=1000;
            }
            
            stopDataase();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       return othExp;
   }
   
   public double CapitalLeft(){
       String getCapital = "SELECT sum(w_price*qty) FROM polinpharmacyerp.ppm_medicine_list;";
       double getC = 0;
        try {
            pst = config.getCon().prepareStatement(getCapital);
            rs = pst.executeQuery();
            
            if (rs.next()) {
                getC = rs.getDouble(1);
                getC /= 1000;
            }
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       
       return getC;
   }
    
   public void othExTable(DefaultTableModel dom, String othExQuary){
       try {
            pst = config.getCon().prepareStatement(othExQuary);
            rs = pst.executeQuery();
        
            while(rs.next()){
                String othTile = rs.getString("oth_title");
                String othDetails = rs.getString("oth_comment");
                String othCost = rs.getString("oth_cost");
                String othDate = rs.getString("oth_date");
                String othUser = rs.getString("oth_user");
                
                
                dom.addRow(new Object[] {othTile,othDetails,othCost,othUser,othDate});
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
   
    }
   
   public void CustomarTableUpdate(String quary, DefaultTableModel dom){
       
       int totalRow = dom.getRowCount();

        for (int i = totalRow - 1; i >= 0; i--) {
            dom.removeRow(i);
        }
        try {
            pst = config.getCon().prepareStatement(quary);
            rs = pst.executeQuery();
            
            while(rs.next()){
                String id = rs.getString("sales_customar_id");
                String cName = rs.getString("sales_customer_n");
                String cPhone = rs.getString("sales_custonar_phone");
                String cQty = rs.getString("sales_qty");
                String cPrice = rs.getString("sales_s_price");
                String cMName = rs.getString("medicine_name")+" "+rs.getString("size_mg");
                String cDate = rs.getString("sales_date");

                dom.addRow(new Object[]{id, cName, cPhone,cMName, cQty, cPrice,cDate});
                
            }
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        
   
   }
   
   
   public boolean loginAccess (String loginQ){
       boolean x = false;
        try {
            pst = config.getCon().prepareStatement(loginQ);
             rs = pst.executeQuery();
            
            if (rs.next()) {
               uName = rs.getString("userName");
               uRole = rs.getString("usRole");
               x=true;
            }
            System.out.println("From Kits "+uName);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UiKits.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return x;
   }
   
     public void imageSize(JLabel btn, String image) {

        Dimension d = btn.getSize();
        try {
            Image img = ImageIO.read(new File("files/"+image+"")).getScaledInstance((int) (d.height), (int) (d.height), Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        } catch (Exception e) {
        }

    }
   
}



