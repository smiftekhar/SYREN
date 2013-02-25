
configuration AutoStarterC
{
  uses interface SplitControl;
}

implementation
{
  components MainC, AutoStarterP;
  
  SplitControl = AutoStarterP;
  AutoStarterP.Boot -> MainC;
}
