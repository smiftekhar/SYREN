import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class SerialMessenger {

	public static void main(String[] args) {
		if(args.length != 1){
		  System.out.println("Usage: java SerialMessenger </dev/ttyUSB0>");
		  System.exit(0);
		 }
		Commander c = new Commander(args[0]);
		//ControlledCommander c = new ControlledCommander();
		
		//DataAnalyzer analyzer = new DataAnalyzer();
		//analyzer.analyze();
		
		/*analyzer.extractCorrelation("/root/Projects/SerialMessenger/Data/Correlation/HomeTx=1Grid=3x3Turn/");
		
		if(args[0].contains("Deluge"))
			analyzer.extractDelugeStats(args[0]);
		else
			analyzer.extractSimCorrelation(args[0]);
		
		//analyzer.testCorrelation();
		*/
	}

}
class Commander implements ActionListener{
	JTextField nodeIdField;
	JButton startButton;
	JButton stopButton;
	JButton reportButton;
	JButton reportLocalButton;
	JButton eraseLogButton;
	JButton runTurnButton;
	JButton runConcurrentButton;
	String nodeId;
	Messenger messenger;
	Reporter reporter;
	ReportProcessor processor;
	BlockingQueue<Report> bq = new LinkedBlockingQueue<Report>();
	private byte numberOfReports = 10;
	private byte numberOfPackets = 48;
	private int numberOfNodes = 2;
	private byte transmissionPower = 1;
	private byte threshold = 85;
	private byte probabilityT = 0;
	private byte probabilityR = 60;

	public Commander(String arg){
		messenger = new Messenger("serial@"+arg+":telosb");
		
		//reporter = new Reporter("serial@/dev/ttyUSB1:telosb",bq);
		//processor = new ReportProcessor(bq,107); //107
		
		
		messenger.start();
		//reporter.start();
		
		try {
	      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } catch(Exception e) {
	      System.out.println("Error setting native LAF: " + e);
	    }
	    JFrame f = new JFrame("Command Window");
	    f.setSize(400, 150);
	    Container content = f.getContentPane();
	    content.setBackground(Color.white);
	    content.setLayout(new FlowLayout());
	    JLabel label = new JLabel("Node Id");
	    content.add(label);
	    nodeIdField =new JTextField(10);
	    content.add(nodeIdField);
	    nodeIdField.addActionListener(this);
	    startButton = new JButton("Start"); 
	    startButton.addActionListener(this);
	    content.add(startButton);
	    stopButton = new JButton("Stop");
	    stopButton.addActionListener(this);
	    content.add(stopButton);
	    
	    reportButton = new JButton("Report");
	    reportButton.addActionListener(this);
	    content.add(reportButton);
	    
	    reportLocalButton = new JButton("Report Local");
	    reportLocalButton.addActionListener(this);
	    content.add(reportLocalButton);
	    
	    eraseLogButton = new JButton("Erase Log");
	    eraseLogButton.addActionListener(this);
	    content.add(eraseLogButton);
	    
	    runTurnButton = new JButton("RUN By Turn");
	    runTurnButton.addActionListener(this);
	    content.add(runTurnButton);
	    

	    runConcurrentButton = new JButton("RUN Concurrently");
	    runConcurrentButton.addActionListener(this);
	    content.add(runConcurrentButton);
	    
	    f.addWindowListener(new ExitListener());
	    f.setVisible(true);
	}
	public void test(){
		
		double l = 0.45;
		double cp = 0;
		double th = 0.85;
		int timesCovered = 0;
		int num = 0;
		while(timesCovered < 24){
			System.out.println(cp);
			cp =  1 - (1-cp)*(1-l);
			if(cp > th){
				timesCovered++;
				cp = cp - th;
			}
			num++;
		}
		System.out.println("Number of transmissions "+num);
	}
	public void command(){
		
	}
	byte []makePacket(byte type,String id, byte []args){
		byte packet[] = new byte[8];
		packet[0] = type;
		packet[1] = (byte)(Integer.parseInt(id) & 0xFF);
		packet[2] = (byte)((Integer.parseInt(id) >> 8 )& 0xFF);
		for(int i = 0; i < args.length; i++)
			packet[3+i] = args[i];
		return packet;
	}

