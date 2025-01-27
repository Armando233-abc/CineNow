package it.unisa.application.model.dao;

import it.unisa.application.database_connection.DataSourceSingleton;
import it.unisa.application.model.entity.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrenotazioneDAO {
    private final DataSource ds;

    public PrenotazioneDAO() {
        this.ds = DataSourceSingleton.getInstance();
    }

    public boolean create(Prenotazione prenotazione) {
        String sql = "INSERT INTO prenotazione (email_cliente, id_proiezione) VALUES (?, ?)";
        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, prenotazione.getCliente().getEmail());
            ps.setInt(2, prenotazione.getProiezione().getId());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    prenotazione.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Prenotazione retrieveById(int id) {
        String sql = "SELECT * FROM prenotazione WHERE id = ?";
        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Prenotazione prenotazione = new Prenotazione();
                prenotazione.setId(rs.getInt("id"));
                prenotazione.setCliente(new Cliente());
                prenotazione.getCliente().setEmail(rs.getString("email_cliente"));
                prenotazione.setProiezione(new Proiezione());
                prenotazione.getProiezione().setId(rs.getInt("id_proiezione"));
                return prenotazione;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Prenotazione> retrieveAllByCliente(Cliente cliente) {
        List<Prenotazione> prenotazioni = new ArrayList<>();
        String sql = "SELECT * FROM prenotazione WHERE email_cliente = ?";
        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cliente.getEmail());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Prenotazione prenotazione = new Prenotazione();
                prenotazione.setId(rs.getInt("id"));
                prenotazione.setCliente(cliente);
                prenotazione.setProiezione(new Proiezione());
                prenotazione.getProiezione().setId(rs.getInt("id_proiezione"));

                List<PostoProiezione> posti = new ArrayList<>();
                String sqlPosti = "SELECT * FROM occupa WHERE id_prenotazione = ?";
                try (PreparedStatement psPosti = connection.prepareStatement(sqlPosti)) {
                    psPosti.setInt(1, prenotazione.getId());
                    ResultSet rsPosti = psPosti.executeQuery();
                    while (rsPosti.next()) {
                        Posto posto = new Posto();
                        posto.setSala(new Sala());
                        posto.getSala().setId(rsPosti.getInt("id_sala"));
                        posto.setFila(rsPosti.getString("fila").charAt(0));
                        posto.setNumero(rsPosti.getInt("numero"));

                        PostoProiezione postoProiezione = new PostoProiezione();
                        postoProiezione.setPosto(posto);
                        postoProiezione.setProiezione(prenotazione.getProiezione());
                        posti.add(postoProiezione);
                    }
                }

                prenotazione.setPostiPrenotazione(posti);
                prenotazioni.add(prenotazione);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prenotazioni;
    }

}
