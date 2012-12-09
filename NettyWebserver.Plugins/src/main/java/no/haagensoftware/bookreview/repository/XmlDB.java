package no.haagensoftware.bookreview.repository;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import no.haagensoftware.bookreview.repository.query.Parameter;
import no.haagensoftware.bookreview.repository.query.Query;
import no.haagensoftware.bookreview.repository.query.QueryTree;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XmlDB {
	private Driver drv = null;
	private Connection conn = null;
	private ResultSet rs = null;
	private Statement stmt = null;
	private DataSource dataSource = null;
	QueryTree tree = null;
	private String host;
	private String db;
	private String user;
	private String pass;
	private boolean pooled = false;
	private String queryfile;
	private String errs = "";
	private boolean hasResultSet = false;
	private String configFileName = "";
	private static Logger log = Logger.getLogger(XmlDB.class);

	public XmlDB(String dbName, String dbHost, String dbUser, String dbPass, String dbQueryFile) {
		this.db = dbName;
		this.host = dbHost;
		this.user = dbUser;
		this.pass = dbPass;
		this.queryfile = dbQueryFile;
		
		log.info("Connecting to DB with. \nHost: " + host + "\nUser: " + user + "\nPass: " + pass + "\nQueryFile: " + queryfile);

		try {
			errs += "Parsing XML to TreeMap <br>";
			log.error("Parsing XML to TreeMap");
			tree = new QueryTree("sqlquery", queryfile);
			errs += "Finished parsing XML to TreeMap <br>";
		} catch (Exception e) {
			errs += "unable to parse queryfile: " + e + "<br>";
			e.printStackTrace();
			log.error("unable to parse queryfile: " + e);
		}

	}

	public String getConfigFileName() {
		return configFileName;
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String printErrMsgs() {
		return errs.replaceAll("\n", "<br />");
	}

	public String getNodeValue(Element root, String targetName) {
		NodeList elements = root.getElementsByTagName(targetName);
		if (elements.getLength() > 0)
			return ((Element) elements.item(0)).getFirstChild().getNodeValue();

		return "No value for: " + targetName;
	}

	private javax.sql.DataSource getPoolDB() throws javax.naming.NamingException {
		log.debug("Looking up DataSource: " + db);
		javax.naming.Context c = new javax.naming.InitialContext();
		return (DataSource) c.lookup(db);
	}

	public void connectDB() {
		if (pooled) {
			log.debug("Getting DB from Pool");
			try {
				if (dataSource == null) {
					dataSource = getPoolDB();
				}

				try {
					conn = dataSource.getConnection();
				} catch (SQLException e) {
					errs += "connectDB(): unable to connect to DB: " + e + "<br>";
					log.error("connectDB(): unable to connect to DB: " + e);
				}
			} catch (NamingException e) {
				errs += "connectDB: Unable to get Pool for DSJDB: " + e;

				log.error("Unable to get Pool for DSJDB: " + e);
			}
		} else {
			try {
				log.error("Creating DB Connection");
				errs += "Creating DB Connection <br>";
				// com.mysql.jdbc.Driver
				// org.postgresql.Driver
				// org.gjt.mm.mysql.Driver
				drv = (Driver) Class.forName("org.gjt.mm.mysql.Driver").newInstance();
				// drv =
				// (Driver)Class.forName("com.mysql.jdbc.Driver").newInstance();

				errs += "Driver created <br>";

				// jdbc:mysql://localhost/
				// jdbc:postgresql://localhost/
				conn = DriverManager.getConnection("jdbc:mysql://" + host + "/" + db + "?user=" + user + "&password=" + pass + "&autoreconnect=true");
			} catch (Exception e) {
				errs += "connectDB(): unable to connect to DB: " + e + "<br>";

				log.error("connectDB(): unable to connect to DB: " + e);
			}
		}
	}

	public void closeConnection() {
		log.debug("Closing connection / Resetting connection back to pool");
		try {
			conn.close();
		} catch (SQLException e) {
			log.error("Unable to close connection: " + e);
		}
	}

	public boolean next() throws SQLException {
		if (hasResultSet)
			return rs.next();
		else
			return false;
	}

	public String getString(String columnName) throws Exception {
		return rs.getString(columnName);
	}

	public int getInt(String columnName) throws Exception {
		return rs.getInt(columnName);
	}

	public Date getDate(String columnName) throws Exception {
		return rs.getDate(columnName);
	}

	public double getDouble(String columnName) throws Exception {
		return rs.getDouble(columnName);
	}

	public ResultSet getResultSet() throws SQLException {
		return rs;
	}

	public Long getLong(int columnIndex) throws SQLException {
		return rs.getLong(columnIndex);
	}

	public boolean executeQuery(String name, List<Parameter> parameters) {
		PreparedStatement statement = null;
		try {
			if (conn == null || conn.isClosed())
				connectDB();

			Query query = tree.getQuery(name, parameters);

			if (query != null) {
				String sql = query.getQuery();

				statement = conn.prepareStatement(sql);
				statement = setParameters(statement, parameters);
				log.debug(query.getQuery());
				log.debug("Parameters: " + parameters.toString());

				try {
					rs.close();
				} catch (Exception e) {
				}
				rs = statement.executeQuery();

				hasResultSet = rs.first();
				rs.beforeFirst();
			} else {
				hasResultSet = false;
				errs += "DBConn: executeQuery: No query matching name and params:" + name + " - " + parameters.toString() + "<br>";

				log.error("DBConn: executeQuery: No query matching name and params:" + name + " - " + parameters.toString());
				return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				// statement.close();
			} catch (Exception e) {
			}
		}

		return hasResultSet;
	}

	public List executeQuery(String name, List<Parameter> parameters, Class clazz) throws SQLException {
		List retList = new ArrayList();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			if (conn == null || conn.isClosed())
				connectDB();

			Query query = tree.getQuery(name, parameters);

			if (query != null) {
				String sql = query.getQuery();

				statement = conn.prepareStatement(sql);
				statement = setParameters(statement, parameters);
				log.debug(query.getQuery());
				log.debug("Parameters: " + parameters.toString());

				resultSet = statement.executeQuery();
				while (resultSet.next()) {
					Object obj;
					try {
						obj = clazz.newInstance();

						if (obj instanceof DSJDBPopulatedObject) {
							((DSJDBPopulatedObject) obj).populate(resultSet);
							retList.add((obj));
						}
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				errs += "DBConn: executeQuery: No query matching name and params:" + name + " - " + parameters.toString() + "<br>";
				log.error("DBConn: executeQuery: No query matching name and params:" + name + " - " + parameters.toString());
			}
		} finally {
			try {
				resultSet.close();
				statement.close();
			} catch (Exception e) {
			}
		}

		return retList;
	}

	public Boolean executeUpdate(String name, List<Parameter> parameters) {
		boolean success = false;
		PreparedStatement statement = null;
		try {
			try {
				rs.close();
			} catch (Exception e) {
			}
			if (conn == null || conn.isClosed()) {
				connectDB();
			}

			Query query = tree.getQuery(name, parameters);

			if (query != null) {
				statement = conn.prepareStatement(query.getQuery());

				statement = setParameters(statement, parameters);
				log.debug(query.getQuery());
				log.debug("Parameters: " + parameters.toString());

				hasResultSet = false;
				int numUpdatedRows = statement.executeUpdate();
				success = numUpdatedRows > 0;
			} else {
				errs += "DBConn: executeUpdate: No query matching name and params:" + name + " - " + parameters.toString() + "<br>";
				log.error("DBConn: executeUpdate: No query matching name and params:" + name + " - " + parameters.toString());
				return null;
			}
		} catch (SQLException e) {
			log.error("DSJDB: Exception while executing update: " + e, e);
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (Exception e) {
			}
		}

		return success;
	}

	private PreparedStatement setParameters(PreparedStatement statement, List<Parameter> parameters) throws SQLException {
		// For each parameter check it's type and set it's value in the
		// statement.
		for (int i = 0; i < parameters.size(); i++) {
			Parameter parameter = parameters.get(i);
			// log.debug("setParameters(): " + parameter.getIndex() + " - " +
			// parameter.getName() + " - " + parameter.getValue());
			Object param = parameter.getValue();
			if (param instanceof Integer) {
				statement.setInt(parameter.getIndex(), (Integer) param);
			} else if (param instanceof Long) {
				statement.setLong(parameter.getIndex(), (Long) param);
			} else if (param instanceof Timestamp) {
				statement.setTimestamp(parameter.getIndex(), (Timestamp) param);
			} else if (param instanceof Double) {
				statement.setDouble(parameter.getIndex(), (Double) param);
			} else if (param instanceof String) {
				statement.setString(parameter.getIndex(), (String) param);
			} else {
				statement.setObject(parameter.getIndex(), param);
			}
		}

		return statement;
	}

	public int numRows() throws Exception {
		rs.last();
		int pos = rs.getRow();
		rs.beforeFirst();
		return pos;
	}

	public void resetPosition() throws Exception {
		rs.beforeFirst();
	}
}