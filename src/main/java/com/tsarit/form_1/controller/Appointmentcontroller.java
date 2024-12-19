//package com.tsarit.form_1.controller;
//
//import java.io.ByteArrayOutputStream;
//import java.util.List;
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestController;
//import org.xhtmlrenderer.pdf.ITextRenderer;
//
//import com.tsarit.form_1.model.userData;
//import com.tsarit.form_1.service.AppointmentService;
//
//@CrossOrigin(origins = "http://localhost:3000")
//@RestController
//public class Appointmentcontroller {
//
//	@Autowired
//	private AppointmentService appointmentService;
//	@GetMapping("/api/generate-certificate/{patientId}/{treatmentId}")
//    public ResponseEntity<ByteArrayResource> generateCertificate(@PathVariable String treatmentId,@PathVariable int patientId,@RequestParam Map<String, Object> formData) {
//        try {
//            // Find the user by email, expecting a list of webinar objects
//        	String username=appointmentService.getLoggedInUserEmail();
//                  String email=AuthenticationController.email;
//                 userData  userall= appointmentService.find(username);
//                 
//                 
//                 
//                 List<Map<String, Object>> records = appointmentService.fetch(patientId);
//                 String firstName = null;
//                 String lastName=null;
//                 String phoneNumber=null;
//                 int age=0;
//                 String disease=null;
//                 String gender=null;
//                 for (Map<String, Object> record : records) {
//                	  firstName = (String) record.get("firstName");
//                	  lastName = (String) record.get("lastName");
//                	  phoneNumber = (String) record.get("phoneNumber");
//                	  disease = (String) record.get("disease");
//                	  gender = (String) record.get("gender");
//                	  Object ageObj = record.get("age");
//                      if (ageObj instanceof String) {
//                          age = Integer.parseInt((String) ageObj);
//                      } else if (ageObj instanceof Integer) {
//                          age = (Integer) ageObj;
//                      }
//                	 
//                 }
//                 List<Map<String, Object>> treatment=appointmentService.fetchtreatment(treatmentId);
//                 String tabletCount=null;
//                 String tabletName1=null;
//                 String otherTabletName=null;
//                 String injectionSize=null;
//                 String injectionName=null;
//                 String injectionMg=null;
//                 String tests=null;
//                 String doctorAdvice=null;
//                 for (Map<String, Object> treatment2 : treatment) {
//                  tabletCount = (String) treatment2.get("tabletCount");
//                  tabletName1=(String) treatment2.get("tabletName1");
//                  otherTabletName=(String) treatment2.get("otherTabletName");
//                  injectionSize=(String) treatment2.get("injectionSize");
//                  injectionName=(String) treatment2.get("injectionName");
//                  injectionMg=(String) treatment2.get("injectionMg");
//                  tests=(String) treatment2.get("tests");
//                  doctorAdvice=(String) treatment2.get("doctorAdvice");
//                 }
//
//            // Get the absolute path for the certificate image
//            String certificateImagePath = getClass().getClassLoader().getResource("static/pdf.jpg").toExternalForm();
//            System.out.println("Image Path: " + certificateImagePath);
//            
//            // HTML for the certificate with the absolute path for the background image
//            String html = 
//            	    "<!DOCTYPE html>" +
//            	    "<html>" +
//            	    "<head>" +
//            	    "    <title>Prescription</title>" +
//            	    "    <style>" +
//            	    "        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4; }" +
//            	    "        .container { width: 100%; max-width: 800px; margin: 0 auto; background-color: white; padding: 20px; border: 1px solid #ccc; border-radius: 8px; }" +
//            	    "        .header { text-align: center; border-bottom: 2px solid #4CAF50; padding-bottom: 10px; }" +
//            	    "        .header h1 { font-size: 24px; margin: 0; color: #333; }" +
//            	    "        .header p { margin: 0; font-size: 14px; color: #777; }" +
//            	    "        .doctor-info, .patient-info { margin-top: 20px; }" +
//            	    "        .doctor-info h3, .patient-info h3 { margin-bottom: 5px; color: #4CAF50; font-size: 18px; }" +
//            	    "        .info-table { width: 100%; border-collapse: collapse; margin-top: 20px; }" +
//            	    "        .info-table th, .info-table td { padding: 10px; border: 1px solid #ddd; text-align: left; }" +
//            	    "        .info-table th { background-color: #4CAF50; color: white; }" +
//            	    "        .info-table td { background-color: #f9f9f9; }" +
//            	    "    </style>" +
//            	    "</head>" +
//            	    "<body>" +
//            	    "    <div class='container'>" +
//            	    "        <div class='header'>" +
//            	    "            <h1>"+userall.getHospitalname()+"</h1>" +
//            	    "            <p>"+userall.getAddress()+"</p>" +
//            	    "            <p>Contact:"+userall.getPhonenumber() +"| Email:"+userall.getEmailid()+"</p>" +
//            	    "        </div>" +
//            	    "        <div class='doctor-info'>" +
//            	    "            <h3>Doctor Details:</h3>" +
//            	    "            <p><strong>"+"Doctor Name:"+userall.getFirstname()+userall.getLastname()+"</strong></p>" +
//            	    "            <p><strong>Specialization:</strong> Cardiology</p>" +
//            	    "        </div>" +
//            	    "        <div class='patient-info'>" +
//            	    "            <h3>Patient Details:</h3>" +
//            	    "            <p><strong>Patient Name:</strong>"+firstName+lastName+"</p>" +
//            	    "              <p><strong>phoneNumber:</strong>"+phoneNumber+"</p>" +
//            	    "              <p><strong>disease:</strong>"+disease+"</p>" +
//            	    "            <p><strong>Age:</strong> "+age+"</p>" +
//            	    "            <p><strong>Gender:</strong>"+gender+"</p>" +
//            	    "        </div>" +
//            	    "        <div class='patient-info'>" +
//            	    "            <h3>Doctor's Prescription:</h3>" +
//            	    "            <p><strong>tabletCount:</strong>"+tabletCount+"</p>" +
//            	    "              <p><strong>injectionSize:</strong>"+injectionSize+"</p>" +
//            	    "              <p><strong>injectionName:</strong>"+injectionName+"</p>" +
//            	    "            <p><strong>injectionMg:</strong> "+injectionMg+"</p>" +
//            	    "            <p><strong>tests:</strong>"+tests+"</p>" +
//            	    "            <p><strong>doctorAdvice:</strong>"+doctorAdvice+"</p>" +
//            	    "        </div>" +
//            	    "        <table class='info-table'>" +
//            	    "            <thead>" +
//            	    "                <tr>" +
//            	    "                    <th>Tablet Name</th>" +
//            	    "                    <th>Dosage (mg)</th>" +
//            	    "                    <th>Quantity</th>" +
//            	    "                    <th>Instructions</th>" +
//            	    "                </tr>" +
//            	    "            </thead>" +
//            	    "            <tbody>" +
//            	    "                <tr>" +
//            	    "                    <td>Paracetamol</td>" +
//            	    "                    <td>500</td>" +
//            	    "                    <td>10</td>" +
//            	    "                    <td>Twice a day after meal</td>" +
//            	    "                </tr>" +
//            	    "                <tr>" +
//            	    "                    <td>"+tabletName1+"</td>" +
//            	    "                    <td>200</td>" +
//            	    "                    <td>15</td>" +
//            	    "                    <td>Once a day before meal</td>" +
//            	    "                </tr>" +
//            	    "            </tbody>" +
//            	    "        </table>" +
//            	    "        <div class='footer' style='margin-top: 40px; text-align: center;'>" +
//            	    "            <p><strong>Note:</strong> Please take the prescribed medication as directed by your doctor.</p>" +
//            	    "            <p>Issued by Hospital Name - All Rights Reserved</p>" +
//            	    "        </div>" +
//            	    "    </div>" +
//            	    "</body>" +
//            	    "</html>";
//
//
//            // Convert HTML to PDF
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            ITextRenderer renderer = new ITextRenderer();
//
//            // Set the page size based on the certificate image size, assuming it's 700x600 pixels
//            // Convert pixel values to points (1 pixel = 0.75 points)
//            renderer.getSharedContext().setBaseURL("file:src/main/resources/static/");
//            renderer.setDocumentFromString(html);
//            renderer.layout();
//
//            // Set custom PDF page size to match the certificate image size (no margins)
//            renderer.getOutputDevice();
//            renderer.createPDF(outputStream, false);
//            renderer.finishPDF();
//
//            // Prepare PDF response
//            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate.pdf");
//            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
//
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .contentLength(outputStream.size())
//                    .body(resource);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
////	@PostMapping("/save")
////	public ModelAndView save(@RequestParam Map<String, String> allParams) {
////		allParams.forEach((key, value) -> System.out.println("Param: " + key + " = " + value));
////		String username = allParams.get("username");
//////		appointmentService.saveUserData(username, allParams);
////		System.out.println("user naame from" + username);
////		ModelAndView m = new ModelAndView("redirect:/Appointment");
////		m.addObject("message", "User registered successfully...");
////		
////		 userData user = appointmentService.data();
////		 String firstname = user.getFirstname();
////		    String lastname = user.getLastname();
////		    String hospitalname = user.getHospitalname();
////		m.addObject("firstname", firstname);
////		m.addObject("lastname", lastname);
////		m.addObject("hospitalname", hospitalname);
////		System.out.println(user);
////
////		return m;
////	}
////
////	@GetMapping("/Appointment")
////	public ModelAndView getAllAppointments1(@RequestParam(value = "keyword", required = false) String keyword,
////			@RequestParam(value = "username", required = false) String username) {
////		ModelAndView m = new ModelAndView();
////
////		List<Map<String, Object>> userData = appointmentService.fetchData(username, keyword);
////		m.addObject("appointment", userData);
////		
////		LocalDateTime now = LocalDateTime.now();
////		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
////        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
////        String formattedDate = "Date: " + now.format(dateFormatter);
////        String formattedTime = "Time: " + now.format(timeFormatter);
////
////        m.addObject("date", formattedDate);
////        m.addObject("time", formattedTime);
////        
////        userData user = appointmentService.data();
////		 String firstname = user.getFirstname();
////		    String lastname = user.getLastname();
////		    String hospitalname = user.getHospitalname();
////		m.addObject("firstname", firstname);
////		m.addObject("lastname", lastname);
////		m.addObject("hospitalname", hospitalname);
////		System.out.println("user"+user);
////		
////		System.out.println("user name for:" + username);
////		System.out.println("key word :" + keyword);
////		m.setViewName("Appointment");
////		return m;
////	}
////
//////	@GetMapping("/delete/{id}")
////	public ModelAndView delete(@RequestParam(value = "username", required = false) String username,
////			@PathVariable("id") int id) {
//////		appointmentService.delete(username, id);
////		ModelAndView modelAndView = new ModelAndView("redirect:/Appointment");
////		modelAndView.addObject("message", "User deleted successfully...");
////
////		return modelAndView;
////
////	}
////
////	@GetMapping("/today")
////	public ModelAndView getTodaysAppointments(@RequestParam(value = "username", required = false) String username) {
////		LocalDate today = LocalDate.now();
////		List<Map<String, Object>> list = appointmentService.getAppointmentsByDate(username, today);
////		ModelAndView m = new ModelAndView();
////		m.setViewName("Appointment");
////		m.addObject("appointment", list);
////		
////		LocalDateTime now = LocalDateTime.now();
////		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
////        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
////        String formattedDate = "Date: " + now.format(dateFormatter);
////        String formattedTime = "Time: " + now.format(timeFormatter);
////
////        m.addObject("date", formattedDate);
////        m.addObject("time", formattedTime);
////        
////        userData user = appointmentService.data();
////		 String firstname = user.getFirstname();
////		    String lastname = user.getLastname();
////		    String hospitalname = user.getHospitalname();
////		m.addObject("firstname", firstname);
////		m.addObject("lastname", lastname);
////		m.addObject("hospitalname", hospitalname);
////		System.out.println("user"+user);	
////		
////		return m;
////	}
////
////	@GetMapping("/DoctorView")
////	public ModelAndView getTodayCount(String username, Model model) {
////		long todayCount = appointmentService.countTodayAppointments(username);
////		ModelAndView m = new ModelAndView("DoctorView");
////		m.addObject("todayCount", todayCount);
////		System.out.println("Today Count: " + todayCount);
////		
////		userData user = appointmentService.data();
////		 String firstname = user.getFirstname();
////		    String lastname = user.getLastname();
////		    String hospitalname = user.getHospitalname();
////		m.addObject("firstname", firstname);
////		m.addObject("lastname", lastname);
////		m.addObject("hospitalname", hospitalname);
////		System.out.println("user"+user);		
////		return m;
////	}
////
////	@GetMapping("/count")
////	public ModelAndView getDashboard(String username) {
////		long patientCount = appointmentService.TotalPatientCount(username);
////		ModelAndView m = new ModelAndView("count");
//////        m.setViewName("dashboard");
////		m.addObject("patientCount", patientCount);
////		
////		 userData user = new userData();
////		 String firstname = user.getFirstname();
////		    String lastname = user.getLastname();
////		    String hospitalname = user.getHospitalname();
////		m.addObject("firstname", firstname);
////		m.addObject("lastname", lastname);
////		m.addObject("hospitalname", hospitalname);
////		System.out.println(user);
////		System.out.println(m);
////		return m;
////	}
////	
////	
////
////	@GetMapping("/editAppointment/{id}")
////	public ModelAndView showUpdateForm( String username, @PathVariable("id") int id) {
////	    Map<String, Object> appointment = appointmentService.getAppointmentById(username, id);
////	    ModelAndView m = new ModelAndView();
////
////	    m.setViewName("editAppointment");
////	    m.addObject("appointment", appointment);
////
////	    return m;
////	}
////	
////	
////	@PostMapping("/update")
////	public ModelAndView updateAppointment(
////	        @RequestParam("id") Integer id,
////	        @RequestParam("username") String username,
////	        @RequestParam("firstName") String firstName,
////	        @RequestParam("lastName") String lastName,
////	        @RequestParam("phoneNumber") String phoneNumber,
////	        @RequestParam("email") String email,
////	        @RequestParam(value = "age", required = false) Integer age,
////	        @RequestParam(value = "gender", required = false) String gender,
////	        @RequestParam(value = "weight", required = false) Double weight) {
////
////	    // Debug output
////	    System.out.println("Received parameters: id=" + id + ", age=" + age + ", weight=" + weight);
////
////	    // Update the appointment
////	    appointmentService.updateAppointment(username, id, firstName, lastName, phoneNumber, email, age, gender, weight);
////
////	    ModelAndView modelAndView = new ModelAndView("redirect:/Appointment");
////	    modelAndView.addObject("message", "Appointment updated successfully!");
////
////	    return modelAndView;
////	}
//
//
//
//}
