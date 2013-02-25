#include <stdio.h>
#include "NetworkCoding.h"
// Acknowdlegement to original Rateless Deluge implementation
void initializeCoder(unsigned char numOfPackets, unsigned char numOfBytes, unsigned char prim){
      
	NC_PKTS_PER_BATCH = numOfPackets;
	NC_BYTES_PER_PKT = numOfBytes;
	primitive = prim;
	
  	moddiv[0]=0;		moddiv[1]=1;		moddiv[2]=142;		moddiv[3]=244;		moddiv[4]=71;		moddiv[5]=167;		moddiv[6]=122;		moddiv[7]=186;
	moddiv[8]=173;		moddiv[9]=157;		moddiv[10]=221;		moddiv[11]=152;		moddiv[12]=61;		moddiv[13]=170;		moddiv[14]=93;		moddiv[15]=150;
	moddiv[16]=216;		moddiv[17]=114;		moddiv[18]=192;		moddiv[19]=88;		moddiv[20]=224;		moddiv[21]=62;		moddiv[22]=76;		moddiv[23]=102;
	moddiv[24]=144;		moddiv[25]=222;		moddiv[26]=85;		moddiv[27]=128;		moddiv[28]=160;		moddiv[29]=131;		moddiv[30]=75;		moddiv[31]=42;
	moddiv[32]=108;		moddiv[33]=237;		moddiv[34]=57;		moddiv[35]=81;		moddiv[36]=96;		moddiv[37]=86;		moddiv[38]=44;		moddiv[39]=138;
	moddiv[40]=112;		moddiv[41]=208;		moddiv[42]=31;		moddiv[43]=74;		moddiv[44]=38;		moddiv[45]=139;		moddiv[46]=51;		moddiv[47]=110;
	moddiv[48]=72;		moddiv[49]=137;		moddiv[50]=111;		moddiv[51]=46;		moddiv[52]=164;		moddiv[53]=195;		moddiv[54]=64;		moddiv[55]=94;
	moddiv[56]=80;		moddiv[57]=34;		moddiv[58]=207;		moddiv[59]=169;		moddiv[60]=171;		moddiv[61]=12;		moddiv[62]=21;		moddiv[63]=225;
	moddiv[64]=54;		moddiv[65]=95;		moddiv[66]=248;		moddiv[67]=213;		moddiv[68]=146;		moddiv[69]=78;		moddiv[70]=166;		moddiv[71]=4;
	moddiv[72]=48;		moddiv[73]=136;		moddiv[74]=43;		moddiv[75]=30;		moddiv[76]=22;		moddiv[77]=103;		moddiv[78]=69;		moddiv[79]=147;
	moddiv[80]=56;		moddiv[81]=35;		moddiv[82]=104;		moddiv[83]=140;		moddiv[84]=129;		moddiv[85]=26;		moddiv[86]=37;		moddiv[87]=97;
	moddiv[88]=19;		moddiv[89]=193;		moddiv[90]=203;		moddiv[91]=99;		moddiv[92]=151;		moddiv[93]=14;		moddiv[94]=55;		moddiv[95]=65;
	moddiv[96]=36;		moddiv[97]=87;		moddiv[98]=202;		moddiv[99]=91;		moddiv[100]=185;	moddiv[101]=196;	moddiv[102]=23;		moddiv[103]=77;
	moddiv[104]=82;		moddiv[105]=141;	moddiv[106]=239;	moddiv[107]=179;	moddiv[108]=32;		moddiv[109]=236;	moddiv[110]=47;		moddiv[111]=50;
	moddiv[112]=40;		moddiv[113]=209;	moddiv[114]=17;		moddiv[115]=217;	moddiv[116]=233;	moddiv[117]=251;	moddiv[118]=218;	moddiv[119]=121;
	moddiv[120]=219;	moddiv[121]=119;	moddiv[122]=6;		moddiv[123]=187;	moddiv[124]=132;	moddiv[125]=205;	moddiv[126]=254;	moddiv[127]=252;
	moddiv[128]=27;		moddiv[129]=84;		moddiv[130]=161;	moddiv[131]=29;		moddiv[132]=124;	moddiv[133]=204;	moddiv[134]=228;	moddiv[135]=176;
	moddiv[136]=73;		moddiv[137]=49;		moddiv[138]=39;		moddiv[139]=45;		moddiv[140]=83;		moddiv[141]=105;	moddiv[142]=2;		moddiv[143]=245;
	moddiv[144]=24;		moddiv[145]=223;	moddiv[146]=68;		moddiv[147]=79;		moddiv[148]=155;	moddiv[149]=188;	moddiv[150]=15;		moddiv[151]=92;
	moddiv[152]=11;		moddiv[153]=220;	moddiv[154]=189;	moddiv[155]=148;	moddiv[156]=172;	moddiv[157]=9;		moddiv[158]=199;	moddiv[159]=162;
	moddiv[160]=28;		moddiv[161]=130;	moddiv[162]=159;	moddiv[163]=198;	moddiv[164]=52;		moddiv[165]=194;	moddiv[166]=70;		moddiv[167]=5;
	moddiv[168]=206;	moddiv[169]=59;		moddiv[170]=13;		moddiv[171]=60;		moddiv[172]=156;	moddiv[173]=8;		moddiv[174]=190;	moddiv[175]=183;
	moddiv[176]=135;	moddiv[177]=229;	moddiv[178]=238;	moddiv[179]=107;	moddiv[180]=235;	moddiv[181]=242;	moddiv[182]=191;	moddiv[183]=175;
	moddiv[184]=197;	moddiv[185]=100;	moddiv[186]=7;		moddiv[187]=123;	moddiv[188]=149;	moddiv[189]=154;	moddiv[190]=174;	moddiv[191]=182;
	moddiv[192]=18;		moddiv[193]=89;		moddiv[194]=165;	moddiv[195]=53;		moddiv[196]=101;	moddiv[197]=184;	moddiv[198]=163;	moddiv[199]=158;
	moddiv[200]=210;	moddiv[201]=247;	moddiv[202]=98;		moddiv[203]=90;		moddiv[204]=133;	moddiv[205]=125;	moddiv[206]=168;	moddiv[207]=58;
	moddiv[208]=41;		moddiv[209]=113;	moddiv[210]=200;	moddiv[211]=246;	moddiv[212]=249;	moddiv[213]=67;		moddiv[214]=215;	moddiv[215]=214;
	moddiv[216]=16;		moddiv[217]=115;	moddiv[218]=118;	moddiv[219]=120;	moddiv[220]=153;	moddiv[221]=10;		moddiv[222]=25;		moddiv[223]=145;
	moddiv[224]=20;		moddiv[225]=63;		moddiv[226]=230;	moddiv[227]=240;	moddiv[228]=134;	moddiv[229]=177;	moddiv[230]=226;	moddiv[231]=241;
	moddiv[232]=250;	moddiv[233]=116;	moddiv[234]=243;	moddiv[235]=180;	moddiv[236]=109;	moddiv[237]=33;		moddiv[238]=178;	moddiv[239]=106;
	moddiv[240]=227;	moddiv[241]=231;	moddiv[242]=181;	moddiv[243]=234;	moddiv[244]=3;		moddiv[245]=143;	moddiv[246]=211;	moddiv[247]=201;
	moddiv[248]=66;		moddiv[249]=212;	moddiv[250]=232;	moddiv[251]=117;	moddiv[252]=127;	moddiv[253]=255;	moddiv[254]=126;	moddiv[255]=253;

}

