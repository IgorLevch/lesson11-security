package ru.geekbraines.lesson11.configs;


import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.geekbraines.lesson11.services.UserService;

// настраиваем безопасность 
@EnableWebSecurity   // врубаем правила безопасности прописанные в данном файле, а не стандартные правила  SS
@RequiredArgsConstructor
@Slf4j
//@EnableGlobalMethodSecurity(prePostEnabled=true, securedEnabled=true, jsr250Enabled=true)  - эта аннотация должна работать 
// создаем некий конфиг безопасности: 
public class SecurityConfig extends WebSecurityConfiguration{  // в уроке было -- WebSecurityConfigurerAdapter

    private final UserService userService;


    @Override //как Спринг поймет, что надо защищать, а что не надо 
    protected void configure(HttpSecurity http) throws Exception{ // конфигурируем наш http security

            log.info("Dao Authentication Provider");
            http.authorizeRequests(  // говорим, что хотим настроить параметры авторизации неких запросов
                .antMatchers("/auth_page/**").authenticated() // здесь мы говорим, что если кто-то постучался относительно корня 
                 // по адресу /auth_page/** (** - может идти все, что угодно)  -- мы хотим, чтобы этот пользов-ль был аутентифицированным
                 // (нам не важна его роль : главное, чтобы заходил не гость )
                .antMatchers("/**/*.css",  "/**/*.js").anonymous() // это из Ушаровского. Говорит о том, что статика (css, js и др.)
                // доступна всем (anonymous)   !!! Но правильнее будет .permitAll()  (а не .anonymous())
                .antMatchers("/user_info").authenticated() // если ктото постучался на /user_info -  то он тоже должен быть аутентифицирован
                .antMatchers("/admin/**").hasAnyRole("ADMIN", "SUPERADMIN") // если кто-то постучался на /admin/ - должен иметь роль  
                // ADMIN или SUPERADMIN. Роль User тут не подойдет . Т.к. есть проверка роли hasAnyRole  - то гость тоже не подойдет  
                //.antMatchers("/...").hasAnyAuthority("READ_MSGS")  -- а это , если делаем доступ по правам, а не по ролям: в данном 
                // случае, если предоставляется право "только читать сообщения"


                // что означают *:
                // если написать 
                //  /product  -- будет применяться строго к такому префиксу
                //  /product/*  -- данный префикс + 1 сегмент (например, /product/1)
                //  /product/**   -- может быть все, что угодно после product/   

                
                .anyRequest().permitAll() // все остальные запросы кроме перечисленных выше енд-пойнтов могут быть доступны абсолютно всем
                .and()
                .formLogin() // для того, чтобы полльз-ль мог вбивать логин-пароль , мы ему предоставляем форму логина 
                // выбираем способ авторизации форм-логин 
                // (ему покажется стандартная такая формочка)
                // вариант формы:   .httpBasic()  - это означает . что невошедшему польз-лю покажется виндовая стандартная форма с 2-мя полями
                // логин/ пароль и кнопкой ОК 
                .and()
                .logout() // ниже настраиваем logout:  
                .invalidateHttpSession(true)  //
                .deleteCookies("JSESSIONID");   // при логауте хотим удалить куки  JSESSIONID, чтобы вообще затереть то, что польз-ль делал 
                // .and
                // .sessionManagement()
                // .maximumSessions(1)
                // .maxSessionsPreventsLogin(true); // здесь мы указываем кол-во сессий одновременных под какой-то учеткой  


            )





    }



    @Bean
    // если хотим хранить в чистом виде хранить пароль (не ХЭШ) , то можем тут поставить какой-ниб. plain passwordEncoder
    // здесь будет такое хэширование паролей, какой passwordEncoder мы создадим 
    // в нашем случае будет алгоритм  BCrypt 
    public BCryptPasswordEncoder  passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    // Это центральный блок -- есть на кратинке под названием  Authetification Provider
    // его задача (мы ему отдали логин-пароль) - а его задача сказать: существует ли такой польз-ль или нет 
    // и если существует, то его нужно положить в Спринг Секьюрити Контекст 
    public DaoAuthenticationProvider daoAuthenticationProvider(){

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(); //  создаем новый DaoAuthenticationProvider 
 // DaoAuthenticationProvider - это такой преднастроенный провайдер , который говорит, что видимо мы где то в БД будем искать Юзеров
        // и с ними работать 
        authenticationProvider.setPasswordEncoder(passwordEncoder()); // для работы DaoAuthenticationProvider нужен PasswordEncoder
        // который будет заниматься хэшиованием паролей и сравнением ХЭШей от паролей 
        authenticationProvider.setUserDetailsService(userService); // источник данных с преобразователем (здесь он смотрит, 
        //существует ли такой польз-ль или нет) 
        return authenticationProvider;

    }





}
