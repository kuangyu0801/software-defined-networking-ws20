public class Link {
    DatapathId src;
    DatapathId dst;

    public Link(int src, int dst) {
        this.src = new DatapathId(src);
        this.dst = new DatapathId(dst);
    }

    public Link(DatapathId dpid, DatapathId dpid1) {
        this.src = dpid;
        this.dst = dpid1;
    }

    public DatapathId getSrc() {
        return src;
    }

    public DatapathId getDst() {
        return dst;
    }
}
