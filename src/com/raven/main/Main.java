package com.raven.main;

import com.formdev.flatlaf.FlatLightLaf;
import com.raven.event.EventItem;
import com.raven.form.FormHome;
import com.raven.model.ModelItem;
import java.awt.BorderLayout;
import java.awt.Color;
import static java.awt.Color.red;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTargetAdapter;
import org.jdesktop.animation.timing.interpolation.PropertySetter;
import raven.toast.Notifications;
import subjtable.Header;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Main extends javax.swing.JFrame {

    private FormHome home;
    private Animator animator;
    private Point animatePoint;
    private ModelItem itemSelected;
    String finalpath = "";
    String finalpath2 = "";
    Clip clip;

    public Main() {
        initComponents();
        init();

        jLabel5.setVisible(false);
        Notifications.getInstance().setJFrame(this);

        this.setLocationRelativeTo(null);

        //Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Data already exists");
        animator = PropertySetter.createAnimator(800, mainPanel, "imageLocation", animatePoint, mainPanel.getTargetLocation());
        animator.addTarget(new PropertySetter(mainPanel, "imageSize", new Dimension(250, 150), mainPanel.getTargetSize()));
        animator.addTarget(new TimingTargetAdapter() {
            @Override
            public void end() {
                mainPanel.setImageOld(null);
            }
        });
        animator.setResolution(0);
        animator.setAcceleration(0.2f);
        animator.setDeceleration(0.1f);

        materialTabbed1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int selectedIndex = materialTabbed1.getSelectedIndex();

                if (selectedIndex == 0) {
                    testData();
                } else if (selectedIndex == 1) {
                    walk_inhis();
                } else {
                    Prod_load();
                }
            }
        });
    }

    private void init() {
        home = new FormHome();
        winButton.initEvent(this, background1);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(home);
        testData();

    }

    private void testData() {
        home.removeCard();
        home.setEvent(new EventItem() {
            @Override
            public void itemClick(Component com, ModelItem item) {
                if (itemSelected != null) {
                    mainPanel.setImageOld(itemSelected.getImage());
                }
                if (itemSelected != item) {
                    if (!animator.isRunning()) {
                        itemSelected = item;

                        new sound().playSound(clip);

                        try {
                            Thread.sleep(900);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }

                        Point animatePoint = new Point(900, 25);
                        mainPanel.setImage(item.getImage());
                        mainPanel.setImageLocation(animatePoint);
                        mainPanel.setImageSize(new Dimension(100, 100));
                        mainPanel.repaint();
                        home.setSelected(com);
                        home.showItem(item);
                        animator.start();

                    }
                }
            }
        }
        );

        String query = "SELECT prod_id, prod_name, prod_brand, prod_price, prod_des, front, side  FROM prod_info";

        try (Connection connection = dbase.urcon(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int prod_id = resultSet.getInt("prod_id");
                String prodname = resultSet.getString("prod_name");
                String prodbrand = resultSet.getString("prod_brand");
                String proddes = resultSet.getString("prod_des");
                float prodprice = resultSet.getFloat("prod_price");

                byte[] imageData = resultSet.getBytes("front");
                ImageIcon imageIcon1 = new ImageIcon(imageData);

                byte[] imageData2 = resultSet.getBytes("side");
                ImageIcon imageIcon2 = new ImageIcon(imageData2);

                home.addItem(new ModelItem(prod_id, prodname, proddes, prodprice, prodbrand, imageIcon2, imageIcon1));

            }

        } catch (SQLException e) {
            // Handle the SQLException
            System.out.println(e);
        }
    }

    private void walk_inhis() {

        DefaultTableModel model = (DefaultTableModel) tableCustom2.getModel();
        tableCustom2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        model.setRowCount(0);

        String query = "SELECT transaction_id, clien, date, prod_id, prod_name, prod_brand, quantity,totals  FROM Transactions";

        int newTotal = 0;
        int total = 0;

        try (Connection connection = dbase.urcon(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int transactionId = resultSet.getInt("transaction_id");

                if (!containsTransactionId(model, transactionId)) {
                    String client = resultSet.getString("clien");
                    String date = resultSet.getString("date");
                    total = resultSet.getInt("totals");

                    newTotal = total + newTotal;

                    model.addRow(new Object[]{transactionId, client, date, newTotal, getSubTableData(transactionId)});
                    tableCustom2.autoRowHeight(tableCustom2);
                }
            }
        } catch (SQLException e) {
            // Handle the SQLException
        }
    }

    private boolean containsTransactionId(DefaultTableModel model, int transactionId) {
        int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            int existingTransactionId = (int) model.getValueAt(i, 0);
            if (existingTransactionId == transactionId) {
                return true;
            }
        }
        return false;
    }

    public DefaultTableModel getSubTableData(int transactionId) {
        DefaultTableModel data = new DefaultTableModel();
        tableCustom2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        data.setColumnCount(5);
        data.addRow(new Object[]{new Header("Prod ID", 40), new Header("Prod Name", 90), new Header("Brand", 60), new Header("Quantity", 60), new Header("Subtotal", 70)});

        // Construct and execute the SELECT query
        String query = "SELECT prod_id, prod_name, prod_brand, quantity , total FROM Transactions WHERE transaction_id = '" + transactionId + "'";

        try {

            Statement s = dbase.urcon().createStatement();
            ResultSet resultSet = s.executeQuery(query);

            // Retrieve and populate the data
            while (resultSet.next()) {
                int prod_id = resultSet.getInt("prod_id");
                String prod_name = resultSet.getString("prod_name");
                String brand = resultSet.getString("prod_brand");
                String prod_quant = resultSet.getString("quantity");
                String subtotal = resultSet.getString("total");

                // Add a new row to the table model
                data.addRow(new Object[]{prod_id, prod_name, brand, prod_quant, subtotal});

            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any database errors
        }
        return data;
    }

    private void saveProductToDatabase() {

        String PN = textField1.getText();
        String Des = jTextPane1.getText();
        String brand = textField2.getText();
        String price = textField3.getText();

        float price1 = 0;

        if (!isProductDuplicate(PN)) {

            File imageFile = new File(finalpath);
            byte[] imageData = new byte[(int) imageFile.length()];

            try (FileInputStream fis = new FileInputStream(imageFile)) {
                fis.read(imageData);
            } catch (IOException e) {
                e.printStackTrace();
            }

            File imageFile2 = new File(finalpath2);
            byte[] imageData2 = new byte[(int) imageFile2.length()];

            try (FileInputStream fis = new FileInputStream(imageFile2)) {
                fis.read(imageData2);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (price.isEmpty()) {

                Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Price field is empty");
            } else {
                price1 = Float.parseFloat(price);

                String query = "INSERT INTO prod_info (prod_name, prod_brand, prod_des, prod_price, front, side) VALUES (?, ?, ?, ?, ?, ?)";

                try (PreparedStatement preparedStatement = dbase.urcon().prepareStatement(query)) {
                    preparedStatement.setString(1, PN);
                    preparedStatement.setString(3, Des);
                    preparedStatement.setString(2, brand);
                    preparedStatement.setFloat(4, price1);
                    preparedStatement.setBytes(5, imageData);
                    preparedStatement.setBytes(6, imageData2);

                    preparedStatement.executeUpdate();
                    Prod_load();
                    clearText();
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Product Added");

                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        dbase.urcon().close(); // Close the connection
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } else {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Product already exist");
        }

    }

    public void setLabelImage(JLabel label, ImageIcon imageIcon) {
        if (label != null && imageIcon != null) {
            Image scaledImage = imageIcon.getImage().getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            label.setIcon(scaledIcon);
        }
    }

    public String getFilePath(JLabel filePathLabel, JLabel previewLabel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            filePathLabel.setText(filePath);
            filePathLabel.setForeground(Color.GREEN);

            // Load image and set preview
            try {
                ImageIcon imageIcon = loadImageIcon(selectedFile);
                setLabelImage(previewLabel, imageIcon);
            } catch (IOException e) {
                e.printStackTrace();
                // Handle error loading image
            }

            return filePath;
        } else {
            // User canceled the file chooser dialog
            // Handle accordingly, like showing an error message
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Select an image file");
            return null;
        }
    }

    private ImageIcon loadImageIcon(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        inputStream.read(bytes);
        inputStream.close();
        return new ImageIcon(bytes);
    }

    public void clearText() {

        jLabel1.setIcon(null);
        jLabel2.setIcon(null);
        jLabel3.setText("empty filepath");
        jLabel3.setForeground(red);
        jLabel4.setText("empty filepath");
        jLabel4.setForeground(red);
        textField1.setText("");
        textField2.setText("");
        textField3.setText("");
        jTextPane1.setText("");

    }

    public void Prod_load() {

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        model.setRowCount(0);

        String query = "SELECT prod_id, prod_name, prod_brand, prod_price FROM prod_info";

        try (Connection connection = dbase.urcon(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int prodid = resultSet.getInt("prod_id");
                String prodname = resultSet.getString("prod_name");
                String prodbrand = resultSet.getString("prod_brand");
                float prodprice = resultSet.getFloat("prod_price");

                model.addRow(new Object[]{prodid, prodname, prodbrand, prodprice});

            }
        } catch (SQLException e) {
            // Handle the SQLException
        }

    }

    public void dataload(int id) {

        String strFromNumber = String.valueOf(id);
        jLabel5.setText(strFromNumber);
        String prodname = "";
        String prodbrand = "";
        float prodprice;
        String stringValue = "";
        String proddes = "";

        byte[] imageData1;
        ImageIcon imageIcon1 = null;

        byte[] imageData2;
        ImageIcon imageIcon2 = null;

        String query = "SELECT  prod_name, prod_brand, prod_price,prod_des,front,side FROM prod_info where prod_id = ?";

        try (Connection connection = dbase.urcon(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                prodname = resultSet.getString("prod_name");
                prodbrand = resultSet.getString("prod_brand");
                prodprice = resultSet.getFloat("prod_price");
                stringValue = Float.toString(prodprice);
                proddes = resultSet.getString("prod_des");

                imageData1 = resultSet.getBytes("front");
                imageIcon1 = new ImageIcon(imageData1);

                imageData2 = resultSet.getBytes("side");
                imageIcon2 = new ImageIcon(imageData2);

            }
        } catch (SQLException e) {
            // Handle the SQLException

        }

        textField1.setText(prodname);
        textField2.setText(prodbrand);
        textField3.setText(stringValue);
        jTextPane1.setText(proddes);

        setLabelImage(jLabel1, imageIcon1);
        setLabelImage(jLabel2, imageIcon2);

    }

    private boolean isProductDuplicate(String PN) {
        String query = "SELECT prod_name FROM prod_info WHERE prod_name = ? ";

        try (PreparedStatement preparedStatement = dbase.urcon().prepareStatement(query)) {
            preparedStatement.setString(1, PN);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                dbase.urcon().close(); // Close the connection
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        background1 = new com.raven.swing.Background();
        header = new javax.swing.JPanel();
        winButton = new com.raven.swing.win_button.WinButton();
        materialTabbed1 = new com.raven.swing.MaterialTabbed();
        mainPanel = new com.raven.swing.MainPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableCustom2 = new subjtable.TableCustom();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        textField1 = new textfield.TextField();
        textField2 = new textfield.TextField();
        textField3 = new textfield.TextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        background1.setBackground(new java.awt.Color(247, 246, 246));
        background1.setOpaque(true);

        header.setOpaque(false);

        javax.swing.GroupLayout headerLayout = new javax.swing.GroupLayout(header);
        header.setLayout(headerLayout);
        headerLayout.setHorizontalGroup(
            headerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headerLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(winButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        headerLayout.setVerticalGroup(
            headerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(winButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        materialTabbed1.setBackground(new java.awt.Color(51, 51, 51));
        materialTabbed1.setFont(new java.awt.Font("SansSerif", 3, 14)); // NOI18N

        mainPanel.setBackground(new java.awt.Color(51, 51, 51));
        mainPanel.setOpaque(true);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 899, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 630, Short.MAX_VALUE)
        );

        materialTabbed1.addTab("Shop", mainPanel);

        jPanel2.setBackground(new java.awt.Color(246, 244, 244));

        tableCustom2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Client", "Date", "Total", "Other info"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableCustom2.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        tableCustom2.setRowHeight(30);
        jScrollPane1.setViewportView(tableCustom2);
        if (tableCustom2.getColumnModel().getColumnCount() > 0) {
            tableCustom2.getColumnModel().getColumn(0).setPreferredWidth(30);
            tableCustom2.getColumnModel().getColumn(0).setMaxWidth(30);
            tableCustom2.getColumnModel().getColumn(1).setPreferredWidth(150);
            tableCustom2.getColumnModel().getColumn(1).setMaxWidth(150);
            tableCustom2.getColumnModel().getColumn(2).setPreferredWidth(90);
            tableCustom2.getColumnModel().getColumn(2).setMaxWidth(90);
            tableCustom2.getColumnModel().getColumn(3).setPreferredWidth(90);
            tableCustom2.getColumnModel().getColumn(3).setMaxWidth(90);
        }

        jButton1.setText("Refresh");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 887, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(378, 378, 378)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 526, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        materialTabbed1.addTab("Transactions", jPanel2);

        jTable1.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                " ID", "Product Name", "Product Brand", "Product Price"
            }
        ));
        jTable1.setRowHeight(30);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setPreferredWidth(70);
            jTable1.getColumnModel().getColumn(0).setMaxWidth(70);
        }

        textField1.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        textField1.setLabelText("Product Name");
        textField1.setLineColor(new java.awt.Color(51, 51, 51));

        textField2.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        textField2.setLabelText("Product Brand");
        textField2.setLineColor(new java.awt.Color(51, 51, 51));

        textField3.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        textField3.setLabelText("Product Price");
        textField3.setLineColor(new java.awt.Color(51, 51, 51));

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Front View");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102), 3));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Side View");
        jLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102), 3));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        jTextPane1.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jScrollPane3.setViewportView(jTextPane1);

        jButton2.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        jButton2.setText("ADD");
        jButton2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 204, 51), 3));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        jButton3.setText("UPDATE");
        jButton3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 239, 14), 3));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        jButton4.setText("CLEAR");
        jButton4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 51, 153), 3));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        jButton5.setText("DELETE");
        jButton5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(182, 10, 10), 3));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(204, 0, 0));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel3.setText("empty filepath");

        jLabel4.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(204, 0, 0));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel4.setText("empty filepath");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textField2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(textField1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane3)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addGap(43, 43, 43)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(46, 46, 46))
                            .addComponent(textField3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textField1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textField2, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textField3, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton6.setText("Refresh");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel5.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 527, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(196, 196, 196)
                        .addComponent(jButton6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)))
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 589, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        materialTabbed1.addTab("Manage", jPanel1);

        javax.swing.GroupLayout background1Layout = new javax.swing.GroupLayout(background1);
        background1.setLayout(background1Layout);
        background1Layout.setHorizontalGroup(
            background1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(background1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(background1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(materialTabbed1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, background1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(header, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)))
                .addContainerGap())
        );
        background1Layout.setVerticalGroup(
            background1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(background1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(header, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(materialTabbed1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(background1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(background1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        walk_inhis();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:

        if (!textField1.toString().isEmpty() && !textField2.toString().isEmpty() && !textField3.toString().isEmpty() && !jTextPane1.toString().isEmpty()) {
            saveProductToDatabase();

        } else {
            Notifications.getInstance().show(Notifications.Type.ERROR, Notifications.Location.TOP_CENTER, "Please fill up form");
        }

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:

        String id = jLabel5.getText();
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to perform this action?", "ACTION CONFIRMATION NOTICE", JOptionPane.CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.OK_OPTION) {
            try {
                Connection connection = dbase.urcon();

                // Delete from products table
                String deleteQuery = "DELETE FROM prod_info WHERE prod_id = ?";
                PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
                deleteStatement.setString(1, id);
                deleteStatement.executeUpdate();

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Product Deleted");
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            clearText();
            Prod_load();

        } else {
            Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Select Product first from the table");
        }

    }//GEN-LAST:event_jButton5ActionPerformed

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        // TODO add your handling code here:
        finalpath = getFilePath(jLabel3, jLabel1);
    }//GEN-LAST:event_jLabel1MouseClicked

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
        // TODO add your handling code here:
        finalpath2 = getFilePath(jLabel4, jLabel2);
    }//GEN-LAST:event_jLabel2MouseClicked

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:

        String pid = jLabel5.getText();
        int id = Integer.parseInt(pid);

        String prodname = textField1.getText();

        String prodbrand = textField2.getText();
        String stringValue = textField3.getText();
        float price = Float.parseFloat(stringValue);
        String proddes = jTextPane1.getText();

        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to perform this action?", "ACTION CONFIRMATION NOTICE", JOptionPane.CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.OK_OPTION) {
            try {

                String updateQuery = "UPDATE prod_info SET prod_name = ?, prod_brand = ?, prod_price = ?, prod_des = ? WHERE prod_id = ?";
                PreparedStatement preparedStatement = dbase.urcon().prepareStatement(updateQuery);
                preparedStatement.setString(1, prodname);
                preparedStatement.setString(2, prodbrand);
                preparedStatement.setFloat(3, price);
                preparedStatement.setString(4, proddes);
                preparedStatement.setInt(5, id); // Assuming id is the prod_id of the product you want to update

                preparedStatement.executeUpdate();

                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Product Updated");

            } catch (Exception e) {
                e.printStackTrace();
            }
            Prod_load();
            clearText();
        }

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        // TODO add your handling code here:

        int r = jTable1.getSelectedRow();
        int id = (int) jTable1.getValueAt(r, 0);

        jLabel5.setText(jTable1.getValueAt(r, 0).toString());
        dataload(id);
    }//GEN-LAST:event_jTable1MouseClicked

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        clearText();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        Prod_load();
    }//GEN-LAST:event_jButton6ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        FlatLightLaf.setup();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.raven.swing.Background background1;
    private javax.swing.JPanel header;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextPane jTextPane1;
    private com.raven.swing.MainPanel mainPanel;
    private com.raven.swing.MaterialTabbed materialTabbed1;
    private subjtable.TableCustom tableCustom2;
    private textfield.TextField textField1;
    private textfield.TextField textField2;
    private textfield.TextField textField3;
    private com.raven.swing.win_button.WinButton winButton;
    // End of variables declaration//GEN-END:variables
}
