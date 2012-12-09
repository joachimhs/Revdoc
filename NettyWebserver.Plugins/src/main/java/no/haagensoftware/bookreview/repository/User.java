package no.haagensoftware.bookreview.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User implements DSJDBPopulatedObject {
	private String userId;
	
	@Override
	public void populate(ResultSet resultset) throws SQLException {
		this.userId = resultset.getString("UserId");
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}

}
