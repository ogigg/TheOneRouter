package util;

public class RREQListEntity {
    private int RREQID;
    private int receivedFrom;


    public RREQListEntity(int rreqid, int receivedFrom) {
        RREQID = rreqid;

        this.receivedFrom = receivedFrom;
    }

    public int getRREQID() {
        return RREQID;
    }

    public int getReceivedFrom() {
        return receivedFrom;
    }
}