	public void runConcurrently(){
		//Erase ALL
		byte packet[];
		byte args[] = new byte[5];
		String id;
		args[0] = transmissionPower;
		args[1] = threshold;
		args[2] = probabilityT;
		args[3] = probabilityR;
		args[4] = numberOfReports;
		
		System.out.println("Erasing all ...");
		id = new Integer(0xFFFF).toString();
		packet = makePacket((byte)54, id, args);
		try{
			messenger.sendPacket(packet);
			Thread.sleep(5000);
		}catch(Exception exc){
			System.err.println(exc);
		}
		
		for (int run = 0; run < numberOfReports; run++){
			//start all
			System.out.println("Starting all for run "+run);
			id = (new Integer(0xFFFF)).toString();
			packet = makePacket((byte)51, id, args);
			try{
				messenger.sendPacket(packet);
				Thread.sleep(60000+40000);
			}catch(Exception exc){
				System.err.println(exc);
			}
			//stop ALL
			System.out.println("Stopping all for run "+run);
			id = (new Integer(0xFFFF)).toString();
			packet = makePacket((byte)49, id, args);
			try{
				messenger.sendPacket(packet);
				Thread.sleep(10000);
			}catch(Exception exc){
				System.err.println(exc);
			}
			
			//dummy ALL
			System.out.println("Stopping all for run "+run);
			id = (new Integer(0xFFFF)).toString();
			packet = makePacket((byte)0, id, args);
			try{
				messenger.sendPacket(packet);
				Thread.sleep(5000);
			}catch(Exception exc){
				System.err.println(exc);
			}
		}
		//dummy ALL
		
		id = (new Integer(0xFFFF)).toString();
		packet = makePacket((byte)0, id, args);
		try{
			messenger.sendPacket(packet);
			Thread.sleep(1000);
		}catch(Exception exc){
			System.err.println(exc);
		}

	}

