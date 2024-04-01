import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import lombok.extern.slf4j.Slf4j;

@EnableWebSecurity
@Slf4j
public class SecurityConfig2 extends WebSecurityConfigurerAdapter{

    // это inMemory доступ. Просто, дополнение : как конфигурируется, если польз-ли живут в памяти 

    @Bean
    public UserDetailsService users(){
        // сразу Бин создается. 
        //создается парочка ЮзерДетейлсов (усеченная инфо о юзерах)

        UserDetails user = User.builder()
                .username("user")
                .password("{noop}100")    // noop - пароль будет храниться в чистом виде в памяти
                .roles("USER")
                .build();
        UserDetails admin = User.builder()
                .username("admin")
                .password("{bcrypt}100")    // bcrypt -- ХЭШ от пароля 
                .roles("USER", "ADMIN")
                .build();

    // и создается  InMemoryUserDetailsManager - это сразу и ЮзерДетейлс и Провайдер некий (2 в одном)        

    return new InMemoryUserDetailsManager(user, admin);

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception{
            log.info("In-memory Security Configuration");
            http.authorizeRequests()
                .antMatchers("/auth_page/**").authenticated()
                .antMatchers("/user_info").authenticated()
                .antMatchers("/admin/**").hasAnyRole("ADMIN", "SUPERADMIN")
                .anyRequest().permitAll()
                .and()
                .httpBasic();

    }




}
