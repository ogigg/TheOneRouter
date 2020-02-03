package routing;
import core.*;
import util.RREP;
import util.RREQ;
import util.RoutingTable;
import util.RoutingTableEntryy;
import java.util.logging.Logger;

import java.util.*;

public class MyRouter extends ActiveRouter {


    private RoutingTable routingTable;
    private List<Integer> listOfReceivedRREQIds; //test variable
    public MyRouter(Settings r) {
        super(r);
    }


    private static final long serialVersionUID=1L;
    public static Logger logger=Logger.getLogger("global");
    protected MyRouter(MyRouter r) {
        super(r);
        this.routingTable = new RoutingTable();
        this.listOfReceivedRREQIds = new ArrayList<>();
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
        //this.tryAllRREQMessagesToAllConnections();

        super.exchangeDeliverableMessages();
    }


    private void sendNewRREQMessage(int destination){
        RREQ rreq = new RREQ(destination, getHost().getAddress(),15);
        this.sendRREQMessages(rreq);
        logger.fine("sendNewRREQMessage:" + getHost().getAddress());
        //return tryMessagesToConnections(RREQmessages, connections);
    }

    private void sendRREPMessages(RREP rrep){
        /**
         Function sending one RREP to previous node
         */
        logger.warning("I am " +getHost().getAddress() + " and I am sending RREP to : "+rrep.getDestination() + "through : "+rrep.getPreviousNode());
        List<Connection> connections = getConnections();
        for( Connection c : connections) {
            if(c.getOtherNode(getHost()).getAddress() == rrep.getPreviousNode()){
                MyRouter router = (MyRouter) c.getOtherNode(getHost()).getRouter(); //Acces to MyRouter Class on the other side of connection
                router.processRREP(rrep);
            }
        }
    }

    private void processRREP(RREP rrep){
        rrep.update(rrep.getPreviousNode());
    }

    private void sendRREQMessages(RREQ rreq){
        /**
            Function sending one RREQ to all connected nodes.
         */
        List<Connection> connections = getConnections();
        for( Connection c : connections) {
            MyRouter router = (MyRouter) c.getOtherNode(getHost()).getRouter(); //Acces to MyRouter Class on the other side of connection
//            if(!router.routingTable.nodeIsInRoutingTable(rreq.getDestination())){ //If there is no routing entry in other node
//
//            }
            RREP rrep = router.processRREQ(getHost(),rreq);
            if (rrep != null){ //Node returned RREP frame
                //TODO sprawdzic czy to jest dobrze
                logger.severe("Jestem " +getHost().getAddress() + " i dodałem  "+rrep.getSource() + " i nastepny nod to : "+rrep.getPreviousNode() );
                RoutingTableEntryy rteTemp = new RoutingTableEntryy(rrep.getSource(),rrep.getSource());
                routingTable.AddToTable(rteTemp);
                this.sendRREPMessages(rrep);
                //TODO


            }
        }


        //return tryMessagesToConnections(RREQmessages, connections);
    }
    protected RREP processRREQ(DTNHost source, RREQ rreq){
        //check if rreq with this ID was here previously
        if(!listOfReceivedRREQIds.contains(rreq.getID())){
            //if not add ID of this RREQ to list
            listOfReceivedRREQIds.add(rreq.getID());
            //add to routing table entry with destination of RREQ and previous node
            RoutingTableEntryy rte1 = new RoutingTableEntryy(rreq.getSource(),rreq.getPreviousNode());
            routingTable.AddToTable(rte1);
            //check if this node has route to destination
            RoutingTableEntryy nextNode = routingTable.GetNextNodeEntry(rreq.getDestination());
            if(nextNode != null) //node has route to destination
            {
                logger.warning("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX I am " +getHost().getAddress() + " and I have a route to: "+rreq.getDestination());
                //TODO send RREP here
                RREP rrep = new RREP(rreq.getSource(),getHost().getAddress(),rreq.getHopCount(),rreq.getPreviousNodes());
                return rrep;
            }
            else{ //node doesn't have route to destination
                //spam RREQ to all connected nodes
                logger.info("I am " +getHost().getAddress() + " and I DONT have a route to: "+rreq.getDestination());
                rreq.update(getHost().getAddress());
                this.sendRREQMessages(rreq);
            }
        }
        return null;
    }
//    protected RREQ tryAllRREQMessages(Connection con, RREQ rreq) {
//            int retVal = startTransfer(rreq, con);
//            if (retVal == RCV_OK) {
//                return rreq;	// accepted a message, don't try others
//            }
//            else if (retVal > 0) {
//                return null; // should try later -> don't bother trying others
//            }
//
//        return null; // no message was accepted
//    }
//





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
                //logger.info("Node " + otherNode + "  jeszcze nie byl w tablicy " + getHost().getAddress());
                RoutingTableEntryy routingTableEntryy = new RoutingTableEntryy(otherNode, otherNode);
                routingTable.AddToTable(routingTableEntryy);
                this.sendNewRREQMessage(356);
            }
            //logger.info("Polaczono node " + otherNode + " i " + getHost().getAddress() + '/' + getHost());
            //logger.info("Tablica node " + routingTable.GetRoutingTable());
        }
        else{ //disconnection
            routingTable.RemoveFromRoutingTable(otherNode); //if disconnected remove from routing table.
            //logger.info("Disconnected and removed node " + otherNode);

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
