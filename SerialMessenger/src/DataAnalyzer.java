import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.BitSet;
import java.util.Vector;

class RunInformation{
	int runNumber;
	int startingTime;
	int endingTime;
	int totalTx;
	int coveredNodes;
}
public class DataAnalyzer {
	private int numberOfRuns = 5;
	private int nbrhoodSize = 15;
	private int pktBoundary = 12;
	private int numberOfNodes = 49;
	private int []setOfNodes = {3,21,10,20};
	private byte [][][][]correlationInfo;
	 
	public DataAnalyzer(){
	
		correlationInfo = new byte[numberOfRuns][numberOfNodes][numberOfNodes][pktBoundary];
	}
	public void analyze(){
		//extractSimCorrelation("/root/Projects/tinyos-main-read-only/apps/Simulation/SenderSelection_Test/output/");
		//extractSimCorrelation("/root/Projects/tinyos-main-read-only/apps/Simulation/SYREN_Test/output/");
		extractSimCorrelation("/root/Projects/tinyos-main-read-only/apps/Simulation/SYREN_wop_Test/output/");
		extractSimCorrelation("/root/Projects/tinyos-main-read-only/apps/Simulation/SYREN_nop_Test/output/");
		//extractDelugeStats("/root/Projects/SerialMessenger/Data/Secon13/RatelessDeluge/");
		//extractSimCorrelation("/root/Projects/tinyos-main-read-only/apps/Simulation/SYREN_Test/output/");
		//extractSimCorrelation("/root/Projects/tinyos-main-read-only/apps/Simulation/SYREN_wop_Test/output/");
		
		//extractSimCorrelation("/root/Projects/tinyos-main-read-only/apps/Simulation/Broadcast_Test/output/");
		//extractSimCorrelation("/root/Projects/tinyos-main-read-only/apps/Simulation/CollectiveFlood_Test/output/");
		//extractDelugeStats("/root/Projects/tinyos-main-read-only/apps/Simulation/Deluge_Test/output/");
		//extractDelugeStats("/root/Projects/tinyos-main-read-only/apps/Simulation/RatelessDeluge_Test/output/");
		//extractControlledDelugeStats("/root/Projects/tinyos-main-read-only/apps/Simulation/RatelessDeluge_Test/output/");
	}
	private BitSet getBitSet(byte []data){
		BitSet bits = new BitSet(data.length*8);
		for(int i = 0; i < data.length; i++){
			for(int j = 0; j < 8; j++){
				if((((0xFF & data[i]) >> j) & 0x1) == 1){
					bits.set(i*8+j);
				}
			}
		}
		//System.out.println(bits);
		return bits;
	}
	public void extractDelugeStats(){
		int currentRun = -1;
		try{
			BufferedWriter bw2 = new BufferedWriter(new FileWriter("Stats.txt"));
			bw2.write("Run\tSource\tNumOfTx\tNumOfReq\tNumOfAdv\tNumOfPkts\tNumOfRx\tRxTime\tTxTtime\n");
			for (int i = 2; i <= numberOfNodes; i++){
					
					BufferedReader reader = new BufferedReader(new FileReader("Node"+i+".txt"));
					
					
					String line = "";
					while((line = reader.readLine()) != null){
						if(line.startsWith("Run")){
							currentRun = Integer.parseInt(line.split(" ")[1]);
							if(currentRun > numberOfRuns)
								break;
							//System.out.println(currentRun);
						}
						else if (line.length() > 0){
							String []temp = line.split(" ");
							int k = 0;
							int numOfTx = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
							int numOfAdv = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
							int numOfReq = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
							int numOfRx = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
							int timeOfRx = (0xFF & Byte.parseByte(temp[k+3])); 
							timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k+2]));
							timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k+1]));
							timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k]));
							k += 4;
							int timeOfTx = (0xFF & Byte.parseByte(temp[k+3])); 
							timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k+2]));
							timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k+1]));
							timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k]));
							k += 4;
							bw2.write(currentRun+"\t"+i+"\t"+numOfTx+"\t"+numOfReq+"\t"+numOfAdv+"\t"+(numOfTx+numOfReq+numOfAdv)+"\t"+numOfRx+"\t"+timeOfRx+"\t"+timeOfTx+"\n");
							//System.out.println(i+":Nt = "+(numOfTx+numOfReq+numOfAdv)+" Nr = "+numOfRx+" tR = "+timeOfRx+" tN = "+timeOfTx);
							
						}
					}
				}
				bw2.close();
			}
			catch(Exception exc){
				exc.printStackTrace();	
			}

	}
	public void extractCorrelation(String fileLocation){
		int currentRun = -1;
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileLocation+"Correlation.txt"));
			BufferedWriter bw1 = new BufferedWriter(new FileWriter(fileLocation+"LinkStats.txt"));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(fileLocation+"Stats.txt"));
			bw.write("Run\tSource\tNeighbor\tReceiver\tCorrelation\n");
			bw1.write("Run\tSource\tNeighbor\tQuality\n");
			bw2.write("Run\tSource\tNumOfTx\tNumOfRx\tRxTime\tTxTtime\n");
			for (int i = 2; i <= numberOfNodes; i++){
					BufferedReader reader = new BufferedReader(new FileReader(fileLocation+"Node"+i+".txt"));
					
					
					String line = "";
					while((line = reader.readLine()) != null){
						if(line.startsWith("Run")){
							currentRun = Integer.parseInt(line.split(" ")[1]);
							if(currentRun > numberOfRuns)
								break;
							
						}
						else if (line.length() > 0){
							String []temp = line.split(" ");
							int k = 0;
							for(int j = 0; j < nbrhoodSize; j++){	
								int nbr = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8); 
								int linkQuality = (0xFF & Byte.parseByte(temp[k+3]));
								linkQuality = (linkQuality << 8) | (0xFF & Byte.parseByte(temp[k+2]));
								linkQuality = (linkQuality << 8) | (0xFF & Byte.parseByte(temp[k+1]));
								linkQuality = (linkQuality << 8) | (0xFF & Byte.parseByte(temp[k]));
								if(nbr > 0)
									bw1.write(currentRun+"\t"+i+"\t" + nbr+"\t"+ Float.intBitsToFloat(linkQuality)+"\n");
								k += 6;
								
							}
							for(int l = 0; l < nbrhoodSize; l++){
								int sender = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
								
								for(int m = 0; m < nbrhoodSize; m++){
									
									int nbr = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8); 
									int linkCorr = (0xFF & Byte.parseByte(temp[k+3]));
									linkCorr = (linkCorr << 8) | (0xFF & Byte.parseByte(temp[k+2]));
									linkCorr = (linkCorr << 8) | (0xFF & Byte.parseByte(temp[k+1]));
									linkCorr = (linkCorr << 8) | (0xFF & Byte.parseByte(temp[k]));
									if(sender > 0 && nbr > 0)
										bw.write(currentRun+"\t"+sender+"\t" + nbr+"\t"+ i+"\t"+Float.intBitsToFloat(linkCorr)+"\n");
								
									k += 6;
								}
								
							}
							if (k < temp.length){
								//System.out.println(k+" "+temp.length);
								int numOfTx = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);	
								int numOfRx = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
								int timeOfRx = (0xFF & Byte.parseByte(temp[k+3])); 
								timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k+2]));
								timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k+1]));
								timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k]));
								k += 4;
								int timeOfTx = (0xFF & Byte.parseByte(temp[k+3])); 
								timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k+2]));
								timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k+1]));
								timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k]));
								k += 4;
								bw2.write(currentRun+"\t"+i+"\t"+numOfTx+"\t"+numOfRx+"\t"+timeOfRx+"\t"+timeOfTx+"\n");
								//System.out.println(i+":Nt = "+numOfTx+" Nr = "+numOfRx+" tR = "+timeOfRx+" tN = "+timeOfTx);
							}
						}
					}
				}

				bw.close();
				bw1.close();
				bw2.close();
			}
			catch(Exception exc){
				System.out.println("Exception while processing run = "+currentRun);
				exc.printStackTrace();	
			}

	}

	public void testCorrelation(){
		int currentRun = -1;
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter("Correlation.txt"));
			BufferedWriter bw1 = new BufferedWriter(new FileWriter("LinkStats.txt"));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter("Stats.txt"));
			bw.write("Run\tSource\tNeighbor\tReceiver\tCorrelation\n");
			bw1.write("Run\tSource\tNeighbor\tQuality\n");
			bw2.write("Run\tSource\tNumOfTx\tNumOfRx\tRxTime\tTxTtime\n");
			for (int g = 0; g < setOfNodes.length; g++){
					int i = setOfNodes[g];
					BufferedReader reader = new BufferedReader(new FileReader("Node"+i+".txt"));
					
					
					String line = "";
					while((line = reader.readLine()) != null){
						if(line.startsWith("Run")){
							currentRun = Integer.parseInt(line.split(" ")[1]);
							if(currentRun > numberOfRuns)
								break;
							//System.out.println(currentRun);
						}
						else if (line.length() > 0){
							String []temp = line.split(" ");
							int k = 0;
							for(int j = 0; j < nbrhoodSize; j++){	
								int nbr = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8); 
								int linkQuality = (0xFF & Byte.parseByte(temp[k+3]));
								linkQuality = (linkQuality << 8) | (0xFF & Byte.parseByte(temp[k+2]));
								linkQuality = (linkQuality << 8) | (0xFF & Byte.parseByte(temp[k+1]));
								linkQuality = (linkQuality << 8) | (0xFF & Byte.parseByte(temp[k]));
								if(nbr > 0)
									bw1.write(currentRun+"\t"+i+"\t" + nbr+"\t"+ Float.intBitsToFloat(linkQuality)+"\n");
								k += 6;
							}
							for(int l = 0; l < nbrhoodSize; l++){
								int sender = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
								
								for(int m = 0; m < nbrhoodSize; m++){
									
									int nbr = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8); 
									int linkCorr = (0xFF & Byte.parseByte(temp[k+3]));
									linkCorr = (linkCorr << 8) | (0xFF & Byte.parseByte(temp[k+2]));
									linkCorr = (linkCorr << 8) | (0xFF & Byte.parseByte(temp[k+1]));
									linkCorr = (linkCorr << 8) | (0xFF & Byte.parseByte(temp[k]));
									if(sender > 0 && nbr > 0)
										bw.write(currentRun+"\t"+sender+"\t" + nbr+"\t"+ i+"\t"+Float.intBitsToFloat(linkCorr)+"\n");
								
									k += 6;
								}
								
							}
							if (k < temp.length){
								//System.out.println(k+" "+temp.length);
								int numOfTx = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);	
								int numOfRx = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
								int timeOfRx = (0xFF & Byte.parseByte(temp[k+3])); 
								timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k+2]));
								timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k+1]));
								timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k]));
								k += 4;
								int timeOfTx = (0xFF & Byte.parseByte(temp[k+3])); 
								timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k+2]));
								timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k+1]));
								timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k]));
								k += 4;
								bw2.write(currentRun+"\t"+i+"\t"+numOfTx+"\t"+numOfRx+"\t"+timeOfRx+"\t"+timeOfTx+"\n");
								//System.out.println(i+":Nt = "+numOfTx+" Nr = "+numOfRx+" tR = "+timeOfRx+" tN = "+timeOfTx);
							}
						}
					}
				}

				bw.close();
				bw1.close();
				bw2.close();
			}
			catch(Exception exc){
				
				System.out.println(exc);	
			}

	}

	public void computeCorrelation(){
//		System.out.println("Corr");
		analyzeData();
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter("Correlation.txt"));
			BufferedWriter bw1 = new BufferedWriter(new FileWriter("LinkStats.txt"));
			bw.write("Run\tSource\tNeighbor\tReceiver\tCorrelation\n");
			bw1.write("Run\tSource\tNeighbor\tQuality\n");
			for(int r = 0; r < numberOfRuns; r++){
				//System.out.println("Run = "+(r+1));
				for (int i = 2; i <= numberOfNodes; i++){
					//System.out.println("Source = "+i);
					for (int j = 2; j <= numberOfNodes; j++){
						if(j != i){
							BitSet me = getBitSet(correlationInfo[r][i-1][j-1]);
							//System.out.println("Me = "+j + ": "+me + " "+out);
							if(me.cardinality() > 0)
								bw1.write((r+1)+"\t"+i+"\t" + j+"\t"+ ((float)me.cardinality()/50)+"\n");
							for (int k = 2; k <= numberOfNodes && me.cardinality() > 0; k++){
								if(k != j && k != i){
									BitSet he = getBitSet(correlationInfo[r][i-1][k-1]);
									//System.out.println("He = "+j + ": "+he);
									he.and(me);
									//if(r == 0 && i == 2)
									bw.write((r+1)+"\t"+i+"\t" + k +"\t"+j+"\t"+ ((float)he.cardinality()/me.cardinality())+"\n");
								}
							}
						}
					}
				}
			}
			bw.close();
			bw1.close();
		}catch(Exception exc){
			
		}
	}
	public void showCorrelationInfo(int run, int sourceNode){
	
		for(int j = 0; j < numberOfNodes; j++){
			String out = "Node "+ (j+1) +": ";
			for (int i = 0; i < pktBoundary; i++){
				out += correlationInfo[run-1][sourceNode-1][j][i]+" ";
			}
		System.out.println(out);
		}
	}
	
	public void analyzeData(){
		int currentRun = -1;
	
		for (int i = 2; i <= numberOfNodes; i++){
			try{
				BufferedReader reader = new BufferedReader(new FileReader("Node"+i+".txt"));
				String line = "";
				while((line = reader.readLine()) != null){
					if(line.startsWith("Run")){
						currentRun = Integer.parseInt(line.split(" ")[1]) - 1;
						//System.out.println(currentRun);
					}
					else if (line.length() > 0){
						String []temp = line.split(" ");
						
						int []sources = new int[nbrhoodSize];
						int j = 0;
						for (int k = 0; k < nbrhoodSize; k++){
							sources[k] = (0xFF & Byte.parseByte(temp[j++]));
							sources[k] = (0xFF & Byte.parseByte(temp[j++]) << 8) | sources[k];
						}
						for (int l = 0; l < nbrhoodSize && sources[l] > 0; l++){
							for(int m = 0; m < pktBoundary; m++){
								correlationInfo[currentRun][sources[l]-1][i-1][m] = Byte.parseByte(temp[j++]);
							}
						}
					}
				}
			}catch(Exception exc){
				System.out.println(exc);	
			}
		}
	}
	
	public RunInformation[] extractSimCorrelation(String fileLocation){
		RunInformation []info = new RunInformation[numberOfRuns+1];
		Statistics []stat = new Statistics[numberOfRuns+1];
		for(int cnt = 0; cnt < info.length; cnt++){
			info[cnt] = new RunInformation();
			info[cnt].coveredNodes = 0;
			info[cnt].endingTime = 0;
			info[cnt].runNumber = 0;
			info[cnt].totalTx = 0;
			info[cnt].startingTime = 0;
			stat[cnt] = new Statistics(numberOfNodes);
		}
		int currentRun = -1;
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileLocation+"Correlation.txt"));
			BufferedWriter bw1 = new BufferedWriter(new FileWriter(fileLocation+"LinkStats.txt"));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(fileLocation+"Stats.txt"));
			bw.write("Run\tSource\tNeighbor\tReceiver\tCorrelation\n");
			bw1.write("Run\tSource\tNeighbor\tQuality\n");
			bw2.write("Run\tSource\tNumOfTx\tNumOfRx\tRxTime\tTxTtime\n");
			for (int i = 1; i <= numberOfNodes; i++){
					BufferedReader reader = new BufferedReader(new FileReader(fileLocation+"Node"+i+".txt"));
					
					
					String line = "";
					while((line = reader.readLine()) != null){
						if(line.startsWith("Run")){
							currentRun = Integer.parseInt(line.split(" ")[1]);
							if(currentRun > numberOfRuns)
								break;
							info[currentRun].runNumber = currentRun;
							//System.out.println(currentRun);
						}
						else if (line.length() > 0){
							String []temp = line.split(" ");
							int k = 0;
							for(int j = 0; j < nbrhoodSize; j++){	
								int nbr = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8); 
								k += 2;
								int linkQuality = (0xFF & Byte.parseByte(temp[k+3]));
								linkQuality = (linkQuality << 8) | (0xFF & Byte.parseByte(temp[k+2]));
								linkQuality = (linkQuality << 8) | (0xFF & Byte.parseByte(temp[k+1]));
								linkQuality = (linkQuality << 8) | (0xFF & Byte.parseByte(temp[k]));
								if(nbr > 0)
									bw1.write(currentRun+"\t"+i+"\t" + nbr+"\t"+ Float.intBitsToFloat(linkQuality)+"\n");
								k += 4;
							}
							for(int l = 0; l < nbrhoodSize; l++){
								int sender = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
								k += 2; //padding
								for(int m = 0; m < nbrhoodSize; m++){
									
									int nbr = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
									k += 2;
									int linkCorr = (0xFF & Byte.parseByte(temp[k+3]));
									linkCorr = (linkCorr << 8) | (0xFF & Byte.parseByte(temp[k+2]));
									linkCorr = (linkCorr << 8) | (0xFF & Byte.parseByte(temp[k+1]));
									linkCorr = (linkCorr << 8) | (0xFF & Byte.parseByte(temp[k]));
									if(sender > 0 && nbr > 0)
										bw.write(currentRun+"\t"+sender+"\t" + nbr+"\t"+ i+"\t"+Float.intBitsToFloat(linkCorr)+"\n");
								
									k += 4;
								}
								
							}
							if (k < temp.length){
								//System.out.println(k+" "+temp.length);
								int numOfTx = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);	
								int numOfRx = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
								int timeOfRx = (0xFF & Byte.parseByte(temp[k+3])); 
								timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k+2]));
								timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k+1]));
								timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k]));
								k += 4;
								int timeOfTx = (0xFF & Byte.parseByte(temp[k+3])); 
								timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k+2]));
								timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k+1]));
								timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k]));
								k += 4;
								bw2.write(currentRun+"\t"+i+"\t"+numOfTx+"\t"+numOfRx+"\t"+timeOfRx+"\t"+timeOfTx+"\n");
								//System.out.println(i+":Nt = "+numOfTx+" Nr = "+numOfRx+" tR = "+timeOfRx+" tN = "+timeOfTx);
								
								info[currentRun].totalTx += numOfTx;
								stat[currentRun].insert(numOfTx);
								
								if(i == 1){
									info[currentRun].startingTime = timeOfRx;
									info[currentRun].coveredNodes++;
								}
								else{
									int timeToCompare = 0;
									if(numOfRx == 24){
										timeToCompare = timeOfRx;
										info[currentRun].coveredNodes++;
									}
									else timeToCompare = timeOfTx;
									
									if(info[currentRun].endingTime < timeToCompare)
										info[currentRun].endingTime = timeToCompare;
								}
							}
						}
					}
				}

				bw.close();
				bw1.close();
				bw2.close();
			}
			catch(Exception exc){
				
				exc.printStackTrace();
			}
			//System.out.println(fileLocation+":");
			float numberOfPkts = 0;
			float delay = 0;
			float reliability = 0;
			float localCount = 0;
			float stdev = 0;
			float overallRel = 0;
			for(int i = 1; i< info.length;i++){
				overallRel += ((float)info[i].coveredNodes/numberOfNodes);
				/*if(info[i].coveredNodes == numberOfNodes)*/{
					reliability += ((float)info[i].coveredNodes/numberOfNodes);
					numberOfPkts += info[i].totalTx;
					delay += (info[i].endingTime-info[i].startingTime);
					stdev += stat[i].standard_deviation();
					localCount++;
				}
				//System.out.println(info[i].runNumber+"\t"+info[i].totalTx + "\t"+(info[i].endingTime-info[i].startingTime)+"\t"+(info[i].coveredNodes));
			}
			//System.out.println("Avg Number of Pkts = "+numberOfPkts/numberOfRuns+"\nAvg Delay = "+delay/numberOfRuns+"\nAvg reliability = "+100*reliability/numberOfRuns);
			if(localCount > 0)
				System.out.println(numberOfPkts/localCount+"\t"+0.001*delay/localCount+"\t"+100*reliability/localCount+"\t"+stdev/localCount+"\t"+localCount/numberOfRuns + "\t"+ overallRel/numberOfRuns);
			else
				System.out.println(info[1].totalTx+"\t"+(float)info[1].coveredNodes/numberOfNodes +"\t"+0.001*(info[1].endingTime-info[1].startingTime)+"\t"+overallRel/numberOfRuns);
			return info;
	}
	public RunInformation[] extractDelugeStats(String fileLocation){
		int currentRun = -1;
		RunInformation []info = new RunInformation[numberOfRuns+1];
		Statistics []stat = new Statistics[numberOfRuns+1];
		for(int cnt = 0; cnt < info.length; cnt++){
			info[cnt] = new RunInformation();
			info[cnt].coveredNodes = 0;
			info[cnt].endingTime = 0;
			info[cnt].runNumber = 0;
			info[cnt].totalTx = 0;
			info[cnt].startingTime = 0;
			stat[cnt] = new Statistics(numberOfNodes);
		}
		try{
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(fileLocation+"Stats.txt"));
			bw2.write("Run\tSource\tNumOfTx\tNumOfReq\tNumOfAdv\tNumOfPkts\tNumOfRx\tRxTime\tTxTtime\n");
			for (int i = 2; i <= numberOfNodes; i++){
					
					BufferedReader reader = new BufferedReader(new FileReader(fileLocation+"Node"+i+".txt"));
					
					
					String line = "";
					while((line = reader.readLine()) != null){
						if(line.startsWith("Run")){
							currentRun = Integer.parseInt(line.split(" ")[1]);
							if(currentRun > numberOfRuns)
								break;
							info[currentRun].runNumber = currentRun;
							//System.out.println(currentRun);
						}
						else if (line.length() > 0){
							String []temp = line.split(" ");
							int k = 0;
							int numOfTx = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
							int numOfAdv = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
							int numOfReq = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
							int numOfRx = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
							int timeOfRx = (0xFF & Byte.parseByte(temp[k+3])); 
							timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k+2]));
							timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k+1]));
							timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k]));
							k += 4;
							int timeOfTx = (0xFF & Byte.parseByte(temp[k+3])); 
							timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k+2]));
							timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k+1]));
							timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k]));
							k += 4;
							bw2.write(currentRun+"\t"+i+"\t"+numOfTx+"\t"+numOfReq+"\t"+numOfAdv+"\t"+(numOfTx+numOfReq+numOfAdv)+"\t"+numOfRx+"\t"+timeOfRx+"\t"+timeOfTx+"\n");
							//System.out.println(i+":Nt = "+(numOfTx+numOfReq+numOfAdv)+" Nr = "+numOfRx+" tR = "+timeOfRx+" tN = "+timeOfTx);
							info[currentRun].totalTx += (numOfTx + numOfAdv + numOfReq);
							stat[currentRun].insert(numOfTx + numOfAdv + numOfReq);
							if(i == 1){
								info[currentRun].startingTime = timeOfRx;
								info[currentRun].coveredNodes++;
							}
							else{
								int timeToCompare = 0;
								if(numOfRx == 24){
									timeToCompare = timeOfRx;
									info[currentRun].coveredNodes++;
								}
								else timeToCompare = timeOfTx;
								
								if(info[currentRun].endingTime < timeToCompare)
									info[currentRun].endingTime = timeToCompare;
							}
							
						}
					}
				}
				bw2.close();
			}
			catch(Exception exc){
				System.out.println(exc);	
			}
			//System.out.println(fileLocation+":");
			float numberOfPkts = 0;
			float delay = 0;
			float reliability = 0;
			int localCount = 0;
			float stdev = 0;
			float overallRel = 0;
			for(int i = 1; i< info.length;i++){
				overallRel += ((float)info[i].coveredNodes/numberOfNodes);
				/*if(info[i].coveredNodes == numberOfNodes)*/{
					numberOfPkts += info[i].totalTx;
					delay += (info[i].endingTime-info[i].startingTime);
					reliability += ((float)info[i].coveredNodes/numberOfNodes);
					stdev += stat[i].standard_deviation();
					localCount++;
				}
				//System.out.println(info[i].runNumber+"\t"+info[i].totalTx + "\t"+(info[i].endingTime-info[i].startingTime)+"\t"+(info[i].coveredNodes));
			}
			//System.out.print("Avg Number of Pkts = "+numberOfPkts/numberOfRuns+"\nAvg Delay = "+delay/numberOfRuns+"\nAvg reliability = "+100*reliability/numberOfRuns);
			System.out.println(numberOfPkts/localCount+"\t"+0.001*delay/localCount+"\t"+100*reliability/localCount+"\t"+stdev/localCount+"\t"+(float)localCount/numberOfRuns+"\t"+overallRel/numberOfRuns);
			return info;
	}

	public void extractControlledCorrelation(String fileLocation){
		int currentRun = -1;
		int cc = -1;
		try{
			BufferedWriter []bw = new BufferedWriter[5];
			BufferedWriter []bw1 = new BufferedWriter[5];
			BufferedWriter []bw2 = new BufferedWriter[5];
			for(int i = 0; i < 5; i++){
				bw[i] = new BufferedWriter(new FileWriter(fileLocation+"Correlation"+i+".txt"));
				bw1[i] = new BufferedWriter(new FileWriter(fileLocation+"LinkStats"+i+".txt"));
				bw2[i] = new BufferedWriter(new FileWriter(fileLocation+"Stats"+i+".txt"));
				bw[i].write("Run\tSource\tNeighbor\tReceiver\tCorrelation\n");
				bw1[i].write("Run\tSource\tNeighbor\tQuality\n");
				bw2[i].write("Run\tSource\tNumOfTx\tNumOfRx\tRxTime\tTxTtime\n");

			}
			for (int i = 2; i <= numberOfNodes; i++){
					BufferedReader reader = new BufferedReader(new FileReader(fileLocation+"Node"+i+".txt"));
					
					
					String line = "";
					while((line = reader.readLine()) != null){
						if(line.startsWith("Run")){
							cc = Integer.parseInt(line.split(" ")[1]);
							currentRun = ( cc - 1)%numberOfRuns + 1 ;
							
							if(currentRun > numberOfRuns)
								break;
							
						}
						else if (line.length() > 0){
							String []temp = line.split(" ");
							int k = 0;
							for(int j = 0; j < nbrhoodSize; j++){	
								int nbr = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8); 
								int linkQuality = (0xFF & Byte.parseByte(temp[k+3]));
								linkQuality = (linkQuality << 8) | (0xFF & Byte.parseByte(temp[k+2]));
								linkQuality = (linkQuality << 8) | (0xFF & Byte.parseByte(temp[k+1]));
								linkQuality = (linkQuality << 8) | (0xFF & Byte.parseByte(temp[k]));
								if(nbr > 0)
									bw1[(cc-1)/numberOfRuns].write(currentRun+"\t"+i+"\t" + nbr+"\t"+ Float.intBitsToFloat(linkQuality)+"\n");
								
								k += 6;
							}
							for(int l = 0; l < nbrhoodSize; l++){
								int sender = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
								
								for(int m = 0; m < nbrhoodSize; m++){
									
									int nbr = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8); 
									int linkCorr = (0xFF & Byte.parseByte(temp[k+3]));
									linkCorr = (linkCorr << 8) | (0xFF & Byte.parseByte(temp[k+2]));
									linkCorr = (linkCorr << 8) | (0xFF & Byte.parseByte(temp[k+1]));
									linkCorr = (linkCorr << 8) | (0xFF & Byte.parseByte(temp[k]));
									if(sender > 0 && nbr > 0)
										bw[(cc-1)/numberOfRuns].write(currentRun+"\t"+sender+"\t" + nbr+"\t"+ i+"\t"+Float.intBitsToFloat(linkCorr)+"\n");
								
									k += 6;
								}
								
							}
							if (k < temp.length){
								//System.out.println(k+" "+temp.length);
								int numOfTx = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);	
								int numOfRx = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
								int timeOfRx = (0xFF & Byte.parseByte(temp[k+3])); 
								timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k+2]));
								timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k+1]));
								timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k]));
								k += 4;
								int timeOfTx = (0xFF & Byte.parseByte(temp[k+3])); 
								timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k+2]));
								timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k+1]));
								timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k]));
								k += 4;
								bw2[(cc-1)/numberOfRuns].write(currentRun+"\t"+i+"\t"+numOfTx+"\t"+numOfRx+"\t"+timeOfRx+"\t"+timeOfTx+"\n");
								//System.out.println(i+":Nt = "+numOfTx+" Nr = "+numOfRx+" tR = "+timeOfRx+" tN = "+timeOfTx);
							}
						}
					}
				}
				for(int i = 0; i < 5; i++){
					bw[i].close();
					bw1[i].close();
					bw2[i].close();
				}
			}
			catch(Exception exc){
				System.out.println("Exception while processing run = "+currentRun);
				exc.printStackTrace();	
			}

	}

	public void extractControlledDelugeStats(String fileLocation){
		int currentRun = -1;
		int cc = -1;
		RunInformation [][]info = new RunInformation[5][numberOfRuns+1];
		BufferedWriter []bw2 = new BufferedWriter[5];
		for (int i = 0; i < 5; i++){
			try{
				bw2[i] = new BufferedWriter(new FileWriter(fileLocation+"Stats"+i+".txt"));
				bw2[i].write("Run\tSource\tNumOfTx\tNumOfReq\tNumOfAdv\tNumOfPkts\tNumOfRx\tRxTime\tTxTtime\n");
			}catch(Exception exc){
				exc.printStackTrace();
			}
			for(int cnt = 0; cnt < numberOfRuns+1; cnt++){
				info[i][cnt] = new RunInformation();
				info[i][cnt].coveredNodes = 0;
				info[i][cnt].endingTime = 0;
				info[i][cnt].runNumber = 0;
				info[i][cnt].totalTx = 0;
				info[i][cnt].startingTime = 0;
			}
		}
		try{
			
			for (int i = 2; i <= numberOfNodes; i++){
					
					BufferedReader reader = new BufferedReader(new FileReader(fileLocation+"Node"+i+".txt"));
					
					
					String line = "";
					while((line = reader.readLine()) != null){
						if(line.startsWith("Run")){
							cc = Integer.parseInt(line.split(" ")[1]);
							currentRun = ( cc - 1)%numberOfRuns + 1 ;
							if(currentRun > numberOfRuns)
								break;
							System.out.println(currentRun + " " + cc);
							info[(cc-1)/numberOfRuns][currentRun].runNumber = currentRun;
							
						}
						else if (line.length() > 0){
							String []temp = line.split(" ");
							int k = 0;
							int numOfTx = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
							int numOfAdv = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
							int numOfReq = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
							int numOfRx = (0xFF & Byte.parseByte(temp[k++])) | ((0xFF & Byte.parseByte(temp[k++])) << 8);
							int timeOfRx = (0xFF & Byte.parseByte(temp[k+3])); 
							timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k+2]));
							timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k+1]));
							timeOfRx = (timeOfRx << 8) | (0xFF & Byte.parseByte(temp[k]));
							k += 4;
							int timeOfTx = (0xFF & Byte.parseByte(temp[k+3])); 
							timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k+2]));
							timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k+1]));
							timeOfTx = (timeOfTx << 8) | (0xFF & Byte.parseByte(temp[k]));
							k += 4;
							bw2[(cc-1)/numberOfRuns].write(currentRun+"\t"+i+"\t"+numOfTx+"\t"+numOfReq+"\t"+numOfAdv+"\t"+(numOfTx+numOfReq+numOfAdv)+"\t"+numOfRx+"\t"+timeOfRx+"\t"+timeOfTx+"\n");
							//System.out.println(i+":Nt = "+(numOfTx+numOfReq+numOfAdv)+" Nr = "+numOfRx+" tR = "+timeOfRx+" tN = "+timeOfTx);
							info[(cc-1)/numberOfRuns][currentRun].totalTx += (numOfTx + numOfAdv + numOfReq);
							
							if(i == 1){
								info[(cc-1)/numberOfRuns][currentRun].startingTime = timeOfRx;
								info[(cc-1)/numberOfRuns][currentRun].coveredNodes++;
							}
							else{
								int timeToCompare = 0;
								if(numOfRx == 24){
									timeToCompare = timeOfRx;
									info[(cc-1)/numberOfRuns][currentRun].coveredNodes++;
								}
								else timeToCompare = timeOfTx;
								
								if(info[(cc-1)/numberOfRuns][currentRun].endingTime < timeToCompare)
									info[(cc-1)/numberOfRuns][currentRun].endingTime = timeToCompare;
							}
							
						}
					}
				}
			for (int i = 0; i < 5; i++)
				bw2[i].close();
			}
			catch(Exception exc){
				exc.printStackTrace();	
			}
			//System.out.println(fileLocation+":");
			float numberOfPkts = 0;
			float delay = 0;
			float reliability = 0;
			for(int i = 1; i< info.length;i++){
				numberOfPkts += info[(cc-1)/numberOfRuns][i].totalTx;
				delay += (info[(cc-1)/numberOfRuns][i].endingTime-info[(cc-1)/numberOfRuns][i].startingTime);
				reliability += ((float)info[(cc-1)/numberOfRuns][i].coveredNodes/numberOfNodes);
				//System.out.println(info[i].runNumber+"\t"+info[i].totalTx + "\t"+(info[i].endingTime-info[i].startingTime)+"\t"+(info[i].coveredNodes));
			}
			//System.out.print("Avg Number of Pkts = "+numberOfPkts/numberOfRuns+"\nAvg Delay = "+delay/numberOfRuns+"\nAvg reliability = "+100*reliability/numberOfRuns);
			System.out.print(numberOfPkts/numberOfRuns+"\t"+100*reliability/numberOfRuns+"\t");
	}
	
}

