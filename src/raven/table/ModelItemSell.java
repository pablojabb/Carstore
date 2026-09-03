package raven.table;

import java.text.DecimalFormat;

public class ModelItemSell {

    /**
     * @return the productId
     */
    public int getProductId() {
        return productId;
    }

    /**
     * @param productId the productId to set
     */
    public void setProductId(int productId) {
        this.productId = productId;
    }

    /**
     * @return the productName
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @param productName the productName to set
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * @return the productBrand
     */
    public String getProductBrand() {
        return productBrand;
    }

    /**
     * @param productBrand the productBrand to set
     */
    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }

    /**
     * @return the qty
     */
    public int getQty() {
        return qty;
    }

    /**
     * @param qty the qty to set
     */
    public void setQty(int qty) {
        this.qty = qty;
    }

    /**
     * @return the price
     */
    public float getPrice() {
        return price;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(float price) {
        this.price = price;
    }

    /**
     * @return the total
     */
    public float getTotal() {
        return total;
    }

    /**
     * @param total the total to set
     */
    public void setTotal(float total) {
        this.total = total;
    }

    public ModelItemSell() {
    }

    public ModelItemSell(int productId, String productName, String productBrand, int qty, float price, float total) {
        this.productId = productId;
        this.productName = productName;
        this.productBrand = productBrand;
        this.qty = qty;
        this.price = price;
        this.total = total;
    }

    private int productId;
    private String productName;
    private String productBrand;
    private int qty;
    private float price;
    private float total;

    public Object[] toTableRow(int rowNum) {
        DecimalFormat df = new DecimalFormat("#,##0.##");
        return new Object[]{this, productId, productName, productBrand, qty, "P" + df.format(price), "P" + df.format(total)};
    }
}