	public void runByTurn(){
		//Erase ALL
		byte packet[];
		byte args[] = new byte[5];
		String id;
		args[0] = transmissionPower;
		args[1] = threshold;
		args[2] = probabilityT;
		args[3] = probabilityR;
		args[4] = numberOfReports;

		System.out.println("Erasing all ...");
		id = new Integer(0xFFFF).toString();
		packet = makePacket((byte)54, id, args);
		try{
			messenger.sendPacket(packet);
			Thread.sleep(5000);
		}catch(Exception exc){
			System.err.println(exc);
		}
	
		for (int run = 0; run < numberOfReports; run++){
			//start by turn
			
			for (int i = 1; i <= numberOfNodes; i++ ){
				System.out.println("Starting ..."+i+ " for run "+run);
				id = i +"";
				packet = makePacket((byte)51, id, args);
				try{
					messenger.sendPacket(packet);
					Thread.sleep(1000 * numberOfPackets + 5000);
				}catch(Exception exc){
					System.err.println(exc);
				}
			}
			
			//stop ALL
			System.out.println("Stopping all for run "+run);
			id = (new Integer(0xFFFF)).toString();
			packet = makePacket((byte)49, id, args);
			try{
				messenger.sendPacket(packet);
				Thread.sleep(5000);
			}catch(Exception exc){
				System.err.println(exc);
			}
			

			//dummy ALL
			System.out.println("Stopping all for run "+run);
			id = (new Integer(0xFFFF)).toString();
			packet = makePacket((byte)0, id, args);
			try{
				messenger.sendPacket(packet);
				Thread.sleep(5000);
			}catch(Exception exc){
				System.err.println(exc);
			}
		}

	}
	@Override
	public void actionPerformed(ActionEvent e) {
		byte packet[];
		byte args[] = new byte[5];
		args[0] = transmissionPower;
		args[1] = threshold;
		args[2] = probabilityT;
		args[3] = probabilityR;
		args[4] = numberOfReports;

		if(e.getSource() == runTurnButton){
			runByTurn();
		}
		if(e.getSource() == runConcurrentButton){
			runConcurrently();
		}
		if(e.getSource() == nodeIdField){
			nodeId = nodeIdField.getText();
			System.out.println(nodeId);
		}
		if (e.getSource() == reportButton){
			packet = makePacket((byte)52, nodeId, args);
			System.out.println("Start node "+ nodeId + " Packet "+packet[1]+""+packet[2]);
			try{
				messenger.sendPacket(packet);
			}catch(Exception exc){
				System.err.println(exc);
			}
		}
		if (e.getSource() == eraseLogButton){
			packet = makePacket((byte)54, nodeId, args);
			System.out.println("Start node "+ nodeId + " Packet "+packet[1]+""+packet[2]);
			try{
				messenger.sendPacket(packet);
			}catch(Exception exc){
				System.err.println(exc);
			}
		}
		if (e.getSource() == reportLocalButton){
			packet = makePacket((byte)52, (new Integer(0xFFFF)).toString(), args);
			System.out.println("Start node "+ nodeId + " Packet "+packet[1]+""+packet[2]);
			try{
				messenger.sendPacket(packet);
			}catch(Exception exc){
				System.err.println(exc);
			}
		}
		if (e.getSource() == startButton){
			packet = makePacket((byte)51, nodeId, args);
			System.out.println("Start node "+ nodeId + " Packet "+packet[1]+""+packet[2]);
			try{
				messenger.sendPacket(packet);
			}catch(Exception exc){
				System.err.println(exc);
			}
		}
		if (e.getSource() == stopButton){
			
			packet = makePacket((byte)49, (new Integer(0xFFFF)).toString(), args);
			System.out.println("Stop node "+ nodeId + " Packet "+ packet[1]+""+packet[2]); 
			try{
				messenger.sendPacket(packet);
			}catch(Exception exc){
				System.err.println(exc);
			}
		}
	}
	
} 

class ExitListener extends WindowAdapter {
	  public void windowClosing(WindowEvent event) {
	    System.exit(0);
	  }
	}

class ControlledCommander implements ActionListener{
	JTextField nodeIdField;
	JButton startButton;
	JButton stopButton;
	JButton reportButton;
	JButton reportLocalButton;
	JButton eraseLogButton;
	JButton runTurnButton;
	JButton runConcurrentButton;
	String nodeId;
	Messenger messenger;
	Reporter reporter;
	ReportProcessor processor;
	BlockingQueue<Report> bq = new LinkedBlockingQueue<Report>();
	private byte numberOfReports = 50;
	private byte numberOfPackets = 8;
	private int numberOfNodes = 12;
	private byte transmissionPower = 1;
	private byte threshold = 85;
	private byte []probabilityT = {60,45, 36, 28, 0 };
	private byte []probabilityR = {0, 27, 37, 44, 60};

