package com.pillmate.pillmate.Security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.pillmate.pillmate.Domain.User;

import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails, OAuth2User {
    
    private final User user;
    
    public CustomUserDetails(User user) {
        this.user = user;
    }
    
    // UserDetails 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    // OAuth2User 구현
    @Override
    public Map<String, Object> getAttributes() {
        return Collections.emptyMap();
    }
    
    @Override
    public String getName() {
        return user.getEmail();
    }
    
    // 추가 메서드
    public Long getId() {
        return user.getId();
    }
    
    public String getEmail() {
        return user.getEmail();
    }
}

