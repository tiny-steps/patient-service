package com.tinysteps.patientservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 🛡️ Enhanced Security Service for Patient Service
 * 
 * Handles security context and branch access validation with defensive programming.
 * Extracts user information and branch context from JWT tokens with comprehensive
 * error handling and input validation.
 * 
 * Key Features:
 * - Robust JWT token validation
 * - Comprehensive input validation
 * - Detailed security logging
 * - Fail-safe error handling
 */
@Service
@Slf4j
public class SecurityService {
    
    private static final Set<String> VALID_DOMAIN_TYPES = Set.of(
        "healthcare", "ecommerce", "cab-booking", "payment", "financial"
    );

    /**
     * Get the current user ID from the JWT token with enhanced validation.
     * @return User UUID from JWT token
     * @throws SecurityException if authentication context is invalid or user ID cannot be extracted
     */
    public UUID getCurrentUserId() {
        log.debug("Retrieving current user ID from JWT token");
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                log.warn("No authentication context found");
                throw new SecurityException("No authentication context found");
            }
            
            if (!auth.isAuthenticated()) {
                log.warn("User is not authenticated");
                throw new SecurityException("User is not authenticated");
            }
            
            if (!(auth.getPrincipal() instanceof Jwt jwt)) {
                log.warn("Authentication principal is not a JWT token");
                throw new SecurityException("No valid JWT authentication found");
            }
            
            String userIdStr = jwt.getClaimAsString("sub");
            if (!StringUtils.hasText(userIdStr)) {
                log.warn("User ID claim 'sub' is missing or empty in JWT token");
                throw new SecurityException("User ID not found in JWT token");
            }
            
