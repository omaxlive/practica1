package com.example.practica1.configuration;

import com.example.practica1.entity.Role;
import com.example.practica1.service.UserSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.security.SecureRandom;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserSecurityService userSecurityService;

    private static final String SALT = "salt";

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12, new SecureRandom(SALT.getBytes()));
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userSecurityService).passwordEncoder(passwordEncoder());
    }

    private static final String[] PUBLIC_MATCHERS = {
            "/h2-console/**",
            "/rest/**",
            "/graphql",
            "/graphiql",
            "/subscriptions",
            "/vendor/**"
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests() //NOTE: More to less restrictive
                .antMatchers(PUBLIC_MATCHERS).permitAll()
                //.mvcMatchers("/", "/index").hasAuthority(Role.ROLE_USER)
                .anyRequest().authenticated()
                .and()
                .formLogin() // NOTE: a login form is showed when no authenticated request
                //.loginPage("/login")
                .failureUrl("/login?error")
                .defaultSuccessUrl("/index")
                .usernameParameter("email")
                .passwordParameter("password")
                .permitAll()
                //.and()
                //.httpBasic()
                .and()
                .rememberMe()
                .rememberMeParameter("remember-me") // NOTE: without rememberMeParameter it doesn't work!!!
                .tokenValiditySeconds(2419200) //28 days
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // NOTE: must be like this instead of .logoutUrl("/logout") when .csrf() is enabled, ya que tendriamos que hacer un post en ese caso
                .logoutSuccessUrl("/logout")
                .deleteCookies("remember-me")
                .permitAll()
                .and()
                //.cors()
                //.and()
                .csrf().disable();
        http.headers().frameOptions().disable();
        //.ignoringAntMatchers("/sourcelist/**") //deshabilitamos la proteccion csrf solo en la api source
        //.and().requiresChannel().anyRequest().requiresSecure();
    }
}
