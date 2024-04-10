// Clase para almacenar la información de los jugadores
class OfertaInfo {
    private String usuario;
    private int oferta;

    public OfertaInfo(String usuario, int oferta) {
        this.usuario = usuario;
        this.oferta = oferta;
    }

    public String getNombre() {
        return usuario;
    }

    public int getOferta() {
        return oferta;
    }
}