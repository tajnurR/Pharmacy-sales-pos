package possystem.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import possystem.utility.Config;
import possystem.utility.UiKits;

public class Dashboard extends javax.swing.JFrame {

    
//    String xNames = login.uName;
    // UI Kits (UiKits) class contains tools and Methods
    UiKits kits = new UiKits();
    //Config is Database connecions
    Config config = new Config();

    // SQL connections
    PreparedStatement pst;
    ResultSet rs;

    // DefaultListModel Reference
    DefaultListModel dlm;

    //Final Product ID (fID)
    // Search result send this ID and ShowTable get data from Database base on it
    public static int fID = 0;
    
    public static String customarIDn = null;

    //DefaultTableModel Referance
    DefaultTableModel dom;

    // sQtry Qty that user add for sales
    String sQtry;
    //After sMRP comse form database and it's a single item price
    String sMRP;

    // DecimalFormat is a Time Formator
    DecimalFormat d = new DecimalFormat("0.00");

    // Last Stock avaiable in database
    int avStock;

    // Image Path
    String logoPath = "image.png";
   
   

    // Combobox items
    String dosegesForms[] = {"Tablet", "Capsule", "Syrup", "Solutions", "Powders", "Herbal", "Pastes", "Gases", "Eye drops", "Lotions", "Ointments", "Emulsions", "Injection"};
    
    //user role session
    String userName = kits.uName;
    String userRole = kits.uRole;
    public Dashboard() {

        initComponents();
        
       String userImg = "user.png";
       txtuserName.setText(userName);
       kits.setImage(txtUserImage, userImg);
        
        display();
        // Logo Set and Resize
        kits.setImage(jLLogo, logoPath);

        // Search Image Set and Resize
        imageSize();
//        Image for Deshbord
           String dImage = "btn/d_white.png";
           imageSize(tbnDashboard, dImage);
           
           // Image for Sales
           String sImage = "btn/s_white.png";
           imageSize(tbnSales, sImage);
           
           // Image for Add Item
           String addImage = "btn/add_white.png";
           imageSize(tbnAddItem, addImage);
           
           // Image for Expenses
           String exImage = "btn/ex_white.png";
           imageSize(tbnExpense, exImage);
           
           // Image for Record
           String reImage = "btn/re_white.png";
           imageSize(tbnRecords, reImage);

        // Default List Model Object
        dlm = new DefaultListModel();

        // Suggest menus for sales Search box
        // sManu is a Popup Menu and slist is a list item
        sMenu.add(sPan);
        sList.setModel(dlm);
        
        // Suggest menus for sales Search box
        // sManu is a Popup Menu and slist is a list item
        popIsearch.add(pIseach);
        listISearch.setModel(dlm);

        // Suggest menus for Generic Search box from Add Items tab
        // popupGene is a Popup Menu and genericList is a list item
        popupGene.add(pGene);
        genericList.setModel(dlm);

        // Suggest menus for Company Search box from Add Items tab
        // popupGene is a Popup Menu and listComapny is a list item
        popCompany.add(pCompany);
        listComapny.setModel(dlm);
        
        // Suggest menus for Customar Search box from Recors tab
        // popCustomarSerach is a Popup Menu and listCustomarS is a list item
        popCustomarSerach.add(panCustomarS);
        listCustomarS.setModel(dlm);

        //getDates Method set the current time and date
        jDate.setText(getDates());

        //FixTable set Table column names
        fixTable();

        // setComboBoxItem Method add drop down menus list 
        kits.setComboBoxItem(conDosForm, dosegesForms);
        
    }
      public java.sql.Date dateValidator(String date){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-mm-dd");
        java.sql.Date dates = null;
        try {
            dtf.parse(date);
            java.sql.Date d = java.sql.Date.valueOf(date);
            dates = d;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invaid Formate! use 'YYYY-MM-DD'");
        }
        
        return dates;
    }
      
      
      
    public void fixTable() {
        String column[] = {"ID", "Name", "Company", "Type", "Group", "MRP", "Qty", "Sale Price","Price"};
        dom = new DefaultTableModel();
        dom.setColumnIdentifiers(column);
        tblCart.setModel(dom);
        tblCart.getColumnModel().getColumn(0).setPreferredWidth(5);
        tblCart.getColumnModel().getColumn(8).setPreferredWidth(0);
        tblCart.getColumnModel().getColumn(8).setMinWidth(0);
        tblCart.getColumnModel().getColumn(8).setMaxWidth(0);
    }

    public void showTable() {
        int id = fID;
        ResultSet r;
        String sq = "SELECT m.medicine_list_id, m.medicine_name, c.company_name, m.physical_type, m.mrp_price, m.group, m.size_mg, m.w_price FROM ppm_medicine_list m INNER JOIN ppm_company_name c ON m.company_name_ml = c.company_id WHERE m.medicine_list_id =?";

        try {
            pst = config.getCon().prepareStatement(sq);
            pst.setInt(1, id);
            r = pst.executeQuery();

            while (r.next()) {
                int tID = r.getInt("medicine_list_id");
                String tname = r.getString("medicine_name") + " " + r.getString("size_mg");
                String tCompany = r.getString("company_name");
                String tGroup = r.getString("group");
                double tMrp = r.getDouble("mrp_price");
                String tType = r.getString("physical_type");
                int qty = 1;
                double pPrice = r.getDouble("w_price");
                
                int currentTotalRow = dom.getRowCount();
                int dupCount = 0;
                for (int i = 0; i < currentTotalRow; i++) {
                    int tableRow = (int) dom.getValueAt(i, 0);
                    if (tID == tableRow) {
                        dupCount+=1;
                    }
                }
                if (dupCount == 0) {
                     dom.addRow(new Object[]{tID, tname, tCompany, tType, tGroup, tMrp, qty, tMrp, pPrice});
                }else{
                    JOptionPane.showMessageDialog(this, "This Item Already added");
                    
                }
            }

            pst.close();
            config.getCon().close();

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String getDates() {
        LocalDate cDate = LocalDate.now();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return cDate.format(f);
    }

    static void display() {
        int deley = 1000;
        Timer timer = new Timer(deley, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Date dateTime = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss a");
                String formatedtime = formatter.format(dateTime);
                jTime.setText(formatedtime);
            }
        });
        timer.start();
    }
    

    public void imageSize() {

        Dimension d = btnSearch.getSize();
        try {
            Image img = ImageIO.read(new File("files/search.png")).getScaledInstance(d.width / 2, d.width / 2, Image.SCALE_SMOOTH);
            btnSearch.setIcon(new ImageIcon(img));
        } catch (Exception e) {
        }

    }   
    
