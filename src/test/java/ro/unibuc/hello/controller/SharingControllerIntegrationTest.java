package ro.unibuc.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.web.context.WebApplicationContext;

import ro.unibuc.hello.data.*;
import ro.unibuc.hello.dto.request.*;
import ro.unibuc.hello.dto.response.*;
import ro.unibuc.hello.service.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("IntegrationTest")
@WithMockUser(username = "admin", roles = {"ADMIN"})
public class SharingControllerIntegrationTest {
    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.20")
            .withExposedPorts(27017)
            .withSharding();

    @BeforeAll
    public static void setUp() {
        mongoDBContainer.start();
    }

    @AfterAll
    public static void tearDown() {
        mongoDBContainer.stop();
    }

    @BeforeEach
    void clearDatabaseAndAddTestData() {
        userRepository.deleteAll();
        userListRepository.deleteAll();
        requestRepository.deleteAll();

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        RegisterDto creatorRegisterDto = RegisterDto.builder().username("creatorUser")
                            .email("email1@yahoo.com")
                            .password("P@ssword123")
                            .build();

        authService.register(creatorRegisterDto);

    
        RegisterDto userRegisterDto = RegisterDto.builder().username("userTest")
                            .email("email2@yahoo.com")
                            .password("P@ssword123")
                            .build();

        authService.register(userRegisterDto);
       
        LoginDto userLoginDto = LoginDto.builder().username("creatorUser")
                            .password("P@ssword123")
                            .build();

        authService.login(userLoginDto); 
       
        ToDoListResponseDto toDoListDto = ToDoListResponseDto.builder()
        .name("list") 
        .description("description")  
        .build(); 

        todoService.createToDoList(toDoListDto);

      
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        final String MONGO_URL = "mongodb://host.docker.internal:";
        final String PORT = String.valueOf(mongoDBContainer.getMappedPort(27017));

    
        registry.add("mongodb.connection.url", () -> MONGO_URL + PORT);
    }

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private SharingService sharingService;

    @Autowired
    private ToDoService todoService;
    
    @Autowired
    private UserListRepository userListRepository;
    
    @Autowired
    private RequestRepository requestRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private WebApplicationContext context;

    @Test
    public void testCreateRequest_WhenSuccessful_ShouldReturnRequestResponseDto() throws Exception {
    RequestDto requestDto = new RequestDto("userTest", "list", "desc");

    LoginDto userLoginDto = LoginDto.builder().username("userTest")
    .password("P@ssword123")
    .build();

    authService.login(userLoginDto); 

    mockMvc.perform(post("/request/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("userTest"))
            .andExpect(jsonPath("$.toDoList").value("list"))
            .andExpect(jsonPath("$.description").value("desc"));
    }


    @Test
    public void testDenyRequest_WhenRequestExists_ShouldReturnTrue() throws Exception {
        requestRepository.save(new RequestEntity("userTest", "list", "desc"));
        RequestDto requestDto = new RequestDto("userTest", "list", "desc");

        mockMvc.perform(post("/request/deny")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testDenyRequest_WhenRequestDoesNotExist_ShouldReturnFalse() throws Exception {
        RequestDto requestDto = new RequestDto("userTest", "list", "desc");

        mockMvc.perform(post("/request/deny")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void testAcceptRequest_WhenRequestDoesNotExist_ShouldReturnNotFound() throws Exception {
        RequestDto requestDto = new RequestDto("userTest", "list2", "desc");

        mockMvc.perform(post("/request/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }


}
