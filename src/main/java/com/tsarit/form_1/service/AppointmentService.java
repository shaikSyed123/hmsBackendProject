package com.tsarit.form_1.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.tsarit.form_1.config.JwtUtil;
import com.tsarit.form_1.model.Getintouch;
import com.tsarit.form_1.model.SupportTicket;
import com.tsarit.form_1.model.userData;
import com.tsarit.form_1.repository.GetintouchRepository;
import com.tsarit.form_1.repository.SupportticketRepository;
import com.tsarit.form_1.repository.userRepository;
import com.tsarit.form_1.responses.LoginResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.Sides;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

@Service
public class AppointmentService {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private DataSource dataSource;

	@Autowired
	private userRepository userRepository;

	@Autowired
	private SupportticketRepository supportticketRepository;

	@Autowired
	private GetintouchRepository getintouchRepository;

	@Autowired
	private JwtUtil jwtUtii;
	@Autowired
	private LoginResponse LoginResponse;

	@Autowired
	private HttpServletRequest request;

	public void savetouch(Getintouch getintouch) {
		getintouchRepository.save(getintouch);
	}

	public void saveticket(SupportTicket supportTicket) {
		supportticketRepository.save(supportTicket);
	}

	public List<SupportTicket> getAlltickets() {
		return supportticketRepository.findAll();
	}

	public List<userData> getAllHospitals() {
		return userRepository.findAll();
	}

	public List<Getintouch> getAllTouchMessages() {
		return getintouchRepository.findAll();
	}

	@Autowired
	private CurrentUserUtil currentUserUtil;
//       public String getCurrentUserEmail() {
////	        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
////	        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
////	            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
////	            return userDetails.getUsername(); // email is stored as username
////	        }
////	        return null; // Handle case where no user is authenticated
//    	   String token=LoginResponse.getToken();
//	        System.out.println(token);
//          String	username=jwtUtii.getUsernameFromToken(token);
//          return username; 
//	    }
//	    
//	    public String getLoggedInUsername() {
////			org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
////			if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
////				 UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//	    	 String token=LoginResponse.getToken();
//		        System.out.println(token);
//	           String	username=jwtUtii.getUsernameFromToken(token);
//	             System.out.println("username LLLLLLLoooooovvvvveeee :"+username );
//				// Sanitize the username to create a valid table name
//				String sanitizedUsername = username.replaceAll("[^a-zA-Z0-9]", "_");
//				String tableName = "user_" + sanitizedUsername;
//
//				System.out.println("Sanitized Username: " + sanitizedUsername);
//				System.out.println("Table Name: " + tableName);
//
//				return tableName;
////			}
////			return null;
//		}

	private String loggedInUserEmail;

	public void storeLoggedInUserEmail(String emailid) {
		this.loggedInUserEmail = emailid;
		UserContext.setCurrentUserEmail(emailid);
	}

	public String getCurrentUserEmail() {
		System.out.println("loggedInUserEmail is :" + this.loggedInUserEmail);
		return this.loggedInUserEmail;
//    		return UserContext.getCurrentUserEmail();
	}

	public String getLoggedInUsername() {
		String username = getCurrentUserEmail();
		System.out.println("username :" + username);
		String sanitizedUsername = username.replaceAll("[^a-zA-Z0-9]", "_");
		String tableName = "user_" + sanitizedUsername;
		System.out.println("Sanitized Username: " + sanitizedUsername);
		System.out.println("Table Name: " + tableName);
		return tableName;
	}

	public userData find(String email) {
		return userRepository.findByemailid(email);
	}

	public void updateLastLoginDate() {
		String username = getCurrentUserEmail();
		userData user = this.userRepository.findByemailid(username);
		user.setLastLoginTimeDate(LocalDateTime.now());
		this.userRepository.save(user);
	}

	public LocalDateTime getLastLoginDate() {
		String username = getCurrentUserEmail();
		userData user = userRepository.findByemailid(username);
		return user.getLastLoginTimeDate();
	}

	public userData data() {
		String email = getCurrentUserEmail(); // Assuming 'emailid' is used as the principal
		System.out.println("Email retrieved from authentication: " + email);

		userData user = userRepository.findByemailid(email); // Query based on email
		if (user == null) {
			System.out.println("No user found with email: " + email);
		}
		return user;
	}

