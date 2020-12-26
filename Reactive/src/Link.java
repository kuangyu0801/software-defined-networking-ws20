public class Link {
    DatapathId src;
    DatapathId dst;

    public Link(int src, int dst) {
        this.src = new DatapathId(src);
        this.dst = new DatapathId(dst);
    }

    public DatapathId getSrc() {
        return src;
    }

    public DatapathId getDst() {
        return dst;
    }
}
