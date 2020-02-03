package util;

import java.util.List;

public class RREP {

    private int destination;
    private int source;
    private List<Integer> previousNodes;
    private int ID;
    private int hopCount;
    private int currentHop; //x-source, 0-destination
    private int TTL;
    public RREP(int destination, int source, int TTL, List<Integer> previousNodes) {
        this.setSource(source);
        this.setDestination(destination);
        this.setCurrentHop(TTL);
        this.ID= (int)(Math.random() * (10000));
        this.previousNodes = previousNodes;
    }
    public void update(int currentNode){
        this.addToPreviousNodes(currentNode);
        this.updateTTL();
        this.updateHopCount();
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public void setTTL(int ttl) {
        this.TTL = ttl;
    }

    private void updateTTL(){
        this.setTTL(this.getTTL()-1);
    }

    private void updateHopCount(){
        this.setHopCount(this.getHopCount()+1);
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public int getHopCount() {  return hopCount;}

    public int getID() {
        return ID;
    }

    public int getPreviousNode(){
        return previousNodes.get(previousNodes.size() - 1); //get last element of previous nodes list
    }

    public List<Integer> getPreviousNodes() {
        return previousNodes;
    }

    public void addToPreviousNodes(int nodeId ) {
        this.previousNodes.add(nodeId);
    }

    public int getTTL() {
        return TTL;
    }

    public int getCurrentHop() {
        return currentHop;
    }
    public int setCurrentHop(int x) {
        return this.currentHop = x;
    }


}
