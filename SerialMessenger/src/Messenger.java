// $Id: SFListen.java,v 1.5 2010-06-29 22:07:41 scipio Exp $

/*									tab:4
 * Copyright (c) 2000-2003 The Regents of the University  of California.  
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 * - Neither the name of the University of California nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright (c) 2002-2003 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */


/**
 * File: ListenServer.java
 *
 * Description:
 * The Listen Server is the heart of the serial forwarder.  Upon
 * instantiation, this class spawns the SerialPortReader and the
 * Multicast threads.  As clients connect, this class spawns
 * ServerReceivingThreads as wells as registers the new connection
 * SerialPortReader.  This class also provides the central
 * point of contact for the GUI, allowing the server to easily
 * be shut down
 *
 * @author <a href="mailto:bwhull@sourceforge.net">Bret Hull</a>
 * @author <a href="mailto:dgay@intel-research.net">David Gay</a>
 */


import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import net.tinyos.message.MoteIF;
import net.tinyos.message.Sender;
import net.tinyos.packet.*;

public class Messenger extends Thread implements PacketListenerIF, PhoenixError {
    PhoenixSource source;
    private String motecom;
    //private BlockingQueue<TestSerialMsg> bq;
    private Sender sender;
    public Messenger(String motecom) {
    	this.motecom = motecom;
    }

    
    public void error(IOException e) {
		if (e.getMessage() != null) {
		    //System.out.println(e.getMessage());
		}
		//System.out.println(source.getPacketSource().getName() + " died - restarting");
		try {
		    sleep(500);
		}
		catch (InterruptedException ie) { 
			System.err.println(ie);
		}
		
    }

    public void run() {
	    System.out.println("Listening to " + this.motecom);

	    source = BuildSource.makePhoenix(this.motecom, null);
	    if (source == null) {
	    	System.out.println("Invalid source " + this.motecom + ", pick one of:");
	    	System.out.println(BuildSource.sourceHelp());
	    	return;
	    }
	    sender = new Sender(source);
	    source.setPacketErrorHandler(this);
	    //source.registerPacketListener(this);
	    source.start();
	       
    }

	@Override
	public void packetReceived(byte[] packet) {
		SerialMsg sp = new SerialMsg(packet);
		 String out = "";
		   for (int i = 0; i < sp.dataGet().length;i++)
			   out += sp.dataGet()[i]+" ";
		   System.out.println(out);
		System.out.println(sp);
		//bq.add(sp);
		
	}
   public void sendPacket(byte []packet) throws IOException{
	   SerialMsg msg = new SerialMsg(packet);
	   String out = "";
	   for (int i = 0; i < msg.dataGet().length;i++)
		   out += msg.dataGet()[i]+" ";
	   System.out.println(out);
	   //sender.send(0, msg);
	   MoteIF mif = new MoteIF(source);
	   mif.send(0, msg);
   }

}
