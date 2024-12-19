package com.tsarit.form_1.controller;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.tsarit.form_1.config.AuthenticationRequest;
import com.tsarit.form_1.config.JwtUtil;
import com.tsarit.form_1.model.Getintouch;
import com.tsarit.form_1.model.SupportTicket;
import com.tsarit.form_1.model.userData;
import com.tsarit.form_1.repository.userRepository;
import com.tsarit.form_1.responses.LoginResponse;
import com.tsarit.form_1.service.AppointmentService;
import com.tsarit.form_1.service.CurrentUserUtil;
import com.tsarit.form_1.service.OtpService;
import com.tsarit.form_1.service.UserContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.tsarit.form_1.config.coustomUserDetailService;

@Controller
//@CrossOrigin(origins = "http://localhost:3000")
public class AuthenticationController {
	
	@Autowired(required=true)
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtil jwtUtii;
	
	@Autowired
	private AppointmentService appointmentService;
	
	@Autowired
	private coustomUserDetailService coustomUserDetailService;
	@Autowired
	private userRepository repository;
	
	@Autowired
	private LoginResponse LoginResponse;
	
	@Autowired
	private BCryptPasswordEncoder bp;
	
	 @Autowired
	    private OtpService otpService;

	 @Autowired
		private CurrentUserUtil currentUserUtil;
	 
	 @GetMapping(value = {"/", "/{path:[^\\.]*}"})
	    public String index() {
	        // Forward to index.html
	        return "forward:/index.html";
	    }
	 
//	 static String email=null;
//	 
//	  @ModelAttribute
//	    public void setNoCacheHeaders(HttpServletResponse response) {
//	        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
//	        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
//	        response.setDateHeader("Expires", 0); // Proxies
//	    }
//	 @ResponseBody
//	    public String loginPage(HttpServletResponse response) {
//	        // Disable browser caching for login page
//	        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
//	        response.setHeader("Pragma", "no-cache");
//	        response.setHeader("Expires", "0");
//	        
//	        return "login";
//	    }
	 
	 @PostMapping("/api/contact")
	 public ResponseEntity<String> saveTouch(@RequestBody Getintouch getintouch){
		 try {
			 appointmentService.savetouch(getintouch);
			 return ResponseEntity.ok("saved");
		 }catch (Exception e) {
			// TODO: handle exception
			 e.printStackTrace();
			 return ResponseEntity.ok("faild");
		}
	 }
	 
	 @PostMapping("/support")
	 public ResponseEntity<String> saveticket(@RequestBody SupportTicket st){
		 try {
			 appointmentService.saveticket(st);
			 return ResponseEntity.ok("Ticket rised successfully.");
		 }catch (Exception e) {
			// TODO: handle exception
			 e.printStackTrace();
			 return ResponseEntity.ok("error");
		}
	 }
	 
	 @GetMapping("/support/tickets")
	 public ResponseEntity<List<SupportTicket>> findAll() {
		    try {
		        List<SupportTicket> tickets = appointmentService.getAlltickets();
		        return ResponseEntity.ok(tickets); 
		    } catch (Exception e) {
		        e.printStackTrace();
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		    }
		}

	 
	  @PostMapping("/resend-otp")
	    public ResponseEntity<String> resendOtp(@RequestBody userData ud) {
		  System.out.println("resended Received data: " + ud);
	        try {
	        	
	     	    String otp = otpService.generateOtp();

	             // Save temporary user data and OTP
	             otpService.saveTemporaryUser(ud, otp);

	             // Send the OTP to the user's email
	             otpService.sendOtp(ud.getEmailid(), otp);
	             System.out.println("otp resende :"+otp);
	            return ResponseEntity.ok("OTP has been resent successfully.");
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to resend OTP.");
	        }
	    }
	    
