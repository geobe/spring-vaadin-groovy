package de.fh_zwickau.pti.geobe;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.vaadin.spring.config.VaadinExtensionsConfiguration;
import org.vaadin.spring.http.HttpService;
import org.vaadin.spring.security.annotation.EnableVaadinSharedSecurity;
import org.vaadin.spring.security.config.VaadinSharedSecurityConfiguration;
import org.vaadin.spring.security.web.VaadinRedirectStrategy;
import org.vaadin.spring.security.web.authentication.VaadinAuthenticationSuccessHandler;
import org.vaadin.spring.security.web.authentication.VaadinUrlAuthenticationSuccessHandler;

/**
 * Configure Spring Security. Adapted from Vaadin4Spring security sample
 * @author Petter Holmström (petter@vaadin.com)
 * @author Georg Beier
 *
 */
@Configuration
@EnableWebSecurity
@Import({VaadinExtensionsConfiguration.class, VaadinSharedSecurityConfiguration.class})
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, proxyTargetClass = true)
@EnableVaadinSharedSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("user").password("user").roles("USER")
                .and()
                .withUser("admin").password("admin").roles("ADMIN");
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable(); // Use Vaadin's built-in CSRF protection instead
        httpSecurity.authorizeRequests()
                .antMatchers("/login/**").anonymous()
                .antMatchers("/vaadinServlet/UIDL/**").permitAll()
                .antMatchers("/vaadinServlet/HEARTBEAT/**").permitAll()
                .anyRequest().authenticated();
        httpSecurity.authorizeRequests()
                .antMatchers("/console/**").permitAll();
        httpSecurity.csrf().disable(); // Use Vaadin's built-in CSRF protection instead
        httpSecurity.headers().frameOptions().disable();
        httpSecurity.httpBasic().disable();
        httpSecurity.formLogin().disable();
        httpSecurity.logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll();
        httpSecurity.exceptionHandling()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));
        httpSecurity.rememberMe().rememberMeServices(rememberMeServices()).key("myAppKey");
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/VAADIN/**");
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public RememberMeServices rememberMeServices() {
        // Is there some way of exposing the RememberMeServices instance that the remember me
        // configurer creates by default?
        TokenBasedRememberMeServices services =
                new TokenBasedRememberMeServices("myAppKey", userDetailsService());
        services.setAlwaysRemember(true);
        return services;
    }

    @Bean(name = VaadinSharedSecurityConfiguration.VAADIN_AUTHENTICATION_SUCCESS_HANDLER_BEAN)
    VaadinAuthenticationSuccessHandler vaadinAuthenticationSuccessHandler(
            HttpService httpService, VaadinRedirectStrategy vaadinRedirectStrategy) {
        return new VaadinUrlAuthenticationSuccessHandler(httpService, vaadinRedirectStrategy, "/");
    }
}
