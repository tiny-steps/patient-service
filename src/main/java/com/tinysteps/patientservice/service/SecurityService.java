package com.tinysteps.patientservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service for handling security context and branch-related operations.
 * Extracts user information and branch context from JWT tokens.
 */
@Service
@Slf4j
public class SecurityService {

    /**
     * Get the current user ID from the JWT token
     */
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String userIdStr = jwt.getClaimAsString("sub");
            if (userIdStr != null) {
                return UUID.fromString(userIdStr);
            }
        }
        throw new SecurityException("Unable to extract user ID from token");
    }

    /**
     * Get the current user's roles from the JWT token
     */
    @SuppressWarnings("unchecked")
    public List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsStringList("roles");
        }
        throw new SecurityException("Unable to extract user roles from token");
    }

    /**
     * Get the list of context IDs for a specific domain type
     * @param domainType The domain type (e.g., "healthcare", "ecommerce", "cab-booking")
     * @return List of context IDs for the specified domain
     */
    @SuppressWarnings("unchecked")
    public List<UUID> getContextIds(String domainType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            List<String> contextIdStrings = jwt.getClaimAsStringList("context_ids");
            String tokenDomainType = jwt.getClaimAsString("domain_type");
            
            if (contextIdStrings != null && domainType.equals(tokenDomainType)) {
                return contextIdStrings.stream()
                        .map(UUID::fromString)
                        .toList();
            }
        }
        throw new SecurityException("Unable to extract context IDs for domain: " + domainType);
    }

    /**
     * Get the primary context ID for a specific domain type
     * @param domainType The domain type (e.g., "healthcare", "ecommerce", "cab-booking")
     * @return Primary context ID for the specified domain
     */
    public UUID getPrimaryContextId(String domainType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String primaryContextIdStr = jwt.getClaimAsString("primary_context_id");
            String tokenDomainType = jwt.getClaimAsString("domain_type");
            
            if (primaryContextIdStr != null && domainType.equals(tokenDomainType)) {
                return UUID.fromString(primaryContextIdStr);
            }
        }
        throw new SecurityException("Unable to extract primary context ID for domain: " + domainType);
    }

    /**
     * Get the list of branch IDs the current user has access to
     * @deprecated Use getContextIds("healthcare") instead
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public List<UUID> getBranchIds() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            // Try new context-based claims first
            try {
                return getContextIds("healthcare");
            } catch (SecurityException e) {
                // Fall back to legacy branch claims
                List<String> branchIdStrings = jwt.getClaimAsStringList("branch_ids");
                if (branchIdStrings != null) {
                    return branchIdStrings.stream()
                            .map(UUID::fromString)
                            .toList();
                }
            }
        }
        throw new SecurityException("Unable to extract branch IDs from token");
    }

    /**
     * Get the primary branch ID for the current user
     * @deprecated Use getPrimaryContextId("healthcare") instead
     */
    @Deprecated
    public UUID getPrimaryBranchId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            // Try new context-based claims first
            try {
                return getPrimaryContextId("healthcare");
            } catch (SecurityException e) {
                // Fall back to legacy branch claims
                String primaryBranchIdStr = jwt.getClaimAsString("primary_branch_id");
                if (primaryBranchIdStr != null) {
                    return UUID.fromString(primaryBranchIdStr);
                }
            }
        }
        throw new SecurityException("Unable to extract primary branch ID from token");
    }

    /**
     * Check if the current user has access to a specific context in a domain
     * @param contextId The context ID to check access for
     * @param domainType The domain type (e.g., "healthcare", "ecommerce", "cab-booking")
     * @return true if user has access, false otherwise
     */
    public boolean hasAccessToContext(UUID contextId, String domainType) {
        if (contextId == null) {
            return false;
        }
        
        try {
            List<UUID> userContextIds = getContextIds(domainType);
            return userContextIds.contains(contextId);
        } catch (SecurityException e) {
            log.warn("Failed to check context access for domain {}: {}", domainType, e.getMessage());
            return false;
        }
    }

    /**
     * Check if the current user has access to a specific branch
     * @deprecated Use hasAccessToContext(branchId, "healthcare") instead
     */
    @Deprecated
    public boolean hasAccessToBranch(UUID branchId) {
        if (branchId == null) {
            return false;
        }
        
        try {
            List<UUID> userBranchIds = getBranchIds();
            return userBranchIds.contains(branchId);
        } catch (SecurityException e) {
            log.warn("Failed to check branch access: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if the current user is an admin
     */
    public boolean isAdmin() {
        try {
            List<String> roles = getCurrentUserRoles();
            return roles.contains("ADMIN");
        } catch (SecurityException e) {
            log.warn("Failed to check admin role: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate that the user has access to the specified branch
     * Throws SecurityException if access is denied
     */
    public void validateBranchAccess(UUID branchId) {
        if (!isAdmin() && !hasAccessToBranch(branchId)) {
            throw new SecurityException("Access denied to branch: " + branchId);
        }
    }
}