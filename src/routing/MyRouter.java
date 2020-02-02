package routing;
import core.*;
import util.RREQ;
import util.RoutingTable;
import util.RoutingTableEntryy;
import java.util.logging.Logger;

import java.util.*;

public class MyRouter extends ActiveRouter {

    private int nrOfHopsToPropagateRREQ;
    private List<Connection> currentConnections; //test variable
    private RoutingTable routingTable;
    public MyRouter(Settings r) {
        super(r);
    }
    private static final long serialVersionUID=1L;
    public static Logger logger=Logger.getLogger("global");
    protected MyRouter(MyRouter r) {
        super(r);
        this.routingTable = new RoutingTable();
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
        //this.getConnections();
        // then try any/all message to any/all connection
        this.tryAllRREQMessagesToAllConnections();
        super.exchangeDeliverableMessages();
    }


    private void sendNewRREQMessage(int destination){
        RREQ rreq = new RREQ(destination, getHost().getAddress(),15);
        List<Connection> connections = getConnections();
        //return tryMessagesToConnections(RREQmessages, connections);
    }

    protected Connection tryAllRREQMessagesToAllConnections(){
        List<Connection> connections = getConnections();
        if (connections.size() == 0 || this.getNrofMessages() == 0) {
            return null;
        }

        List<Message> messages =
                new ArrayList<Message>(this.getMessageCollection());
        List<Message> RREQmessages =
                new ArrayList<Message>(this.getOnlyRREQMessages(messages));

        return tryMessagesToConnections(RREQmessages, connections);
    }

    @Override
    public void changedConnection(Connection con) {
        /**
         Funkcja wywoływana po zmianie statusu połaczenia pomiędzy node - dodanie do tablicy routingu node lub usunięcie z niego.
         */
        int otherNode = con.getOtherNode(getHost()).getAddress();
        if(con.isUp()) { //new connection


            if (!routingTable.nodeIsInRoutingTable(otherNode)) { //sprawdzanie czy routing nie jest już w tablicy
                logger.info("Node " + otherNode + "  jeszcze nie byl w tablicy " + getHost().getAddress());
                RoutingTableEntryy routingTableEntryy = new RoutingTableEntryy(otherNode, otherNode);
                routingTable.AddToTable(routingTableEntryy);
            }
            logger.info("Polaczono node " + otherNode + " i " + getHost().getAddress() + '/' + getHost());
            //logger.info("Tablica node " + routingTable.GetRoutingTable());
        }
        else{ //disconnection
            routingTable.RemoveFromRoutingTable(otherNode); //if disconnected remove from routing table.
            logger.info("Disconnected and removed node " + otherNode);

        }
    }

    protected List<Message> getOnlyRREQMessages(List<Message> msgList) {
        /** Zwraca tylko wiadomości o typie RREQ */
        List<Message> list = new ArrayList<>();
        for (Message msg : msgList) {
            if(msg.isRREQ()){
                list.add(msg);
            }
        }
        return list;
    }

    @Override
    public MyRouter replicate() {
        return new MyRouter(this);
    }
}
