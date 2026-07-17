package com.truckmate.repository;

import com.truckmate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRoleOrderByPriorityAsc(User.Role role);
    Optional<User> findByRoleAndTurnActive(User.Role role, Boolean turnActive);
    long countByRole(User.Role role);
}
