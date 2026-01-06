package com.logiflow.user.model;

public enum Role {
    SUPER_ADMIN,      // Can manage all users and system settings
    ADMIN,            // Can manage users except super admins
    WAREHOUSE_MANAGER, // Can manage warehouse operations and workers
    WAREHOUSE_WORKER,  // Can perform warehouse tasks
    CUSTOMER          // Can view orders and inventory status
}
