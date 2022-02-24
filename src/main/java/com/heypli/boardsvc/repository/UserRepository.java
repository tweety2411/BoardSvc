package com.heypli.boardsvc.repository;

import com.heypli.boardsvc.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    public User findByName(String user);

    public User findByEmail(String email);
}