class Statistics{
	private double []population;
	private int index;
	
	public Statistics(int size){
		index = 0;
		population = new double[size];
	}
	public void insert(long x){
		population[index++] = (double)x;
	}
	public void insert(double x){
		population[index++] = (double)x;
	}
	/**
	 * @param population an array, the population
	 * @return the variance
	 */

	public double max(){
		double max = Double.MIN_VALUE;
		for (double x : population) {
			if (x > max)
				max = x;
		}
		return max;
	}
	
	public double min(){
		double min = Double.MAX_VALUE;
		for (double x : population) {
			if (x < min)
				min = x;
		}
		return min;
	}
	public double mean() {
		long n = 0;
		double mean = 0;
		

		for (double x : population) {
			n++;
			double delta = x - mean;
			mean += delta / n;
			
		}
		// if you want to calculate std deviation
		// of a sample change this to (s/(n-1))
		return mean;
	}

	public double variance() {
		long n = 0;
		double mean = 0;
		double s = 0.0;

		for (double x : population) {
			n++;
			double delta = x - mean;
			mean += delta / n;
			s += delta * (x - mean);
		}
		// if you want to calculate std deviation
		// of a sample change this to (s/(n-1))
		return (s / (n-1));
	}

	/**
	 * @param population an array, the population
	 * @return the standard deviation
	 */
	public double standard_deviation() {
		return Math.sqrt(variance());
	}
	
	public double error(double level, int size){
		double value1;
		double value2;
		return 2.33*(standard_deviation()/(Math.sqrt(size)));
		
	}
}
