package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import ro.unibuc.hello.config.AvailabilityIndicator;
import ro.unibuc.hello.controller.TestShutdownController;
import ro.unibuc.hello.data.Role;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.UserRepository;
import ro.unibuc.hello.dto.request.LoginDto;
import ro.unibuc.hello.dto.request.RegisterDto;
import ro.unibuc.hello.dto.response.AuthDto;
import ro.unibuc.hello.exception.EntityNotFoundException;
import ro.unibuc.hello.exception.EntityAlreadyExistsException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private ModelMapper modelMapper;

    private AvailabilityIndicator availabilityIndicator;

    private AuthService authService;
    
    private MeterRegistry meterRegistry;

    private UserEntity user;
    private RegisterDto registerDto;
    private LoginDto loginDto;

    @BeforeEach
    void beforeEach(){
        
        meterRegistry = new SimpleMeterRegistry();

        authService = new AuthService(
            userRepository,
            passwordEncoder,
            jwtService,
            authenticationManager,
            modelMapper,
            meterRegistry,
            availabilityIndicator
        );
        SecurityContextHolder.clearContext();
        user = UserEntity.builder()
                .username("user")
                .password("password")
                .role("USER")
                .build();
        registerDto = RegisterDto.builder()
                .username("user")
                .email("email")
                .password("password")
                .build();
        loginDto = new LoginDto("user","password");
    }

    @Test
    void register_ShouldThrowException_WhenUserAlreadyExists(){
        when(userRepository.findByUsername(registerDto.getUsername())).thenReturn(Optional.ofNullable(user));

        assertThrows(EntityAlreadyExistsException.class,()->authService.register(registerDto));
    }

    @Test
    void register_ShouldSucceed_WhenUserDoesNotExisty(){
        when(userRepository.findByUsername(registerDto.getUsername())).thenReturn(Optional.empty());
        when(modelMapper.map(registerDto,UserEntity.class)).thenReturn(user);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encoded");
        when(userRepository.save(user)).thenReturn(user);
        when(jwtService.generateJwt(user)).thenReturn("token");

        AuthDto authDto = authService.register(registerDto);

        assertNotNull(authDto);
        assertEquals("user",authDto.getUsername());
        assertEquals(Role.USER,authDto.getRole());
        assertEquals("token",authDto.getToken());

    }

    @Test
    void login_ShouldThrowException_WhenUserDoesNotExist(){
        when(userRepository.findByUsername(loginDto.getUsername())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,()->authService.login(loginDto));
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsIncorrect(){
        loginDto.setPassword("wrongPassword");

        when(userRepository.findByUsername(loginDto.getUsername())).thenReturn(Optional.ofNullable(user));
        when(passwordEncoder.matches(loginDto.getPassword(),user.getPassword())).thenReturn(false);
        
        assertThrows(EntityNotFoundException.class,()->authService.login(loginDto));
    }

    @Test
    void login_ShouldThrowException_WhenAuthenticationFails(){
        when(userRepository.findByUsername(loginDto.getUsername())).thenReturn(Optional.ofNullable(user));
        when(passwordEncoder.matches(loginDto.getPassword(), user.getPassword())).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenThrow(new EntityNotFoundException());

        assertThrows(EntityNotFoundException.class,()->authService.login(loginDto));
    }

    @Test
    void login_ShouldReturnAuthDto_WhenCredentialsAreValid(){
        when(userRepository.findByUsername(loginDto.getUsername())).thenReturn(Optional.ofNullable(user));
        when(passwordEncoder.matches(loginDto.getPassword(), user.getPassword())).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(jwtService.generateJwt(user)).thenReturn("token");

        AuthDto response = authService.login(loginDto);

        assertNotNull(response);
        assertEquals("user", response.getUsername());
        assertEquals(Role.USER, response.getRole());
        assertEquals("token", response.getToken());
    }

    @Test
    void logout_ShouldClearAuthenticationContext(){
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        authService.logout();
        
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }


}