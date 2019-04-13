package com.jettech.db;

import org.slf4j.Logger;

//import org.apache.log4j.Logger;

//import org.slf4j.Logger;

abstract public class AbstractModel {

	abstract public StringBuilder buildInfo();

	final public void printInfo(Logger logger) {
		
		logger.info(buildInfo().toString());
		
	}

	final public String getPrintInfo() {
		
		return buildInfo().toString();
		
	}
}
