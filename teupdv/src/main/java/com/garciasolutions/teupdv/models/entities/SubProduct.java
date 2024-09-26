package com.garciasolutions.teupdv.models.entities;


public class SubProduct {
    private String name;
    private double price;

    public SubProduct(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}