unsigned char modular_mult(unsigned short a, unsigned short b, unsigned char prim)
{

	unsigned short product=0;
	unsigned short ahigh;
	unsigned char mytemp = 0;
	for(mytemp=0;mytemp<8;mytemp++)
	{
   			if((b&1)==1)
   			{	
   				product=product^a;
   				product=product&255;	
   			}
   			ahigh=(a&(128));				
   			a=a<<1;						
   			if(ahigh==128)
   			{   a=a^prim;   }			
			b=b>>1;
	}
	
	return product;
}

unsigned short random_number(unsigned short * myseed)
 {
	uint32_t mlcg,p,q ;
	uint64_t tmpseed;
	unsigned short myvar;
 
	
	tmpseed =  (unsigned long)33614U * (unsigned long)*myseed;
	q = (uint32_t)(tmpseed & 0xFFFFFFFF);			
	q = q >> 1;
	p = (uint32_t)(tmpseed >> 32) ;					
	mlcg = p + q;
	*myseed=mlcg&((uint32_t)65535U);
    
	
	p=0;
	q=255;
	for(myvar=1; myvar<257; myvar++)
	{	

		if(*myseed >=p && *myseed <= q)
		{
			return (myvar-1);
			
		}
		p=q;
		q=q+256;
	}

    return 255;
 }

 
 void encode (unsigned char **XPtr, unsigned char numOfPackets, unsigned char payload[], unsigned short packetnumber){

	unsigned short myseed=8090+packetnumber;
	unsigned char B;
	unsigned char temp,temp1;
	
	for(temp1=0; temp1<NC_BYTES_PER_PKT; temp1++)
		payload[temp1]=0;

	for(temp=0; temp<numOfPackets; temp++)
	{   
		B=random_number(&myseed);
		for(temp1=0; temp1<NC_BYTES_PER_PKT; temp1++)
			payload[temp1]=payload[temp1]^modular_mult(XPtr[temp][temp1],B,primitive);			

	}
   
}

