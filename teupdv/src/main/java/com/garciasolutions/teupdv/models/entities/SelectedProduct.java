package com.garciasolutions.teupdv.models.entities;

public class SelectedProduct {
    private String code;
    private String name;
    private double price;
    private int quantity;
    private int id; // Adicionando o ID
    private String motivoCancelamento;
  //  private String formaPagamento;

    public SelectedProduct(int id, String code, String name, double price, int quantity, String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
        this.id = id;
        this.code = code;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
       // this.formaPagamento = formaPagamento;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMotivoCancelamento() { return motivoCancelamento; }

    public void setMotivoCancelamento(String motivoCancelamento) { this.motivoCancelamento = motivoCancelamento; }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void incrementQuantity() {
        this.quantity++;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return price * quantity;
    }


    @Override
    public String toString() {
        return code + " - " + name + " R$" + String.format("%.2f", getTotalPrice()) + " " + quantity + "x";
    }
}
