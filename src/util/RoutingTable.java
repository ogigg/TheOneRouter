package util;

import core.Message;
import routing.MyRouter;

import java.util.ArrayList;
import java.util.List;

public class RoutingTable {
    private List<RoutingTableEntryy> routingList;


    public RoutingTable() {
        this.routingList =  new ArrayList<RoutingTableEntryy>();
        //TODO: copy epidemic settings here (if any)
    }

    public int GetNextNodeAddress(int destination){

        return findRouting(destination).GetNextNode();
    }

    public void AddToTable(RoutingTableEntryy routingTableEntryy){
        routingList.add(routingTableEntryy);
    }

    public List<RoutingTableEntryy> GetRoutingTable(){
        return routingList;
    }



    private RoutingTableEntryy findRouting(int destination) {
        for(RoutingTableEntryy routing : this.routingList) {
            if(routing.GetDestination() == (destination)) {
                return routing;
            }
        }
        return null;
    }
    public boolean nodeIsInRoutingTable(int nodeIde){
        for(RoutingTableEntryy routing : routingList) {
            if(routing.GetDestination() == nodeIde) {
                return true;
            }
        }
        return false;
    }

    public void setRoutingList(List<RoutingTableEntryy> routingList) {
        this.routingList = routingList;
    }
    public void RemoveFromRoutingTable(int nodeId) {
        this.routingList.remove(Integer.valueOf(nodeId)); //Removing of node with ID nodeId
    }
}
