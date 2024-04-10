// Clase para almacenar la información de los jugadores
class JugadorInfo {
    private String nombre;
    private String propietario;
    private int puntos;
    private int valorMercado;
    private int ofertaMinima;

    public JugadorInfo(String nombre, String propietario, int puntos, int valorMercado, int ofertaMinima) {
        this.nombre = nombre;
        this.propietario = propietario;
        this.puntos = puntos;
        this.valorMercado = valorMercado;
        this.ofertaMinima = ofertaMinima;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPropietario() {
        return propietario;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getValorMercado() {
        return valorMercado;
    }

    public int getOfertaMinima() {
        return ofertaMinima;
    }

    public String toString() {
        return nombre + " - " + propietario + " - " + puntos + " - " + valorMercado + " - " + ofertaMinima;

    }
}