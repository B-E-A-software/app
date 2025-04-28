package ro.unibuc.hello.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import ro.unibuc.hello.exception.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import ro.unibuc.hello.data.Role;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.UserRepository;
import ro.unibuc.hello.dto.request.LoginDto;
import ro.unibuc.hello.dto.request.RegisterDto;
import ro.unibuc.hello.dto.response.AuthDto;
import ro.unibuc.hello.exception.EntityAlreadyExistsException;

@Service
public class AuthService {

    
    private final UserRepository userRepository;
    
    private final PasswordEncoder passwordEncoder;
    
    private final JwtService jwtService;
    
    private final AuthenticationManager authenticationManager;
    
    private final ModelMapper modelMapper;

    private final MeterRegistry meterRegistry;

    // METRICS
    private Counter registerCounter;
    private Counter loginCounter;
    private Counter registerFailureCounter;
    private Counter loginFailureCounter;
    private Counter logoutCounter;
    private Timer authenticationTimer;

    public AuthService(UserRepository userRepository,
                   PasswordEncoder passwordEncoder,
                   JwtService jwtService,
                   AuthenticationManager authenticationManager,
                   ModelMapper modelMapper,
                   MeterRegistry meterRegistry) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
    this.modelMapper = modelMapper;
    this.meterRegistry = meterRegistry;

    this.registerCounter = Counter.builder("auth_register_total")
    .description("Total number of user registrations")
    .register(meterRegistry);

    this.registerFailureCounter = Counter.builder("auth_register_failure_total")
        .description("Total number of failed register attempts")
        .register(meterRegistry);

    this.loginCounter = Counter.builder("auth_login_success_total")
        .description("Total number of successful logins")
        .register(meterRegistry);

    this.loginFailureCounter = Counter.builder("auth_login_failure_total")
        .description("Total number of failed login attempts")
        .register(meterRegistry);

    this.logoutCounter = Counter.builder("auth_logout_total")
        .description("Total number of logouts")
        .register(meterRegistry);

    this.authenticationTimer = Timer.builder("auth_authentication_duration_seconds")
        .description("Time taken to authenticate users")
        .register(meterRegistry);
    }



    private void initMetrics() {
        this.registerCounter = Counter.builder("auth_register_total")
            .description("Total number of user registrations")
            .register(meterRegistry);

        this.registerFailureCounter = Counter.builder("auth_register_failure_total")
            .description("Total number of failed register attempts")
            .register(meterRegistry);

        this.loginCounter = Counter.builder("auth_login_success_total")
            .description("Total number of successful logins")
            .register(meterRegistry);

        this.loginFailureCounter = Counter.builder("auth_login_failure_total")
            .description("Total number of failed login attempts")
            .register(meterRegistry);

        this.logoutCounter = Counter.builder("auth_logout_total")
            .description("Total number of logouts")
            .register(meterRegistry);

        this.authenticationTimer = Timer.builder("auth_authentication_duration_seconds")
            .description("Time taken to authenticate users")
            .register(meterRegistry);
    }

    public UserEntity loadUser(String username) {
        return userRepository.findByUsername(username)
                                .orElseThrow(EntityNotFoundException::new);
    }

    public AuthDto register(RegisterDto registerDto){
        userRepository.findByUsername(registerDto.getUsername())
                .ifPresent(user -> {registerFailureCounter.increment();
                     throw new EntityAlreadyExistsException(); });
        
        var user = modelMapper.map(registerDto, UserEntity.class);
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        String token = jwtService.generateJwt(user);
        userRepository.save(user);

        registerCounter.increment();

        return AuthDto.builder()
                    .username(registerDto.getUsername())
                    .email(registerDto.getEmail())
                    .role(user.getRole())
                    .token(token)
                    .build();
    }

    private UserEntity checkLoginDetails(LoginDto loginDto){
        var user = loadUser(loginDto.getUsername());
        if(!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            loginFailureCounter.increment();
            throw new EntityNotFoundException("user");
        }
        return user;
    }

    private void authenticate(LoginDto loginDto){
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()
                    ));
        }catch(Exception e){
            throw new EntityNotFoundException("user");
        }
    }

    public AuthDto login(LoginDto loginDto){
        var user = checkLoginDetails(loginDto);
        authenticate(loginDto);
        String token = jwtService.generateJwt(user);

   
    Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authentication);

        loginCounter.increment();

        return AuthDto.builder()
                .username(loginDto.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .build();
    }

    public void logout(){
        SecurityContextHolder.clearContext();
        logoutCounter.increment();
    }
}
