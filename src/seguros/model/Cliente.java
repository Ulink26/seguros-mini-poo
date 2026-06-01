package seguros.model;

import java.io.Serializable;

/**
 * Representa a una persona que contrata una poliza.
 */
public class Cliente implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String nombre;
    private final String telefono;
    private final String email;

    public Cliente(int id, String nombre, String telefono, String email) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
