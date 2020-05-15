package arbina.app.template.store;

public enum AclRight {

    r, w;

    public boolean has(AclRight a) {
        return this.equals(a) || this.equals(w) && a.equals(r);
    }
}