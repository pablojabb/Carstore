package com.raven.swing;

import java.awt.Color;
import javax.swing.JPanel;

public class PanelItem extends JPanel {

    public PanelItem() {
        setBackground(Color.black);
        setLayout(new WrapLayout(WrapLayout.CENTER, 20, 10));
    }
}