    String fileName = "deshbors.png";
        public void imageSize(JButton btn, String image) {

        Dimension d = btn.getSize();
        try {
            Image img = ImageIO.read(new File("files/"+image+"")).getScaledInstance((int) (d.height/ 1.5), (int) (d.height / 1.5), Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        } catch (Exception e) {
        }

    }

    public void totalCal() {

        int allRow = tblCart.getRowCount();
//        System.out.println("All Rows"+ allRow);
        double total = 0;
        double subTotal = 0;
        for (int i = 0; i < allRow; i++) {
            total += (double) dom.getValueAt(i, 7);
            double mrp = (double) dom.getValueAt(i, 5);
            int qty = (int) dom.getValueAt(i, 6);
            subTotal += mrp * qty;

        }
        double discount = subTotal - total;

        txtTotal.setText(Double.toString(Math.round(total)));
        txtSubTotal.setText(Double.toString(Math.round(subTotal)));
        txtDisc.setText(Double.toString(Math.round(discount)));
    }

    public void SalesFieldClean(){
        txtISearch.setText("");
        txtItemName.setText("");
        txtGeneric.setText("");
        txtCompanyName.setText("");
        txtUnitePrice.setText("");
        txtQtyAdd.setText("");
        txtStrength.setText("");
        conDosForm.setSelectedIndex(0);
        txtMRPPrice.setText("");
        txtID.setText("");
    }
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sPan = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        sList = new javax.swing.JList<>();
        sMenu = new javax.swing.JPopupMenu();
        popupGene = new javax.swing.JPopupMenu();
        pGene = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        genericList = new javax.swing.JList<>();
        popCompany = new javax.swing.JPopupMenu();
        pCompany = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        listComapny = new javax.swing.JList<>();
        popIsearch = new javax.swing.JPopupMenu();
        pIseach = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        listISearch = new javax.swing.JList<>();
        popCustomarSerach = new javax.swing.JPopupMenu();
        panCustomarS = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        listCustomarS = new javax.swing.JList<>();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        tbnDashboard = new javax.swing.JButton();
        tbnSales = new javax.swing.JButton();
        tbnExpense = new javax.swing.JButton();
        jLLogo = new javax.swing.JLabel();
        tbnRecords = new javax.swing.JButton();
        tbnAddItem = new javax.swing.JButton();
        txtUserImage = new javax.swing.JLabel();
        txtuserName = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        tbPanel = new javax.swing.JTabbedPane();
        panSales = new javax.swing.JPanel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        imgTxt = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCart = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        txtSubTotal = new javax.swing.JLabel();
        txtDisc = new javax.swing.JLabel();
        txtTotal = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnGetPayemnt = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        txtQty = new javax.swing.JTextField();
        txtMRP = new javax.swing.JTextField();
        mrpTitle1 = new javax.swing.JLabel();
        txtDis = new javax.swing.JTextField();
        tbnDiscountUpdate = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnRemoveAll = new javax.swing.JButton();
        mrpTitle2 = new javax.swing.JLabel();
        mrpTitle3 = new javax.swing.JLabel();
        mrpTitle4 = new javax.swing.JLabel();
        lStock = new javax.swing.JLabel();
        txtCPhone = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtCName = new javax.swing.JTextField();
        panDeshboard = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        labSalesBanar = new javax.swing.JLabel();
        labSalesTitle = new javax.swing.JLabel();
        labTodaySales = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        labReExpenBanar = new javax.swing.JLabel();
        labReExpensTitle = new javax.swing.JLabel();
        labTodayExpense = new javax.swing.JLabel();
        jPanel16 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        labCapitalLeft = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        labReProfitBanar = new javax.swing.JLabel();
        labReProfitTitle = new javax.swing.JLabel();
        labProfit = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        tbnDateSearch = new javax.swing.JButton();
        txtStartDate = new com.toedter.calendar.JDateChooser();
        txtEndDate = new com.toedter.calendar.JDateChooser();
        jPanel13 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tblLowQtyItem = new javax.swing.JTable();
        panAddItem = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtItemName = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtGeneric = new javax.swing.JTextField();
        txtStrength = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        txtUnitePrice = new javax.swing.JTextField();
        txtMRPPrice = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        txtQtyAdd = new javax.swing.JTextField();
        txtCompanyName = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        conDosForm = new javax.swing.JComboBox<>();
        btnUpdateInsert = new javax.swing.JButton();
        btnIdelete = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        txtID = new javax.swing.JTextField();
        txtISearch = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        PanelExpenses = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        txtExSearch = new javax.swing.JButton();
        jScrollPane8 = new javax.swing.JScrollPane();
        tableExpense = new javax.swing.JTable();
        jLabel20 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jPanel19 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        txtExTitle = new javax.swing.JTextField();
        jScrollPane7 = new javax.swing.JScrollPane();
        txtExDetails = new javax.swing.JTextArea();
        txtExUser = new javax.swing.JTextField();
        txtExCost = new javax.swing.JTextField();
        btnAddExpense = new javax.swing.JButton();
        labExError = new javax.swing.JLabel();
        labExErrorDetails = new javax.swing.JLabel();
        labExErrorCost = new javax.swing.JLabel();
        txtExStart = new com.toedter.calendar.JDateChooser();
        txtExEnd = new com.toedter.calendar.JDateChooser();
        panRecords = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        txtSearchCustomar = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        txtStartDateRecod = new com.toedter.calendar.JDateChooser();
        txtEndDateRecod = new com.toedter.calendar.JDateChooser();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jScrollPane9 = new javax.swing.JScrollPane();
        tblRecord = new javax.swing.JTable();
        cbInvoicePermission = new javax.swing.JCheckBox();
        jPanel9 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jDate = new javax.swing.JLabel();
        jTime = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        sPan.setFocusable(false);

        sList.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        sList.setFocusable(false);
        sList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sListMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(sList);

        javax.swing.GroupLayout sPanLayout = new javax.swing.GroupLayout(sPan);
        sPan.setLayout(sPanLayout);
        sPanLayout.setHorizontalGroup(
            sPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sPanLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 592, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        sPanLayout.setVerticalGroup(
            sPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        sMenu.setFocusable(false);

        popupGene.setBorderPainted(false);
        popupGene.setFocusable(false);

        pGene.setSize(txtGeneric.getWidth(), txtGeneric.getHeight()*6);

        genericList.setSize(txtGeneric.getWidth(), txtGeneric.getHeight()*6);
        genericList.setBorder(null);
        genericList.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        genericList.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        genericList.setDragEnabled(true);
        genericList.setFocusable(false);
        genericList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                genericListMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(genericList);

        javax.swing.GroupLayout pGeneLayout = new javax.swing.GroupLayout(pGene);
        pGene.setLayout(pGeneLayout);
        pGeneLayout.setHorizontalGroup(
            pGeneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
        );
        pGeneLayout.setVerticalGroup(
            pGeneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        popCompany.setFocusable(false);

        listComapny.setBorder(null);
        listComapny.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        listComapny.setFocusable(false);
        listComapny.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listComapnyMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(listComapny);

        javax.swing.GroupLayout pCompanyLayout = new javax.swing.GroupLayout(pCompany);
        pCompany.setLayout(pCompanyLayout);
        pCompanyLayout.setHorizontalGroup(
            pCompanyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
        );
        pCompanyLayout.setVerticalGroup(
            pCompanyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
        );

        popIsearch.setFocusable(false);

        listISearch.setBorder(null);
        listISearch.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        listISearch.setFocusable(false);
        listISearch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listISearchMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(listISearch);

        javax.swing.GroupLayout pIseachLayout = new javax.swing.GroupLayout(pIseach);
        pIseach.setLayout(pIseachLayout);
        pIseachLayout.setHorizontalGroup(
            pIseachLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
        );
        pIseachLayout.setVerticalGroup(
            pIseachLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        popCustomarSerach.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        popCustomarSerach.setBorder(null);
        popCustomarSerach.setFocusable(false);

        panCustomarS.setBackground(new java.awt.Color(255, 255, 255));
        panCustomarS.setFocusable(false);
        panCustomarS.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        listCustomarS.setBorder(null);
        listCustomarS.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        listCustomarS.setFocusable(false);
        listCustomarS.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listCustomarSMouseClicked(evt);
            }
        });
        jScrollPane10.setViewportView(listCustomarS);

        javax.swing.GroupLayout panCustomarSLayout = new javax.swing.GroupLayout(panCustomarS);
        panCustomarS.setLayout(panCustomarSLayout);
        panCustomarSLayout.setHorizontalGroup(
            panCustomarSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        panCustomarSLayout.setVerticalGroup(
            panCustomarSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setMinimumSize(new java.awt.Dimension(1250, 710));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(1200, 800));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(73, 64, 140));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tbnDashboard.setBackground(new java.awt.Color(124, 115, 191));
        tbnDashboard.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        tbnDashboard.setForeground(new java.awt.Color(255, 255, 255));
        tbnDashboard.setText("   Dashboard");
        tbnDashboard.setAutoscrolls(true);
        tbnDashboard.setBorder(null);
        tbnDashboard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbnDashboardMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tbnDashboardMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tbnDashboardMouseExited(evt);
            }
        });
        jPanel2.add(tbnDashboard, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 130, 210, 55));

        tbnSales.setBackground(new java.awt.Color(124, 115, 191));
        tbnSales.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        tbnSales.setForeground(new java.awt.Color(255, 255, 255));
        tbnSales.setText("   Sales        ");
        tbnSales.setBorder(null);
        tbnSales.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbnSalesMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tbnSalesMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tbnSalesMouseExited(evt);
            }
        });
        jPanel2.add(tbnSales, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 190, 208, 55));

        tbnExpense.setBackground(new java.awt.Color(124, 115, 191));
        tbnExpense.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        tbnExpense.setForeground(new java.awt.Color(255, 255, 255));
        tbnExpense.setText("   Expense");
        tbnExpense.setBorder(null);
        tbnExpense.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbnExpenseMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tbnExpenseMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tbnExpenseMouseExited(evt);
            }
        });
        jPanel2.add(tbnExpense, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 310, 208, 55));
        jPanel2.add(jLLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 10, 120, 110));

        tbnRecords.setBackground(new java.awt.Color(124, 115, 191));
        tbnRecords.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        tbnRecords.setForeground(new java.awt.Color(255, 255, 255));
        tbnRecords.setText("   Records");
        tbnRecords.setBorder(null);
        tbnRecords.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbnRecordsMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tbnRecordsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tbnRecordsMouseExited(evt);
            }
        });
        jPanel2.add(tbnRecords, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 370, 208, 55));

        tbnAddItem.setBackground(new java.awt.Color(124, 115, 191));
        tbnAddItem.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        tbnAddItem.setForeground(new java.awt.Color(255, 255, 255));
        tbnAddItem.setText("   Add Items");
        tbnAddItem.setBorder(null);
        tbnAddItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbnAddItemMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tbnAddItemMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tbnAddItemMouseExited(evt);
            }
        });
        jPanel2.add(tbnAddItem, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 250, 208, 55));
        jPanel2.add(txtUserImage, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 570, 30, 30));

        txtuserName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtuserName.setForeground(new java.awt.Color(204, 204, 204));
        jPanel2.add(txtuserName, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 570, 110, 30));

        jLabel35.setFont(new java.awt.Font("Bell MT", 0, 12)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(255, 255, 255));
        jLabel35.setText("Login as:");
        jPanel2.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 540, 170, 20));

        jButton2.setText("Log out");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });
        jPanel2.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 620, 190, -1));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 710));

        tbPanel.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        tbPanel.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        tbPanel.setToolTipText("");

        panSales.setBackground(new java.awt.Color(255, 255, 255));
        panSales.setPreferredSize(new java.awt.Dimension(1250, 700));

        txtSearch.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        txtSearch.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(191, 192, 192), 1, true));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
        });

        btnSearch.setBackground(new java.awt.Color(88, 77, 168));
        btnSearch.setForeground(new java.awt.Color(255, 255, 255));
        btnSearch.setBorder(null);

        tblCart.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        tblCart.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblCart.setRowHeight(30);
        tblCart.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCartMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblCart);

        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        txtSubTotal.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        txtSubTotal.setText("0.00");

        txtDisc.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        txtDisc.setText("0.00");

        txtTotal.setBackground(new java.awt.Color(51, 255, 0));
        txtTotal.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        txtTotal.setText("0.00");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtSubTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDisc, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(txtSubTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDisc, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("SUB-TOTAL: ");

        jLabel3.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("DISCOUNT (TK) ");

        jLabel4.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("TOTAL: ");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        btnGetPayemnt.setBackground(new java.awt.Color(102, 255, 0));
        btnGetPayemnt.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        btnGetPayemnt.setText("Confirm Payment");
        btnGetPayemnt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnGetPayemntMouseClicked(evt);
            }
        });

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Modify Cart Item"));

        txtQty.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        txtQty.setBorder(null);
        txtQty.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtQtyKeyReleased(evt);
            }
        });

        txtMRP.setEditable(false);
        txtMRP.setBackground(new java.awt.Color(255, 255, 255));
        txtMRP.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        txtMRP.setBorder(null);
        txtMRP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMRPActionPerformed(evt);
            }
        });

        mrpTitle1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        mrpTitle1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        mrpTitle1.setText("New Price");

        txtDis.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        txtDis.setBorder(null);
        txtDis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtDisKeyReleased(evt);
            }
        });

        tbnDiscountUpdate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        tbnDiscountUpdate.setText("Update Qty and Discount");
        tbnDiscountUpdate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbnDiscountUpdateMouseClicked(evt);
            }
        });

        btnRemove.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnRemove.setText("Remove");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        btnRemoveAll.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        btnRemoveAll.setText("Remove All");
        btnRemoveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveAllActionPerformed(evt);
            }
        });

        mrpTitle2.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        mrpTitle2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        mrpTitle2.setText("Add Qty");

        mrpTitle3.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        mrpTitle3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        mrpTitle3.setText("Add discount (%)");

        mrpTitle4.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        mrpTitle4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mrpTitle4.setText("Stock: ");

        lStock.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        lStock.setForeground(new java.awt.Color(51, 204, 0));
        lStock.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(mrpTitle4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lStock, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtMRP, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mrpTitle1, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mrpTitle2, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtQty, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mrpTitle3, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDis, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tbnDiscountUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoveAll, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(mrpTitle1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMRP, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mrpTitle4, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lStock, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(mrpTitle2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtQty, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(mrpTitle3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDis, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(tbnDiscountUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemoveAll, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(256, 256, 256))
        );

        jLabel7.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        jLabel7.setText("Customar Name");

        jLabel8.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        jLabel8.setText("Counter Number");

        javax.swing.GroupLayout panSalesLayout = new javax.swing.GroupLayout(panSales);
        panSales.setLayout(panSalesLayout);
        panSalesLayout.setHorizontalGroup(
            panSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panSalesLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 595, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(panSalesLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(panSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panSalesLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(panSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panSalesLayout.createSequentialGroup()
                                .addComponent(txtCName, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(20, 20, 20)
                                .addComponent(txtCPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(panSalesLayout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(40, 40, 40)
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(181, 181, 181))))
                    .addGroup(panSalesLayout.createSequentialGroup()
                        .addGroup(panSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 689, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panSalesLayout.createSequentialGroup()
                                .addGap(290, 290, 290)
                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8)
                                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panSalesLayout.createSequentialGroup()
                                .addGap(290, 290, 290)
                                .addComponent(btnGetPayemnt, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(11, 11, 11)))
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(panSalesLayout.createSequentialGroup()
                .addGap(290, 290, 290)
                .addComponent(imgTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panSalesLayout.setVerticalGroup(
            panSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panSalesLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(panSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(panSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panSalesLayout.createSequentialGroup()
                        .addGroup(panSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtCName, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                            .addComponent(txtCPhone, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                        .addGap(10, 10, 10)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15)
                        .addGroup(panSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addComponent(btnGetPayemnt, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 550, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(438, 438, 438)
                .addComponent(imgTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        tbPanel.addTab("tab1", panSales);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jPanel5.setBackground(new java.awt.Color(254, 171, 72));

        labSalesBanar.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        labSalesBanar.setForeground(new java.awt.Color(255, 255, 255));
        labSalesBanar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labSalesBanar.setText("Today Sales");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labSalesBanar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labSalesBanar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)
        );

        labSalesTitle.setBackground(new java.awt.Color(255, 255, 255));
        labSalesTitle.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        labSalesTitle.setForeground(new java.awt.Color(102, 102, 102));
        labSalesTitle.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labSalesTitle.setText("   Total Sales");

        labTodaySales.setFont(new java.awt.Font("Times New Roman", 0, 30)); // NOI18N
        labTodaySales.setForeground(new java.awt.Color(51, 153, 0));
        labTodaySales.setText("  3030.55k (TK)");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(labSalesTitle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(labTodaySales, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(labSalesTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labTodaySales, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));

        jPanel11.setBackground(new java.awt.Color(8, 61, 119));

        labReExpenBanar.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        labReExpenBanar.setForeground(new java.awt.Color(255, 255, 255));
        labReExpenBanar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labReExpenBanar.setText("Expenses Today");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labReExpenBanar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labReExpenBanar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)
        );

        labReExpensTitle.setBackground(new java.awt.Color(255, 255, 255));
        labReExpensTitle.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        labReExpensTitle.setForeground(new java.awt.Color(102, 102, 102));
        labReExpensTitle.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labReExpensTitle.setText("   Total Expenses");

        labTodayExpense.setFont(new java.awt.Font("Times New Roman", 0, 30)); // NOI18N
        labTodayExpense.setForeground(new java.awt.Color(51, 153, 0));
        labTodayExpense.setText("  3030.55k (TK)");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(labReExpensTitle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(labTodayExpense, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(labReExpensTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labTodayExpense, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel16.setBackground(new java.awt.Color(255, 255, 255));

        jPanel17.setBackground(new java.awt.Color(239, 71, 111));

        jLabel29.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(255, 255, 255));
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel29.setText("Latest Capital");

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel29, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel29, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)
        );

        jLabel30.setBackground(new java.awt.Color(255, 255, 255));
        jLabel30.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(102, 102, 102));
        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel30.setText("   Capital");

        labCapitalLeft.setFont(new java.awt.Font("Times New Roman", 0, 30)); // NOI18N
        labCapitalLeft.setForeground(new java.awt.Color(51, 153, 0));
        labCapitalLeft.setText("  3030.55k (TK)");

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel17, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel30, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(labCapitalLeft, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labCapitalLeft, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel14.setBackground(new java.awt.Color(255, 255, 255));

        jPanel15.setBackground(new java.awt.Color(73, 64, 140));

        labReProfitBanar.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        labReProfitBanar.setForeground(new java.awt.Color(255, 255, 255));
        labReProfitBanar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labReProfitBanar.setText("Today Profits");

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labReProfitBanar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labReProfitBanar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)
        );

        labReProfitTitle.setBackground(new java.awt.Color(255, 255, 255));
        labReProfitTitle.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        labReProfitTitle.setForeground(new java.awt.Color(102, 102, 102));
        labReProfitTitle.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labReProfitTitle.setText("   Profits");

        labProfit.setFont(new java.awt.Font("Times New Roman", 0, 30)); // NOI18N
        labProfit.setForeground(new java.awt.Color(51, 153, 0));
        labProfit.setText("  3030.55k (TK)");

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(labReProfitTitle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(labProfit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(labReProfitTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labProfit, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));
        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Set Date to Get Old Rrecord", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 12), new java.awt.Color(102, 102, 102))); // NOI18N

        jLabel21.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel21.setText("Start (YYYY-MM-DD)");

        jLabel24.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel24.setText("End (YYYY-MM-DD)");

        tbnDateSearch.setBackground(new java.awt.Color(73, 64, 140));
        tbnDateSearch.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        tbnDateSearch.setForeground(new java.awt.Color(255, 255, 255));
        tbnDateSearch.setText("Search");
        tbnDateSearch.setBorder(null);
        tbnDateSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbnDateSearchMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tbnDateSearchMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tbnDateSearchMouseExited(evt);
            }
        });

        txtStartDate.setDateFormatString("yyyy-MM-dd");
        txtStartDate.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        txtEndDate.setDateFormatString("yyyy-MM-dd");
        txtEndDate.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(57, 57, 57)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(txtEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42)
                        .addComponent(tbnDateSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(402, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tbnDateSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtStartDate, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                    .addComponent(txtEndDate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jLabel24))
                .addContainerGap())
        );

        jPanel13.setBackground(new java.awt.Color(255, 255, 255));
        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("List of low Qty items"));

        tblLowQtyItem.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane6.setViewportView(tblLowQtyItem);

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 967, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(98, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panDeshboardLayout = new javax.swing.GroupLayout(panDeshboard);
        panDeshboard.setLayout(panDeshboardLayout);
        panDeshboardLayout.setHorizontalGroup(
            panDeshboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panDeshboardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panDeshboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panDeshboardLayout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(8, Short.MAX_VALUE))
        );
        panDeshboardLayout.setVerticalGroup(
            panDeshboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panDeshboardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panDeshboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tbPanel.addTab("tab3", panDeshboard);

        panAddItem.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Arial", 1, 30)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(102, 102, 102));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Add new item in Store");

        jLabel9.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        jLabel9.setText("Item Name");

        txtItemName.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        txtItemName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtItemNameActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        jLabel10.setText("Dosage form");

        jLabel11.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        jLabel11.setText("Generic");

        txtGeneric.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        txtGeneric.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtGenericKeyReleased(evt);
            }
        });

        txtStrength.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        jLabel12.setText("Strength");

        jLabel14.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        jLabel14.setText("Unite Price");

        txtUnitePrice.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N

        txtMRPPrice.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N

        jLabel15.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        jLabel15.setText("MRP (Unite)");

        jLabel16.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        jLabel16.setText("Qty");

        txtQtyAdd.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N

        txtCompanyName.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        txtCompanyName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCompanyNameKeyReleased(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        jLabel17.setText("Company");

        conDosForm.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        conDosForm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conDosFormActionPerformed(evt);
            }
        });

        btnUpdateInsert.setBackground(new java.awt.Color(76, 42, 133));
        btnUpdateInsert.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        btnUpdateInsert.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdateInsert.setText("Add New or Update");
        btnUpdateInsert.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnUpdateInsertMouseClicked(evt);
            }
        });

        btnIdelete.setBackground(new java.awt.Color(219, 22, 47));
        btnIdelete.setFont(new java.awt.Font("Arial", 1, 20)); // NOI18N
        btnIdelete.setForeground(new java.awt.Color(255, 255, 255));
        btnIdelete.setText("Delete");
        btnIdelete.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnIdeleteMouseClicked(evt);
            }
        });

        btnReset.setBackground(new java.awt.Color(85, 166, 48));
        btnReset.setFont(new java.awt.Font("Arial", 1, 20)); // NOI18N
        btnReset.setForeground(new java.awt.Color(255, 255, 255));
        btnReset.setText("Reset");
        btnReset.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnResetMouseClicked(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        jLabel13.setText("ID");

        txtID.setEditable(false);
        txtID.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N

        txtISearch.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        txtISearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtISearchKeyReleased(evt);
            }
        });

        jPanel4.setBackground(new java.awt.Color(76, 42, 133));

        jLabel19.setBackground(new java.awt.Color(76, 42, 133));
        jLabel19.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("Search");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panAddItemLayout = new javax.swing.GroupLayout(panAddItem);
        panAddItem.setLayout(panAddItemLayout);
        panAddItemLayout.setHorizontalGroup(
            panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panAddItemLayout.createSequentialGroup()
                .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 686, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panAddItemLayout.createSequentialGroup()
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(panAddItemLayout.createSequentialGroup()
                                    .addComponent(txtISearch, javax.swing.GroupLayout.PREFERRED_SIZE, 465, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(txtItemName, javax.swing.GroupLayout.PREFERRED_SIZE, 558, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(panAddItemLayout.createSequentialGroup()
                            .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                                .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panAddItemLayout.createSequentialGroup()
                                    .addComponent(txtQtyAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(28, 28, 28)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(panAddItemLayout.createSequentialGroup()
                                    .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtCompanyName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtUnitePrice, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(txtGeneric, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(28, 28, 28)
                                    .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtMRPPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtStrength, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(conDosForm, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnUpdateInsert, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panAddItemLayout.createSequentialGroup()
                                .addComponent(btnIdelete, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(37, 37, 37)
                                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(326, Short.MAX_VALUE))
        );
        panAddItemLayout.setVerticalGroup(
            panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panAddItemLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtISearch, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panAddItemLayout.createSequentialGroup()
                        .addGap(105, 105, 105)
                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(panAddItemLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtItemName, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panAddItemLayout.createSequentialGroup()
                                .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtGeneric, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtCompanyName, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtUnitePrice, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panAddItemLayout.createSequentialGroup()
                                .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtStrength, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(conDosForm, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtMRPPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel15))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtQtyAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)))
                .addComponent(btnUpdateInsert, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panAddItemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnIdelete, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(166, 166, 166))
        );

        tbPanel.addTab("tab2", panAddItem);

        PanelExpenses.setBackground(new java.awt.Color(255, 255, 255));

        jLabel18.setFont(new java.awt.Font("Arial", 1, 36)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(155, 93, 229));
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel18.setText(" Add Expense and Review cost");
        jLabel18.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 100, 2, 2, new java.awt.Color(155, 93, 229)));

        txtExSearch.setBackground(new java.awt.Color(155, 93, 229));
        txtExSearch.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        txtExSearch.setForeground(new java.awt.Color(255, 255, 255));
        txtExSearch.setText("Search");
        txtExSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtExSearchMouseClicked(evt);
            }
        });

        tableExpense.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane8.setViewportView(tableExpense);

        jLabel20.setForeground(new java.awt.Color(153, 153, 153));
        jLabel20.setText("Start (YYYY-MM-DD)");

        jLabel22.setForeground(new java.awt.Color(153, 153, 153));
        jLabel22.setText("End (YYYY-MM-DD)");

        jPanel19.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Add Other Expenses", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(102, 102, 102))); // NOI18N

        jLabel23.setFont(new java.awt.Font("Bell MT", 0, 18)); // NOI18N
        jLabel23.setText("Expense Title");

        jLabel26.setFont(new java.awt.Font("Bell MT", 0, 18)); // NOI18N
        jLabel26.setText("Expense Details");

        jLabel27.setFont(new java.awt.Font("Bell MT", 0, 18)); // NOI18N
        jLabel27.setText("Expense Cost");

        jLabel28.setFont(new java.awt.Font("Bell MT", 0, 18)); // NOI18N
        jLabel28.setText("Expense User");

        txtExTitle.setFont(new java.awt.Font("Bell MT", 0, 18)); // NOI18N

        txtExDetails.setColumns(20);
        txtExDetails.setRows(5);
        jScrollPane7.setViewportView(txtExDetails);

        txtExUser.setFont(new java.awt.Font("Bell MT", 0, 18)); // NOI18N

        txtExCost.setFont(new java.awt.Font("Bell MT", 0, 18)); // NOI18N

        btnAddExpense.setBackground(new java.awt.Color(155, 93, 229));
        btnAddExpense.setFont(new java.awt.Font("Bell MT", 1, 18)); // NOI18N
        btnAddExpense.setForeground(new java.awt.Color(255, 255, 255));
        btnAddExpense.setText("Add Expense");
        btnAddExpense.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAddExpenseMouseClicked(evt);
            }
        });

        labExError.setFont(new java.awt.Font("Bell MT", 0, 14)); // NOI18N
        labExError.setForeground(new java.awt.Color(255, 0, 0));

        labExErrorDetails.setFont(new java.awt.Font("Bell MT", 0, 14)); // NOI18N
        labExErrorDetails.setForeground(new java.awt.Color(255, 0, 0));

        labExErrorCost.setFont(new java.awt.Font("Bell MT", 0, 14)); // NOI18N
        labExErrorCost.setForeground(new java.awt.Color(255, 0, 0));

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel19Layout.createSequentialGroup()
                            .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel19Layout.createSequentialGroup()
                            .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(labExError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtExTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)))
                        .addGroup(jPanel19Layout.createSequentialGroup()
                            .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(labExErrorDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtExCost, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(labExErrorCost, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnAddExpense, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtExUser, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE))))
                .addContainerGap(42, Short.MAX_VALUE))
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(txtExTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labExError, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel26, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel19Layout.createSequentialGroup()
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labExErrorDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtExCost, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labExErrorCost, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtExUser, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(btnAddExpense, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        txtExStart.setDateFormatString("yyyy-MM-dd");
        txtExStart.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        txtExEnd.setDateFormatString("yyyy-MM-dd");
        txtExEnd.setFocusTraversalPolicyProvider(true);
        txtExEnd.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        javax.swing.GroupLayout PanelExpensesLayout = new javax.swing.GroupLayout(PanelExpenses);
        PanelExpenses.setLayout(PanelExpensesLayout);
        PanelExpensesLayout.setHorizontalGroup(
            PanelExpensesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelExpensesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelExpensesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelExpensesLayout.createSequentialGroup()
                        .addGroup(PanelExpensesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PanelExpensesLayout.createSequentialGroup()
                                .addComponent(txtExStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtExEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(txtExSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(45, 45, 45))
                            .addGroup(PanelExpensesLayout.createSequentialGroup()
                                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 542, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 21, Short.MAX_VALUE)))
                        .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(PanelExpensesLayout.createSequentialGroup()
                        .addGroup(PanelExpensesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 946, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(PanelExpensesLayout.createSequentialGroup()
                                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(41, 41, 41))
        );
        PanelExpensesLayout.setVerticalGroup(
            PanelExpensesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelExpensesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(47, 47, 47)
                .addGroup(PanelExpensesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PanelExpensesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(PanelExpensesLayout.createSequentialGroup()
                        .addGroup(PanelExpensesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtExStart, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtExSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtExEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 374, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(93, 93, 93))
        );

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PanelExpenses, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PanelExpenses, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        tbPanel.addTab("tab4", jPanel18);

        panRecords.setBackground(new java.awt.Color(255, 255, 255));

        jLabel31.setFont(new java.awt.Font("Bell MT", 1, 36)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(131, 56, 236));
        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel31.setText(" Get Yours sales Records");
        jLabel31.setBorder(javax.swing.BorderFactory.createMatteBorder(4, 100, 4, 4, new java.awt.Color(131, 56, 236)));

        txtSearchCustomar.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        txtSearchCustomar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchCustomarKeyReleased(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(131, 56, 236));
        jButton1.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Search Customar");

        txtStartDateRecod.setDateFormatString("yyyy-MM-dd");
        txtStartDateRecod.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N

        txtEndDateRecod.setDateFormatString("yyyy-MM-dd");
        txtEndDateRecod.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N

        jLabel32.setForeground(new java.awt.Color(204, 204, 204));
        jLabel32.setText("Start (YYYY-MM-DD)");

        jLabel33.setForeground(new java.awt.Color(204, 204, 204));
        jLabel33.setText("End (YYYY-MM-DD)");

        tblRecord.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblRecord.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblRecordMouseClicked(evt);
            }
        });
        jScrollPane9.setViewportView(tblRecord);

        cbInvoicePermission.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cbInvoicePermission.setText("Get result from specific invoice");

        javax.swing.GroupLayout panRecordsLayout = new javax.swing.GroupLayout(panRecords);
        panRecords.setLayout(panRecordsLayout);
        panRecordsLayout.setHorizontalGroup(
            panRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panRecordsLayout.createSequentialGroup()
                .addGroup(panRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panRecordsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 983, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panRecordsLayout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addGroup(panRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panRecordsLayout.createSequentialGroup()
                                .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panRecordsLayout.createSequentialGroup()
                                .addGroup(panRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(panRecordsLayout.createSequentialGroup()
                                        .addComponent(txtStartDateRecod, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtEndDateRecod, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(txtSearchCustomar)
                                    .addComponent(cbInvoicePermission, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 927, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        panRecordsLayout.setVerticalGroup(
            panRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panRecordsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(panRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearchCustomar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbInvoicePermission)
                .addGap(20, 20, 20)
                .addGroup(panRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtStartDateRecod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtEndDateRecod, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel32)
                    .addComponent(jLabel33))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(78, 78, 78))
        );

        tbPanel.addTab("tab5", panRecords);

        jPanel1.add(tbPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 40, 1060, 690));

        jPanel9.setBackground(new java.awt.Color(73, 64, 140));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Date: ");

        jDate.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jDate.setForeground(new java.awt.Color(255, 255, 255));

        jTime.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTime.setForeground(new java.awt.Color(255, 255, 255));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Time");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap(545, Short.MAX_VALUE)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDate, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTime, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(113, 113, 113))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTime, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jDate, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 0, 1050, 40));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1200, 710));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void tbnDashboardMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnDashboardMouseEntered
        tbnDashboard.setBackground(Color.WHITE);
        tbnDashboard.setForeground(Color.BLACK);
        String hImage = "btn/d_color.png";
        imageSize(tbnDashboard, hImage);

    }//GEN-LAST:event_tbnDashboardMouseEntered

    private void tbnDashboardMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnDashboardMouseExited
        tbnDashboard.setBackground(new Color(124, 115, 191));
        tbnDashboard.setForeground(Color.WHITE);
        String exImg = "btn/d_white.png";
        imageSize(tbnDashboard, exImg);
        
    }//GEN-LAST:event_tbnDashboardMouseExited

    private void tbnSalesMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnSalesMouseEntered
        tbnSales.setBackground(Color.WHITE);
        tbnSales.setForeground(Color.BLACK);
        String sImg = "btn/s_color.png";
        imageSize(tbnSales, sImg);
        
    }//GEN-LAST:event_tbnSalesMouseEntered

    private void tbnSalesMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnSalesMouseExited
        tbnSales.setBackground(new Color(124, 115, 191));
        tbnSales.setForeground(Color.WHITE);
        String sImg = "btn/s_white.png";
        imageSize(tbnSales, sImg);
    }//GEN-LAST:event_tbnSalesMouseExited

    private void tbnExpenseMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnExpenseMouseEntered
        tbnExpense.setBackground(Color.WHITE);
        tbnExpense.setForeground(Color.BLACK);
        
        String exImg = "btn/ex_color.png";
        imageSize(tbnExpense, exImg);
    }//GEN-LAST:event_tbnExpenseMouseEntered

    private void tbnExpenseMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnExpenseMouseExited
        tbnExpense.setBackground(new Color(124, 115, 191));
        tbnExpense.setForeground(Color.WHITE);
        
         String exOImg = "btn/ex_white.png";
        imageSize(tbnExpense, exOImg);
    }//GEN-LAST:event_tbnExpenseMouseExited

    private void sListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sListMouseClicked
        String id = sList.getSelectedValue();
        String spID[] = id.split("\\(");
        String[] fid = spID[1].split("\\)");
        fID = Integer.parseInt(fid[0]);

        showTable();
        totalCal();
        
        
    }//GEN-LAST:event_sListMouseClicked

    private void tbnDashboardMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnDashboardMouseClicked
        // Generate Current Date base on Mysql formate
        java.util.Date javaDate = new java.util.Date();
         java.sql.Date mySQLDate = new java.sql.Date(javaDate.getTime());
         
         //Generate Todays sale sum
        String todaySalesQ = "SELECT sum(sales_s_price) FROM polinpharmacyerp.ppm_sales_inventory WHERE sales_date = '"+mySQLDate+"';";
        double tSales =  Double.parseDouble(kits.todaySales(todaySalesQ));
        
        // Set expenses in Todays sales, Today Expenses, Today Profit and Capital Left
        //  Today Sales 
        labTodaySales.setText("  "+d.format(tSales)+"k  "+"(TK)");
        labSalesTitle.setText("   Total Sales");
        labSalesBanar.setText("Today Sales");
        
        // Generate todays Expenses
        String othExpenTable = "SELECT sum(oth_cost) FROM ppm_other_expenses WHERE oth_date = '"+mySQLDate+"';";
        String expensQ = "SELECT sum(sale_p_price*sales_qty) FROM polinpharmacyerp.ppm_sales_inventory WHERE sales_date = '"+mySQLDate+"';";
        double totalExpens = kits.OtherExpenseTotal(othExpenTable) + kits.todayExpense(expensQ);
        
        // Today Expenses 
        labReExpensTitle.setText("   Total Expenses");
        labTodayExpense.setText("  "+d.format(totalExpens)+"k  "+"(TK)");
        labReExpenBanar.setText("Expenses Today");
        
        // Today Profit 
        double Tprofit = tSales - totalExpens;
        labReProfitTitle.setText("   Profits");
        labProfit.setText("  "+d.format(Tprofit)+"k  "+"(TK)");
        labReProfitBanar.setText("Today Profits");
        
        //Total Capital Left 
        double capital = kits.CapitalLeft();
        labCapitalLeft.setText("  "+d.format(capital)+"k  "+"(TK)");
        
