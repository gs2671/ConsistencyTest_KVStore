package consistency;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualLinkedHashBidiMap;
import org.apache.commons.collections4.bidimap.DualTreeBidiMap;
import org.json.simple.JSONObject;

import java.util.*;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
class SetNotFoundException extends Exception
{
    //Parameterless Constructor
    public SetNotFoundException() {}

    //Constructor that accepts a message
    public SetNotFoundException(String message)
    {
        super(message);
    }
}

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
public class ConsistencyChecker
{
    public static Map<String,Set<String>> createGraph(Queue<JSONObject> jsonObjects) throws SetNotFoundException
    {

        Map<String,Set<String>> adjList=new HashMap<>();
        BidiMap<Integer,String> A=new DualTreeBidiMap<>();
        BidiMap<Integer,String> temp=new DualTreeBidiMap<>();
        BidiMap<Integer,String> B=new DualLinkedHashBidiMap<>();
        TreeMap<String,String> opsMap=new TreeMap<>();
        TreeMap<String,String> valMap=new TreeMap<>();
        TreeMap<String,String> readWriteMap=new TreeMap<>();

        //Maintain the read count
        int readCount=0;


        //For each json object, create an key in the adjacency list
        for(JSONObject obj:jsonObjects)
        {
            adjList.put(obj.get("Id").toString(),new TreeSet<String>());
            A.put(Integer.parseInt(obj.get("Start").toString()), obj.get("Id").toString());
            temp.put(Integer.parseInt(obj.get("End").toString()), obj.get("Id").toString());
            if(obj.get("Operation").toString().equals("GET"))
                readCount++;
            opsMap.put(obj.get("Id").toString(),obj.get("Operation").toString());
            valMap.put(obj.get("Id").toString(),obj.get("Value").toString());
        }
        List<Integer> endTimes=new ArrayList<>();
        endTimes.addAll(temp.keySet());
        Collections.reverse(endTimes);
        for (int et:endTimes)
        {
            B.put(et,temp.get(et));
        }

        //Adding Time edges
        for(int x:A.keySet())
        {
            int time=Integer.MIN_VALUE;
            for(int y:B.keySet())
            {
                if(y<x)
                {
                    if(time<y)
                    {
                        adjList.get(B.get(y)).add(A.get(x));
                        time=Math.max(time, A.getKey(B.get(y)));
                    }
                    else
                        break;
                }
            }
        }

        //Adding Data Edges
        for(String id1:valMap.keySet())
        {
            for(String id2:valMap.keySet())
            {
                if(valMap.get(id2).equals(valMap.get(id1)) && opsMap.get(id2).equals("SET") && opsMap.get(id1).equals("GET"))
                {
                    adjList.get(id2).add(id1);
                    readWriteMap.put(id1,id2);
                }
            }
        }

        //Check if the each read has a write. If not the server is not consistent
        if(readWriteMap.size()!=readCount)
            throw new SetNotFoundException();

        //Adding Hybrid Edges
        Map<String,Set<String>> adjListCopy=new HashMap<>();
        adjListCopy.putAll(adjList);
        for(String id1:opsMap.keySet())
        {
            if(opsMap.get(id1).equals("SET"))
            {
                for(String id2:opsMap.keySet())
                {
                    if(opsMap.get(id2).equals("GET"))
                    {
                        if(!valMap.get(id1).equals(valMap.get(id2)))
                        {
                            if(CheckPath(id1,id2,adjListCopy))
                            {
                                //String val=valMap.get(id2);
                                if(readWriteMap.containsKey(id2))
                                {
                                    String dWrite=readWriteMap.get(id2);
                                    adjList.get(id1).add(dWrite);
                                }
                                else
                                    throw new SetNotFoundException();
                            }
                        }
                    }
                }
            }
        }
        return adjList;
    }

    public static boolean CheckPath(String id1,String id2,Map<String,Set<String>> adjList)
    {
        if(id1.equals(id2))
            return true;

        Map<String,Boolean> visited=new HashMap<>();
        for(String s:adjList.keySet())
            visited.put(s, Boolean.FALSE);

        Queue<String> q=new LinkedList<>();

        q.add(id1);
        visited.replace(id1, Boolean.TRUE);

        while(!q.isEmpty())
        {
            String node=q.poll();
            for(String child:adjList.get(node))
            {
                if(child.equals(id2))
                    return true;
                if(visited.get(child)==Boolean.FALSE)
                {
                    visited.replace(child, Boolean.TRUE);
                    q.add(child);
                }
            }
        }
        return false;
    }

    public static boolean DFSVisit(Map<String,Set<String>> adjList,Map<String,Character> color,String id)
    {
        //Mark the current vertex visited
        color.replace(id,'g');
        for(String child:adjList.get(id))
        {
            //Check for Back edge. Back edges mean that its a cycle.
            if(color.get(child)=='g')
                return true;
            else if(color.get(child)=='w')
            {
                if(DFSVisit(adjList,color,child))
                    return true;
            }
        }
        color.replace(id,'b');
        return false;
    }

    public static boolean CheckCycle(Map<String,Set<String>> adjList)
    {
        Map<String,Character> color=new HashMap<>();
        boolean isCycle=false;

        //Assume all nodes are unvisited
        for(String id:adjList.keySet())
            color.put(id,'w');

        //Check cycle for each vertex in the adjacency matrix
        for(String id:adjList.keySet())
        {
            if(color.get(id)=='w')
            {
                isCycle=DFSVisit(adjList,color,id);
                if(isCycle)
                    return true;
            }
        }
        return false;
    }

}