int decode(unsigned char **XPtr, unsigned char **BPtr, unsigned short xid[], int togenerate)
{

	unsigned short myseed;
	unsigned char outer,inner;
	unsigned char maxval;
	unsigned char maxind;
	unsigned char i=0;
	unsigned char j=0;	
	unsigned char * temprow; 
	unsigned char temp1;	
	int ld=0;
	unsigned char my1;
	//unsigned char *BPtr[NC_PKTS_PER_BATCH];
	unsigned char returnVal = -1;
	unsigned short temp;
	
	
	if (togenerate > 0){
	  for(i=togenerate; i>0; i--)
	      {	
		myseed=8090+xid[NC_PKTS_PER_BATCH-i];
		for(inner=0; inner<NC_PKTS_PER_BATCH; inner++)
		  BPtr[NC_PKTS_PER_BATCH-i][inner]=random_number(&myseed);	
	      }
	}
	else{
	  for(outer=0; outer<NC_PKTS_PER_BATCH; outer++)
	  {
		  myseed=8090+xid[outer];
		  for(inner=0; inner<NC_PKTS_PER_BATCH; inner++)
		      BPtr[outer][inner]=random_number(&myseed);	
		  
	  }
	}
	i=0; j=0;
	while( (i<NC_PKTS_PER_BATCH) && (j<(NC_PKTS_PER_BATCH+1)) )
	{
		//find maximum value in current row				
		maxval=BPtr[i][j];
		maxind=i;

		for(my1=i; my1<NC_PKTS_PER_BATCH; my1++)
		{	
			if(BPtr[my1][j]>maxval)
			{
				maxval=BPtr[my1][j];
				maxind=my1;
			}
		}

		if(maxval!=0)
		{
			//switch rows of matrix
			
			temprow=(unsigned char *)BPtr[i];
			BPtr[i]=BPtr[maxind];
			BPtr[maxind]=(unsigned char *)temprow;

			temp=xid[i];
			xid[i]=xid[maxind];
			xid[maxind]=temp;

			//switch rows of Y

			temprow=XPtr[i];
			XPtr[i]=XPtr[maxind];
			XPtr[maxind]=temprow;

				//Divide row i by maxval;

			for(temp1=0; temp1<NC_PKTS_PER_BATCH; temp1++)
				BPtr[i][temp1]=modular_mult(BPtr[i][temp1],moddiv[maxval],primitive)&255;
			
			for(temp1=0; temp1<NC_BYTES_PER_PKT; temp1++)
				XPtr[i][temp1]=modular_mult(XPtr[i][temp1],moddiv[maxval],primitive)&255;
				
			for(my1=i+1; my1<NC_PKTS_PER_BATCH; my1++)       //subtract A[u,j] * i from row u for all rows
			{		
				maxval=BPtr[my1][j];
				for(temp1=j; temp1<NC_PKTS_PER_BATCH; temp1++)
					BPtr[my1][temp1]=BPtr[my1][temp1]^modular_mult(BPtr[i][temp1],maxval,primitive);	

				for(temp1=0; temp1<NC_BYTES_PER_PKT; temp1++)
					XPtr[my1][temp1]=XPtr[my1][temp1]^modular_mult(XPtr[i][temp1],maxval,primitive);
			}				
		}
		i++;
		j++;
	}

	

		//Back Substitution
	
	togenerate=0;
	ld=0;	
	
	
	if(BPtr[NC_PKTS_PER_BATCH-1][NC_PKTS_PER_BATCH-1]==0)
	{	j=NC_PKTS_PER_BATCH-1;
		for(i=NC_PKTS_PER_BATCH-1; i!=255; i--)
		{
		  if(BPtr[i][j]==0)
				togenerate++;
			else			
				break;
			j--;
		}
	}
	else
		ld=1;

	//printf("IsIndepe = %d\n",ld);    
	if(ld)
	{
	    for(i=NC_PKTS_PER_BATCH-2; i!=255; i--)	
		    for(j=i+1; j<NC_PKTS_PER_BATCH; j++)
			    for(my1=0; my1<NC_BYTES_PER_PKT; my1++) 	
				    XPtr[i][my1]=XPtr[i][my1]^modular_mult(XPtr[j][my1],BPtr[i][j],primitive);
	    
	    returnVal  = NC_PKTS_PER_BATCH;
	}
	else
	  returnVal = NC_PKTS_PER_BATCH-togenerate;
	
	
	return returnVal;
		
}

