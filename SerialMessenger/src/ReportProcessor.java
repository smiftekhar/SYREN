import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.concurrent.BlockingQueue;


public class ReportProcessor implements Runnable{
    private BlockingQueue<Report> bq;
    private int []reportCounter = new int[100];
    private int unit;
	public ReportProcessor(BlockingQueue<Report> bq, int unit){
		this.bq = bq;
		this.unit = unit;
		for (int i = 0; i < reportCounter.length; i++)
			reportCounter[i] = 0;
		Thread th = new Thread(this);
		th.start();
	}
	@Override
	public void run() {
		while(true){
			try{
				Report rpt = bq.take();
				reportCounter[rpt.getNameOfReportingNode()]++;
				BufferedWriter writer = new BufferedWriter(new FileWriter("Node"+rpt.getNameOfReportingNode()+".txt",true));
				
				if(reportCounter[rpt.getNameOfReportingNode()] % unit == 1)
					writer.write("\nRun "+(1 + reportCounter[rpt.getNameOfReportingNode()] / unit)+"\n");
				byte []temp = rpt.getData();
				for (int i = 0; i <temp.length; i++ ){
					writer.write(temp[i]+" ");
					System.out.print(temp[i]+" ");
				}
				writer.close();
			}catch(Exception exc){
				System.err.println(exc);
			}
		}
		
	}
}
