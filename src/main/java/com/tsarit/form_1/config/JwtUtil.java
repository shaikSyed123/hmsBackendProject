package com.tsarit.form_1.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {

	@Value("${jwt.auth.app}")
	private String appName;
	
	@Value("${jwt.auth.secret_key}")
	private String secretKey;
	
	@Value("${jwt.auth.expires_in}")
    private int expiresIn;
	
	private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;


	
	private Claims getAllClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

	
	 public String getUsernameFromToken(String token) {
	        String username;
	        try {
	            final Claims claims = this.getAllClaimsFromToken(token);
	            username = claims.getSubject();
	        } catch (Exception e) {
	            username = null;
	        }
	        return username;
	 }
	 public String getRoleFromToken(String token) {
	        try {
	            final Claims claims = this.getAllClaimsFromToken(token);
	            return claims.get("role", String.class);  // Expecting role stored as a claim
	        } catch (Exception e) {
	            return null;
	        }
	    }
	 
	 public String generateToken(String username, String role) throws InvalidKeySpecException, NoSuchAlgorithmException {
	        Map<String, Object> claims = new HashMap<>();
	        claims.put("role", role);  // Adding the role as part of claims
	        return Jwts.builder()
	                .setIssuer(appName)
	                .setClaims(claims)
	                .setSubject(username)
	                .setIssuedAt(new Date())
	                .setExpiration(generateExpirationDate())
	                .signWith(SIGNATURE_ALGORITHM, secretKey)
	                .compact();
	    }
	 
	 private Date generateExpirationDate() {
		 return new Date(new Date().getTime() + expiresIn * 1000);
	 }
	 
	 public Boolean validateToken(String token, UserDetails userDetails) {
	        final String username = getUsernameFromToken(token);
	        return (
	                username != null &&
	                username.equals(userDetails.getUsername()) &&
	                        !isTokenExpired(token)
	        );
	  }
	 
	 public boolean isTokenExpired(String token) {
		Date expireDate=getExpirationDate(token);
		return expireDate.before(new Date());
	}


	private Date getExpirationDate(String token) {
		 Date expireDate;
	        try {
	            final Claims claims = this.getAllClaimsFromToken(token);
	            expireDate = claims.getExpiration();
	        } catch (Exception e) {
	        	expireDate = null;
	        }
	        return expireDate;
	}


	public Date getIssuedAtDateFromToken(String token) {
	        Date issueAt;
	        try {
	            final Claims claims = this.getAllClaimsFromToken(token);
	            issueAt = claims.getIssuedAt();
	        } catch (Exception e) {
	            issueAt = null;
	        }
	        return issueAt;
	  }
	
	public String getToken( HttpServletRequest request ) {
      
        String authHeader = getAuthHeaderFromHeader( request );
        if ( authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

	public String getAuthHeaderFromHeader( HttpServletRequest request ) {
        return request.getHeader("Authorization");
    }
}
