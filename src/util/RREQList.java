package util;


import java.util.ArrayList;
import java.util.List;

public class RREQList {
    private List<RREQListEntity> rreqList;


    public RREQList() {
        this.rreqList = new ArrayList<RREQListEntity>();
    }

    public void AddToTable(RREQ rreq) {
        RREQListEntity rreqListEntity = new RREQListEntity(rreq.getID(),rreq.getPreviousNode());
        rreqList.add(rreqListEntity);
    }

    private RREQListEntity findRREQ(int rreqID) {
        for (RREQListEntity rreq : this.rreqList) {
            if (rreq.getRREQID() == (rreqID)) {
                return rreq;
            }
        }
        return null;
    }

    public boolean RREQIsInTable(RREQ rreq) {
        for (RREQListEntity rreqListEntity : rreqList) {
            if (rreqListEntity.getRREQID() == rreq.getID() && rreqListEntity.getReceivedFrom() == rreq.getPreviousNode()) {
                return true;
            }
        }
        return false;
    }
    public boolean RREQIsInTable(int rreqId, int rreqSource) {
        for (RREQListEntity rreqListEntity : rreqList) {
            if (rreqListEntity.getRREQID() == rreqId && rreqListEntity.getReceivedFrom() == rreqSource) {
                return true;
            }
        }
        return false;
    }
}
