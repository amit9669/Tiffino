package com.tiffino.service.impl;

import com.tiffino.repository.UserRepository;
import com.tiffino.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;
}