	  @CrossOrigin(origins = "https://hms.tsaritservices.com")
	    @PostMapping("/verify-otp")
	    public ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> request) {
	    	 System.out.println("Received request: " + request);
	        String emailid = request.get("emailid");
	        String otp = request.get("otp");

	        System.out.println("Verifying OTP for email: " + emailid + " with OTP: " + otp);
	        boolean isOtpValid = otpService.verifyOtp(emailid, otp);

	        if (isOtpValid) {
	            userData ud = otpService.getTemporaryUser(emailid);
	            System.out.println(ud);
	            if (ud != null) {
	                // Save the user data to the database
	            	userData em=repository.findByemailid(emailid);
	            	if(em==null){
	                repository.save(ud);
	                appointmentService.createUserTables(emailid);
	                appointmentService.createAppointmentsTables( emailid);
	                otpService.clearTemporaryData(emailid); // Clear temporary data
	                return ResponseEntity.ok("OTP verified successfully. User registered.");
	            	}
	                else {
	                	return ResponseEntity.ok("email is already present! please enter new email");
	              }
	            } else {
	                return ResponseEntity.status(400).body("No user data found for the provided email.");
	            }
	        } else {
	            return ResponseEntity.status(400).body("Invalid OTP.");
	        }
	    }

	
	
	    @CrossOrigin(origins = "https://hms.tsaritservices.com")
	  @PostMapping("/register-react")
	public ResponseEntity<String> register(@RequestBody userData ud) {
	    System.out.println("Received data: " + ud);
	    ud.setPassword(bp.encode(ud.getPassword()));
	    ud.setRole("ROLE_USER");
//	    repository.save(ud);
	    String otp = otpService.generateOtp();

        // Save temporary user data and OTP
        otpService.saveTemporaryUser(ud, otp);

        // Send the OTP to the user's email
        otpService.sendOtp(ud.getEmailid(),otp);
        
	    return ResponseEntity.ok("otp sended successfully");
	}
	

	    @PostMapping("/auth/login")
	    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest) {
	      try {
	        if (authenticationRequest.getEmailid() == null || authenticationRequest.getEmailid().isEmpty() || authenticationRequest
	          .getPassword() == null || authenticationRequest.getPassword().isEmpty())
	          throw new IllegalArgumentException("Email ID and Password cannot be null or empty"); 
	        Authentication authentication = this.authenticationManager.authenticate((Authentication)new UsernamePasswordAuthenticationToken(authenticationRequest
	              .getEmailid(), authenticationRequest.getPassword()));
	        SecurityContextHolder.getContext().setAuthentication(authentication);
	        userData user = (userData)authentication.getPrincipal();
	        String jwtToken = this.jwtUtii.generateToken(user.getEmailid(),user.getRole());
//	           email = user.getEmailid();
	        UserContext.setCurrentUserEmail(user.getEmailid());
	        this.appointmentService.storeLoggedInUserEmail(user.getEmailid());
	        this.appointmentService.updateLastLoginDate();
	        String tableName = this.appointmentService.getLoggedInUsername();
	        this.appointmentService.data();
	        this.LoginResponse.setToken(jwtToken);
	        String token = this.LoginResponse.getToken();
	        System.out.println("token :" + token);
	        return ResponseEntity.ok(this.LoginResponse);
	      } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
	      } 
	    }
	
	@GetMapping("/getusername")
	public String getusername(HttpServletRequest request ){
		String token = request.getHeader("Authorization").substring(7); // Assuming token is sent as Bearer
        String username = jwtUtii.getUsernameFromToken(token); 
		return username;
	}
	
	@GetMapping({"/api/dashboard"})
	  public ResponseEntity<?> getDashboard(@RequestParam(value = "username", required = false) String username) {
	    username = this.appointmentService.getLoggedInUsername();
	    Map<String, Object> response = new HashMap<>();
	    String token = this.LoginResponse.getToken();
	    System.out.println(token);
	    System.out.println("username:" + username);
	    try {
	      if (username == null || username.isEmpty())
	        username = this.jwtUtii.getUsernameFromToken(username); 
	      long TotalpatientCount = this.appointmentService.TotalPatientCount(username);
	      response.put("TotalpatientCount", Long.valueOf(TotalpatientCount));
	      System.out.println(TotalpatientCount);
	      long TodaypatientCount = this.appointmentService.countTodayPatient(username);
	      response.put("TodaypatientCount", Long.valueOf(TodaypatientCount));
	      System.out.println(TodaypatientCount);
	      long TotalAmmount = this.appointmentService.TotalAmount(username);
	      response.put("totalPayment", Long.valueOf(TotalAmmount));
	      long TodayTotalAmmount = this.appointmentService.TodayTotalAmount(username);
	      response.put("todayPayment", Long.valueOf(TodayTotalAmmount));
	      
	      long TotalAppointments = this.appointmentService.TotalAppointmentCount(username);
	      response.put("TotalAppointments", Long.valueOf(TotalAppointments));
	      long TodayAppointments = this.appointmentService. countTodayAppointment(username);
	      response.put("TodayAppointments", Long.valueOf(TodayAppointments));
	      
	      this.appointmentService.savecoutofPatents(Long.valueOf(TotalpatientCount), Long.valueOf(TodaypatientCount));
	      LocalDate currentDate = LocalDate.now();
	      LocalTime currentTime = LocalTime.now();
	      LocalDateTime lastLoginDate = this.appointmentService.getLastLoginDate();
	      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	      String formattedDate = lastLoginDate.format(formatter);
	      userData user = this.appointmentService.data();
	      if (user != null) {
	        response.put("firstname", user.getFirstname());
	        response.put("lastname", user.getLastname());
	        response.put("hospitalname", user.getHospitalname());
	        response.put("currentDate", currentDate);
	        response.put("currentTime", currentTime);
	        response.put("formattedDate", formattedDate);
	      } else {
	        return ResponseEntity.status((HttpStatusCode)HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "User not found"));
	      } 
	      System.out.println("response :" + response);
	      return ResponseEntity.ok(response);
	    } catch (Exception e) {
	      e.printStackTrace();
	      return ResponseEntity.status((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "An internal error occurred"));
	    } 
	  }
	 @PostMapping("/saveAthentication")
	    public ResponseEntity<Long> saveUserData(
	    		@RequestBody Map<String, String> userData) {
		 userData.forEach((key, value) -> System.out.println("Param: " + key + " = " + value));
	        try {
	        	 Long id =appointmentService.saveUserData( userData);
	            return ResponseEntity.ok(id);
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                                 .body(null);
	        }
	    }
	 @PostMapping("/reactlogout")
	    public ResponseEntity<String> logout(HttpServletRequest request,HttpServletResponse response) {
//	        // Invalidate the current user session
//	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//	        if (authentication != null) {
//	            SecurityContextHolder.getContext().setAuthentication(null);
//	            System.out.println("logout successfull");
//	        }
//	        // Here you could also invalidate the JWT or perform other logout operations if needed
		 // Invalidate the session if necessary
	        request.getSession().invalidate();
//	        AuthenticationController.email=null;
	        // Clear any authentication details from SecurityContextHolder
	        SecurityContextHolder.clearContext();
             
	        return ResponseEntity.ok("Logged out successfully.");
	    }
	
	   
	   
	 

	  @GetMapping({"/api/records"})
	  public ResponseEntity<Map<String, Object>> getAllRecords(@RequestParam(value = "username", required = false) String username, @RequestParam(value = "keyword", required = false) String keyword) {
	    username = this.appointmentService.getLoggedInUsername();
	    Map<String, Object> response = new HashMap<>();
	    try {
	      List<Map<String, Object>> records = this.appointmentService.fetchData(username);
	      response.put("records", records);
	      long TotalpatientCount = this.appointmentService.TotalPatientCount(username);
	      response.put("TotalpatientCount", Long.valueOf(TotalpatientCount));
	      long TodaypatientCount = this.appointmentService.countTodayPatient(username);
	      response.put("TodaypatientCount", Long.valueOf(TodaypatientCount));
	      return ResponseEntity.ok(response);
	    } catch (Exception e) {
	      return ResponseEntity.status(500).body(null);
	    } 
	  }
	 
	 @GetMapping("/api/record/{patientId}")
	    public ResponseEntity<List<Map<String, Object>>> getRecords(@PathVariable int patientId) {
	        try {
	        	 System.out.println("Received ID: " + patientId);

	             // Convert the ID from String to Integer
	            List<Map<String, Object>> records = appointmentService.fetch(patientId);
	            return ResponseEntity.ok(records);
	        } catch (Exception e) {
	             e.printStackTrace();
	             return null;
	        }
	 }
	 
	 @GetMapping("/get/api/")
	    public ResponseEntity<userData> get() {
	        try {
	          userData records = appointmentService.fetch();
	            return ResponseEntity.ok(records);
	        } catch (Exception e) {
	            return ResponseEntity.status(500).body(null);
	        }
	 }
	    
	    @GetMapping("/api/payments/today")
	    public ResponseEntity<List<Map<String, Object>>> TotalPatient() {
	    	try {
	    		List<Map<String, Object>> records=appointmentService.TotalPatient();
	    		return ResponseEntity.ok(records);
			} catch (Exception e) {
				// TODO: handle exception
				 return ResponseEntity.status(500).body(null);
			}
	    }
	    
	    @GetMapping("/api/today")
	    public ResponseEntity<List<Map<String, Object>>> TodayPatient() {
	    	try {
	    		List<Map<String, Object>> records=appointmentService.TodayPatient();
	    		return ResponseEntity.ok(records);
			} catch (Exception e) {
				// TODO: handle exception
				 return ResponseEntity.status(500).body(null);
			}
	    }
	    
	    @GetMapping("/delete/{id}")
	    public ResponseEntity<String> deleteRecord(@PathVariable int id) {
	    	try {
	    		appointmentService.delete(id);
	    		return ResponseEntity.ok("Record deleted successfully");
			} catch (Exception e) {
				// TODO: handle exception
				return ResponseEntity.status(404).body("Record not found");
			}
	    }
	    
	    @GetMapping("/monthly-total")
	    public ResponseEntity<Map<String, Object>> getMonthlyTotalAmount() {
	    	try {
	            Map<String, Object> totals = appointmentService.getMonthlyAndYearlyTotalAmount();
	            return ResponseEntity.ok(totals);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while fetching the data"));
	        }
	    }
	    
	    
	    @PostMapping("/reset-password")
	    public ResponseEntity<String> resetPassword(@RequestBody userData ud)
	    {
	        try {
	        	String email=ud.getEmailid();
	        	String password=bp.encode(ud.getPassword());
	        	String repetepassword=ud.getPassword();
	        	
                 appointmentService.forgotPassword(email,password,repetepassword);	        	
	            return ResponseEntity.ok("Password has been reset successfully.");
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Error: " + e.getMessage());
	        }
	    }
	    @PostMapping("/forgot-otp/{emailid}")
	    public ResponseEntity<String> forgotOtp(@PathVariable String emailid) {
	    	System.out.println(emailid);
	      userData	ud=appointmentService.fetch2(emailid);
		  System.out.println("resended Received data: " + ud);
	        try {
	        
	     	    String otp = otpService.generateOtp();

	             // Save temporary user data and OTP
	             otpService.saveTemporaryUser(ud, otp);

	             // Send the OTP to the user's email
	             otpService.sendOtp(emailid, otp);
	             System.out.println("otp resende :"+otp);
	            return ResponseEntity.ok("OTP has been resent successfully.");
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to resend OTP.");
	        }
	    }
	    
	    
	    @PostMapping("/update/{id}")
	    public ResponseEntity<String> updateUserData(
	    		@RequestBody Map<String, String> userData,@PathVariable("id") int id) {
		 userData.forEach((key, value) -> System.out.println("Param: " + key + " = " + value));
	        try {
	        	appointmentService.updateAppointment(userData,id);
	            return ResponseEntity.ok("User data saved successfully.");
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                                 .body("Error saving user data: " + e.getMessage());
	        }
	    }
	    @PostMapping("/save-bank-details")
	    public ResponseEntity<?> savebank(@RequestBody userData request){
	    	try {
	    		 Long accountNumber = request.getAccountNumber();
	    	        String ifscCode = request.getIfscCode();
	    	        String bankName = request.getBankName();
	    	        appointmentService.savebank(bankName, ifscCode, accountNumber);
	    		return ResponseEntity.ok("saved");
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return ResponseEntity.ok("error");
			}
	    }
	    
	    @PostMapping("/print")
	    public ResponseEntity<String> printText(@RequestBody String text) {
	        try {
	        	appointmentService.printTextOnA4Paper(text);
	            return ResponseEntity.ok("Printing started successfully.");
	        } catch (Exception e) {
	            return ResponseEntity.status(500).body("Error occurred while printing: " + e.getMessage());
	        }
	    }
	    
	    
	    static String str =null;
		 @PostMapping("/savetreatment")
		    public ResponseEntity<String> save(
		    		@RequestBody Map<String, String> userData) {
			 userData.forEach((key, value) -> System.out.println("Param: " + key + " = " + value));
		        try {
		        	Long treatmentId =appointmentService.saveTreatment( userData);
		        	AuthenticationController.str = String.valueOf(treatmentId);
		        	System.err.println(AuthenticationController.str);
		        	if (treatmentId != null) {
		                return ResponseEntity.ok(str);
		            } else {
		                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		                                     .body("Error saving treatment data.");
		            }
		        } catch (Exception e) {
		        	e.printStackTrace();
		            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		                                 .body("Error saving user data: " + e.getMessage());
		        }
		    }
	    @GetMapping("/api/generate-certificate/{patientId}")
	    @ResponseBody
	    public ResponseEntity<ByteArrayResource> generateCertificate(@PathVariable int patientId,@RequestParam Map<String, Object> formData) {
	        try {
	            // Find the user by email, expecting a list of webinar objects
	        	String username=appointmentService.getCurrentUserEmail();
//	                  String email=AuthenticationController.email;
	                 userData  userall= appointmentService.find(username);
	                 
	                 
	                 
	                 List<Map<String, Object>> records = appointmentService.fetch(patientId);
	                 String firstName = null;
	                 String lastName=null;
	                 String phoneNumber=null;
	                 int age=0;
	                 String disease=null;
	                 String gender=null;
	                 for (Map<String, Object> record : records) {
	                	  firstName = (String) record.get("firstName");
	                	  lastName = (String) record.get("lastName");
	                	  phoneNumber = (String) record.get("phoneNumber");
	                	  disease = (String) record.get("disease");
	                	  gender = (String) record.get("gender");
	                	  Object ageObj = record.get("age");
	                      if (ageObj instanceof String) {
	                          age = Integer.parseInt((String) ageObj);
	                      } else if (ageObj instanceof Integer) {
	                          age = (Integer) ageObj;
	                      }
	                	 
	                 }
	                 List<Map<String, Object>> treatment=appointmentService.fetchtreatment(AuthenticationController.str);
	                 String tabletCount=null;
	                 String tabletName1=null;
	                 String otherTabletName=null;
	                 String injectionSize=null;
	                 String injectionName=null;
	                 String injectionMg=null;
	                 String tests=null;
	                 String doctorAdvice=null;
	                 for (Map<String, Object> treatment2 : treatment) {
	                  tabletCount = (String) treatment2.get("tabletCount");
	                  tabletName1=(String) treatment2.get("tabletName1");
	                  otherTabletName=(String) treatment2.get("otherTabletName");
	                  injectionSize=(String) treatment2.get("injectionSize");
	                  injectionName=(String) treatment2.get("injectionName");
	                  injectionMg=(String) treatment2.get("injectionMg");
	                  tests=(String) treatment2.get("tests");
	                  doctorAdvice=(String) treatment2.get("doctorAdvice");
	                 }

	            // Get the absolute path for the certificate image
//	            String certificateImagePath = getClass().getClassLoader().getResource("static/pdf.jpg").toExternalForm();
//	            System.out.println("Image Path: " + certificateImagePath);
	            
	            // HTML for the certificate with the absolute path for the background image
	            String html = 
	            	    "<!DOCTYPE html>" +
	            	    "<html>" +
	            	    "<head>" +
	            	    "    <title>Prescription</title>" +
	            	    "    <style>" +
	            	    "        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4; }" +
	            	    "        .container { width: 100%; max-width: 800px; margin: 0 auto; background-color: white; padding: 20px; border: 1px solid #ccc; border-radius: 8px; }" +
	            	    "        .header { text-align: center; border-bottom: 2px solid #4CAF50; padding-bottom: 10px; }" +
	            	    "        .header h1 { font-size: 24px; margin: 0; color: #333; }" +
	            	    "        .header p { margin: 0; font-size: 14px; color: #777; }" +
	            	    "        .doctor-info, .patient-info { margin-top: 20px; }" +
	            	    "        .doctor-info h3, .patient-info h3 { margin-bottom: 5px; color: #4CAF50; font-size: 18px; }" +
	            	    "        .info-table { width: 100%; border-collapse: collapse; margin-top: 20px; }" +
	            	    "        .info-table th, .info-table td { padding: 10px; border: 1px solid #ddd; text-align: left; }" +
	            	    "        .info-table th { background-color: #4CAF50; color: white; }" +
	            	    "        .info-table td { background-color: #f9f9f9; }" +
	            	    "    </style>" +
	            	    "</head>" +
	            	    "<body>" +
	            	    "    <div class='container'>" +
	            	    "        <div class='header'>" +
	            	    "            <h1>"+userall.getHospitalname()+"</h1>" +
	            	    "            <p>"+userall.getAddress()+"</p>" +
	            	    "            <p>Contact:"+userall.getPhonenumber() +"| Email:"+userall.getEmailid()+"</p>" +
	            	    "        </div>" +
	            	    "        <div class='doctor-info'>" +
	            	    "            <h3>Doctor Details:</h3>" +
	            	    "            <p><strong>"+"Doctor Name:"+userall.getFirstname()+userall.getLastname()+"</strong></p>" +
	            	    "            <p><strong>Specialization:</strong> Cardiology</p>" +
	            	    "        </div>" +
	            	    "        <div class='patient-info'>" +
	            	    "            <h3>Patient Details:</h3>" +
	            	    "            <p><strong>Patient Name:</strong>"+firstName+lastName+"</p>" +
	            	    "              <p><strong>phoneNumber:</strong>"+phoneNumber+"</p>" +
	            	    "              <p><strong>disease:</strong>"+disease+"</p>" +
	            	    "            <p><strong>Age:</strong> "+age+"</p>" +
	            	    "            <p><strong>Gender:</strong>"+gender+"</p>" +
	            	    "        </div>" +
	            	    "        <div class='patient-info'>" +
	            	    "            <h3>Doctor's Prescription:</h3>" +
	            	    "            <p><strong>tabletCount:</strong>"+tabletCount+"</p>" +
	            	    "              <p><strong>injectionSize:</strong>"+injectionSize+"</p>" +
	            	    "              <p><strong>injectionName:</strong>"+injectionName+"</p>" +
	            	    "            <p><strong>injectionMg:</strong> "+injectionMg+"</p>" +
	            	    "            <p><strong>tests:</strong>"+tests+"</p>" +
	            	    "            <p><strong>doctorAdvice:</strong>"+doctorAdvice+"</p>" +
	            	    "        </div>" +
	            	    "        <table class='info-table'>" +
	            	    "            <thead>" +
	            	    "                <tr>" +
	            	    "                    <th>Tablet Name</th>" +
	            	    "                    <th>Dosage (mg)</th>" +
	            	    "                    <th>Quantity</th>" +
	            	    "                    <th>Instructions</th>" +
	            	    "                </tr>" +
	            	    "            </thead>" +
	            	    "            <tbody>" +
	            	    "                <tr>" +
	            	    "                    <td>Paracetamol</td>" +
	            	    "                    <td>500</td>" +
	            	    "                    <td>10</td>" +
	            	    "                    <td>Twice a day after meal</td>" +
	            	    "                </tr>" +
	            	    "                <tr>" +
	            	    "                    <td>"+tabletName1+"</td>" +
	            	    "                    <td>200</td>" +
	            	    "                    <td>15</td>" +
	            	    "                    <td>Once a day before meal</td>" +
	            	    "                </tr>" +
	            	    "            </tbody>" +
	            	    "        </table>" +
	            	    "        <div class='footer' style='margin-top: 40px; text-align: center;'>" +
	            	    "            <p><strong>Note:</strong> Please take the prescribed medication as directed by your doctor.</p>" +
	            	    "            <p>Issued by Hospital Name - All Rights Reserved</p>" +
	            	    "        </div>" +
	            	    "    </div>" +
	            	    "</body>" +
	            	    "</html>";


	            // Convert HTML to PDF
	            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	            ITextRenderer renderer = new ITextRenderer();

	            // Set the page size based on the certificate image size, assuming it's 700x600 pixels
	            // Convert pixel values to points (1 pixel = 0.75 points)
	            renderer.getSharedContext().setBaseURL("file:src/main/resources/static/");
	            renderer.setDocumentFromString(html);
	            renderer.layout();

	            // Set custom PDF page size to match the certificate image size (no margins)
	            renderer.getOutputDevice();
	            renderer.createPDF(outputStream, false);
	            renderer.finishPDF();

	            // Prepare PDF response
	            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

	            HttpHeaders headers = new HttpHeaders();
	            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate.pdf");
	            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

	            return ResponseEntity.ok()
	                    .headers(headers)
	                    .contentLength(outputStream.size())
	                    .body(resource);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	        }
	    }
	    
	    
