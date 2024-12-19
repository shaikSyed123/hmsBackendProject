package com.tsarit.form_1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tsarit.form_1.service.AppointmentService;
import com.tsarit.form_1.service.UserContext;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
	@Autowired
    private AppointmentService appointmentService;

	@Autowired
	private coustomUserDetailService coustomUserDetailService;
	@Autowired
	private JwtUtil jwtUtil;
	
	private static final ThreadLocal<String> currentUsername = new ThreadLocal<>();

	public JwtRequestFilter(coustomUserDetailService coustomUserDetailService, JwtUtil jwtUtil) {
		this.coustomUserDetailService = coustomUserDetailService;
		this.jwtUtil = jwtUtil;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
//      try {
		String requestURI = request.getRequestURI();
		if (requestURI.equals("/auth/login") || requestURI.equals("/auth/register")) {
			chain.doFilter(request, response);
			return; // Skip further filter processing
		}

		final String authorizationHeader = request.getHeader("Authorization");
		String username = null;
		String jwt = null;

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			jwt = authorizationHeader.substring(7);
			username = jwtUtil.getUsernameFromToken(jwt);
			try {
//				username = jwtUtil.getUsernameFromToken(jwt); // Extract username from JWT token
				 currentUsername.set(username);
//				   UserContext.setCurrentUserEmail(username);
				 System.out.println("Extracted username: " + username);
			} catch (Exception e) {
				System.out.println("Error extracting username from JWT token: " + e.getMessage());
			}
		}
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			UserDetails userDetails = this.coustomUserDetailService.loadUserByUsername(username);
			
			 System.out.println("Username stored in ThreadLocal: " + currentUsername.get());
			if (jwtUtil.validateToken(jwt, userDetails)) {
				 String role =jwtUtil.getRoleFromToken(jwt);
				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
				usernamePasswordAuthenticationToken
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
				 UserContext.setCurrentUserEmail(username);
				 appointmentService.createUserTables(username);
		        appointmentService.createAppointmentsTables( username);
				System.out.println("User authenticated: " + username); // Add logging here
			} else {
				System.out.println("Invalid JWT token for user: " + username);
			}
		} else {
			System.out.println("No JWT token found or user already authenticated");
		}
		chain.doFilter(request, response);
//      }
//      finally {
//          currentUsername.remove();  // Always clear the ThreadLocal after the request is done
//      }
	}
	
	 public static String getCurrentUsername() {
	        return currentUsername.get();
	    }
}
