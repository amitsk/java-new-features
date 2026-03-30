package com.example;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    UserRepository mockRepo;

    @InjectMocks
    UserService userService;

    @Test
    @DisplayName("should return username when user exists")
    void returnsUsernameForValidId() {
        when(mockRepo.findById(1)).thenReturn(Optional.of(new User(1, "Alice")));

        String name = userService.getUsername(1);

        assertThat(name).isEqualTo("Alice");
        verify(mockRepo).findById(1);
    }

    @Test
    @DisplayName("should throw when user not found")
    void throwsForUnknownId() {
        when(mockRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUsername(99))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("99");
    }

    @Test
    @DisplayName("should save a new user on register")
    void savesUserOnRegister() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        userService.register("Bob", "bob@example.com");

        verify(mockRepo).save(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("Bob");
    }
}