	public ControlledCommander(){
		messenger = new Messenger("serial@/dev/ttyUSB0:telosb");
		
		reporter = new Reporter("serial@/dev/ttyUSB1:telosb",bq);
		processor = new ReportProcessor(bq,2); //2
		
		
		messenger.start();
		reporter.start();
		
		try {
	      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } catch(Exception e) {
	      System.out.println("Error setting native LAF: " + e);
	    }
	    JFrame f = new JFrame("Command Window");
	    f.setSize(400, 150);
	    Container content = f.getContentPane();
	    content.setBackground(Color.white);
	    content.setLayout(new FlowLayout());
	    JLabel label = new JLabel("Node Id");
	    content.add(label);
	    nodeIdField =new JTextField(10);
	    content.add(nodeIdField);
	    nodeIdField.addActionListener(this);
	    startButton = new JButton("Start"); 
	    startButton.addActionListener(this);
	    content.add(startButton);
	    stopButton = new JButton("Stop");
	    stopButton.addActionListener(this);
	    content.add(stopButton);
	    
	    reportButton = new JButton("Report");
	    reportButton.addActionListener(this);
	    content.add(reportButton);
	    
	    reportLocalButton = new JButton("Report Local");
	    reportLocalButton.addActionListener(this);
	    content.add(reportLocalButton);
	    
	    eraseLogButton = new JButton("Erase Log");
	    eraseLogButton.addActionListener(this);
	    content.add(eraseLogButton);
	    
	    runTurnButton = new JButton("RUN By Turn");
	    runTurnButton.addActionListener(this);
	    content.add(runTurnButton);
	    

	    runConcurrentButton = new JButton("RUN Concurrently");
	    runConcurrentButton.addActionListener(this);
	    content.add(runConcurrentButton);
	    
	    f.addWindowListener(new ExitListener());
	    f.setVisible(true);
	}
	public void test(){
		
		double l = 0.45;
		double cp = 0;
		double th = 0.85;
		int timesCovered = 0;
		int num = 0;
		while(timesCovered < 24){
			System.out.println(cp);
			cp =  1 - (1-cp)*(1-l);
			if(cp > th){
				timesCovered++;
				cp = cp - th;
			}
			num++;
		}
		System.out.println("Number of transmissions "+num);
	}
	public void command(){
		
	}
	byte []makePacket(byte type,String id, byte []args){
		byte packet[] = new byte[8];
		packet[0] = type;
		packet[1] = (byte)(Integer.parseInt(id) & 0xFF);
		packet[2] = (byte)((Integer.parseInt(id) >> 8 )& 0xFF);
		for(int i = 0; i < args.length; i++)
			packet[3+i] = args[i];
		return packet;
	}

	public void runConcurrently(){
		//Erase ALL
		byte packet[];
		byte args[] = new byte[5];
		String id;
		args[0] = transmissionPower;
		args[1] = threshold;
		args[2] = probabilityT[0];
		args[3] = probabilityR[0];
		args[4] = numberOfReports;
		
		System.out.println("Erasing all ...");
		id = new Integer(0xFFFF).toString();
		packet = makePacket((byte)54, id, args);
		try{
			messenger.sendPacket(packet);
			Thread.sleep(5000);
		}catch(Exception exc){
			System.err.println(exc);
		}
		
		for (int run = 0; run < numberOfReports; run++){
			//start all
			args[2] = probabilityT[run/10];
			args[3] = probabilityR[run/10];
			System.out.println("Starting all for run "+run);
			id = (new Integer(0xFFFF)).toString();
			packet = makePacket((byte)51, id, args);
			try{
				messenger.sendPacket(packet);
				Thread.sleep(numberOfPackets*1000 +12000 + 20000);
			}catch(Exception exc){
				System.err.println(exc);
			}
			//stop ALL
			System.out.println("Stopping all for run "+run);
			id = (new Integer(0xFFFF)).toString();
			packet = makePacket((byte)49, id, args);
			try{
				messenger.sendPacket(packet);
				Thread.sleep(10000);
			}catch(Exception exc){
				System.err.println(exc);
			}
			
			//dummy ALL
			System.out.println("Stopping all for run "+run);
			id = (new Integer(0xFFFF)).toString();
			packet = makePacket((byte)0, id, args);
			try{
				messenger.sendPacket(packet);
				Thread.sleep(5000);
			}catch(Exception exc){
				System.err.println(exc);
			}
		}
		//dummy ALL
		
		id = (new Integer(0xFFFF)).toString();
		packet = makePacket((byte)0, id, args);
		try{
			messenger.sendPacket(packet);
			Thread.sleep(1000);
		}catch(Exception exc){
			System.err.println(exc);
		}

	}

