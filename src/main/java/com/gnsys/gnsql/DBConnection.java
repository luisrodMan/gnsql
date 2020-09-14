package com.gnsys.gnsql;

import java.sql.Connection;
import java.sql.SQLException;

public interface DBConnection {

	String getName();
	
	Connection createConnection() throws ClassNotFoundException, SQLException;
	
}
