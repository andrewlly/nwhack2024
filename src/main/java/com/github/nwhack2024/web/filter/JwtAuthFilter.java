package com.github.nwhack2024.web.filter;

import com.github.nwhack2024.domain.entity.User;
import com.github.nwhack2024.security.LoginUser;
import com.github.nwhack2024.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    LoginUser loginUser;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if(token == null){
            doFilter(request,response,filterChain);
            return;
        }

        Claims claims;
        try {
            claims = JwtUtil.parseJWT(token);
        } catch (SignatureException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("token invalid, please re-login");
            return;
        }
        Long uuid = claims.get("uid",Long.class);
        String email = claims.get("email", String.class);
        String username = claims.get("username",String.class);
        String avatar = claims.get("avatar",String.class);
        List<String> perms = claims.get("perms", ArrayList.class);
        User user = new User();
        user.setUid(uuid);
        user.setEmail(email);
        user.setAvatarUrl(avatar);
        user.setUserName(username);
        LoginUser loginUser = new LoginUser();
        loginUser.setUser(user);
        loginUser.setPermission(perms);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser,null,loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        doFilter(request,response,filterChain);
    }
}
