package ru.geekbraines.lesson11.services;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.management.relation.Role;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ru.geekbraines.lesson11.entities.User;
import ru.geekbraines.lesson11.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService{
 // наследуем ЮзерДетейлс Сервис из нашей схемы 
    private final UserRepository userRepository;

    public Optional<User> findByUsername(String username){
        return ((UserService) userRepository).findByUsername(username);    // метод, который позволяет найти польз-ля по имени 
    }

    // этот метод находит польз-ля в БД и преобразовывает нашего польз-ля в поль-ля, который понимает Спринг
    @Override  
    @Transactional // Transactional здесь, потому что: открывается транзакция и мы пользователя достаем без ролей
        // (роли в lazy load) . А далее в мапере «map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList())»
        // мы обращаемся к ролям . И если transactional не будет, мы будем падать с тем, что сессии нет открытой , чтобы роли догрузить 
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username).orElseThrow(() ->new UsernameNotFoundException(
            String.format("User '%s' not found", username)));
        //throw new UnsupportedOperationException("Unimplemented method 'loadUserByUsername'");

        return  new org.springframework.security.core.userdetails.User(  // это спринговый юзер, не наш 
        //(мы нашего польз-ля преобразоывваем к спринговому)
            user.getUsername(),user.getPassword(), mapRolesToAuthorities(user.getRoles())); // в имя и пароль 
            //мы кидаем имя и пароль нашего польз-ля
    }
    
    // чтобы преобразовать наши роли в эти GrantedAuthority мы сделали целый метод (см. ниже)

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<ru.geekbraines.lesson11.entities.Role> collection){
        //  GrantedAuthority   -  это просто обертка над строкой (право доступа - это всего лишь обычная строка)

        return collection.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
        // в этом методе мы у польз-ля запрашиваем список ролей: user.getRoles() -- на входе 
        // далее в списке ролей каждую роль преобразуем к  SimpleGrantedAuthority:
        // roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
        // (мы создаем объект, отдавая туда имя нашей роли)
        // строку преобразовываем к строке и переупаковываем в другой объект , далее - собрали в коллекцию , вернули. 
    }



}
