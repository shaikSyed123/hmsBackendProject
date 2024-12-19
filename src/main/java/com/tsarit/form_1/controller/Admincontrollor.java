package com.tsarit.form_1.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tsarit.form_1.model.Getintouch;
import com.tsarit.form_1.model.userData;
import com.tsarit.form_1.service.AppointmentService;
@Controller
@RequestMapping("/Admin")
public class Admincontrollor {

	@Autowired
	private AppointmentService appointmentService;
	 @GetMapping("/fetchAll-hospitals")
	    public  ResponseEntity<java.util.List<userData>> fetchHospitals(){
	    	try {
	    		List<userData> records=appointmentService.getAllHospitals();
	    		return ResponseEntity.ok(records);

			} catch (Exception e) {
				// TODO: handle exception
				return ResponseEntity.ok(null);
			}
	    }
	    
	    @GetMapping("/fetchTouchmessages")
	    public ResponseEntity<List<Getintouch>> fetchTouchmessages(){
	    	try {
				List<Getintouch> records=appointmentService.getAllTouchMessages();
				return ResponseEntity.ok(records);
			} catch (Exception e) {
				// TODO: handle exception
				return ResponseEntity.ok(null);
			}
	    }
	    
}