//        Low Qty intems Tables 
          kits.qtyTable(dom, tblLowQtyItem);
        
//        Set pan
        tbPanel.setSelectedIndex(1);
    }//GEN-LAST:event_tbnDashboardMouseClicked

    private void tbnSalesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnSalesMouseClicked
        tbPanel.setSelectedIndex(0);
    }//GEN-LAST:event_tbnSalesMouseClicked

    String othExQuary =null;
    private void tbnExpenseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnExpenseMouseClicked
        if (userRole.equalsIgnoreCase("admin")) {
            
        String[] exTableCol = {"Title", "Details", "Cost", "User","Date"};
        dom = new DefaultTableModel();
        dom.setColumnIdentifiers(exTableCol);
        tableExpense.setModel(dom);
        tableExpense.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableExpense.getColumnModel().getColumn(1).setPreferredWidth(100);
        tableExpense.getColumnModel().getColumn(2).setPreferredWidth(10);
        tableExpense.getColumnModel().getColumn(3).setPreferredWidth(10);
        
       
        othExQuary = "SELECT * FROM polinpharmacyerp.ppm_other_expenses ORDER BY oth_date DESC, oth_id DESC LIMIT 50;";
        
        kits.othExTable(dom, othExQuary);
        
        
        tbPanel.setSelectedIndex(3);
        
        }else{
            JOptionPane.showMessageDialog(this, "You are not allow to access this");
        
        }
        
    }//GEN-LAST:event_tbnExpenseMouseClicked

    private void conDosFormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conDosFormActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_conDosFormActionPerformed

    private void txtGenericKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtGenericKeyReleased
        String gKeyword = txtGeneric.getText();
        String genericQ = "SELECT DISTINCT m.group  FROM ppm_medicine_list m WHERE m.group LIKE ? LIMIT 5;";
        String groupNameCol = "group";
        kits.setPopupMenues(dlm, gKeyword, genericQ, groupNameCol);
        popupGene.show(txtGeneric, 0, txtGeneric.getHeight());
    }//GEN-LAST:event_txtGenericKeyReleased

    private void genericListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_genericListMouseClicked

        txtGeneric.setText(genericList.getSelectedValue());
        genericList.setVisible(false);
        pGene.setVisible(false);
        popupGene.setVisible(false);
    }//GEN-LAST:event_genericListMouseClicked

    private void txtCompanyNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCompanyNameKeyReleased
        String companyListQ = "SELECT c.company_name  FROM ppm_company_name c WHERE c.company_name LIKE ? LIMIT 5;";
        String companyNameKey = txtCompanyName.getText();
        String companyNameCol = "company_name";
        kits.setPopupMenues(dlm, companyNameKey, companyListQ, companyNameCol);
        popCompany.show(txtCompanyName, 0, txtCompanyName.getHeight());

    }//GEN-LAST:event_txtCompanyNameKeyReleased

    private void listComapnyMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listComapnyMouseClicked
        txtCompanyName.setText(listComapny.getSelectedValue());
        listComapny.setVisible(false);
        pCompany.setVisible(false);
        popCompany.setVisible(false);
    }//GEN-LAST:event_listComapnyMouseClicked

    private void btnUpdateInsertMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnUpdateInsertMouseClicked
    String updateQ = "UPDATE ppm_medicine_list SET medicine_name=?,company_name_ml=(SELECT company_id FROM ppm_company_name WHERE company_name =?),physical_type=?,mrp_price=?,w_price=?, ppm_medicine_list.group=?,size_mg=?,qty=(qty+?) WHERE medicine_list_id = ?";
    String insertQ = "INSERT INTO ppm_medicine_list (medicine_name,company_name_ml,physical_type,mrp_price,w_price,ppm_medicine_list.group,size_mg,qty) VALUES (?,(SELECT company_id FROM ppm_company_name WHERE company_name =?),?,?,?,?,?,?);";
    String iId = txtID.getText().trim();
    String iName = txtItemName.getText().trim();
    String iCompany = txtCompanyName.getText().trim();
    String iType = (String) conDosForm.getSelectedItem();
    double iMRP = Double.parseDouble(txtMRPPrice.getText().trim());
    double iPprice = Double.parseDouble(txtUnitePrice.getText().trim());
    String iGroup = txtGeneric.getText().trim();
    String iSize = txtStrength.getText().trim();
    int iQty = Integer.parseInt(txtQtyAdd.getText().trim());
    
        if (!(iName.isEmpty() && iCompany.isEmpty() && iType.isEmpty() && iMRP >=1 && iPprice >=1 && iGroup.isEmpty() && iSize.isEmpty() && iQty >=1)) {
            if (iId.isEmpty()) {
               kits.InsertOrUpdateItem(insertQ, iName, iCompany, iType, iMRP, iPprice, iGroup, iSize, iQty);
               kits.expenseInsert(iPprice, iQty);
               JOptionPane.showMessageDialog(this, iName+" is Add your Store");
                SalesFieldClean();
            }else{
                kits.InsertOrUpdateItem(updateQ, iName, iCompany, iType, iMRP, iPprice, iGroup, iSize, iQty, Integer.parseInt(iId));
                kits.expenseInsert(iPprice, iQty, Integer.parseInt(iId));
                JOptionPane.showMessageDialog(this, iName+" is Updated"); 
                 SalesFieldClean();
            }
            
        }else{
        JOptionPane.showMessageDialog(this, "Can't Leave any Field Blank");
        }
        
    }//GEN-LAST:event_btnUpdateInsertMouseClicked

    private void txtItemNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtItemNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtItemNameActionPerformed

    private void txtISearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtISearchKeyReleased
       String keyTest = txtISearch.getText();
       String iSearchQ = "SELECT * FROM ppm_medicine_list m WHERE m.medicine_name LIKE ? LIMIT 5;";
      
      kits.setPopupMenues(dlm, keyTest, iSearchQ);
      popIsearch.show(txtISearch, 0, txtISearch.getHeight());
    }//GEN-LAST:event_txtISearchKeyReleased

    private void listISearchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listISearchMouseClicked
        String SelectedValue = listISearch.getSelectedValue();
        String[] getID1 = SelectedValue.split(" ");
        int getId = Integer.parseInt(getID1[0]);
        
        String selectAllQ = "SELECT *, c.company_name FROM ppm_medicine_list m INNER JOIN ppm_company_name c WHERE m.medicine_list_id = ? AND c.company_id = m.company_name_ml;";
        try {
            pst = config.getCon().prepareStatement(selectAllQ);
            pst.setInt(1, getId);
            rs = pst.executeQuery();
            
            while (rs.next()) {    
            txtItemName.setText(rs.getString("medicine_name"));
            txtGeneric.setText(rs.getString("group"));
            txtCompanyName.setText(rs.getString("company_name"));
            txtUnitePrice.setText(rs.getString("w_price"));
            txtQtyAdd.setText(rs.getString("qty"));
            txtStrength.setText(rs.getString("size_mg"));
            txtMRPPrice.setText(rs.getString("mrp_price"));
            txtID.setText(rs.getString("medicine_list_id"));
            String comboValue = rs.getString("physical_type");
                for (int i = 0; i<dosegesForms.length; i++) {
                    if (comboValue.equals(dosegesForms[i])) {
                        conDosForm.setSelectedItem(dosegesForms[i]);
                    }else{
                    conDosForm.setSelectedIndex(1);
                    }
                }
            
            }
            pst.close();
            config.getCon().close();
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
//        listISearch.setVisible(false);
//        pIseach.setVisible(false);
//        popIsearch.setVisible(false);
        
    }//GEN-LAST:event_listISearchMouseClicked

    private void btnIdeleteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnIdeleteMouseClicked
       String deleteQ = "DELETE FROM ppm_medicine_list WHERE medicine_list_id = ?;";
       int deleteId = Integer.parseInt(txtID.getText());
       int result = JOptionPane.showConfirmDialog(this,"Do you want to delete this Item?", "Swing Tester",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            kits.deleteItem(deleteQ, deleteId);
            JOptionPane.showMessageDialog(this, " "+txtItemName.getText()+" is Permanently Deleted");
            SalesFieldClean();
        }else if(result == JOptionPane.NO_OPTION){
            JOptionPane.showMessageDialog(this, " "+txtItemName.getText()+" is NOT Deleted");
        }
    }//GEN-LAST:event_btnIdeleteMouseClicked

    private void btnResetMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnResetMouseClicked
        SalesFieldClean();
    }//GEN-LAST:event_btnResetMouseClicked

    private void tbnDateSearchMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnDateSearchMouseEntered
        tbnDateSearch.setBackground(Color.white);
        tbnDateSearch.setForeground(Color.BLACK);
        tbnDateSearch.setBorderPainted(true);
        tbnDateSearch.setBorder(BorderFactory.createMatteBorder(3, 1, 3, 1, Color.decode("#49408C")));
    }//GEN-LAST:event_tbnDateSearchMouseEntered

    private void tbnDateSearchMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnDateSearchMouseExited
       tbnDateSearch.setBorderPainted(false);
       tbnDateSearch.setBackground(Color.decode("#49408C"));
       tbnDateSearch.setForeground(Color.white);
    }//GEN-LAST:event_tbnDateSearchMouseExited

    private void tbnDateSearchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnDateSearchMouseClicked
        String startDate = ((JTextField)txtStartDate.getDateEditor().getUiComponent()).getText();
        String endDate = ((JTextField)txtEndDate.getDateEditor().getUiComponent()).getText();
        
