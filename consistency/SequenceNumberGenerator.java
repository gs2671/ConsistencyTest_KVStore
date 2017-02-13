package consistency;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author guru316
 */
@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
public class SequenceNumberGenerator
{
    private static final int Port_Number= 5432;
    public static void main(String[] args)
    {
        try
        {
            SequenceNumberRequestHandler handler=new SequenceNumberRequestHandler();
            SequenceService.Processor processor=new SequenceService.Processor(handler);
            TServerTransport serverTransport=new TServerSocket(Port_Number);
            TServer server=new TSimpleServer(new TServer.Args(serverTransport).processor(processor));
            System.out.printf("Starting the Sequence Server %d", Port_Number);
            server.serve();
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }
}
