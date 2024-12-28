package it.unisa.application.model.dao;

import it.unisa.application.model.entity.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

public class ClienteDAO {
    private DataSource ds;

    public ClienteDAO(DataSource ds) {
        this.ds = ds;
    }

    public boolean create(Cliente cliente) {
        String sqlUtente = "INSERT INTO utente (email, password, ruolo) VALUES (?, ?, ?)";
        String sqlCliente = "INSERT INTO cliente (email, nome, cognome) VALUES (?, ?, ?)";
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtUtente = conn.prepareStatement(sqlUtente);
                 PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente)) {
                stmtUtente.setString(1, cliente.getEmail());
                stmtUtente.setString(2, cliente.getPassword());
                stmtUtente.setString(3, "cliente");
                stmtUtente.executeUpdate();
                stmtCliente.setString(1, cliente.getEmail());
                stmtCliente.setString(2, cliente.getNome());
                stmtCliente.setString(3, cliente.getCognome());
                stmtCliente.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Cliente retrieveByEmail(String email, String password) {
        String sql = "SELECT c.email, c.nome, c.cognome " +
                     "FROM cliente c " +
                     "JOIN utente u ON c.email = u.email " +
                     "WHERE u.email = ? AND u.password = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setEmail(rs.getString("email"));
                    cliente.setNome(rs.getString("nome"));
                    cliente.setCognome(rs.getString("cognome"));
                    //PrenotazioneDAO pDao = new PrenotazioneDAO(ds);
                    //pDao.prenotazioniByCliente(cliente.getEmail());
                    //cliente.setPrenotazioni() Retrive prenotazioni da PrenotazioneDAO
                    return cliente;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
