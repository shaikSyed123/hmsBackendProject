//package com.tsarit.form_1.config;
//
//import java.util.Collection;
//import java.util.HashSet;
//
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import com.tsarit.form_1.model.userData;
//
//public class coustomUserdetails implements UserDetails{
//
//	
//	private static final long serialVersionUID = 1L;
//	private userData u;
//
//	public coustomUserdetails(userData u) {
//		super();
//		this.u = u;
//	}
//
//	@Override
//	public Collection<? extends GrantedAuthority> getAuthorities() {
//         HashSet<SimpleGrantedAuthority> set=new HashSet<SimpleGrantedAuthority>();
//         set.add(new SimpleGrantedAuthority(u.getRole()));
//		return null;
//	}
//
//	@Override
//	public String getPassword() {
//		// TODO Auto-generated method stub
//		return u.getPassword();
//	}
//
//	@Override
//	public String getUsername() {
//		// TODO Auto-generated method stub
//		return u.getEmailid();
//	}
//
//	@Override
//	public boolean isAccountNonExpired() {
//		// TODO Auto-generated method stub
//		return true;
//	}
//
//	@Override
//	public boolean isAccountNonLocked() {
//		// TODO Auto-generated method stub
//		return true;
//	}
//
//	@Override
//	public boolean isCredentialsNonExpired() {
//		// TODO Auto-generated method stub
//		return true;
//	}
//
//	@Override
//	public boolean isEnabled() {
//		// TODO Auto-generated method stub
//		return true;
//	}
//     
//}
