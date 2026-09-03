/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package subjtable;

public class Header {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public Header(String name, int width) {
        this.name = name;
        this.width = width;
    }

    public Header(String name) {
        this.name = name;
        this.width = -1;
    }

    private String name;
    private int width;

    @Override
    public String toString() {
        return name;
    }
}