package com.outsider.midnight.user.query.application.service;


import com.outsider.midnight.user.command.application.dto.UserInfoRequestDTO;
import com.outsider.midnight.user.command.domain.aggregate.User;
import com.outsider.midnight.user.command.domain.repository.UserCommandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserInfoService {

    private final UserCommandRepository userRepository;

    @Autowired
    public UserInfoService(UserCommandRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Method to retrieve user by ID and map to UserInfoRequestDTO
    public Optional<UserInfoRequestDTO> getUserInfoById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.map(this::convertToDTO);
    }

    // Method to retrieve user by email and map to UserInfoRequestDTO
    public Optional<UserInfoRequestDTO> getUserInfoByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.map(this::convertToDTO);
    }

    // Private method to convert User entity to UserInfoRequestDTO
    private UserInfoRequestDTO convertToDTO(User user) {
        return new UserInfoRequestDTO(
                user.getEmail(),
                user.getPassword(),
                user.getUserName(),
                user.getAge(),
                user.getGender(),
                user.getLocation(),
                user.getAuthority()
        );
    }
}