//pharmcy-code
	    @PostMapping("/savepharmacy")
	    public ResponseEntity<String> savepharmacy(@RequestBody Map<String, String> userData) {
	    	try {
				appointmentService.savepharmcy(userData);
				return ResponseEntity.ok("sucsuss");
			} catch (Exception e) {
                  e.printStackTrace();
                  return ResponseEntity.ok("error");
			}
	    }
	    
	    @GetMapping("/fetchpharmacy")
	    public ResponseEntity<List<Map<String, Object>>> fetchpharmacy() {
	    	try {
	    		List<Map<String, Object>> records=appointmentService.fetchpharmacy();
	    		return ResponseEntity.ok(records);
			} catch (Exception e) {
				// TODO: handle exception
				 return ResponseEntity.status(500).body(null);
			}
	    }
	    
	 // lab-code	    
	    @PostMapping("/saveLab")
	    public ResponseEntity<String> saveLab(@RequestBody Map<String, String> userData) {
	    	try {
				appointmentService.savelab(userData);
				return ResponseEntity.ok("sucsuss");
			} catch (Exception e) {
                  e.printStackTrace();
                  return ResponseEntity.ok("error");
			}
	    }
	    
	    @GetMapping("/getlabtests")
	    public ResponseEntity<List<Map<String, Object>>> TotalPatientLabTests() {
	    	try {
	    		List<Map<String, Object>> records=appointmentService.fetchlab();
	    		return ResponseEntity.ok(records);
			} catch (Exception e) {
				// TODO: handle exception
				 return ResponseEntity.status(500).body(null);
			}
	    }
	    
	    @GetMapping("/Lab-bill/{patientId}/{Labid}")
	    public ResponseEntity<String> getLabTreatmentBill(@PathVariable int patientId,@PathVariable int Labid) {
	    	
	    	String username=appointmentService.getCurrentUserEmail();
//            String email=AuthenticationController.email;
           userData  userall= appointmentService.find(username);
           List<Map<String, Object>> lab = appointmentService.fetchlab(Labid);
           String testName=null;
           Long testCost=null;
           for (Map<String, Object> record : lab) {
        	   testName=(String) record.get("testName"); 
        	   BigDecimal testCostDecimal = (BigDecimal) record.get("testCost");
        	    if (testCostDecimal != null) {
        	        testCost = testCostDecimal.longValue();  // Convert BigDecimal to Long
        	    }
           }
           
           List<Map<String, Object>> records = appointmentService.fetch(patientId);
           String firstName = null;
           String lastName=null;
           String phoneNumber=null;
           int age=0;
           String disease=null;
           String gender=null;
           for (Map<String, Object> record : records) {
          	  firstName = (String) record.get("firstName");
          	  lastName = (String) record.get("lastName");
          	  phoneNumber = (String) record.get("phoneNumber");
          	  disease = (String) record.get("disease");
          	  gender = (String) record.get("gender");
          	  Object ageObj = record.get("age");
                if (ageObj instanceof String) {
                    age = Integer.parseInt((String) ageObj);
                } else if (ageObj instanceof Integer) {
                    age = (Integer) ageObj;
                }
          	 
           }
	        // HTML content as a string
           String htmlContent = 
        		    "<!DOCTYPE html>" +
        		    "<html lang='en'>" +
        		    "<head>" +
        		    "    <meta charset='UTF-8'>" +
        		    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
        		    "    <title>Lab Treatment Bill</title>" +
        		    "    <style>" +
        		    "        body { font-family: Arial, sans-serif; margin: 20px; }" +
        		    "        .bill-container { width: 80%; margin: auto; border: 1px solid #000; padding: 20px; border-radius: 10px; }" +
        		    "        h2 { text-align: center; margin-bottom: 20px; }" +
        		    "        .details, .tests, .charges { margin-bottom: 15px; }" +
        		    "        .details table, .tests table, .charges table { width: 100%; border-collapse: collapse; }" +
        		    "        .details td, .tests th, .tests td, .charges td { border: 1px solid #000; padding: 8px; text-align: left; }" +
        		    "        .totals { text-align: right; }" +
        		    "        .totals h3 { margin-top: 10px; }" +
        		    "        .header { text-align: center; border-bottom: 2px solid #4CAF50; padding-bottom: 10px; }" +
            	    "        .header h1 { font-size: 24px; margin: 0; color: #333; }" +
            	    "        .header p { margin: 0; font-size: 14px; color: #777; }" +
        		    "    </style>" +
        		    "</head>" +
        		    "<body>" +
        		    "<div class='bill-container'>" +
        		    " <div class='header'>" +
            	    "            <h1>"+userall.getHospitalname()+"</h1>" +
            	    "            <p>"+userall.getAddress()+"</p>" +
            	    "            <p>Contact:"+userall.getPhonenumber() +"| Email:"+userall.getEmailid()+"</p>" +
            	    "    <h2>Lab Treatment Bill</h2>" +
            	    "        </div>" +
        		    "    <div class='details'>" +
        		    "        <table>" +
        		    "            <tr>" +
        		    "                <td><strong>Patient Name:</strong>"+firstName+""+lastName+"</td>" +
        		    "                <td><strong>Patient ID:</strong>"+patientId+"</td>" +
        		    "            </tr>" +
        		    "            <tr>" +
        		    "                <td><strong>Doctor:</strong>"+userall.getFirstname()+""+userall.getLastname()+"</td>" +
        		    "                <td><strong>Date:</strong>"+LocalDate.now()+"</td>" +
        		    "            </tr>" +
        		    "            <tr>" +
        		    "                <td><strong>Lab Name:</strong> ABC Diagnostics</td>" +
        		    "                <td><strong>Bill No:</strong>"+Labid+"</td>" +
        		    "            </tr>" +
        		    "        </table>" +
        		    "    </div>" +
        		    "    <div class='tests'>" +
        		    "        <h3>Tests Conducted</h3>" +
        		    "        <table>" +
        		    "            <thead>" +
        		    "                <tr>" +
        		    "                    <th>testName</th>" +
//        		    "                    <th>Quantity</th>" +
//        		    "                    <th>Cost per Unit (₹)</th>" +
        		    "                    <th>testCost (₹)</th>" +
        		    "                </tr>" +
        		    "            </thead>" +
        		    "            <tbody>" +
        		    "                <tr>" +
        		    "                    <td>"+testName+"</td>" +
//        		    "                    <td>1</td>" +
//        		    "                    <td>500</td>" +
        		    "                    <td>"+testCost+"</td>" +
        		    "                </tr>" +
//        		    "                <tr>" +
//        		    "                    <td>X-ray</td>" +
//        		    "                    <td>1</td>" +
//        		    "                    <td>1000</td>" +
//        		    "                    <td>1000</td>" +
//        		    "                </tr>" +
//        		    "                <tr>" +
//        		    "                    <td>Urine Test</td>" +
//        		    "                    <td>1</td>" +
//        		    "                    <td>300</td>" +
//        		    "                    <td>300</td>" +
//        		    "                </tr>" +
        		    "            </tbody>" +
        		    "        </table>" +
        		    "    </div>" +
        		    "    <div class='charges'>" +
//        		    "        <h3>Additional Charges</h3>" +
//        		    "        <table>" +
//        		    "            <tr>" +
//        		    "                <td><strong>Consultation Fee (₹):</strong></td>" +
//        		    "                <td>300</td>" +
//        		    "            </tr>" +
//        		    "            <tr>" +
//        		    "                <td><strong>Service Charge (₹):</strong></td>" +
//        		    "                <td>200</td>" +
//        		    "            </tr>" +
//        		    "        </table>" +
        		    "    </div>" +
//        		    "    <div class='totals'>" +
//        		    "        <h3>Total Amount (₹): 2300</h3>" +
//        		    "        <h3>Amount Paid (₹): 2300</h3>" +
//        		    "        <h3>Balance Due (₹): 0</h3>" +
//        		    "    </div>" +
        		    "</div>" +
        		    "</body>" +
        		    "</html>";


	        // Set headers for the response
	        HttpHeaders headers = new HttpHeaders();
	        headers.add("Content-Type", "text/html; charset=UTF-8");

	        // Return the HTML content as a response entity with status 200 OK
	        return new ResponseEntity<>(htmlContent, headers, HttpStatus.OK);
	    }
	    
	    
