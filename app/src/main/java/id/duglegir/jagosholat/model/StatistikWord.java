package id.duglegir.jagosholat.model;


public class StatistikWord {
    String id, waktu, shalat;

    public StatistikWord(String id, String waktu, String shalat) {
        this.id = id;
        this.waktu = waktu;
        this.shalat = shalat;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWaktu() {
        return waktu;
    }

    public void setWaktu(String waktu) {
        this.waktu = waktu;
    }

    public String getShalat() {
        return shalat;
    }

    public void setShalat(String shalat) {
        this.shalat = shalat;
    }
}