	public void createUserTables(String username) {
//		username = getLoggedInUsername();
		String sanitizedUsername = username.replaceAll("[^a-zA-Z0-9]", "_");
		String tableName = "user_" + sanitizedUsername;

		String checkTableQuery = "SHOW TABLES LIKE ?";
		List<String> tables = jdbcTemplate.queryForList(checkTableQuery, new Object[] { tableName }, String.class);
		if (tables.size() > 1) {
			throw new IllegalStateException("Multiple tables found with the name: " + tableName);
		}
		// If the table does not exist, create it
		if (tables.isEmpty()) {
			String createTableQuery = "CREATE TABLE " + tableName + " (" + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
					+ "firstName VARCHAR(100) NOT NULL, " + "lastName VARCHAR(100), " + "email VARCHAR(100), "
					+ "phoneNumber VARCHAR(15), " + "aadharNumber VARCHAR(20), " + "address TEXT, "
					+ "gender VARCHAR(10), " + "disease VARCHAR(255), " + "otherDisease VARCHAR(255), "
					+ "day VARCHAR(20), " + "month VARCHAR(20), " + "year VARCHAR(20), " + "age VARCHAR(20), "
					+ "modeOfPayment VARCHAR(50), " + "amount DECIMAL(10,2), " + "upiTransactionNo VARCHAR(100), "
					+ "netBankingTransactionId VARCHAR(100), " + "netBankingScreenshot LONGBLOB, "
					+ "accountTransactionId VARCHAR(100), " + "accountDocument LONGBLOB, " + "reference VARCHAR(255), "
					+ "insurance VARCHAR(100), " + "otherPayment VARCHAR(100), " + "weight DECIMAL(5,2), "
					+ "bp VARCHAR(10), " + "appointmentTaken VARCHAR(20), " + "appointmentDetails TEXT, "
					+ "modeOfPatient VARCHAR(50), " + "bedAssign VARCHAR(20), " + "bedDetails TEXT, "
					+ "bedNo VARCHAR(10), " + "bedDays VARCHAR(30), " + "date DATE, " + "time TIME" + ")";

			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			jdbcTemplate.execute(createTableQuery);
			System.out.println("Created table for user: " + tableName);
		}

		String treatment = tableName + "_treatment";
		List<String> tables2 = jdbcTemplate.queryForList(checkTableQuery, new Object[] { treatment }, String.class);
		if (tables2.size() > 1) {
			throw new IllegalStateException("Multiple tables found with the name: " + treatment);
		}

		// Create the second table with a foreign key
		if (tables2.isEmpty()) {
			String createSecondTableQuery = "CREATE TABLE " + treatment + "(" + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
//                + "patientTreatment TEXT, "
					+ "tabletCount VARCHAR(255), " + "tabletName1 VARCHAR(255), " + "tabletName2 VARCHAR(255), "
					+ "tabletName3 VARCHAR(255), " + "tabletName4 VARCHAR(255), " + "tabletName5 VARCHAR(255), "
					+ "tabletName6 VARCHAR(255), " + "tabletName7 VARCHAR(255), " + "tabletName8 VARCHAR(255), "
					+ "tabletName9 VARCHAR(255), " + "tabletName10 VARCHAR(255), " + "otherTabletName VARCHAR(255), "
					+ "injectionSize VARCHAR(255)," + "injectionName VARCHAR(255)," + "injectionMg VARCHAR(255),"
					+ "tests VARCHAR(255), " + "doctorAdvice TEXT, " + "patientId BIGINT, "
					+ "FOREIGN KEY (patientId) REFERENCES " + tableName + "(id) ON DELETE CASCADE" + ")";

			jdbcTemplate.execute(createSecondTableQuery);
			System.out.println("Created treatment table for user: " + tableName);
		} else {
			System.out.println("Table already exists: " + tableName);
		}

		String lab = tableName + "_lab";
		List<String> tables3 = jdbcTemplate.queryForList(checkTableQuery, new Object[] { lab }, String.class);
		if (tables2.size() > 1) {
			throw new IllegalStateException("Multiple tables found with the name: " + lab);
		}

		// Create the second table with a foreign key
		if (tables3.isEmpty()) {
			String sql = "CREATE TABLE IF NOT EXISTS " + lab + " (" + "id INT PRIMARY KEY AUTO_INCREMENT, "
					+ "testName VARCHAR(255), "
//				    + "patientId INT, "
					+ "testDate DATE, " + "result VARCHAR(255), " + "doctorName VARCHAR(255), "
					+ "technician VARCHAR(255), " + "sampleType VARCHAR(255), " + "testCost DECIMAL(10, 2), "
					+ "insuranceProvider VARCHAR(255), " + "status VARCHAR(100), " + "notes TEXT,"
					+ "patientId BIGINT, " + "FOREIGN KEY (patientId) REFERENCES " + tableName
					+ "(id) ON DELETE CASCADE" + ")";
			jdbcTemplate.execute(sql);
		}
		String Pharmacy = tableName + "_Pharmacy";
		List<String> tables4 = jdbcTemplate.queryForList(checkTableQuery, new Object[] { Pharmacy }, String.class);
		if (tables2.size() > 1) {
			throw new IllegalStateException("Multiple tables found with the name: " + Pharmacy);
		}

		// Create the second table with a foreign key
		if (tables4.isEmpty()) {
			String sql = "CREATE TABLE IF NOT EXISTS " + Pharmacy + " (" + "id INT PRIMARY KEY AUTO_INCREMENT, "
					+ "medicineName VARCHAR(255), " + "quantity INT, " + "expiryDate DATE, " + "price DECIMAL(10, 2), "
					+ "batchNumber VARCHAR(100), " + "manufacturer VARCHAR(255), " + "supplierName VARCHAR(255), "
					+ "prescriptionRequired VARCHAR(3) DEFAULT 'No', " + "dosageForm VARCHAR(255), "
					+ "stockStatus VARCHAR(50) DEFAULT 'Available', " + "discount DECIMAL(5, 2), " + "notes TEXT"
//				    + "patientId BIGINT, "
//	                + "FOREIGN KEY (patientId) REFERENCES " + tableName + "(id) ON DELETE CASCADE"
					+ ")";
			jdbcTemplate.execute(sql);
		}
	}

