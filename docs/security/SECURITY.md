# Security Documentation

## Overview

This document describes the security measures implemented in the Price Rules AI Drools Service, including protection against known vulnerabilities and adherence to OWASP security best practices.

## CVE-2024-38821 Mitigation

### Vulnerability Description

CVE-2024-38821 is a static resource bypass vulnerability that could allow unauthorized access to static resources in Spring WebFlux applications. While this application uses Spring Web MVC (not WebFlux), we have implemented comprehensive security measures to prevent any form of static resource bypass.

### Mitigation Implemented

1. **Explicit Denial of Static Resources**
   - All common static resource paths (`/static/**`, `/resources/**`, `/public/**`, `/webjars/**`) are explicitly denied
   - Direct access to files with common extensions (`.js`, `.css`, `.html`, `.json`) is blocked
   - This prevents any potential bypass attempts

2. **Strict HTTP Firewall**
   - Implemented `StrictHttpFirewall` to block:
     - URL-encoded characters that could be used for path traversal
     - Backslashes and null characters
     - Double URL encoding attempts
     - Semicolons in URLs
   - Only allows specific HTTP methods (GET, POST, PUT, DELETE, OPTIONS, HEAD)

3. **Path Traversal Protection**
   - Blocks patterns like `/../`, `/./`, `//` in URLs
   - Prevents directory traversal attacks
   - Validates all incoming paths before processing

## Security Headers

The following security headers are implemented to protect against common web vulnerabilities:

### Content Security Policy (CSP)
```
default-src 'self';
script-src 'self' 'unsafe-inline' 'unsafe-eval';
style-src 'self' 'unsafe-inline';
img-src 'self' data: https:;
font-src 'self' data:;
connect-src 'self';
frame-ancestors 'none';
base-uri 'self';
form-action 'self';
```

### Additional Headers
- **X-Frame-Options**: DENY - Prevents clickjacking attacks
- **X-Content-Type-Options**: nosniff - Prevents MIME type sniffing
- **X-XSS-Protection**: 1; mode=block - Enables browser XSS protection
- **Strict-Transport-Security**: max-age=31536000; includeSubDomains; preload - Forces HTTPS
- **Referrer-Policy**: strict-origin-when-cross-origin - Controls referrer information
- **Permissions-Policy**: Disables unnecessary browser features

## Authentication & Authorization

### JWT-Based Authentication
- Stateless authentication using JWT tokens
- BCrypt password encoding with strength 12
- Token expiration and refresh mechanisms
- Secure token storage recommendations

### Role-Based Access Control (RBAC)
- Method-level security with `@PreAuthorize`, `@Secured`, and `@RolesAllowed`
- Role hierarchy: ADMIN > USER
- Protected actuator endpoints require ADMIN role

## API Security

### Public Endpoints
Only the following endpoints are accessible without authentication:
- `/api/auth/login` - User login
- `/api/auth/signup` - User registration
- `/actuator/health` - Health check
- `/actuator/info` - Application info
- `/actuator/metrics` - Basic metrics
- `/actuator/prometheus` - Prometheus metrics

### Protected Endpoints
All other endpoints require:
- Valid JWT token in Authorization header
- Appropriate role for the requested resource
- Proper CORS origin (for browser requests)

## CORS Configuration

Cross-Origin Resource Sharing (CORS) is configured to:
- Allow only specific origins (configure in production)
- Support credentials
- Limit allowed methods and headers
- Set appropriate max age for preflight caching

## Input Validation

### Request Validation
- All input is validated using Spring Validation
- Custom validators for business logic
- Sanitization of user inputs
- Protection against SQL injection through parameterized queries

### File Upload Security
- File type validation
- Size limitations
- Virus scanning (when implemented)
- Secure file storage outside web root

## Dependency Security

### Regular Updates
- Spring Boot 3.4.5 with latest security patches
- Regular dependency updates via Dependabot
- Security vulnerability scanning in CI/CD pipeline

### Known Issues
- Monitor Spring Security advisories
- Track OWASP Top 10 compliance
- Regular security audits

## Security Testing

### Test Coverage
- Unit tests for security configurations
- Integration tests for authentication flows
- Penetration testing for common vulnerabilities
- OWASP ZAP scanning in CI/CD

### Test Cases
1. Static resource bypass prevention (CVE-2024-38821)
2. Path traversal protection
3. Authentication and authorization
4. Security header validation
5. CORS configuration
6. Input validation
7. Rate limiting (when implemented)

## Incident Response

### Security Incident Procedure
1. **Detection**: Monitor logs for suspicious activity
2. **Containment**: Isolate affected systems
3. **Investigation**: Analyze logs and identify root cause
4. **Remediation**: Apply patches and fixes
5. **Recovery**: Restore normal operations
6. **Lessons Learned**: Document and improve procedures

### Contact Information
- Security Team: security@yourdomain.com
- Emergency Contact: [Emergency Phone Number]

## Compliance

### Standards
- OWASP Top 10 2021 compliance
- GDPR data protection (where applicable)
- PCI DSS (if handling payment data)
- SOC 2 Type II (if applicable)

### Audit Trail
- All security-sensitive operations are logged
- Immutable audit logs
- Regular log reviews
- Retention policy: 90 days minimum

## Security Checklist

### Deployment Checklist
- [ ] Update all dependencies to latest secure versions
- [ ] Configure HTTPS with valid certificates
- [ ] Set secure environment variables
- [ ] Enable security headers
- [ ] Configure firewall rules
- [ ] Set up monitoring and alerting
- [ ] Review and update CORS settings
- [ ] Disable debug mode
- [ ] Configure rate limiting
- [ ] Set up backup and recovery procedures

### Development Security Practices
- [ ] Code reviews for all changes
- [ ] Security testing in CI/CD pipeline
- [ ] Dependency vulnerability scanning
- [ ] Static code analysis (SAST)
- [ ] Dynamic application security testing (DAST)
- [ ] Regular penetration testing
- [ ] Security training for developers

## References

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [CVE-2024-38821 Details](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2024-38821)
- [NIST Cybersecurity Framework](https://www.nist.gov/cyberframework)
- [CIS Controls](https://www.cisecurity.org/controls)

## Version History

| Date | Version | Changes | Author |
|------|---------|---------|--------|
| 2024-10-20 | 1.0 | Initial security documentation and CVE-2024-38821 mitigation | Security Team |