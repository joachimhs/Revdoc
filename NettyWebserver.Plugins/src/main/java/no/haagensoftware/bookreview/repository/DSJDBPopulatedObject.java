package no.haagensoftware.bookreview.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface DSJDBPopulatedObject {
	public void populate(ResultSet resultset) throws SQLException;
}