//        Sales Record Between 2 date 
//        dateValidator(startDate)
         String rSalesQ = "SELECT sum(sales_s_price) FROM polinpharmacyerp.ppm_sales_inventory WHERE sales_date BETWEEN '"+dateValidator(startDate)+"' AND '"+dateValidator(endDate)+"';";
        double recordSales =  Double.parseDouble(kits.todaySales(rSalesQ));
        labTodaySales.setText("  "+d.format(recordSales)+"k  "+"(TK)");
        labSalesTitle.setText(startDate + " To " + endDate);
        labSalesBanar.setText("Sales");
        
//        Expences Between two dates 
        String rExpensQ = "SELECT sum(ex_p_price*ex_qty) FROM polinpharmacyerp.ppm_expenses_table WHERE ex_date BETWEEN '"+dateValidator(startDate)+"' AND '"+dateValidator(endDate)+"';";
        String rExpenTable = "SELECT sum(oth_cost) FROM ppm_other_expenses WHERE oth_date BETWEEN '"+dateValidator(startDate)+"' AND '"+dateValidator(endDate)+"';";
        
        double totalExpensByDate = kits.OtherExpenseTotal(rExpenTable) + kits.todayExpense(rExpensQ);
        
        // Total Profit Expenses 
        labReExpensTitle.setText(startDate + " To " + endDate);
        labTodayExpense.setText("  "+d.format(totalExpensByDate)+"k  "+"(TK)");
        labReExpenBanar.setText("Expenses");
        
        
         // Total Profit 
         
         String rPursesQ = "SELECT sum(sale_p_price*sales_qty) FROM polinpharmacyerp.ppm_sales_inventory WHERE sales_date BETWEEN '"+dateValidator(startDate)+"' AND '"+dateValidator(endDate)+"';";
        double recordPurses =  Double.parseDouble(kits.todaySales(rPursesQ));
        System.out.println(recordSales + " And " + recordPurses);
        double TprofitByDate = recordSales - recordPurses;
        labReProfitTitle.setText(startDate + " To " + endDate);
        labProfit.setText("  "+d.format(TprofitByDate)+"k  "+"(TK)");
        labReProfitBanar.setText("Profit");
    }//GEN-LAST:event_tbnDateSearchMouseClicked

    private void tbnRecordsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnRecordsMouseClicked
        tbPanel.setSelectedIndex(4);
         String column[] = {"Id", "Name", "Phone", "Item", "Qty", "Total Price","Date"};
        dom = new DefaultTableModel();
        dom.setColumnIdentifiers(column);
        tblRecord.setModel(dom);
    }//GEN-LAST:event_tbnRecordsMouseClicked

    private void tbnRecordsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnRecordsMouseEntered
        tbnRecords.setBackground(Color.WHITE);
        tbnRecords.setForeground(Color.BLACK);
        
         String reImg = "btn/re_color.png";
        imageSize(tbnRecords, reImg);
        
    }//GEN-LAST:event_tbnRecordsMouseEntered

    private void tbnRecordsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnRecordsMouseExited
        tbnRecords.setBackground(new Color(124, 115, 191));
        tbnRecords.setForeground(Color.WHITE);
        
        String reImg = "btn/re_white.png";
        imageSize(tbnRecords, reImg);
    }//GEN-LAST:event_tbnRecordsMouseExited

    private void tbnAddItemMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnAddItemMouseClicked
        tbPanel.setSelectedIndex(2);
    }//GEN-LAST:event_tbnAddItemMouseClicked

    private void tbnAddItemMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnAddItemMouseEntered
         tbnAddItem.setBackground(Color.WHITE);
        tbnAddItem.setForeground(Color.BLACK);
        
        String addImg = "btn/add_color.png";
        imageSize(tbnAddItem, addImg);
    }//GEN-LAST:event_tbnAddItemMouseEntered

    private void tbnAddItemMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnAddItemMouseExited
         tbnAddItem.setBackground(new Color(124, 115, 191));
        tbnAddItem.setForeground(Color.WHITE);
        
        String addaImg = "btn/add_white.png";
        imageSize(tbnAddItem, addaImg);
    }//GEN-LAST:event_tbnAddItemMouseExited

    private void txtExSearchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtExSearchMouseClicked
    String othStart =((JTextField)txtExStart.getDateEditor().getUiComponent()).getText();
    String othEnd =((JTextField)txtExEnd.getDateEditor().getUiComponent()).getText();
    

    String othExQuaryByDate = "SELECT * FROM polinpharmacyerp.ppm_other_expenses WHERE oth_date BETWEEN '"+dateValidator(othStart)+"' AND '"+dateValidator(othEnd)+"' ORDER BY oth_date DESC;";
     int totalRow = dom.getRowCount();

        for (int i = totalRow - 1; i >= 0; i--) {
            dom.removeRow(i);
        }
        kits.othExTable(dom, othExQuaryByDate);
    
        
    }//GEN-LAST:event_txtExSearchMouseClicked

    private void btnAddExpenseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAddExpenseMouseClicked
          java.util.Date javaDate = new java.util.Date();
         java.sql.Date mySQLDate = new java.sql.Date(javaDate.getTime());
         
        String exTitleH = txtExTitle.getText();
        String exDetailsH = txtExDetails.getText();
        String exCostH = txtExCost.getText();
        String txtExUserH = txtExUser.getText();
