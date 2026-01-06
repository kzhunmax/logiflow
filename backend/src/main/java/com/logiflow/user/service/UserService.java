package com.logiflow.user.service;

import com.logiflow.shared.exception.DuplicateResourceException;
import com.logiflow.shared.exception.UserNotFoundException;
import com.logiflow.user.dto.*;
import com.logiflow.user.model.Role;
import com.logiflow.user.model.User;
import com.logiflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request, User currentUser) {
        validateUserCreationPermission(currentUser, request.role());
        validateUniqueConstraints(request.username(), request.email(), null);

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(request.role())
                .enabled(true)
                .createdBy(currentUser.getId())
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request, User currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        validateUserModificationPermission(currentUser, user, request.role());

        if (request.email() != null) {
            validateUniqueConstraints(null, request.email(), id);
            user.setEmail(request.email());
        }
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request, User currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        validateUserModificationPermission(currentUser, user, null);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id, User currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        validateUserModificationPermission(currentUser, user, null);

        // Prevent self-deletion
        if (user.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Cannot delete your own account");
        }

        userRepository.delete(user);
    }

    @Transactional
    public UserResponse toggleUserStatus(Long id, User currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        validateUserModificationPermission(currentUser, user, null);

        // Prevent self-disabling
        if (user.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Cannot disable your own account");
        }

        user.setEnabled(!user.isEnabled());
        return UserResponse.from(userRepository.save(user));
    }

    private void validateUserCreationPermission(User currentUser, Role targetRole) {
        Role currentRole = currentUser.getRole();

        // Only SUPER_ADMIN can create other SUPER_ADMINs or ADMINs
        if (targetRole == Role.SUPER_ADMIN || targetRole == Role.ADMIN) {
            if (currentRole != Role.SUPER_ADMIN) {
                throw new AccessDeniedException("Only SUPER_ADMIN can create admin accounts");
            }
        }

        // ADMIN can create WAREHOUSE_MANAGER, WAREHOUSE_WORKER, and CUSTOMER
        if (currentRole == Role.ADMIN) {
            if (targetRole != Role.WAREHOUSE_MANAGER &&
                targetRole != Role.WAREHOUSE_WORKER &&
                targetRole != Role.CUSTOMER) {
                throw new AccessDeniedException("ADMIN can only create warehouse staff and customers");
            }
        }

        // WAREHOUSE_MANAGER can only create WAREHOUSE_WORKER
        if (currentRole == Role.WAREHOUSE_MANAGER) {
            if (targetRole != Role.WAREHOUSE_WORKER) {
                throw new AccessDeniedException("WAREHOUSE_MANAGER can only create warehouse workers");
            }
        }

        // WAREHOUSE_WORKER and CUSTOMER cannot create users
        if (currentRole == Role.WAREHOUSE_WORKER || currentRole == Role.CUSTOMER) {
            throw new AccessDeniedException("You do not have permission to create users");
        }
    }

    private void validateUserModificationPermission(User currentUser, User targetUser, Role newRole) {
        Role currentRole = currentUser.getRole();
        Role targetRole = targetUser.getRole();

        // SUPER_ADMIN can modify anyone
        if (currentRole == Role.SUPER_ADMIN) {
            return;
        }

        // ADMIN cannot modify SUPER_ADMIN
        if (targetRole == Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Cannot modify SUPER_ADMIN accounts");
        }

        // ADMIN cannot promote users to SUPER_ADMIN or ADMIN
        if (currentRole == Role.ADMIN) {
            if (newRole == Role.SUPER_ADMIN || newRole == Role.ADMIN) {
                throw new AccessDeniedException("ADMIN cannot promote users to admin roles");
            }
            // ADMIN cannot modify other ADMINs
            if (targetRole == Role.ADMIN && !currentUser.getId().equals(targetUser.getId())) {
                throw new AccessDeniedException("ADMIN cannot modify other admin accounts");
            }
            return;
        }

        // WAREHOUSE_MANAGER can only modify WAREHOUSE_WORKER
        if (currentRole == Role.WAREHOUSE_MANAGER) {
            if (targetRole != Role.WAREHOUSE_WORKER) {
                throw new AccessDeniedException("WAREHOUSE_MANAGER can only modify warehouse workers");
            }
            if (newRole != null && newRole != Role.WAREHOUSE_WORKER) {
                throw new AccessDeniedException("WAREHOUSE_MANAGER cannot change worker roles");
            }
            return;
        }

        throw new AccessDeniedException("You do not have permission to modify users");
    }

    private void validateUniqueConstraints(String username, String email, Long excludeUserId) {
        if (username != null) {
            userRepository.findByUsername(username)
                    .filter(u -> !u.getId().equals(excludeUserId))
                    .ifPresent(_ -> {
                        throw new DuplicateResourceException("Username already exists: " + username);
                    });
        }
        if (email != null) {
            userRepository.findByEmail(email)
                    .filter(u -> !u.getId().equals(excludeUserId))
                    .ifPresent(_ -> {
                        throw new DuplicateResourceException("Email already exists: " + email);
                    });
        }
    }
}

