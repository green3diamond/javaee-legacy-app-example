package com.example;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class RegisterAction extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO (Task 3): Refactor this method to use Spring Service instead of EJB
		// NOTE: The EJB JNDI lookup below will be removed in Task 3 and replaced with Spring @Service injection
		/*
		Context context = new InitialContext();
		Object ref = context.lookup("com.example/RegistrationEJB");
		Object javaRef = PortableRemoteObject.narrow(ref, RegistrationHome.class);
		RegistrationHome registrationHome = (RegistrationHome) javaRef;
		RegistrationEJB registrationEJB = registrationHome.create();
		String value = registrationEJB.register("", "");
		registrationEJB.remove();
		*/
		
		String value = "Registration processed";  // Temporary: will be replaced by service call in Task 3
		System.out.println(value);
		
		return (mapping.findForward("success"));
	}

}