// Appointmrnt-code 	    
	    @PostMapping("/save-appointment")
	    public ResponseEntity<String> saveAppointment(@RequestBody Map<String, Object> requestBody) {
	        try {
	            String username = appointmentService.getLoggedInUsername();
	            String patientName = (String) requestBody.get("patientName");
	            
	            String dayStr = (String) requestBody.get("day");
	            String monthStr = (String) requestBody.get("month");
	            String yearStr = (String) requestBody.get("year");

	            int day = Integer.parseInt(dayStr);    // Convert to int
	            Month month = Month.valueOf(monthStr.toUpperCase());  // Convert to int
	            int year = Integer.parseInt(yearStr);  // Convert to int

	            LocalDate appointmentDate = LocalDate.of(year, month, day);
	            String period = (String) requestBody.get("period");
	            String reason = (String) requestBody.get("reason");
	            String email = (String) requestBody.get("email");
	            String timeStr = (String) requestBody.get("time"); // Expect time in 'HH:mm' or 'HH:mm:ss' format
	            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH"); // Handle both 'HH:mm' and 'HH:mm:ss'
	            LocalTime time = LocalTime.parse(timeStr, timeFormatter);


	            // Call the service to save the appointment data
	            appointmentService.saveAppointmentData(username, patientName, appointmentDate, time, period, reason, email);

	            return ResponseEntity.ok("Appointment saved successfully.");
	        } catch (Exception e) {
	            // Handle errors (like invalid date, time, or missing parameters)
	        	e.printStackTrace();
	            return ResponseEntity.status(400).body("Error saving appointment: " + e.getMessage());
	        }
	    }
	    @GetMapping("/getAllappointments")
	    public ResponseEntity<Map<String, Object>> fetchAppointments() {
	    	String username=appointmentService.getLoggedInUsername();
	    	 Map<String, Object> response = new HashMap<>();
	    	try {
	    		List<Map<String, Object>> records=appointmentService.getAllAppointments(username);
	    		 response.put("records", records);
	    		 long TotalAppointments = this.appointmentService.TotalAppointmentCount(username);
	   	      response.put("TotalAppointments", Long.valueOf(TotalAppointments));
	   	      long TodayAppointments = this.appointmentService. countTodayAppointment(username);
	   	      response.put("TodayAppointments", Long.valueOf(TodayAppointments));
	   	      return ResponseEntity.ok(response);
			} catch (Exception e) {
				// TODO: handle exception
				 return ResponseEntity.status(500).body(null);
			}
	    }
	    
	    @PostMapping("/appointmrnt-email/send/{id}")
		public ResponseEntity<String> save(@PathVariable int id) {
			try {
//				interfaceservice.save(internship);
				userData user = this.appointmentService.data();
				List<Map<String, Object>> records = appointmentService.fetchAppointment(id);
				  String name =null;
				  LocalDate data=null;
				  Time time=null;
				  String period=null;
				  String reason=null;
				  String email=null;
				  for (Map<String, Object> record : records) {
					  name = (String) record.get("patientName");
					  java.sql.Date sqlDate = (java.sql.Date) record.get("appointmentDate");
					  data = sqlDate.toLocalDate();
					  time=(Time) record.get("time");
					  period=(String) record.get("period");
					  reason=(String) record.get("reason");
					  email=(String) record.get("email");
				  }
				String alertEmail =email;
				String subject = " Invitation from the hospital "+user.getHospitalname()+"and doctor name" +user.getFirstname()+""+user.getLastname();
				  String body = "Dear patient "+ name + ",\n\n" +
	                      "I hope this email finds you well.\n\n" +
	                      "Your Appointment was Accepted at time "+time+" "+period+" and date "+data+"\n\n" +
	                      "for the reason "+reason+"\n\n" +
	                      "your Best regards,\n" +
	                      user.getHospitalname()+",\n";
				// Send alert email
				appointmentService.sendEmail(alertEmail, subject, body);
				return ResponseEntity.ok("success");
			} catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.ok("error");
			}
		}
	    
	    
	    
}

