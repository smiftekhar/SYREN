
module AutoStarterP
{
  uses {
    interface Boot;
    interface SplitControl;
  }
}

implementation
{
  event void Boot.booted()
  {
    call SplitControl.start();
  }
  
  event void SplitControl.startDone(error_t error) { }
  event void SplitControl.stopDone(error_t error) { }

}
