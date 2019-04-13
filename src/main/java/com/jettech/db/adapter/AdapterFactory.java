package com.jettech.db.adapter;

import com.jettech.EnumDatabaseType;

public class AdapterFactory {
	public static AbstractAdapter create(EnumDatabaseType type) throws Exception {
		AbstractAdapter adapter = null;
		switch (type) {
		case Mysql:
			adapter = new MySqlAdapter();
			break;
		case Oracle:
			adapter = new OracleAdapter();
			break;
		case DB2:
			adapter = new DB2Adapter();
			break;
		case Informix:
			adapter = new InformixAdapter();
			break;
		case SyBase:
			adapter = new SyBaseAdapter();
			break;
		default:
			throw new Exception("not support database type.");
		}
		return adapter;
	}
}
