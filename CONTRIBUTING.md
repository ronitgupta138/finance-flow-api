# Contributing to FinFlow Core

Thank you for your interest in contributing to **FinFlow Core**! This document provides guidelines for opening issues, submitting pull requests, and contributing to this repository.

---

## 🛠️ Development Workflow

1. **Fork or Clone the Repository**:
   ```bash
   git clone https://github.com/ronitgupta138/finflow-core.git
   cd finflow-core
   ```

2. **Branch Naming Conventions**:
   - `feat/<feature-name>`: New feature implementations
   - `fix/<bug-name>`: Bug fixes and issue patches
   - `docs/<doc-topic>`: Documentation updates and guides
   - `test/<test-suite>`: Unit, integration, or concurrency test additions

3. **Running the Test Suite**:
   Before submitting changes, ensure all unit, integration, and concurrency tests pass locally:
   ```bash
   mvn clean test
   ```

---

## 📝 Pull Request Guidelines

- **Commit Message Format**: Follow standard conventional commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`).
- **Pair Programming & Co-authorship**: For collaborative contributions, include the Git trailer in your commit message:
  ```text
  Co-authored-by: Name <email@example.com>
  ```
- **Review & Verification**: Ensure automated GitHub Actions CI builds pass before requesting merges.

---

## ⚖️ Code of Conduct
Please maintain a respectful, constructive, and collaborative environment across all issues and pull requests.
