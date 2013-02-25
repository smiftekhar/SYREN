
configuration SerialStarterC { }

implementation
{
  components SerialActiveMessageC, AutoStarterC;
  
  AutoStarterC.SplitControl -> SerialActiveMessageC;
}