            UUID userId = UUID.fromString(userIdStr);
            log.debug("Successfully retrieved user ID: {}", userId);
            return userId;
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for user ID: {}", e.getMessage());
            throw new SecurityException("Invalid user ID format in JWT token", e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving user ID: {}", e.getMessage());
            throw new SecurityException("Failed to retrieve user ID from JWT token", e);
        }
    }

    /**
     * Get the current user's roles from the JWT token with enhanced validation.
     * @return List of user roles (never null, empty list if no roles found)
     * @throws SecurityException if authentication context is invalid
     */
    public List<String> getCurrentUserRoles() {
        log.debug("Retrieving current user roles from JWT token");
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                log.warn("No authentication context found");
                throw new SecurityException("No authentication context found");
            }
            
            if (!auth.isAuthenticated()) {
                log.warn("User is not authenticated");
                throw new SecurityException("User is not authenticated");
            }
            
            if (!(auth.getPrincipal() instanceof Jwt jwt)) {
                log.warn("Authentication principal is not a JWT token");
                throw new SecurityException("No valid JWT authentication found");
            }
            
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null || roles.isEmpty()) {
                log.debug("No roles found in JWT token, returning empty list");
                return List.of();
            }
            
            // Filter out null or empty roles
            List<String> validRoles = roles.stream()
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            
            log.debug("Successfully retrieved {} valid roles", validRoles.size());
            return validRoles;
            
        } catch (Exception e) {
            log.error("Unexpected error retrieving user roles: {}", e.getMessage());
            throw new SecurityException("Failed to retrieve user roles from JWT token", e);
        }
    }

    /**
     * Get the list of context IDs for a specific domain type with enhanced validation.
     * @param domainType The domain type (e.g., "healthcare", "ecommerce", "cab-booking")
     * @return List of context IDs for the specified domain (never null, empty list if no contexts found)
     * @throws IllegalArgumentException if domainType is invalid
     * @throws SecurityException if authentication context is invalid
     */
    public List<UUID> getContextIds(String domainType) {
        log.debug("Retrieving context IDs for domain type: {}", domainType);
        
        // Input validation
        if (!StringUtils.hasText(domainType)) {
            log.warn("Domain type is null or empty");
            throw new IllegalArgumentException("Domain type cannot be null or empty");
        }
        
        if (!VALID_DOMAIN_TYPES.contains(domainType.toLowerCase())) {
            log.warn("Invalid domain type provided: {}", domainType);
            throw new IllegalArgumentException("Invalid domain type: " + domainType);
        }
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                log.debug("No authentication context found, returning empty context IDs list");
                return List.of();
            }
            
            if (!(auth.getPrincipal() instanceof Jwt jwt)) {
                log.debug("Authentication principal is not a JWT token, returning empty context IDs list");
                return List.of();
            }
            
            List<String> contextIdStrings = jwt.getClaimAsStringList("context_ids");
            String tokenDomainType = jwt.getClaimAsString("domain_type");
            
            if (contextIdStrings == null || contextIdStrings.isEmpty()) {
                log.debug("No context IDs found in JWT token for domain: {}", domainType);
                return List.of();
            }
            
            if (!domainType.equals(tokenDomainType)) {
                log.debug("Domain type mismatch. Requested: {}, Token: {}", domainType, tokenDomainType);
                return List.of();
            }
            
            List<UUID> contextIds = contextIdStrings.stream()
                    .filter(StringUtils::hasText)
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
            
            log.debug("Successfully retrieved {} context IDs for domain: {}", contextIds.size(), domainType);
            return contextIds;
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in context IDs for domain {}: {}", domainType, e.getMessage());
            throw new SecurityException("Invalid context ID format for domain: " + domainType, e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving context IDs for domain {}: {}", domainType, e.getMessage());
            throw new SecurityException("Failed to retrieve context IDs for domain: " + domainType, e);
        }
    }

    /**
     * Get the primary context ID for a specific domain type with enhanced validation.
     * @param domainType The domain type (e.g., "healthcare", "ecommerce", "cab-booking")
     * @return Primary context ID for the specified domain
     * @throws IllegalArgumentException if domainType is invalid
     * @throws SecurityException if authentication context is invalid or primary context ID not found
     */
    public UUID getPrimaryContextId(String domainType) {
        log.debug("Retrieving primary context ID for domain type: {}", domainType);
        
        // Input validation
        if (!StringUtils.hasText(domainType)) {
            log.warn("Domain type is null or empty");
            throw new IllegalArgumentException("Domain type cannot be null or empty");
        }
        
        if (!VALID_DOMAIN_TYPES.contains(domainType.toLowerCase())) {
            log.warn("Invalid domain type provided: {}", domainType);
            throw new IllegalArgumentException("Invalid domain type: " + domainType);
        }
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                log.warn("No authentication context found for primary context ID retrieval");
                throw new SecurityException("Authentication context not found");
            }
            
            if (!(auth.getPrincipal() instanceof Jwt jwt)) {
                log.warn("Authentication principal is not a JWT token for primary context ID retrieval");
                throw new SecurityException("Invalid authentication token type");
            }
            
            String primaryContextIdStr = jwt.getClaimAsString("primary_context_id");
            String tokenDomainType = jwt.getClaimAsString("domain_type");
            
            // Fallback to legacy claim name for backward compatibility
            if (!StringUtils.hasText(primaryContextIdStr)) {
                primaryContextIdStr = jwt.getClaimAsString("primary_branch_id");
                log.debug("Using fallback primary_branch_id claim for domain: {}", domainType);
            }
            
            if (!StringUtils.hasText(primaryContextIdStr)) {
                log.warn("No primary context ID found in JWT token for domain: {}", domainType);
                throw new SecurityException("Primary context ID not found for domain: " + domainType);
            }
            
            if (!domainType.equals(tokenDomainType)) {
                log.warn("Domain type mismatch. Requested: {}, Token: {}", domainType, tokenDomainType);
                throw new SecurityException("Domain type mismatch for primary context ID");
            }
            
            UUID contextId = UUID.fromString(primaryContextIdStr);
            log.debug("Successfully retrieved primary context ID {} for domain: {}", contextId, domainType);
            return contextId;
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for primary context ID in domain {}: {}", domainType, e.getMessage());
            throw new SecurityException("Invalid primary context ID format for domain: " + domainType, e);
        } catch (SecurityException e) {
            // Re-throw security exceptions as-is
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error retrieving primary context ID for domain {}: {}", domainType, e.getMessage());
            throw new SecurityException("Failed to retrieve primary context ID for domain: " + domainType, e);
        }
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
    /**
     * Check if the current user has access to a specific context in a domain with enhanced validation.
     * @param contextId The context ID to check (must not be null)
     * @param domainType The domain type
     * @return true if the user has access, false otherwise (fail-safe)
     */
    public boolean hasAccessToContext(UUID contextId, String domainType) {
        log.debug("Checking access to context ID: {} for domain: {}", contextId, domainType);
        
        // Input validation
        if (contextId == null) {
            log.warn("Context ID is null");
            return false;
        }
        
        if (!StringUtils.hasText(domainType)) {
            log.warn("Domain type is null or empty");
            return false;
        }
        
        if (!VALID_DOMAIN_TYPES.contains(domainType.toLowerCase())) {
            log.warn("Invalid domain type provided: {}", domainType);
            return false;
        }
        
        try {
            List<UUID> userContextIds = getContextIds(domainType);
            boolean hasAccess = userContextIds.contains(contextId);
            log.debug("Access check result for context {} in domain {}: {}", contextId, domainType, hasAccess);
            return hasAccess;
        } catch (SecurityException e) {
            log.warn("Failed to check context access for domain {}: {}", domainType, e.getMessage());
            return false; // Fail-safe: deny access on security errors
        } catch (Exception e) {
            log.error("Unexpected error checking access to context {} in domain {}: {}", contextId, domainType, e.getMessage());
            return false; // Fail-safe: deny access on unexpected errors
        }
    }

    /**
     * Check if the current user has access to a specific context in a domain with enhanced validation.
     * @param contextId The context ID to check (must be a valid UUID string)
     * @param domainType The domain type
     * @return true if the user has access, false otherwise (fail-safe)
     */
    public boolean hasAccessToContext(String contextId, String domainType) {
        log.debug("Checking access to context ID: {} for domain: {}", contextId, domainType);
        
        // Input validation
        if (!StringUtils.hasText(contextId)) {
            log.warn("Context ID is null or empty");
            return false;
        }
        
        if (!StringUtils.hasText(domainType)) {
            log.warn("Domain type is null or empty");
            return false;
        }
        
        if (!VALID_DOMAIN_TYPES.contains(domainType.toLowerCase())) {
            log.warn("Invalid domain type provided: {}", domainType);
            return false;
        }
        
        try {
            UUID contextUuid = UUID.fromString(contextId);
            boolean hasAccess = hasAccessToContext(contextUuid, domainType);
            log.debug("Access check result for context {} in domain {}: {}", contextId, domainType, hasAccess);
            return hasAccess;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format for context ID {}: {}", contextId, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error checking access to context {} in domain {}: {}", contextId, domainType, e.getMessage());
            return false; // Fail-safe: deny access on unexpected errors
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