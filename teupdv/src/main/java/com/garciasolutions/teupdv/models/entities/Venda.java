package com.garciasolutions.teupdv.models.entities;

import com.garciasolutions.teupdv.models.data.DatabaseConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.beans.property.*;
import java.sql.Date; // Adicione esta importação
import java.time.LocalDate;


public class Venda {

    private int id;
    private boolean cancelada;
    private String codigoProduto;
    private String nomeProduto;
    private Double valor;
    private Double preco;
    private Integer quantidade;
    private Double total;
    private Date data;
    private String paymentMethod;
    private String motivoCancelamento;

    // Construtor
    public Venda(int id, String codigoProduto, String nomeProduto, Double valor, Double preco, Integer quantidade, Date data, String paymentMethod, String motivoCancelamento) {
        this.id = id;
        this.codigoProduto = codigoProduto;
        this.nomeProduto = nomeProduto;
        this.valor = valor;
        this.preco = preco;
        this.quantidade = quantidade;
        this.total = preco * quantidade;
        this.data = data;
        this.paymentMethod = paymentMethod;
        this.motivoCancelamento = motivoCancelamento;
    }

    public boolean isCancelada() {
        return cancelada;
    }
    public void setCancelada(boolean cancelada) {
        this.cancelada = cancelada;
    }

    // Getters
    public int getId() {return id;}
    public String getCodigoProduto() { return codigoProduto; }
    public String getNomeProduto() { return nomeProduto; }
    public Double getPreco() { return preco; }
    public Integer getQuantidade() { return quantidade; }
    public Double getTotal() { return total; }
    public Date getData() { return data; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getMotivoCancelamento() { return motivoCancelamento; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setCodigoProduto(String codigoProduto) { this.codigoProduto = codigoProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }
    public void setPreco(Double preco) { this.preco = preco; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public void setTotal(Double total) { this.total = total; }
    public void setData(Date data) { this.data = data; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setMotivoCancelamento(String motivoCancelamento) { this.motivoCancelamento = motivoCancelamento; }


    public void registrarVenda() {
        String sql = "INSERT INTO vendas (codigo_produto, nome_produto, valor, preco, quantidade, total, data, payment_method) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnect.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, codigoProduto);
            pstmt.setString(2, nomeProduto);
            pstmt.setDouble(3, valor);
            pstmt.setDouble(4, preco);
            pstmt.setInt(5, quantidade);
            pstmt.setDouble(6, total);
            pstmt.setDate(7, java.sql.Date.valueOf(LocalDate.now())); // Adiciona a data atual
            pstmt.setString(8, paymentMethod);

            pstmt.executeUpdate();
            System.out.println("Venda registrada com sucesso!");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erro ao registrar a venda.");
        }
    }
}