	public void createAppointmentsTables(String username) {
//		username = getLoggedInUsername();
		String sanitizedUsername = username.replaceAll("[^a-zA-Z0-9]", "_");
		String tableName = "user_" + sanitizedUsername + "_appointmrnt";

		String checkTableQuery = "SHOW TABLES LIKE ?";
		List<String> tables = jdbcTemplate.queryForList(checkTableQuery, new Object[] { tableName }, String.class);
		if (tables.size() > 1) {
			throw new IllegalStateException("Multiple tables found with the name: " + tableName);
		}
		// If the table does not exist, create it
		if (tables.isEmpty()) {
			String sql = "CREATE TABLE " + tableName + "(" + "id INT AUTO_INCREMENT PRIMARY KEY, "
					+ "patientName VARCHAR(100) NOT NULL, " + "appointmentDate DATE NOT NULL, " + "time TIME NOT NULL, "
					+ "period ENUM('AM', 'PM') NOT NULL, " + "reason VARCHAR(255), " + "email VARCHAR(100) NOT NULL, "
					+ "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" + ");";
			jdbcTemplate.execute(sql);
			System.out.println("Appointment table is succesfuly created");
		}
	}

	public Long saveUserData(Map<String, String> userData) {

		String username = getLoggedInUsername();
		String tableName = username;
		System.out.println("Received username: " + username);

		if (username == null) {
			throw new IllegalArgumentException("Username cannot be null");
		}

		LocalDate currentDate = LocalDate.now();
		LocalTime currentTime = LocalTime.now();

		String insertQuery = "INSERT INTO " + tableName + " ("
				+ "firstName, lastName, email, phoneNumber, aadharNumber, address, gender, disease, otherDisease, day, month, year, age, modeOfPayment, amount, "
				+ "upiTransactionNo, netBankingTransactionId, netBankingScreenshot, accountTransactionId, accountDocument, reference, insurance, otherPayment, "
				+ "weight, bp, appointmentTaken, appointmentDetails, modeOfPatient, bedAssign, bedDetails, bedNo, bedDays, date, time"
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

		String amountStr = userData.get("amount");
		BigDecimal amount = (amountStr == null || amountStr.trim().isEmpty()) ? null : new BigDecimal(amountStr);

		Object[] aparams = { userData.get("firstName"), userData.get("lastName"), userData.get("email"),
				userData.get("phoneNumber"), userData.get("aadharNumber"), userData.get("address"),
				userData.get("gender"), userData.get("disease"), userData.get("otherDisease"), userData.get("day"),
				userData.get("month"), userData.get("year"), userData.get("age"), userData.get("modeOfPayment"), amount,
				userData.get("upiTransactionNo"), userData.get("netBankingTransactionId"),
				userData.get("netBankingScreenshot"), userData.get("accountTransactionId"),
				userData.get("accountDocument"), userData.get("reference"), userData.get("insurance"),
				userData.get("otherPayment"), userData.get("weight"), userData.get("bp"),
				userData.get("appointmentTaken"), userData.get("appointmentDetails"), userData.get("modeOfPatient"),
				userData.get("bedAssign"), userData.get("bedDetails"), userData.get("bedNo"), userData.get("bedDays"),
				currentDate, currentTime };

		// Debugging: Print the parameters array to check the value

		KeyHolder keyHolder = new GeneratedKeyHolder();

		// Prepare the statement with KeyHolder
		try {
			jdbcTemplate.update(new PreparedStatementCreator() {

				@Override
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(insertQuery, new String[] { "id" }); // Assuming
																											// 'id' is
																											// the
																											// auto-generated
																											// column
					for (int i = 0; i < aparams.length; i++) {
						ps.setObject(i + 1, aparams[i]);
					}
					return ps;
				}
			}, keyHolder);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error saving data", e);
		}

