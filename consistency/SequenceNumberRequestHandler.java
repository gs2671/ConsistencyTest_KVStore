package consistency;

import org.apache.thrift.TException;

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
public class SequenceNumberRequestHandler implements SequenceService.Iface
{
    private long seqNo;
    private long interval;
    public SequenceNumberRequestHandler()
    {
        seqNo=0;
        interval=1;
    }

    @Override
    public long getSequenceNumber() throws TException 
    {
        try
        {
            seqNo=seqNo+interval;
            return seqNo;
        }
        catch(Exception ex)
        {
            return -1;
        }
    }
}