//        System.out.println(exTitleH.length());
        if (exTitleH.isEmpty() || exTitleH.length() > 50) {
           labExError.setText("Can't add more then 50 character");
//           txtExTitle.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
//           JOptionPane.showMessageDialog(this, "Can't add more then 100 character");
        }else if (exDetailsH.isEmpty() || exDetailsH.length() > 250) {
            labExErrorDetails.setText("Can't add more then 50 character");
        }else if(exCostH.isEmpty() || exCostH.length()>10){
            labExErrorCost.setText("Can't leave Empty");
        }else{
        Double exCostHD = Double.parseDouble(exCostH);
        String othExpAddQ = "INSERT INTO polinpharmacyerp.ppm_other_expenses (oth_title,oth_cost,oth_comment,oth_date,oth_user) VALUES (?,?,?,?,?);";
        
              try {
                  pst = config.getCon().prepareStatement(othExpAddQ);
                  pst.setString(1, exTitleH);
                  pst.setDouble(2, exCostHD);
                  pst.setString(3, exDetailsH);
                  pst.setDate(4, mySQLDate);
                  pst.setString(5, txtExUserH);
                  
                  pst.executeUpdate();
                  
                  config.getCon().close();
                  pst.close();
                 
                  txtExTitle.setText("");
                  txtExDetails.setText("");
                  txtExCost.setText("");
                  txtExUser.setText("");
                 
                  
              } catch (ClassNotFoundException ex) {
                  Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
              } catch (SQLException ex) {
                  Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
              }
         int totalRow = dom.getRowCount();

        for (int i = totalRow - 1; i >= 0; i--) {
            dom.removeRow(i);
        }
        kits.othExTable(dom, othExQuary);
        }
        
    }//GEN-LAST:event_btnAddExpenseMouseClicked

    private void btnRemoveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveAllActionPerformed
        int totalRow = dom.getRowCount();

        for (int i = totalRow - 1; i >= 0; i--) {
            dom.removeRow(i);
        }
        totalCal();
        txtMRP.setText("");
        txtQty.setText("");
        txtDis.setText("");
    }//GEN-LAST:event_btnRemoveAllActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        //        int rowIndx = tblCart.getSelectedRow();

        if (tblCart.getSelectedRowCount() == 1) {
            dom.removeRow(tblCart.getSelectedRow());
            totalCal();
            txtMRP.setText("");
            txtQty.setText("");
            txtDis.setText("");
        } else if (tblCart.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Select at last 1 row");
        }
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void tbnDiscountUpdateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbnDiscountUpdateMouseClicked
        double newPrice = Double.parseDouble(txtMRP.getText());
        int newQtry = Integer.parseInt(txtQty.getText());
        //        double mPrice = Double.parseDouble(sMRP);
        //        double subTotal = mPrice * newQtry;
        int selectedRow = tblCart.getSelectedRow();
        if (newPrice != 0 && newQtry != 0) {
            if (kits.getQtry(fID) == 0 || kits.getQtry(fID) < newQtry) {
                JOptionPane.showMessageDialog(this, "Not Enough Stock");
            } else {
                dom.setValueAt(newPrice, selectedRow, 7);
                dom.setValueAt(newQtry, selectedRow, 6);
                //            dom.setValueAt(subTotal, selectedRow, 5);
                totalCal();
                txtMRP.setText("");
                txtQty.setText("");
                txtDis.setText("");
            }
        }
        txtMRP.setText("");
        txtQty.setText("");
        txtDis.setText("");
    }//GEN-LAST:event_tbnDiscountUpdateMouseClicked

    private void txtDisKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDisKeyReleased
        double newP = Double.parseDouble(txtMRP.getText());

        int disRate = Integer.parseInt(txtDis.getText());
        if (disRate >= 10 || disRate == 0) {
            JOptionPane.showMessageDialog(this, "Add Discount 1-9");
        } else {

            double disPrce = (newP * disRate) / 100;
            txtMRP.setText(Double.toString(Math.round(newP - disPrce)));
        }
    }//GEN-LAST:event_txtDisKeyReleased

    private void txtMRPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMRPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMRPActionPerformed

    private void txtQtyKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtQtyKeyReleased
        String getQtry = txtQty.getText();
        int rowInxQT = tblCart.getSelectedRow();
        double getMRP = (double) dom.getValueAt(rowInxQT, 5);

        if (!getQtry.isEmpty()) {
            int numQty = Integer.parseInt(getQtry);
            if (!sMRP.isEmpty()) {
                //               double numMRP = Double.parseDouble(sMRP);
                double newPrice = numQty * getMRP;
                txtMRP.setText(Double.toString(Math.round(newPrice)));
            }
        }
    }//GEN-LAST:event_txtQtyKeyReleased

    private void btnGetPayemntMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGetPayemntMouseClicked

        int result = JOptionPane.showConfirmDialog(this,"Did you Recived: " + txtTotal.getText() + " TK", "Swing Tester",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
        if(result == JOptionPane.YES_OPTION){

            int nRow = dom.getRowCount();
            int nCol[] = {0, 5, 6, 7, 8};
            String customarN = txtCName.getText();
            String customarPhone = txtCPhone.getText();
            Object[][] tableData = new Object[nRow][nCol.length];

            for (int i = 0; i < nRow; i++) {
                for (int j = 0; j < nCol.length; j++) {
                    tableData[i][j] = dom.getValueAt(i, nCol[j]);
                }
            }
            try {
                kits.salesUpdate(tableData, customarN,customarPhone);
                JOptionPane.showMessageDialog(this, "Sales Complete");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Sales Faild");
            }

            //        Cleaning Table
            int totalRow = dom.getRowCount();
            for (int i = totalRow - 1; i >= 0; i--) {
                dom.removeRow(i);
            }
            txtMRP.setText("");
            txtQty.setText("");
            txtDis.setText("");
            txtSubTotal.setText("");
            txtDisc.setText("");
            txtTotal.setText("");
            txtCPhone.setText("");
            txtSearch.setText("");
            lStock.setText("");

        }else if (result == JOptionPane.NO_OPTION){
            JOptionPane.showMessageDialog(this, "Check your Mistake");
        }
    }//GEN-LAST:event_btnGetPayemntMouseClicked

    private void tblCartMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCartMouseClicked
        int rowIndx = tblCart.getSelectedRow();
        sQtry = dom.getValueAt(rowIndx, 6).toString().trim();
        sMRP = dom.getValueAt(rowIndx, 5).toString().trim();

        lStock.setText(Integer.toString(kits.getQtry(fID)));
        txtQty.setText(sQtry);
        txtMRP.setText(sMRP);
    }//GEN-LAST:event_tblCartMouseClicked

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
        String searchQ = "SELECT m.medicine_list_id, size_mg, m.medicine_name, c.company_name, m.physical_type FROM ppm_medicine_list m INNER JOIN ppm_company_name c ON m.company_name_ml = c.company_id WHERE m.medicine_name LIKE ? LIMIT 5;";
        String keyword = txtSearch.getText();
        dlm.removeAllElements();
        if (!keyword.isEmpty()) {

            try {
                pst = config.getCon().prepareStatement(searchQ);

                pst.setString(1, keyword + "%");
                rs = pst.executeQuery();
             
                while (rs.next()) {
                    dlm.addElement("(" + rs.getString("medicine_list_id") + ")" + "  " + rs.getString("medicine_name") + " " + rs.getString("size_mg") + "      " + rs.getString("company_name") + "      " + rs.getString("physical_type"));
                }

                pst.close();
                config.getCon().close();

            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
            }

            sMenu.show(txtSearch, 0, txtSearch.getHeight());

        }
    }//GEN-LAST:event_txtSearchKeyReleased

    private void txtSearchCustomarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchCustomarKeyReleased
        String searchText = txtSearchCustomar.getText();
        String searchCName = "SELECT DISTINCT (sales_customar_id), sales_customer_n, sales_custonar_phone FROM polinpharmacyerp.ppm_sales_inventory WHERE sales_customer_n LIKE '"+searchText+"%' OR sales_custonar_phone LIKE '"+searchText+"%' OR sales_customar_id LIKE '"+searchText+"%';";
        dlm.removeAllElements();
        
        try {
            pst = config.getCon().prepareStatement(searchCName);
//        
            rs = pst.executeQuery();
            while (rs.next()) {                
                    dlm.addElement(rs.getString("sales_customar_id")+" | "+rs.getString("sales_customer_n")+" | "+rs.getString("sales_custonar_phone"));
                }
           
            pst.close();
            config.getCon().close();
            
       
              } catch (ClassNotFoundException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
                }
        
        popCustomarSerach.show(txtSearchCustomar, 0, txtSearchCustomar.getHeight());
         
    }//GEN-LAST:event_txtSearchCustomarKeyReleased

    private void tblRecordMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblRecordMouseClicked
