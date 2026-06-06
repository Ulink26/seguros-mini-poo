package seguros.model;
import java.io.Serializable;

// este programa es para simular a una persona que contrata una polzia
//implementa Serializablle para permitir guardar objetos con el objetivo de sacar cosas de archivos o streams
public class Cliente implements Serializable{
    private static final long serialVersionUID=1L;//identificador para la serializacion
    private final int id;//identificador unico del cliente
    private final String nombre;//nombre del cliente
    private final String telefono;//telefono del cliente
    private final String email;//correo del cliente

    //constructor que inicializa los datos
    public Cliente(int id, String nombre,String telefono,String email){
        this.id=id;
        this.nombre=nombre;
        this.telefono=telefono;
        this.email=email;
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
    //se usa un override para devolver una representacion en texto del objeto
    @Override
    public String toString(){
        return nombre;
    }
}