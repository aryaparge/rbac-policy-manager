# RBAC Policy Manager

A Spring Boot application for role-based access control (RBAC) policy management, including subject, role, group, permission, and hierarchy services.

## Overview

This project is a work in progress.

Current capabilities:

- REST API for subjects, roles, groups, actions, resources, and permissions
- Entity lifecycle management with `ACTIVE`, `DISABLED`, and `DELETED` states
- Hierarchy services for role/group graphs and validation
- JWT-based authentication
- Spring Data JPA persistence

## Intended Future Work

- Minimal frontend for policy management and visualization
- Improved API response DTOs and client-friendly payloads
- Secure external configuration for JWT secrets and environment-specific settings
- Expanded test coverage and documentation

## Getting Started

### Build

```bash
./mvnw clean package
```

### Run

```bash
./mvnw spring-boot:run
```

### Test

```bash
./mvnw test
```

## Notes

- This project is not yet complete.
- Frontend work is planned but not implemented.
- Use this repository as a foundation for RBAC policy management features.
