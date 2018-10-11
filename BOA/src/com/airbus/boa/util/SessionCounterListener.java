package com.airbus.boa.util;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.airbus.boa.view.AbstractController;
import com.airbus.boa.view.LogInController;
import com.airbus.boa.view.application.ApplicationController;

public class SessionCounterListener implements HttpSessionListener {
	
  @Override
  public void sessionCreated(HttpSessionEvent pEvent) {
	ApplicationController lApplicationController =
                AbstractController.findBean(ApplicationController.class);
        lApplicationController.setTotalActiveSessions(
                lApplicationController.getTotalActiveSessions() + 1);
  }
    
  @Override
  public void sessionDestroyed(HttpSessionEvent pEvent) {
		ApplicationController lApplicationController = (ApplicationController) pEvent.getSession().getServletContext().getAttribute("applicationController");
		LogInController lLoginController = (LogInController) pEvent.getSession().getAttribute("logInController");
		
		lApplicationController.setTotalActiveSessions(lApplicationController.getTotalActiveSessions() - 1);
				
		//remove user of the connectedUser
        lApplicationController
                .removeConnectedUser(lLoginController.getUserLogged());
  }	
}