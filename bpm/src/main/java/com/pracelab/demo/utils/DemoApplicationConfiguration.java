package com.pracelab.demo.utils;

import com.google.common.base.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


//@Configuration
//@EnableWebSecurity
//@Order(99)
//public class DemoApplicationConfiguration extends WebSecurityConfigurerAdapter {

@Configuration
@EnableSwagger2
public class DemoApplicationConfiguration {
    private Logger logger = LoggerFactory.getLogger(DemoApplicationConfiguration.class);

//    @Override
//    @Autowired
//    public void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(myUserDetailsService());
//    }

    /**
     * Definiujemy "lokalne" konta, dzieki ktorym mozemy autoryzowac m.in. mechanizmy API bez odwolywania sie do Keycloaka
     * W fazie developerskiej jest to bardzo pomocne rozwiazanie
     * @return
     */

    /**
     * Budowanie swaggera w oparciu o wystawiane API
     * @return
     */
    @Bean
    public Docket getSwagger() {
        String groupName = "PraceLab RestAPI";
        return new Docket(DocumentationType.SWAGGER_2)
                .select().apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.ant("/contracts/*"))
                .build().useDefaultResponseMessages(false)
                .groupName(groupName);
    }

    @Bean
    public UserDetailsService myUserDetailsService() {

        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();

        String[][] usersGroupsAndRoles = {
                {"prace_lab_api", "prace_lab_api", "ROLE_ACTIVITI_USER", "GROUP_prace-lab-admins"},
                {"admin_prace_lab", "admin", "ROLE_ACTIVITI_ADMIN", "GROUP_prace-lab-admins"},
        };

        try {

            for (String[] user : usersGroupsAndRoles) {
                List<String> authoritiesStrings = Arrays.asList(Arrays.copyOfRange(user, 2, user.length));
                logger.info("> Registering new user: " + user[0] + " with the following Authorities[" + authoritiesStrings + "]");
                inMemoryUserDetailsManager.createUser(new User(user[0], passwordEncoder().encode(user[1]),
                        authoritiesStrings.stream().map(s -> new SimpleGrantedAuthority(s)).collect(Collectors.toList())));
            }
        }
        catch (Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
        }

        return inMemoryUserDetailsManager;
    }
//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().antMatchers("/api/**");
//    }


//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .csrf().disable()
//    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                .authorizeRequests()
//                .antMatchers("/**")
//                .authenticated()
//                .and()
//                .httpBasic();
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}