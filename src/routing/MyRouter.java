package routing;
import core.*;
import util.*;

import java.util.logging.Logger;

import java.util.*;

public class MyRouter extends ActiveRouter {


    private RoutingTable routingTable;
    private RREQList listOfReceivedRREQIds;
    private boolean once = false; //debug only
    private int a = 0; //debug only
    public MyRouter(Settings r) {
        super(r);
    }


    private static final long serialVersionUID=1L;
    public static Logger logger=Logger.getLogger("global");
    protected MyRouter(MyRouter r) {
        super(r);
        r.deleteDelivered = true;
        this.routingTable = new RoutingTable();
        this.listOfReceivedRREQIds = new RREQList();
    }

    @Override
    public void update() {
        super.update();
        if (isTransferring() || !canStartTransfer()) {
            return; // transferring, don't try other connections yet
        }

        if (exchangeDeliverableMessages() != null) {
            return; // started a transfer, don't try others (yet)
        }
        //this.getConnections();
        // then try any/all message to any/all connection
        //this.tryAllRREQMessagesToAllConnections();
//        super.tryAllMessages()
        super.exchangeDeliverableMessages();
        this.tryAllMessagesUsingRoutingTable();
    }

    private void tryAllMessagesUsingRoutingTable(){
        List<Connection> connections = getConnections();
        List<Message> messages =
                new ArrayList<Message>(this.getMessageCollection());
        for(Message m : messages){
            if(routingTable.nodeIsInRoutingTable(m.getTo().getAddress())){ //we have routing to destination
                int nextHop = routingTable.GetNextNodeAddress(m.getTo().getAddress());
                for(Connection con: connections){
                    if(con.getOtherNode(getHost()).getAddress() == nextHop){
                        int retVal = startTransfer(m, con);
                        if (retVal == RCV_OK) {
                            logger.info("UDALO SIE");
                        }
                        else if (retVal == -1) {
                            //message delivered
                            m=null;
                            //break;   //return null; // should try later -> don't bother trying others
                        }
                    }
                }
            }
            else {// we don't have routing to destination
                this.sendNewRREQMessage(m.getTo().getAddress());
            }
        }
//        logger.severe("I am " + getHost().getAddress() + " and my messages are: " + messages.get(0).getTo());
    }

    private void sendNewRREQMessage(int destination){
        RREQ rreq = new RREQ(destination, getHost().getAddress(),10);
        this.sendRREQMessages(rreq);
    }

    private void sendRREQMessages(RREQ rreq){
        /**
            Function sending one RREQ to all connected nodes.
         */
        if(rreq.canHop()){ //Checking if TTL is still valid
            List<Connection> connections = getConnections();
            for (Connection c : connections) {
                MyRouter router = (MyRouter) c.getOtherNode(getHost()).getRouter(); //Acces to MyRouter Class on the other side of connection

                //check if rreq that we are sending is not from router and any other previous node
                boolean canTransmit = true;
                for(int nodeId : rreq.getPreviousNodes()){
                    if(nodeId == router.getHost().getAddress()){
                        canTransmit = false;
                    }
                }
                if(!listOfReceivedRREQIds.RREQIsInTable(rreq.getID(),router.getHost().getAddress()) && canTransmit) {
                    RREP rrep = router.processRREQ(getHost(), rreq);
                    if (rrep != null) { //Node returned RREP frame
                        logger.severe("I am " + getHost().getAddress() + " and I got RREP from  " + rrep.getSource() +
                                " and next node to : " + rrep.getSource() + " is " + rrep.DEBUG_printNodes());
                        RoutingTableEntryy rteTemp = new RoutingTableEntryy(rrep.getSource(),rrep.getFirstNode());
                        if(!routingTable.nodeIsInRoutingTable(rteTemp.GetDestination())) {
                            routingTable.AddToTable(rteTemp);
                        }

                    }
                }

            }
        }
        else{
            rreq=null; //remove rreq because TTL  == 0
            logger.info("RREQ Destroyed because TTL == 0, node: " + getHost().getAddress());
        }
    }
    protected RREP processRREQ(DTNHost source, RREQ rreq){
        //check if rreq with this ID was added to the table previously
        if(!listOfReceivedRREQIds.RREQIsInTable(rreq)){
            //if not add this RREQ to list
            listOfReceivedRREQIds.AddToTable(rreq);
            //add all nodes from rreq to routing table
            for(int rreqt: rreq.getPreviousNodes()){
                if(!routingTable.nodeIsInRoutingTable(rreqt))
                {
                    RoutingTableEntryy rte1 = new RoutingTableEntryy(rreqt,rreq.getPreviousNode());
                    routingTable.AddToTable(rte1);
                }

            }
            //check if this node has route to destination
            RoutingTableEntryy nextNode = routingTable.GetNextNodeEntry(rreq.getDestination());
            if(nextNode != null) //node has route to destination
            {
                logger.warning("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX I am " +getHost().getAddress() + " and I have a route to: "+rreq.getDestination());
                RREP rrep = new RREP(rreq.getSource(),rreq.getDestination(),rreq.getHopCount(),rreq.getPreviousNodes());
                rrep.addToPreviousNodes(getHost().getAddress());
                return rrep;
            }
            else{ //node doesn't have route to destination
                //spam RREQ to all connected nodes
                logger.info("I am " +getHost().getAddress() + " and I DONT have a route to: "+rreq.getDestination());
                rreq.update(getHost().getAddress());
                this.sendRREQMessages(rreq);
                return null;
            }
        }
        return null;
    }

    @Override
    public void changedConnection(Connection con) {
        /**
         Funkcja wywoływana po zmianie statusu połaczenia pomiędzy node - dodanie do tablicy routingu node lub usunięcie z niego.
         */
        int otherNode = con.getOtherNode(getHost()).getAddress();
        if(con.isUp()) { //new connection


            if (!routingTable.nodeIsInRoutingTable(otherNode)) { //sprawdzanie czy routing nie jest już w tablicy
//                logger.info("Node " + otherNode + "  jeszcze nie byl w tablicy " + getHost().getAddress());
                RoutingTableEntryy routingTableEntryy = new RoutingTableEntryy(otherNode, otherNode);
                routingTable.AddToTable(routingTableEntryy);
                if(a>45 && !once){ //debug only
                    //this.sendNewRREQMessage(356);
                    once = true;
                }


            }
            a++;
            //logger.info("Polaczono node " + otherNode + " i " + getHost().getAddress() + '/' + getHost());
            //logger.info("Tablica node " + routingTable.GetRoutingTable());
        }
        else{ //disconnection
            routingTable.RemoveFromRoutingTable(otherNode); //if disconnected remove from routing table.
            //logger.info("Disconnected and removed node " + otherNode);

        }
    }

    @Override
    public MyRouter replicate() {
        return new MyRouter(this);
    }
}
