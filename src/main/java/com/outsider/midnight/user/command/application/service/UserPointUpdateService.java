package com.outsider.midnight.user.command.application.service;

import com.outsider.midnight.user.command.application.dto.UserPointUpdateDTO;
import com.outsider.midnight.user.command.domain.aggregate.User;
import com.outsider.midnight.user.command.domain.repository.UserCommandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UserPointUpdateService {

    private final UserCommandRepository userRepository;

    @Autowired
    public UserPointUpdateService(UserCommandRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void increaseUserPoints(UserPointUpdateDTO userPointUpdateDTO) {
        User user = userRepository.findById(userPointUpdateDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPoints(user.getPoints().add(userPointUpdateDTO.getPoint()));
        userRepository.save(user);
    }

    @Transactional
    public void decreaseUserPoints(UserPointUpdateDTO userPointUpdateDTO) {
        User user = userRepository.findById(userPointUpdateDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getPoints().compareTo(userPointUpdateDTO.getPoint()) < 0) {
            throw new IllegalArgumentException("Insufficient points");
        }

        user.setPoints(user.getPoints().subtract(userPointUpdateDTO.getPoint()));
        userRepository.save(user);
    }
}

