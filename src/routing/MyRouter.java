package routing;
import core.Connection;
import core.Settings;

import java.util.List;

public class MyRouter extends ActiveRouter {

    private int nrOfHopsToPropagateRREQ;
    private List<Connection> currentConnections; //test variable

    public MyRouter(Settings r) {
        super(r);
    }

    protected MyRouter(MyRouter r) {
        super(r);
        //TODO: copy epidemic settings here (if any)
    }

    @Override
    public void update() {
        super.update();
        if (isTransferring() || !canStartTransfer()) {
            return; // transferring, don't try other connections yet
        }

        // Try first the messages that can be delivered to final recipient
        if (exchangeDeliverableMessages() != null) {
            return; // started a transfer, don't try others (yet)
        }
        this.getConnections();
        // then try any/all message to any/all connection
        this.tryAllMessagesToAllConnections();
    }


    @Override
    public void changedConnection(Connection con) {


    }

    @Override
    public MyRouter replicate() {
        return new MyRouter(this);
    }
}
