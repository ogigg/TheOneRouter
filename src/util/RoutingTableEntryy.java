package util;

public class RoutingTableEntryy {
    public void setDestination(int destination) {
        this.destination = destination;
    }

    public void setNextNode(int nextNode) {
        this.nextNode = nextNode;
    }

    int destination;
    int nextNode;
    public int GetDestination(){
        return destination;
    }
    public int GetNextNode(){
        return nextNode;
    }
    public RoutingTableEntryy(int destination, int nextNode) {
        this.setNextNode(nextNode);
        this.setDestination(destination);

    }

}