		// Get the generated ID from the KeyHolder
		Long generatedId = keyHolder.getKey().longValue();

		System.out.println("Data saved for table: " + tableName + " with ID: " + generatedId);
		return generatedId; // Return the generated ID
	}

	public Long saveTreatment(Map<String, String> userData) {

		String username = getLoggedInUsername() + "_treatment";
		// Ensure the SQL query has the correct number of placeholders
		String sql = "INSERT INTO " + username
				+ "(tabletCount, tabletName1, tabletName2, tabletName3, tabletName4, tabletName5, tabletName6, tabletName7, tabletName8, tabletName9, tabletName10, otherTabletName, injectionSize, injectionMg, tests, doctorAdvice, patientId) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		// Ensure the array has the correct number of parameters
		Object[] aparams = { userData.get("tabletCount"), // 1
				userData.get("tabletName1"), // 2
				userData.get("tabletName2"), // 3
				userData.get("tabletName3"), // 4
				userData.get("tabletName4"), // 5
				userData.get("tabletName5"), // 6
				userData.get("tabletName6"), // 7
				userData.get("tabletName7"), // 8
				userData.get("tabletName8"), // 9
				userData.get("tabletName9"), // 10
				userData.get("tabletName10"), // 11
				userData.get("otherTabletName"), // 12
				userData.get("injectionSize"), // 13
				userData.get("injectionMg"), // 14
				userData.get("tests"), // 15
				userData.get("doctorAdvice"), // 16
				userData.get("patientId") // 17
		};
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			jdbcTemplate.update(connection -> {
				PreparedStatement ps = connection.prepareStatement(sql, new String[] { "treatment_id" });
				for (int i = 0; i < aparams.length; i++) {
					ps.setObject(i + 1, aparams[i]);
				}
				return ps;
			}, keyHolder);
			Long treatmentId = keyHolder.getKey().longValue();
			System.out.println("Data saved for table: " + username);
			System.out.println("data seved at id is" + treatmentId);
			return treatmentId;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Data not saved for table: " + username);
			return null;
		}
	}

	// lab and pharmcy-code
	public void savelab(Map<String, String> userData) {
		String patientIdStr = userData.get("patientId");
		int patientId = Integer.parseInt(patientIdStr);
		if (fetch(patientId) == null) {
			throw new IllegalArgumentException("Patient ID does not exist in the parent table.");
		}

		String username = getLoggedInUsername() + "_lab";
		LocalDate date = LocalDate.now();

		String sql = "INSERT INTO " + username
				+ " (testName, patientId, result, doctorName, technician, sampleType, testCost, insuranceProvider, status, notes, testDate) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		Object[] aparams = { userData.get("testName"), patientId, userData.get("result"), userData.get("doctorName"),
				userData.get("technician"), userData.get("sampleType"), userData.get("testCost"),
				userData.get("insuranceProvider"), userData.get("status"), userData.get("notes"), date };

		jdbcTemplate.update(sql, aparams);
	}

	public void savepharmcy(Map<String, String> userData) {
		String Pharmacy = getLoggedInUsername() + "_Pharmacy";
		LocalDate date = LocalDate.now();
		String sql = "INSERT INTO " + Pharmacy
				+ " (medicineName, quantity, expiryDate, price, batchNumber, manufacturer, supplierName, prescriptionRequired, dosageForm, stockStatus, discount, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Object[] aparams = { userData.get("medicineName"), userData.get("quantity"), userData.get("expiryDate"),
				userData.get("price"), userData.get("batchNumber"), userData.get("manufacturer"),
				userData.get("supplierName"), userData.get("prescriptionRequired"), userData.get("dosageForm"),
				userData.get("stockStatus"), userData.get("discount"), userData.get("notes"), };

		jdbcTemplate.update(sql, aparams);
	}

	public List<Map<String, Object>> fetchpharmacy() {
		String username = getLoggedInUsername() + "_Pharmacy";
		String fetchQuery = "SELECT * FROM " + username;
		try {
			// Execute the query and return the results as a List of Maps
			return jdbcTemplate.queryForList(fetchQuery);
		} catch (Exception e) {
			e.printStackTrace(); // Print stack trace for debugging
			throw new RuntimeException("Error fetching data from table " + username, e);
		}
	}

	public List<Map<String, Object>> fetchlab() {
		String username = getLoggedInUsername() + "_lab";
		String fetchQuery = "SELECT * FROM " + username;
		try {
			// Execute the query and return the results as a List of Maps
			return jdbcTemplate.queryForList(fetchQuery);
		} catch (Exception e) {
			e.printStackTrace(); // Print stack trace for debugging
			throw new RuntimeException("Error fetching data from table " + username, e);
		}
	}

	public List<Map<String, Object>> fetchlab(int id) {
		String username = getLoggedInUsername() + "_lab";
		String fetchQuery = "SELECT * FROM " + username + " WHERE id = ?";
		try {
			// Execute the query and return the results as a List of Maps
			return jdbcTemplate.queryForList(fetchQuery, id);
		} catch (Exception e) {
			e.printStackTrace(); // Print stack trace for debugging
			throw new RuntimeException("Error fetching data from table " + username, e);
		}
	}

	public List<Map<String, Object>> fetch(int id) {
		String username = getLoggedInUsername();
		String fetchQuery = "SELECT * FROM " + username + " WHERE id = ?";
		try {
			// Execute the query and return the results as a List of Maps
			return jdbcTemplate.queryForList(fetchQuery, id);
		} catch (Exception e) {
			e.printStackTrace(); // Print stack trace for debugging
			throw new RuntimeException("Error fetching data from table " + username, e);
		}
	}

	public List<Map<String, Object>> fetchtreatment(String id) {
		String username = getLoggedInUsername();
		String fetchQuery = "SELECT * FROM " + username + "_treatment" + " WHERE id = ?";
		try {
			// Execute the query and return the results as a List of Maps
			return jdbcTemplate.queryForList(fetchQuery, id);
		} catch (Exception e) {
			e.printStackTrace(); // Print stack trace for debugging
			throw new RuntimeException("Error fetching data from table " + username, e);
		}
	}

	public userData fetch() {
		String username = getCurrentUserEmail();
		return userRepository.findByemailid(username);

	}

	public userData fetch2(String email) {
//		String username = getLoggedInUserEmail();
		return userRepository.findByemailid(email);

	}

	// Method to fetch data from a specific user's table
	public List<Map<String, Object>> fetchData(String username
//			String keyword
	) {

		username = getLoggedInUsername();
		String tableName = username;
		System.out.println("Received username: " + username);

		// Construct the SQL query to fetch data from the user's table
		String fetchQuery = "SELECT * FROM " + tableName;

		String query = "SELECT * FROM " + tableName + " WHERE firstName LIKE ? " + "OR lastName LIKE ? "
				+ "OR CAST(id AS CHAR) LIKE ? " + "OR CAST(age AS CHAR) LIKE ? " + "OR CAST(weight AS CHAR) LIKE ? "
				+ "OR CAST(phonenumber AS CHAR) LIKE ? " + "OR email LIKE ? " + "OR CAST(date AS CHAR) LIKE ? "
				+ "OR CAST(time AS CHAR) LIKE ?";
//		if (keyword != null) {
//			try {
//				String keywordPattern = "%" + keyword + "%";
//				return jdbcTemplate.queryForList(query, keywordPattern, keywordPattern, keywordPattern, keywordPattern,
//						keywordPattern, keywordPattern, keywordPattern, keywordPattern, keywordPattern);
//			} catch (Exception e) {
//				e.printStackTrace(); // Print stack trace for debugging
//				throw new RuntimeException("Error fetching data from table " + tableName, e);
//			}
//		} else {
		try {
			// Execute the query and return the results as a List of Maps
			return jdbcTemplate.queryForList(fetchQuery);
		} catch (Exception e) {
			e.printStackTrace(); // Print stack trace for debugging
			throw new RuntimeException("Error fetching data from table " + tableName, e);
		}
//		}

	}

	public void delete(int id) {
		String username = getLoggedInUsername();
		System.out.println("Received username: " + username);
		String query = "DELETE FROM " + username + " WHERE id = ?";
		try {
			// Execute the DELETE query
			int rowsAffected = jdbcTemplate.update(query, id);

			if (rowsAffected > 0) {
				System.out.println("Record with id " + id + " was successfully deleted from table " + username);
			} else {
				System.out.println("No record found with id " + id + " in table " + username);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error executing delete operation for table " + username, e);
		}

	}

	public List<Map<String, Object>> getAppointmentsByDate(String username, LocalDate date) {
		username = getLoggedInUsername();
		String query = "SELECT * FROM " + username + " WHERE date = ?";
		return jdbcTemplate.queryForList(query, date);
	}

	public long countTodayPatient(String username) {
		username = getLoggedInUsername();

		System.out.println("countTodayAppointments :" + username);
		System.out.println("Today pa :" + username);
		LocalDate today = LocalDate.now();
		String query = "SELECT COUNT(*) FROM " + username + " WHERE date = ?";
		return jdbcTemplate.queryForObject(query, Long.class, today);
	}

	public long TotalPatientCount(String username) {

//		String	username = getLoggedInUsername();
		System.out.println("Total pa :" + username);
		String query = "SELECT COUNT(*) FROM " + username;
		Long TotalPatientCount = jdbcTemplate.queryForObject(query, Long.class);
		if (TotalPatientCount == null) {
			TotalPatientCount = 0L; // Provide a default value
		}
		return TotalPatientCount;
	}

	public long TotalAmount(String username) {
		username = getLoggedInUsername();
		String query = "SELECT SUM(amount) FROM " + username;
		Long totalAmount = jdbcTemplate.queryForObject(query, Long.class);
		if (totalAmount == null) {
			totalAmount = 0L; // Provide a default value
		}
		return totalAmount;
	}

	public long TodayTotalAmount(String username) {
//		String username=getLoggedInUsername();
		LocalDate today = LocalDate.now();
		String query = "SELECT SUM(amount) FROM " + username + " WHERE date = ?";
		Long totalAmount = jdbcTemplate.queryForObject(query, Long.class, today);
		return totalAmount != null ? totalAmount : 0; // Return 0 if the result is null
	}

	public List<Map<String, Object>> TotalPatient() {
		String username = getLoggedInUsername();
		System.out.println("Total pa :" + username);
//		LocalDate today = LocalDate.now();
//		String query = "SELECT * FROM " + username+" WHERE date = ?";
		String query = "SELECT * FROM " + username;
		return jdbcTemplate.queryForList(query);
	}

	public List<Map<String, Object>> TodayPatient() {
		String username = getLoggedInUsername();
		System.out.println("Total pa :" + username);
		LocalDate today = LocalDate.now();
		String query = "SELECT * FROM " + username + " WHERE date = ?";
		return jdbcTemplate.queryForList(query, today);
	}

	public Map<String, Object> getMonthlyAndYearlyTotalAmount() {
		String username = getLoggedInUsername();

		// SQL query to get the total amount grouped by each month
		String monthlyQuery = "SELECT month, SUM(totalAmount) AS totalAmount " + "FROM ( "
				+ "    SELECT DATE_FORMAT(date, '%Y-%m') AS month, SUM(amount) AS totalAmount " + "    FROM `"
				+ username + "` " + "    GROUP BY DATE_FORMAT(date, '%Y-%m') " + // Group by year and month
				") AS subquery " + "GROUP BY month " + "ORDER BY STR_TO_DATE(month, '%Y-%m')";

		// SQL query to get the total amount grouped by each year
		String yearlyQuery = "SELECT year, SUM(totalAmount) AS totalAmount " + "FROM ( "
				+ "    SELECT DATE_FORMAT(date, '%Y') AS year, SUM(amount) AS totalAmount " + "    FROM `" + username
				+ "` " + "    GROUP BY DATE_FORMAT(date, '%Y') " + // Group by year
				") AS subquery " + "GROUP BY year " + "ORDER BY year";

		// Fetch the data using JdbcTemplate
		List<Map<String, Object>> monthWiseData = jdbcTemplate.queryForList(monthlyQuery);
		List<Map<String, Object>> yearWiseData = jdbcTemplate.queryForList(yearlyQuery);

		// Format data to match the desired structure
		List<Map<String, Object>> formattedMonthWise = new ArrayList<>();
		for (Map<String, Object> record : monthWiseData) {
			formattedMonthWise.add(Map.of("month", record.get("month"), "totalAmount", record.get("totalAmount")));
		}

		List<Map<String, Object>> formattedYearWise = new ArrayList<>();
		for (Map<String, Object> record : yearWiseData) {
			formattedYearWise.add(Map.of("year", record.get("year"), "amount", record.get("totalAmount")));
		}

		// Prepare the final response map
		Map<String, Object> response = new HashMap<>();
		response.put("monthWise", formattedMonthWise);
		response.put("yearWise", formattedYearWise);

		return response;
	}

	public Map<String, Object> getAppointmentById(String username, int id) {
		username = getLoggedInUsername();
		System.out.println("Finding appointment with ID: " + id);

		// Fetch the appointment by ID
		String query = "SELECT * FROM " + username + " WHERE id = ?";
		List<Map<String, Object>> result = jdbcTemplate.queryForList(query, id);

		if (result.isEmpty()) {
			throw new NoSuchElementException("Appointment not found with id " + id);
		}

		// Return the first (and presumably only) appointment
		return result.get(0);
	}

	public void updateAppointment(Map<String, String> userData, int id) {
		String username = getLoggedInUsername();
		System.out.println("Updating appointment with ID: " + id);

		String updateQuery1 = " UPDATE " + username
				+ " SET firstName = ?, lastName=?, email=?, phoneNumber=?, aadharNumber=?, address=?, gender=?, disease=?, otherDisease=?, day=?, month=?, year=?, age=?, modeOfPayment=?, amount=?, "
				+ "upiTransactionNo=?, netBankingTransactionId=?, netBankingScreenshot=?, accountTransactionId=?, accountDocument=?, reference=?, insurance=?, otherPayment=?, "
				+ "weight=?, bp=?, appointmentTaken=?, appointmentDetails=?, modeOfPatient=?, bedAssign=?, bedDetails=?, bedNo=?, bedDays=?, date=?, time=?  WHERE id = ?";

		LocalDate currentDate = LocalDate.now();
		LocalTime currentTime = LocalTime.now();
		Object[] aparams = { userData.get("firstName"), userData.get("lastName"), userData.get("email"),
				userData.get("phoneNumber"), userData.get("aadharNumber"), userData.get("address"),
				userData.get("gender"), userData.get("disease"), userData.get("otherDisease"), userData.get("day"),
				userData.get("month"), userData.get("year"), userData.get("age"), userData.get("modeOfPayment"),
				userData.get("amount"), userData.get("upiTransactionNo"), userData.get("netBankingTransactionId"),
				userData.get("netBankingScreenshot"), userData.get("accountTransactionId"),
				userData.get("accountDocument"), userData.get("reference"), userData.get("insurance"),
				userData.get("otherPayment"), userData.get("weight"), userData.get("bp"),
				userData.get("appointmentTaken"), userData.get("appointmentDetails"), userData.get("modeOfPatient"),
				userData.get("bedAssign"), userData.get("bedDetails"), userData.get("bedNo"), userData.get("bedDays"),
				currentDate, currentTime, id };
		jdbcTemplate.update(updateQuery1, aparams);
	}

	public void forgotPassword(String email, String password, String repetepassword) {
//		String email=getLoggedInUserEmail();
		userData user = userRepository.findByemailid(email);

		if (user == null) {
			throw new RuntimeException("Invalid or expired reset token.");
		}
		if (password == null) {
			throw new IllegalArgumentException("Password cannot be null or empty.");
		}
		user.setPassword(password);
		user.setRepetepassword(repetepassword);
		userRepository.save(user);
	}

	public void savebank(String bankName, String ifscCode, Long accountNumber) {
		String username = getCurrentUserEmail();
		try {
			userData user = userRepository.findByemailid(username);
			user.setBankName(bankName);
			user.setAccountNumber(accountNumber);
			user.setIfscCode(ifscCode);
			userRepository.save(user);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void savecoutofPatents(Long totalpatents, Long Todaypatents) {
		String username = getCurrentUserEmail();
		try {
			Long pendingAmount = totalpatents * 20;
			userData user = userRepository.findByemailid(username);
			user.setTotalpatents(totalpatents);
			user.setTodaypatents(Todaypatents);
			user.setPendingAmount(pendingAmount);
			userRepository.save(user);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void printTextOnA4Paper(String text) throws Exception {
		// Convert the text content into an InputStream for printing
		InputStream textStream = new ByteArrayInputStream(text.getBytes());

		// Get the default print service (default printer)
		PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
		if (defaultPrintService == null) {
			throw new Exception("No printer found.");
		}

		// Set up print attributes for A4 size paper, portrait orientation, and
		// single-sided printing
		PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();
		printAttributes.add(MediaSizeName.ISO_A4); // A4 paper size
		printAttributes.add(OrientationRequested.PORTRAIT); // Portrait orientation
		printAttributes.add(Sides.ONE_SIDED); // Single-sided printing

		// Create a DocFlavor for plain text
		DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
		Doc document = new SimpleDoc(textStream, flavor, null);

		// Create the print job and pass it to the print service
		DocPrintJob printJob = defaultPrintService.createPrintJob();
		printJob.print(document, printAttributes);

		// Close the InputStream
		textStream.close();
	}

// Appointmrnt-code
	public void saveAppointmentData(String username, String patientName, LocalDate appointmentDate, LocalTime time,
			String period, String reason, String email) {
		// Sanitize the username to ensure a valid table name
		String tableName = username + "_appointmrnt";

		// SQL query to insert the appointment data
		String sql = "INSERT INTO " + tableName + " (patientName, appointmentDate, time, period, reason, email) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";

		// Execute the query using jdbcTemplate
		jdbcTemplate.update(sql, patientName, appointmentDate, time, period, reason, email);

		System.out.println("Appointment data has been successfully saved.");
	}

	public List<Map<String, Object>> getAllAppointments(String username) {
		String tableName = username + "_appointmrnt";
		String sql = "SELECT * FROM " + tableName;
		return jdbcTemplate.queryForList(sql);
	}

	@Autowired
	private JavaMailSender mailSender;

	public void sendEmail(String to, String subject, String body) {
		String email = getCurrentUserEmail();
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(body);
		message.setFrom(email);

		mailSender.send(message);
	}

	public List<Map<String, Object>> fetchAppointment(int id) {
		String username = getLoggedInUsername() + "_appointmrnt";
		String fetchQuery = "SELECT * FROM " + username + " WHERE id = ?";
		try {
			// Execute the query and return the results as a List of Maps
			return jdbcTemplate.queryForList(fetchQuery, id);
		} catch (Exception e) {
			e.printStackTrace(); // Print stack trace for debugging
			throw new RuntimeException("Error fetching data from table " + username, e);
		}
	}

	public long countTodayAppointment(String username) {
		username = getLoggedInUsername() + "_appointmrnt";

		System.out.println("countTodayAppointments :" + username);
		System.out.println("Today pa :" + username);
		LocalDate today = LocalDate.now();
		String query = "SELECT COUNT(*) FROM " + username + " WHERE DATE(created_at) = ?";
		return jdbcTemplate.queryForObject(query, Long.class, today);
	}

	public long TotalAppointmentCount(String username) {

		username = getLoggedInUsername() + "_appointmrnt";
		System.out.println("Total Appointments :" + username);
		String query = "SELECT COUNT(*) FROM " + username;
		Long TotalPatientCount = jdbcTemplate.queryForObject(query, Long.class);
		if (TotalPatientCount == null) {
			TotalPatientCount = 0L; // Provide a default value
		}
		return TotalPatientCount;
	}

}
