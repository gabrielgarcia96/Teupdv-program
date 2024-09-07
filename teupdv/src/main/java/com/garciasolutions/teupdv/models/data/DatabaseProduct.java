package com.garciasolutions.teupdv.models.data;


import com.garciasolutions.teupdv.models.entities.Venda;

import static com.garciasolutions.teupdv.models.data.DatabaseConnect.connect;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



public class DatabaseProduct {
    public void insertProductIntoDatabase(String code, String nameproduct, String priceproduct, String groupproduct) {
        String sql = "INSERT INTO products (code, nameproduct, priceproduct, groupproduct) VALUES (?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            pstmt.setString(2, nameproduct);
            pstmt.setDouble(3, Double.parseDouble(priceproduct)); // Converta o preço para double
            pstmt.setString(4, groupproduct);

            pstmt.executeUpdate();
            System.out.println("Produto cadastrado com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erro ao cadastrar produto.");
        }
    }


    public List<String> getAllProducts() {
        List<String> products = new ArrayList<>();
        String sql = "SELECT code, nameproduct FROM products";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String code = rs.getString("code");
                String name = rs.getString("nameproduct");
                products.add(code + " - " + name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public List<String> searchProducts(String searchText) {
        List<String> products = new ArrayList<>();
        // Usando 'LIKE' e convertendo a string para maiúsculas com a função UPPER()
        String sql = "SELECT code, nameproduct FROM products WHERE UPPER(code) LIKE UPPER(?) OR UPPER(nameproduct) LIKE UPPER(?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + searchText.toUpperCase() + "%"; // Garantindo que o padrão de busca esteja em maiúsculas
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("code");
                    String name = rs.getString("nameproduct");
                    products.add(code + " - " + name);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public double getProductPrice(String code) {
        String sql = "SELECT priceproduct FROM products WHERE code = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("priceproduct");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public String getProductName(String code) {
        String sql = "SELECT nameproduct FROM products WHERE code = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nameproduct");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getProductGroup(String code) {
        String sql = "SELECT groupproduct FROM products WHERE code = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("groupproduct");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void updateProductInDatabase(String code, String nameproduct, String priceproduct, String groupproduct) {
        String sql = "UPDATE products SET nameproduct = ?, priceproduct = ?, groupproduct = ? WHERE code = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nameproduct);
            pstmt.setString(2, priceproduct);
            pstmt.setString(3, groupproduct);
            pstmt.setString(4, code);

            pstmt.executeUpdate();
            System.out.println("Produto atualizado com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erro ao atualizar produto.");
        }
    }

    public void deleteProductFromDatabase(String code) {
        String sql = "DELETE FROM products WHERE code = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);

            pstmt.executeUpdate();
            System.out.println("Produto excluído com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erro ao excluir produto.");
        }
    }

    // Adicionar um novo grupo ao banco de dados
    public void insertGroupIntoDatabase(String groupName) {
        String sql = "INSERT INTO product_groups (name) VALUES (?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupName);
            pstmt.executeUpdate();
            System.out.println("Grupo cadastrado com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erro ao cadastrar grupo.");
        }
    }

    // Recuperar todos os grupos do banco de dados
    public List<String> getAllGroups() {
        List<String> groups = new ArrayList<>();
        String sql = "SELECT name FROM product_groups";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                groups.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    // Verificar se o código do produto já existe
    public boolean isProductCodeExists(String code) {
        String query = "SELECT COUNT(*) FROM products WHERE code = ?";
        try (Connection conn = connect();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, code);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Verificar se o grupo já existe
    public boolean isGroupExists(String group) {
        String query = "SELECT COUNT(*) FROM product_groups WHERE name = ?";
        try (Connection conn = connect();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, group);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public int generateUniqueId() {
        // Simples exemplo para gerar IDs únicos (não recomendado para uso em produção sem ajustes)
        return (int) (Math.random() * 100000);
    }


    // Gerar relatório de vendas
    public List<Venda> getSalesReport(LocalDate startDate, LocalDate endDate) {
        List<Venda> reportData = new ArrayList<>();
        String sql = "SELECT id, codigo_produto, nome_produto, preco, quantidade, total, data, payment_method " +
                "FROM vendas WHERE data >= ? AND data < ? AND cancelada = false";

        // Ajusta o endDate para o início do próximo dia
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        java.sql.Timestamp endTimestamp = java.sql.Timestamp.valueOf(endDateTime);

        try (Connection conn = DatabaseConnect.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setTimestamp(2, endTimestamp); // Inclui até o início do próximo dia

            System.out.println("Iniciando a consulta SQL...");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id"); // Recupere o ID da venda
                String code = rs.getString("codigo_produto");
                String name = rs.getString("nome_produto");
                double price = rs.getDouble("preco");
                int quantity = rs.getInt("quantidade");
                double total = rs.getDouble("total");
                Date date = rs.getDate("data");
                String paymentMethod = rs.getString("payment_method");

                Venda venda = new Venda(
                        id,               // Adicione o ID no construtor
                        code,
                        name,
                        price,
                        price,            // Assumindo que o valor e o preço são iguais
                        quantity,
                        date,
                        paymentMethod,
                        null
                );
                reportData.add(venda);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reportData;
    }

    public List<Venda> getCancelledSalesReport(LocalDate startDate, LocalDate endDate) {
        List<Venda> reportData = new ArrayList<>();
        String sql = "SELECT id, codigo_produto, nome_produto, preco, quantidade, total, data, payment_method, motivo_cancelamento " +
                "FROM vendas WHERE data >= ? AND data < ? AND cancelada = true";

        // Ajusta o endDate para o início do próximo dia
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        java.sql.Timestamp endTimestamp = java.sql.Timestamp.valueOf(endDateTime);

        try (Connection conn = DatabaseConnect.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setTimestamp(2, endTimestamp); // Inclui até o início do próximo dia

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String code = rs.getString("codigo_produto");
                String name = rs.getString("nome_produto");
                double price = rs.getDouble("preco");
                int quantity = rs.getInt("quantidade");
                double total = rs.getDouble("total");
                Date date = rs.getDate("data");
                String paymentMethod = rs.getString("payment_method");
                String motivoCancelamento = rs.getString("motivo_cancelamento");

                Venda venda = new Venda(
                        id,
                        code,
                        name,
                        price,
                        price,
                        quantity,
                        date,
                        paymentMethod,
                        motivoCancelamento
                );
                reportData.add(venda);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reportData;
    }



    public void saveFiscalData(String cnpj, String nome_empresa, String endereco, String cidade, String cep) {
        // Implementar a lógica para conectar ao banco de dados e salvar os dados
        String sql = "INSERT INTO empresa (cnpj, nome_empresa, endereco, cidade, cep) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnect.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cnpj);
            pstmt.setString(2, nome_empresa);
            pstmt.setString(3, endereco);
            pstmt.setString(4, cidade);
            pstmt.setString(5, cep);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Implementar métodos para recuperar dados fiscais

    public String[] getFiscalData() {
        String sql = "SELECT cnpj, nome_empresa, endereco, cidade, cep FROM empresa LIMIT 1";
        try (Connection conn = DatabaseConnect.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new String[] {
                        rs.getString("cnpj"),
                        rs.getString("nome_empresa"),
                        rs.getString("endereco"),
                        rs.getString("cidade"),
                        rs.getString("cep")
                };
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        // Retorna um array com valores nulos se não houver dados
        return new String[] { "", "", "", "", "" };
    }



}