//        String cid = customarIDn;
        
        
    }//GEN-LAST:event_tblRecordMouseClicked

    private void listCustomarSMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listCustomarSMouseClicked
       String getSelectedIDFromPopUp = listCustomarS.getSelectedValue();
       String customarID[] = getSelectedIDFromPopUp.split(" ");
       customarIDn = customarID[0];
       String customarPhone = null;
       String customarName = null;
        try {
            customarPhone = customarID[4];
            customarName = customarID[2];
        } catch (Exception e) {
        }
        
       String sDate = ((JTextField)txtStartDateRecod.getDateEditor().getUiComponent()).getText();
        String eDate = ((JTextField)txtEndDateRecod.getDateEditor().getUiComponent()).getText();
              
        if (cbInvoicePermission.isSelected()) {
           String quaryCustomarID = "SELECT s.sales_customar_id, s.sales_customer_n, s.sales_custonar_phone, s.sales_qty, s.sales_s_price, m.medicine_name, m.size_mg, s.sales_date FROM ppm_sales_inventory s INNER JOIN ppm_medicine_list m WHERE s.sales_customar_id = '"+customarIDn+"' and m.medicine_list_id = s.sales_product_id;";
           kits.CustomarTableUpdate(quaryCustomarID, dom);
        }else if(!(customarPhone== null || customarName== null) && sDate.isEmpty() && eDate.isEmpty()){
            String quaryByCustomarName = "SELECT DISTINCT s.sales_customar_id, s.sales_customer_n, s.sales_custonar_phone, s.sales_qty, s.sales_s_price, m.medicine_name, m.size_mg, s.sales_date FROM ppm_sales_inventory s INNER JOIN ppm_medicine_list m ON  m.medicine_list_id = s.sales_product_id WHERE s.sales_customer_n Like '"+customarName+"' OR s.sales_custonar_phone Like '"+customarPhone+"';";
            kits.CustomarTableUpdate(quaryByCustomarName, dom);
        
        }else if(!(sDate.isEmpty() && eDate.isEmpty())){
        String quaryByCustomarNameDate = "SELECT DISTINCT s.sales_customar_id, s.sales_customer_n, s.sales_custonar_phone, s.sales_qty, s.sales_s_price, m.medicine_name, m.size_mg, s.sales_date FROM ppm_sales_inventory s INNER JOIN ppm_medicine_list m ON  m.medicine_list_id = s.sales_product_id WHERE (s.sales_customer_n Like '"+customarName+"' OR s.sales_custonar_phone Like '"+customarPhone+"') AND  s.sales_date between '"+dateValidator(sDate)+"' and '"+dateValidator(eDate)+"';";
        kits.CustomarTableUpdate(quaryByCustomarNameDate, dom);
            System.out.println("date run");
        }else{
         String quaryCustomarID = "SELECT s.sales_customar_id, s.sales_customer_n, s.sales_custonar_phone, s.sales_qty, s.sales_s_price, m.medicine_name, m.size_mg, s.sales_date FROM ppm_sales_inventory s INNER JOIN ppm_medicine_list m WHERE s.sales_customar_id = '"+customarIDn+"' and m.medicine_list_id = s.sales_product_id;";
           kits.CustomarTableUpdate(quaryCustomarID, dom);
        
        }
        
    }//GEN-LAST:event_listCustomarSMouseClicked

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        userName = "";
        JOptionPane.showMessageDialog(this, "Logout Success");
        dispose();
        new Login().setVisible(true);
    }//GEN-LAST:event_jButton2MouseClicked

    
    
    
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Dashboard().setVisible(true);
                
                
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelExpenses;
    private javax.swing.JButton btnAddExpense;
    private javax.swing.JButton btnGetPayemnt;
    private javax.swing.JButton btnIdelete;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnRemoveAll;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdateInsert;
    private javax.swing.JCheckBox cbInvoicePermission;
    private javax.swing.JComboBox<String> conDosForm;
    private javax.swing.JList<String> genericList;
    private javax.swing.JLabel imgTxt;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jDate;
    private javax.swing.JLabel jLLogo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private static javax.swing.JLabel jTime;
    private javax.swing.JLabel lStock;
    private javax.swing.JLabel labCapitalLeft;
    private javax.swing.JLabel labExError;
    private javax.swing.JLabel labExErrorCost;
    private javax.swing.JLabel labExErrorDetails;
    private javax.swing.JLabel labProfit;
    private javax.swing.JLabel labReExpenBanar;
    private javax.swing.JLabel labReExpensTitle;
    private javax.swing.JLabel labReProfitBanar;
    private javax.swing.JLabel labReProfitTitle;
    private javax.swing.JLabel labSalesBanar;
    private javax.swing.JLabel labSalesTitle;
    private javax.swing.JLabel labTodayExpense;
    private javax.swing.JLabel labTodaySales;
    private javax.swing.JList<String> listComapny;
    private javax.swing.JList<String> listCustomarS;
    private javax.swing.JList<String> listISearch;
    private javax.swing.JLabel mrpTitle1;
    private javax.swing.JLabel mrpTitle2;
    private javax.swing.JLabel mrpTitle3;
    private javax.swing.JLabel mrpTitle4;
    private javax.swing.JPanel pCompany;
    private javax.swing.JPanel pGene;
    private javax.swing.JPanel pIseach;
    private javax.swing.JPanel panAddItem;
    private javax.swing.JPanel panCustomarS;
    private javax.swing.JPanel panDeshboard;
    private javax.swing.JPanel panRecords;
    private javax.swing.JPanel panSales;
    private javax.swing.JPopupMenu popCompany;
    private javax.swing.JPopupMenu popCustomarSerach;
    private javax.swing.JPopupMenu popIsearch;
    private javax.swing.JPopupMenu popupGene;
    private javax.swing.JList<String> sList;
    private javax.swing.JPopupMenu sMenu;
    private javax.swing.JPanel sPan;
    private javax.swing.JTable tableExpense;
    private javax.swing.JTabbedPane tbPanel;
    private javax.swing.JTable tblCart;
    private javax.swing.JTable tblLowQtyItem;
    private javax.swing.JTable tblRecord;
    private javax.swing.JButton tbnAddItem;
    private javax.swing.JButton tbnDashboard;
    private javax.swing.JButton tbnDateSearch;
    private javax.swing.JButton tbnDiscountUpdate;
    private javax.swing.JButton tbnExpense;
    private javax.swing.JButton tbnRecords;
    private javax.swing.JButton tbnSales;
    private javax.swing.JTextField txtCName;
    private javax.swing.JTextField txtCPhone;
    private javax.swing.JTextField txtCompanyName;
    private javax.swing.JTextField txtDis;
    private javax.swing.JLabel txtDisc;
    private com.toedter.calendar.JDateChooser txtEndDate;
    private com.toedter.calendar.JDateChooser txtEndDateRecod;
    private javax.swing.JTextField txtExCost;
    private javax.swing.JTextArea txtExDetails;
    private com.toedter.calendar.JDateChooser txtExEnd;
    private javax.swing.JButton txtExSearch;
    private com.toedter.calendar.JDateChooser txtExStart;
    private javax.swing.JTextField txtExTitle;
    private javax.swing.JTextField txtExUser;
    private javax.swing.JTextField txtGeneric;
    private javax.swing.JTextField txtID;
    private javax.swing.JTextField txtISearch;
    private javax.swing.JTextField txtItemName;
    private javax.swing.JTextField txtMRP;
    private javax.swing.JTextField txtMRPPrice;
    private javax.swing.JTextField txtQty;
    private javax.swing.JTextField txtQtyAdd;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtSearchCustomar;
    private com.toedter.calendar.JDateChooser txtStartDate;
    private com.toedter.calendar.JDateChooser txtStartDateRecod;
    private javax.swing.JTextField txtStrength;
    private javax.swing.JLabel txtSubTotal;
    private javax.swing.JLabel txtTotal;
    private javax.swing.JTextField txtUnitePrice;
    private javax.swing.JLabel txtUserImage;
    private javax.swing.JLabel txtuserName;
    // End of variables declaration//GEN-END:variables
}
