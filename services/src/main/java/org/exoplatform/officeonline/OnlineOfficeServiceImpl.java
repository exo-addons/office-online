package org.exoplatform.officeonline;

import org.picocontainer.Startable;

import org.exoplatform.container.xml.InitParams;

public class OnlineOfficeServiceImpl implements OnlineOfficeService, Startable {
  
  private final String discoveryUrl;
  
  public OnlineOfficeServiceImpl(InitParams params) {
    discoveryUrl = params.getValueParam("discovery-url").getValue();
  }
  

  @Override
  public void start() {
   
    
  }

  @Override
  public void stop() {
    // TODO Auto-generated method stub
    
  }

}