	public void runByTurn(){
		//Erase ALL
		byte packet[];
		byte args[] = new byte[5];
		String id;
		args[0] = transmissionPower;
		args[1] = threshold;
		args[2] = probabilityT[0];
		args[3] = probabilityR[0];
		args[4] = numberOfReports;

		System.out.println("Erasing all ...");
		id = new Integer(0xFFFF).toString();
		packet = makePacket((byte)54, id, args);
		try{
			messenger.sendPacket(packet);
			Thread.sleep(5000);
		}catch(Exception exc){
			System.err.println(exc);
		}
	
		for (int run = 0; run < numberOfReports; run++){
			//start by turn
			
			for (int i = 1; i <= numberOfNodes; i++ ){
				System.out.println("Starting ..."+i+ " for run "+run);
				id = i +"";
				packet = makePacket((byte)51, id, args);
				try{
					messenger.sendPacket(packet);
					Thread.sleep(1000 * numberOfPackets + 5000);
				}catch(Exception exc){
					System.err.println(exc);
				}
			}
			
			//stop ALL
			System.out.println("Stopping all for run "+run);
			id = (new Integer(0xFFFF)).toString();
			packet = makePacket((byte)49, id, args);
			try{
				messenger.sendPacket(packet);
				Thread.sleep(5000);
			}catch(Exception exc){
				System.err.println(exc);
			}
			

			//dummy ALL
			System.out.println("Stopping all for run "+run);
			id = (new Integer(0xFFFF)).toString();
			packet = makePacket((byte)0, id, args);
			try{
				messenger.sendPacket(packet);
				Thread.sleep(5000);
			}catch(Exception exc){
				System.err.println(exc);
			}
		}

	}
	@Override
	public void actionPerformed(ActionEvent e) {
		byte packet[];
		byte args[] = new byte[5];
		args[0] = transmissionPower;
		args[1] = threshold;
		args[2] = probabilityT[0];
		args[3] = probabilityR[0];
		args[4] = numberOfReports;

		if(e.getSource() == runTurnButton){
			runByTurn();
		}
		if(e.getSource() == runConcurrentButton){
			runConcurrently();
		}
		if(e.getSource() == nodeIdField){
			nodeId = nodeIdField.getText();
			System.out.println(nodeId);
		}
		if (e.getSource() == reportButton){
			packet = makePacket((byte)52, nodeId, args);
			System.out.println("Start node "+ nodeId + " Packet "+packet[1]+""+packet[2]);
			try{
				messenger.sendPacket(packet);
			}catch(Exception exc){
				System.err.println(exc);
			}
		}
		if (e.getSource() == eraseLogButton){
			packet = makePacket((byte)54, nodeId, args);
			System.out.println("Start node "+ nodeId + " Packet "+packet[1]+""+packet[2]);
			try{
				messenger.sendPacket(packet);
			}catch(Exception exc){
				System.err.println(exc);
			}
		}
		if (e.getSource() == reportLocalButton){
			packet = makePacket((byte)52, (new Integer(0xFFFF)).toString(), args);
			System.out.println("Start node "+ nodeId + " Packet "+packet[1]+""+packet[2]);
			try{
				messenger.sendPacket(packet);
			}catch(Exception exc){
				System.err.println(exc);
			}
		}
		if (e.getSource() == startButton){
			packet = makePacket((byte)51, nodeId, args);
			System.out.println("Start node "+ nodeId + " Packet "+packet[1]+""+packet[2]);
			try{
				messenger.sendPacket(packet);
			}catch(Exception exc){
				System.err.println(exc);
			}
		}
		if (e.getSource() == stopButton){
			
			packet = makePacket((byte)49, (new Integer(0xFFFF)).toString(), args);
			System.out.println("Stop node "+ nodeId + " Packet "+ packet[1]+""+packet[2]); 
			try{
				messenger.sendPacket(packet);
			}catch(Exception exc){
				System.err.println(exc);
			}
		}
	}
	
} 
