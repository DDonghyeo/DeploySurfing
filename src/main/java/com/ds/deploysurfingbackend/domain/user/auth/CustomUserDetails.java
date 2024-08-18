package com.ds.deploysurfingbackend.domain.user.auth;

import com.ds.deploysurfingbackend.domain.user.entity.User;
import com.ds.deploysurfingbackend.domain.user.entity.type.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails extends AuthUser implements UserDetails {

    //인가용 객체 생성자
    public CustomUserDetails(Long id, String email, String password, List<Role> roles) {
        super(id, email, password, roles);
    }

    //인증용 객체 생성자
    public CustomUserDetails(User user) {
        super(user.getId(), user.getEmail(), user.getPassword(), user.getRoles());
    }

    //LoginUser Role 추가
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<GrantedAuthority>(getRoles().stream().map(Role::toString).map(SimpleGrantedAuthority::new).toList());
    }


    @Override
    public String getUsername() {
        return getEmail();
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
}
