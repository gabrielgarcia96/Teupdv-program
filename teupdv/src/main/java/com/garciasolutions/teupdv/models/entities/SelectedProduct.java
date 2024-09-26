package com.garciasolutions.teupdv.models.entities;

import java.util.ArrayList;
import java.util.List;

public class SelectedProduct {
    private String code;
    private String name;
    private double basePrice; // Preço base do produto
    private int quantity;
    private int id; // Adicionando o ID
    private String motivoCancelamento;
    private List<SubProduct> subProducts; // Lista para armazenar subprodutos

    public SelectedProduct(int id, String code, String name, double basePrice, int quantity, String motivoCancelamento) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.basePrice = basePrice;
        this.quantity = quantity;
        this.motivoCancelamento = motivoCancelamento;
        this.subProducts = new ArrayList<>(); // Inicializa a lista de subprodutos
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public double getBasePrice() {
        return basePrice;
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

    // Adiciona um subproduto à lista
    public void addSubProduct(String subProductName, double subProductPrice) {
        SubProduct subProduct = new SubProduct(subProductName, subProductPrice);
        subProducts.add(subProduct);
    }

    // Retorna a lista de subprodutos
    public List<SubProduct> getSubProducts() {
        return subProducts;
    }

    // Calcula o preço total, incluindo os subprodutos
    public double getTotalPrice() {
        double total = basePrice * quantity; // Preço do produto principal
        for (SubProduct sub : subProducts) {
            total += sub.getPrice(); // Soma o preço dos subprodutos
        }
        return total;
    }

    @Override
    public String toString() {
        return code + " - " + name + " R$" + String.format("%.2f", getTotalPrice()) + " " + quantity + "x";
    }
}
