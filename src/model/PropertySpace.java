package model;

public abstract class PropertySpace extends Space implements Property {
    protected int price;
    protected Player owner;

    public PropertySpace(int number, String name, double[] positions,byte[] image, int price) {
        super(number, name, positions, image);
        action = "property";
        this.price = price;
        owner = null;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPrice() {
        return price;
    }

    public void soldTo(Player player) {
        owner = player;
    }

    public Player getOwner() {
        return owner;
    }

    public abstract int getRentPrice();

    public void soldBack() {
        owner = null;
    }
}
