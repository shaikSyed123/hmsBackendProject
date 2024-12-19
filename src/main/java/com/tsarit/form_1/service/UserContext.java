package com.tsarit.form_1.service;

public class UserContext {
	private static final ThreadLocal<String> currentUserEmail = new ThreadLocal<>();

    // Static method to set the current user's email
    public static void setCurrentUserEmail(String email) {
    	System.out.println("contex usre from "+email);
        currentUserEmail.set(email);
    }

    // Static method to retrieve the current user's email
    public static String getCurrentUserEmail() {
    	 String email = currentUserEmail.get();
    	    System.out.println("Retrieved email from ThreadLocal: " + email);
    	    return email;
    }

    // Static method to clear the email after request completion (important for cleanup)
    public static void clear() {
        currentUserEmail.remove();
    }
}
