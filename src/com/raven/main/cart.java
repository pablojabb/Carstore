/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.raven.main;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import static java.awt.Color.blue;
import static java.awt.Color.red;
import java.awt.Component;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import raven.table.ModelItemSell;
import raven.table.QtyCellEditor;
import raven.toast.Notifications;

/**
 *
 * @author Lenovo
 */
public class cart extends javax.swing.JFrame {

    /**
     * Creates new form cart
     */
    public cart() {
        initComponents();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        Notifications.getInstance().setJFrame(this);

        tb_load();
    }

    public final void tb_load() {
        try {

            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();

            jTable1.getColumnModel().getColumn(4).setCellEditor(new QtyCellEditor(() -> {

                sumAmount();
            }));
            jTable1.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    setHorizontalAlignment(SwingConstants.CENTER);
                    return this;
                }
            });

            String query = "SELECT product_id,product_name,product_brand,quantity,price FROM cart";

            PreparedStatement preparedStatement = null;
            ResultSet rs = null;

            try {
                preparedStatement = dbase.urcon().prepareStatement(query);
                rs = preparedStatement.executeQuery();

                while (rs.next()) {

                    int quantity = rs.getInt("quantity");

                    if (quantity != 0) {

                        int id = rs.getInt("product_id");
                        String name = rs.getString("product_name");
                        String brand = rs.getString("product_brand");
                        float price = rs.getFloat("price");

                        float total = price * quantity;

                        model.addRow(new ModelItemSell(id, name, brand, quantity, total, total).toTableRow(jTable1.getRowCount() + 1));

                    } else {
                    }

                }

                sumAmount();

            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (dbase.urcon() != null) {
                    dbase.urcon().close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sumAmount() {
        int total = 0;
        for (int i = 0; i < jTable1.getRowCount(); i++) {
            ModelItemSell item = (ModelItemSell) jTable1.getValueAt(i, 0);
            total += item.getTotal();
        }
        DecimalFormat df = new DecimalFormat("P #,##0.00");
//        cart_price.setText(df.format(total));
    }

    public void refresh() {

        this.dispose();
        new cart().setVisible(true);
        tb_load();

    }

    public void clearCart() {
        try {
            String deleteQuery = "DELETE FROM cart";
            try (PreparedStatement deleteStatement = dbase.urcon().prepareStatement(deleteQuery)) {
                deleteStatement.executeUpdate();

            } finally {
                if (dbase.urcon() != null) {
                    dbase.urcon().close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCart(int id) {

        try {
            String deleteQuery = "DELETE FROM cart where product_id = ?";
            try (PreparedStatement deleteStatement = dbase.urcon().prepareStatement(deleteQuery)) {
                deleteStatement.setInt(1, id);

                deleteStatement.executeUpdate();

            } finally {
                if (dbase.urcon() != null) {
                    dbase.urcon().close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public boolean cashValid() {

        String textValue = textField2.getText();
        String cleanedTextValue = textValue.replaceAll("[^\\d.]", "");
        Float cash = null;
        try {
            cash = Float.valueOf(cleanedTextValue);
        } catch (NumberFormatException e) {
        }

        String textValue1 = cart_price.getText();

        String cleanedTextValue1 = textValue1.replaceAll("[^\\d.]", "");

        Float prodprice = null;
        try {
            prodprice = Float.valueOf(cleanedTextValue1);
        } catch (NumberFormatException e) {
            System.err.println("Error: Cannot convert string to float. Invalid format.");

        }

        return cash >= prodprice || cash <= prodprice;

    }

    public boolean nameValid() {

        String name = textField3.getText();

        return !name.isEmpty();

    }

    public void transactOperation() {

        String textValue = textField2.getText();
        String cleanedTextValue = textValue.replaceAll("[^\\d.]", "");
        Float cash = null;
        try {
            cash = Float.valueOf(cleanedTextValue);
        } catch (NumberFormatException e) {
        }

        String textValue1 = cart_price.getText();

        String cleanedTextValue1 = textValue1.replaceAll("[^\\d.]", "");

        Float prodprice = null;
        try {
            prodprice = Float.valueOf(cleanedTextValue1);
        } catch (NumberFormatException e) {
            System.err.println("Error: Cannot convert string to float. Invalid format.");

        }

        float Change = cash - prodprice;

        if (Change < 0) {
            int choice = JOptionPane.showConfirmDialog(this, "Cash amount is insuficient", "ACTION CONFIRMATION NOTICE", JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
        } else if (Change == 0) {

            int choice = JOptionPane.showConfirmDialog(this, " Thank You For Buying!!! pls dont snitch us", "ACTION CONFIRMATION NOTICE", JOptionPane.CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
            addToDatabase();

        } else {
            int choice = JOptionPane.showConfirmDialog(this, "Your Change is " + Change + " Thank You For Buying!!!", "ACTION CONFIRMATION NOTICE", JOptionPane.CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
            addToDatabase();

        }

    }

    public void addToDatabase() {

        int OTC_maxId = 0;

        String OTC_maxId_get = "SELECT MAX(transaction_id) FROM Transactions";
        try (Connection connection = dbase.urcon(); Statement maxOrderIdStatement = connection.createStatement(); ResultSet maxOrderIdResult = maxOrderIdStatement.executeQuery(OTC_maxId_get)) {

            if (maxOrderIdResult.next()) {
                OTC_maxId = maxOrderIdResult.getInt(1);

            }
        } catch (SQLException ex) {
        }

        OTC_maxId = OTC_maxId + 1;

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = currentDate.format(formatter);

        int rowCount = jTable1.getRowCount();
        String sql = "INSERT INTO Transactions (transaction_id,clien, date, prod_id, prod_name, prod_brand, quantity, total,totals) VALUES (?,?, ?, ?, ?, ?, ?,?,?)";

        try (Connection connection = dbase.urcon(); java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {

            float totals = 0;
            float prodprice = 0;
            for (int row = 0; row < rowCount; row++) {

                String productName = jTable1.getValueAt(row, 2).toString();
                String productBrand = jTable1.getValueAt(row, 3).toString();
                int productId = Integer.parseInt(jTable1.getValueAt(row, 1).toString());
                int productQuantity = Integer.parseInt(jTable1.getValueAt(row, 4).toString());

                String textValue = jTable1.getValueAt(row, 6).toString();
//                System.out.println(textValue);
                String cleanedTextValue = textValue.replaceAll("[^\\d.]", "");

                try {
                    prodprice = Float.parseFloat(cleanedTextValue); // Corrected Float.parseFloat()
                } catch (NumberFormatException e) {
                    // Handle the NumberFormatException if necessary
                }

                String textValue1 = cart_price.getText();
//                System.out.println(textValue);
                String cleanedTextValue1 = textValue1.replaceAll("[^\\d.]", "");

                try {
                    totals = Float.parseFloat(cleanedTextValue1); // Corrected Float.parseFloat()
                } catch (NumberFormatException e) {
                    // Handle the NumberFormatException if necessary
                }

                String client = textField3.getText();

                statement.setInt(1, OTC_maxId);
                statement.setString(2, client);
                statement.setString(3, formattedDate);
                statement.setInt(4, productId);
                statement.setString(5, productName);
                statement.setString(6, productBrand);
                statement.setInt(7, productQuantity);
                statement.setFloat(8, prodprice);
                statement.setFloat(9, totals);

                statement.executeUpdate();

            }

            clrTxt();
            clearCart();
            refresh();
            Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Transaction saved");

        } catch (SQLException ex) {
            System.out.println(ex);
        }

    }

    public void clrTxt() {
        cart_price.setText("");
        textField2.setText("");
        textField3.setText("");
    }

    public static void selectTableRow(JTable table, int rowIndex) {
        // Ensure that the row index is valid
        if (rowIndex >= 0 && rowIndex < table.getRowCount()) {
            // Select the specified row
            table.setRowSelectionInterval(rowIndex, rowIndex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        panelShadow1 = new com.raven.swing.PanelShadow();
        textField2 = new textfield.TextField();
        jPanel3 = new javax.swing.JPanel();
        cart_price = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        textField3 = new textfield.TextField();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(246, 244, 244));

        jTable1.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "", "ID", "Name", "Brand", "Quantity", "Price", "Total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setToolTipText("Press again item after adjusting quantity");
        jTable1.setRowHeight(40);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jTable1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTable1KeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setMinWidth(0);
            jTable1.getColumnModel().getColumn(0).setPreferredWidth(0);
            jTable1.getColumnModel().getColumn(0).setMaxWidth(0);
            jTable1.getColumnModel().getColumn(1).setPreferredWidth(40);
            jTable1.getColumnModel().getColumn(1).setMaxWidth(40);
            jTable1.getColumnModel().getColumn(4).setPreferredWidth(80);
            jTable1.getColumnModel().getColumn(4).setMaxWidth(80);
            jTable1.getColumnModel().getColumn(5).setPreferredWidth(100);
            jTable1.getColumnModel().getColumn(5).setMaxWidth(100);
        }

        textField2.setForeground(new java.awt.Color(73, 69, 69));
        textField2.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        textField2.setLabelText("Enter Cash");
        textField2.setLineColor(new java.awt.Color(51, 51, 51));
        textField2.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                textField2CaretUpdate(evt);
            }
        });
        textField2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                textField2FocusLost(evt);
            }
        });
        textField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textField2ActionPerformed(evt);
            }
        });
        textField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textField2KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textField2KeyReleased(evt);
            }
        });

        jPanel3.setOpaque(false);

        cart_price.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        cart_price.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jLabel2.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(73, 69, 69));
        jLabel2.setText("Total:");

        jLabel1.setFont(new java.awt.Font("SansSerif", 0, 10)); // NOI18N
        jLabel1.setText("ps. no tax cuz supercars 3X tax in phil");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cart_price, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(cart_price, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jButton1.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(73, 69, 69));
        jButton1.setText("Pay");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        textField3.setForeground(new java.awt.Color(73, 69, 69));
        textField3.setFont(new java.awt.Font("SansSerif", 1, 10)); // NOI18N
        textField3.setLabelText("Enter Fullname");
        textField3.setLineColor(new java.awt.Color(51, 51, 51));
        textField3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textField3KeyReleased(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        jButton2.setForeground(new java.awt.Color(73, 69, 69));
        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton4.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        jButton4.setForeground(new java.awt.Color(73, 69, 69));
        jButton4.setText("Return");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelShadow1Layout = new javax.swing.GroupLayout(panelShadow1);
        panelShadow1.setLayout(panelShadow1Layout);
        panelShadow1Layout.setHorizontalGroup(
            panelShadow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelShadow1Layout.createSequentialGroup()
                .addGroup(panelShadow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(textField3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelShadow1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(textField2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelShadow1Layout.createSequentialGroup()
                .addGap(0, 43, Short.MAX_VALUE)
                .addGroup(panelShadow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(41, 41, 41))
        );
        panelShadow1Layout.setVerticalGroup(
            panelShadow1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelShadow1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textField2, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(textField3, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(8, Short.MAX_VALUE))
        );

        jButton3.setText("Remove Item");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton5.setText("How to adjust quantity");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 625, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelShadow1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(63, 63, 63)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(241, 241, 241)
                .addComponent(jButton5)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 535, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelShadow1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        clearCart();
        clrTxt();
        refresh();
        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Cart Cleared");
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:

        int choice = JOptionPane.showConfirmDialog(this, "Confirm payment for this transaction?", "ACTION CONFIRMATION NOTICE", JOptionPane.CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.OK_OPTION) {

            int rownum = jTable1.getSelectedRow();
            if (rownum < 0) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Cart is empty");
            } else {
                int id = (int) jTable1.getValueAt(rownum, 1);
                deleteCart(id);
                tb_load();
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER, "Item Removed");
            }

        }

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        this.dispose();


    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:

        int ron = jTable1.getRowCount();
        if (ron >= 1) {

            if (!textField2.getText().isBlank()) {

                if (nameValid()) {

                    if (cashValid()) {
                        transactOperation();

                    } else {
                        Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Invalid Cash input");
                    }
                } else {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Invalid Name");
                }
            } else {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Enter Cash amount first");
            }
        } else {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Cart is empty");
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void textField2CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_textField2CaretUpdate

    }//GEN-LAST:event_textField2CaretUpdate

    private void textField2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textField2KeyReleased
        // TODO add your handling code here:

        if (textField2.getText().equalsIgnoreCase("A")) {
            textField2.setCaretColor(Color.red);
            textField2.setLineColor(red);
            textField2.setForeground(red);
        } else {
            textField2.setLineColor(Color.blue);
            textField2.setForeground(blue);
        }

    }//GEN-LAST:event_textField2KeyReleased

    private void textField2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textField2KeyPressed
        // TODO add your handling code here:

    }//GEN-LAST:event_textField2KeyPressed

    private void textField2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textField2FocusLost
        // TODO add your handling code here:

    }//GEN-LAST:event_textField2FocusLost

    private void textField3KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textField3KeyReleased
        // TODO add your handling code here:

        if (textField2.getText().equalsIgnoreCase("A")) {
            textField2.setCaretColor(Color.red);
            textField2.setLineColor(red);
            textField2.setForeground(red);
        } else {
            textField2.setLineColor(Color.blue);
            textField2.setForeground(blue);
        }

    }//GEN-LAST:event_textField3KeyReleased

    private void textField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textField2ActionPerformed

    private void jTable1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable1KeyPressed
        // TODO add your handling code here:
        DecimalFormat df = new DecimalFormat("P #,##0.00");

        float prodprice = 0;
        int rowCount = jTable1.getRowCount();

        if (rowCount <= 0) {

            for (int row = 0; row < rowCount; row++) {

                String textValue = jTable1.getValueAt(row, 6).toString();
//                System.out.println(textValue);
                String cleanedTextValue = textValue.replaceAll("[^\\d.]", "");

                try {
                    prodprice = Float.parseFloat(cleanedTextValue); // Corrected Float.parseFloat()
                } catch (NumberFormatException e) {
                    // Handle the NumberFormatException if necessary
                }

            }
            cart_price.setText(df.format(prodprice));
        }

        //        cart_price.setText(df.format(total));

    }//GEN-LAST:event_jTable1KeyPressed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        // TODO add your handling code here:

        DecimalFormat df = new DecimalFormat("P #,##0.00");
        float total = 0;
        int rowCount = jTable1.getRowCount();

        if (rowCount > 0) {
            for (int row = 0; row < rowCount; row++) {
                String textValue = jTable1.getValueAt(row, 6).toString();
                String cleanedTextValue = textValue.replaceAll("[^\\d.]", "");

                try {
                    float prodprice = Float.parseFloat(cleanedTextValue); // Corrected Float.parseFloat()
                    total += prodprice; // Accumulate prodprice to total
                } catch (NumberFormatException e) {
                    // Handle the NumberFormatException if necessary
                }
            }
            cart_price.setText(df.format(total));
        }


    }//GEN-LAST:event_jTable1MouseClicked

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:

        JOptionPane.showConfirmDialog(this, "Adjust quantity by pressing Item quantity in table \nps. click cell again to update table", "How to adjust quantity", JOptionPane.CLOSED_OPTION, JOptionPane.INFORMATION_MESSAGE);

    }//GEN-LAST:event_jButton5ActionPerformed

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
            java.util.logging.Logger.getLogger(cart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(cart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(cart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(cart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        FlatLightLaf.setup();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new cart().setVisible(true);
            }
        });
    }

    private static JFrame fram;

    public static JFrame getJFrame() {
        return fram;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cart_price;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private com.raven.swing.PanelShadow panelShadow1;
    private textfield.TextField textField2;
    private textfield.TextField textField3;
    // End of variables declaration//GEN-END:variables
}
