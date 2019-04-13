package com.jettech;

public enum EnumDatabaseType {
	Oracle("Oracle"), Mysql("Mysql"), DB2("DB2"), Informix("Informix"), SyBase("SyBase");
	private String _name;

	private EnumDatabaseType(String databaseTypeName) {
		_name = databaseTypeName;
	}

	public String getName() {
		return _name;
	}


}
