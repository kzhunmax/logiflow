package com.logiflow.user.service;

import com.logiflow.shared.exception.DuplicateResourceException;
import com.logiflow.shared.exception.UserNotFoundException;
import com.logiflow.user.dto.*;
import com.logiflow.user.mapper.UserMapper;
import com.logiflow.user.model.Role;
import com.logiflow.user.model.User;
import com.logiflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    private static final Set<Role> ADMIN_ROLES = Set.of(Role.SUPER_ADMIN, Role.ADMIN);
    private static final Set<Role> ADMIN_CREATABLE_ROLES = Set.of(Role.WAREHOUSE_MANAGER, Role.WAREHOUSE_WORKER, Role.CUSTOMER);

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userMapper.toDto(findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#username")
    public UserResponse getUserByUsername(String username) {
        return userMapper.toDto(findByUsernameOrThrow(username));
    }

    @Transactional(readOnly = true)
    public User getEntityByUsername(String username) {
        return findByUsernameOrThrow(username);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(userMapper::toDto)
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

        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    @CachePut(value = "users", key = "#currentUser.username")
    public UserResponse updateUser(Long id, UpdateUserRequest request, User currentUser) {
        User user = findByIdOrThrow(id);
        validateUserModificationPermission(currentUser, user, request.role());

        updateFieldIfPresent(request.email(), email -> {
            validateUniqueConstraints(null, email, id);
            user.setEmail(email);
        });
        updateFieldIfPresent(request.fullName(), user::setFullName);
        updateFieldIfPresent(request.role(), user::setRole);
        updateFieldIfPresent(request.enabled(), user::setEnabled);

        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request, User currentUser) {
        User user = findByIdOrThrow(id);
        validateUserModificationPermission(currentUser, user, null);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#currentUser.username")
    public void deleteUser(Long id, User currentUser) {
        User user = findByIdOrThrow(id);
        validateUserModificationPermission(currentUser, user, null);
        validateNotSelf(currentUser, user, "Cannot delete your own account");

        userRepository.delete(user);
    }

    @Transactional
    public UserResponse toggleUserStatus(Long id, User currentUser) {
        User user = findByIdOrThrow(id);
        validateUserModificationPermission(currentUser, user, null);
        validateNotSelf(currentUser, user, "Cannot disable your own account");

        user.setEnabled(!user.isEnabled());
        return userMapper.toDto(userRepository.save(user));
    }

    private User findByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    private User findByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    private <T> void updateFieldIfPresent(T value, java.util.function.Consumer<T> updater) {
        if (value != null) {
            updater.accept(value);
        }
    }

    private void validateNotSelf(User currentUser, User targetUser, String message) {
        if (targetUser.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(message);
        }
    }

    private void validateUserCreationPermission(User currentUser, Role targetRole) {
        Role currentRole = currentUser.getRole();

        switch (currentRole) {
            case SUPER_ADMIN -> { /* Can create any role */ }
            case ADMIN -> validateAdminCanCreate(targetRole);
            case WAREHOUSE_MANAGER -> validateManagerCanCreate(targetRole);
            default -> throw new AccessDeniedException("You do not have permission to create users");
        }
    }

    private void validateAdminCanCreate(Role targetRole) {
        if (!ADMIN_CREATABLE_ROLES.contains(targetRole)) {
            throw new AccessDeniedException("ADMIN can only create warehouse staff and customers");
        }
    }

    private void validateManagerCanCreate(Role targetRole) {
        if (targetRole != Role.WAREHOUSE_WORKER) {
            throw new AccessDeniedException("WAREHOUSE_MANAGER can only create warehouse workers");
        }
    }

    private void validateUserModificationPermission(User currentUser, User targetUser, Role newRole) {
        Role currentRole = currentUser.getRole();
        Role targetRole = targetUser.getRole();

        if (currentRole == Role.SUPER_ADMIN) {
            return; // Can modify anyone
        }

        if (targetRole == Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Cannot modify SUPER_ADMIN accounts");
        }

        switch (currentRole) {
            case ADMIN -> validateAdminCanModify(currentUser, targetUser, newRole);
            case WAREHOUSE_MANAGER -> validateManagerCanModify(targetUser, newRole);
            default -> throw new AccessDeniedException("You do not have permission to modify users");
        }
    }

    private void validateAdminCanModify(User currentUser, User targetUser, Role newRole) {
        if (ADMIN_ROLES.contains(newRole)) {
            throw new AccessDeniedException("ADMIN cannot promote users to admin roles");
        }
        if (targetUser.getRole() == Role.ADMIN && !currentUser.getId().equals(targetUser.getId())) {
            throw new AccessDeniedException("ADMIN cannot modify other admin accounts");
        }
    }

    private void validateManagerCanModify(User targetUser, Role newRole) {
        if (targetUser.getRole() != Role.WAREHOUSE_WORKER) {
            throw new AccessDeniedException("WAREHOUSE_MANAGER can only modify warehouse workers");
        }
        if (newRole != null && newRole != Role.WAREHOUSE_WORKER) {
            throw new AccessDeniedException("WAREHOUSE_MANAGER cannot change worker roles");
        }
    }

    private void validateUniqueConstraints(String username, String email, Long excludeUserId) {
        if (username != null) {
            checkNotDuplicate(
                    userRepository.findByUsername(username),
                    excludeUserId,
                    "Username already exists: " + username
            );
        }
        if (email != null) {
            checkNotDuplicate(
                    userRepository.findByEmail(email),
                    excludeUserId,
                    "Email already exists: " + email
            );
        }
    }

    private void checkNotDuplicate(java.util.Optional<User> existing, Long excludeUserId, String message) {
        existing
                .filter(u -> !u.getId().equals(excludeUserId))
                .ifPresent(_ -> {
                    throw new DuplicateResourceException(message);
                });
    }
}

