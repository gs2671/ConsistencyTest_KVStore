/**
 * Created by Ankur on 12/3/2016.
 */
package consistency;

import org.apache.thrift.TException;
import org.apache.thrift.transport.*;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TTransport;
import org.json.simple.JSONObject;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
public class KVClient {
    private static final int CONCURRENCY_FACTOR = 20;
    private static final Queue<JSONObject> log = new ConcurrentLinkedQueue<JSONObject>();
    private static AtomicLong UNIQUE_WRITE = new AtomicLong(0L);
    private static AtomicLong OPERATION_ID = new AtomicLong(0L);
    private final ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY_FACTOR);
    private static final String SEQUENCE_SERVER_HOST_IP = "localhost";
    private static final int SEQUENCE_SERVER_PORT_NUMBER = 5432;

    public static void main(String[] args) {
        final String[] server_port = args[1].split(":");
        final KVClient kvclient = new KVClient();
        try {
            kvclient.createConcurrentReadAndWrite(server_port[0], Integer.parseInt(server_port[1]), kvclient);
        } catch (ExecutionException e) {
            System.exit(2);
        } catch (InterruptedException e) {
            System.exit(2);
        } catch (TTransportException e) {
           System.exit(2);
        } catch (SetNotFoundException e) {
            System.exit(1);
        } catch (TException e) {
            System.exit(2);
        }
    }

    private void createConcurrentReadAndWrite(final String server,final int portNumber,final KVClient kvclient) throws ExecutionException, InterruptedException, TException, SetNotFoundException {
        Random random = new Random();
        Runnable task = process(server, portNumber,random );
        GetMeFirstSetOperation(server, portNumber );
        for(int i=0;i<1000;i++) {
            executor.execute(task);
        }
        stopExecutiveService();
        while(!executor.isTerminated()){

        }
        Map<String, Set<String>> adjList = null;
        adjList = ConsistencyChecker.createGraph(kvclient.log);
        if(ConsistencyChecker.CheckCycle(adjList)){
            System.exit(1);
        }
        else{
            System.exit(0);
        }
    }

    private void GetMeFirstSetOperation(String server,int portNumber) throws TException {
        final TTransport transport = new TSocket(server, portNumber);
        final TProtocol protocol = new TBinaryProtocol(transport);
        final KVStore.Client client = new KVStore.Client(protocol);
        transport.open();
        KVClient.log.add(setValue(client));
        transport.close();
    }

    private  Runnable process(String server,int portNumber, Random random)  {
        Runnable task = new Runnable() {
            @Override
            public void run()   {
                final TTransport transport = new TSocket(server, portNumber);
                final TProtocol protocol = new TBinaryProtocol(transport);
                final KVStore.Client client = new KVStore.Client(protocol);
                try {
                    transport.open();
                } catch (TTransportException e) {
                    System.exit(2);
                }
                final int value = random.nextInt(10);
                    if (value % 2 == 0) {
                        try {
                            KVClient.log.add(setValue(client));
                        } catch (TException e) {
                            System.exit(2);
                        }
                    } else {
                        try {
                            KVClient.log.add(getValue(client));
                        } catch (TException e) {
                            System.exit(2);
                        }
                    }
                transport.close();
            }
        };
        return task;
    }

    private void stopExecutiveService() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    private   JSONObject getValue(KVStore.Client client) throws TTransportException, TException {
        String key = "X", value;
        long startSequenceNumber=0, endSequenceNumber=0;
        JSONObject json = new JSONObject();
        startSequenceNumber = getSequenceNumber();
        Result res = new Result();
        res = client.kvget(key);
        if (res.error == ErrorCode.kSuccess) {
            endSequenceNumber = getSequenceNumber();
            OPERATION_ID.incrementAndGet();
            json.put("Id", OPERATION_ID.toString());
            json.put("Operation", "GET");
            json.put("Key", key);
            json.put("Value", res.getValue());
            json.put("Start", startSequenceNumber);
            json.put("End", endSequenceNumber);
        } else {
            System.exit(2);
        }
        return json;
    }

    private  JSONObject setValue(KVStore.Client client) throws TTransportException, TException {
        String key = "X", value;
        long startSequenceNumber=0, endSequenceNumber=0;
        JSONObject json = new JSONObject();
        startSequenceNumber = getSequenceNumber();
        UNIQUE_WRITE.incrementAndGet();
        value = UNIQUE_WRITE.toString();
        Result res = new Result();
        res = client.kvset(key, value);
        if (res.error == ErrorCode.kSuccess) {
            endSequenceNumber = getSequenceNumber();
            OPERATION_ID.incrementAndGet();
            json.put("Id", OPERATION_ID.toString());
            json.put("Operation", "SET");
            json.put("Key", key);
            json.put("Value", res.getValue());
            json.put("Start", startSequenceNumber);
            json.put("End", endSequenceNumber);
        } else {
            System.exit(2);
        }
        return  json;
    }

    private long getSequenceNumber() throws TTransportException, TException {
        final TTransport seqtransport = new TSocket(SEQUENCE_SERVER_HOST_IP, SEQUENCE_SERVER_PORT_NUMBER);
        final TProtocol seqprotocol = new TBinaryProtocol(seqtransport);
        final SequenceService.Client seqclient = new SequenceService.Client(seqprotocol);
        seqtransport.open();
        long num = seqclient.getSequenceNumber();
        seqtransport.close();
        if(num<1){
        	System.exit(2);
        }
        return num;
    }

}
