package util;

import java.util.List;

public class RouteTable {
    private List<Routing> routingList;
    public void AddToTable(Routing routing){
        routingList.add(routing);
    }
    public List<Routing> GetRoutingTable(){
        return routingList;
    }

    public String GetNextNodeAddress(String destination){

        return findRouting(destination).GetNextNode();
    }
    private Routing findRouting(String destination) {
        for(Routing routing : routingList) {
            if(routing.GetDestination().equals(destination)) {
                return routing;
            }
        }
        return null;
    }

}
