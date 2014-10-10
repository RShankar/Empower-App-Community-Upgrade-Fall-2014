package edu.fau.communityupgrade.application;

import com.parse.ParseACL;


public class DefaultParseACL extends ParseACL{

	public DefaultParseACL()
	{
		super();
		
		//Allow user to read 
		this.setPublicReadAccess(true);
	}
	
}
