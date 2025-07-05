# Contributing to FF8 Magic Creator

Thank you for your interest in contributing to the FF8 Magic Creator project! This guide will help you understand our development standards and contribution process.

## 🎯 Core Principles

### 1. **Modern Java 21** 🚀
- Use Java 21 features whenever appropriate
- Embrace modern language constructs and patterns
- Leverage new APIs and performance improvements

### 2. **Lombok Integration** 🔧
- Use Lombok annotations to reduce boilerplate code
- Follow established Lombok patterns in the codebase
- Maintain readability while reducing verbosity

### 3. **Architectural Respect** 🏗️
- Follow hexagonal architecture principles
- Maintain clean separation of concerns
- Respect layer boundaries and dependencies

### 4. **Comprehensive Testing** 🧪
- Write tests for all new functionality
- Maintain high test coverage
- Use appropriate testing patterns and frameworks

### 5. **Thorough Documentation** 📚
- Document all public APIs and complex logic
- Include JavaDoc for public methods and classes
- Update relevant documentation files

## 🏗️ Architecture Guidelines

### Hexagonal Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                     Infrastructure Layer                    │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                Application Layer                    │   │
│  │  ┌─────────────────────────────────────────────┐   │   │
│  │  │              Domain Layer                   │   │   │
│  │  │         (Business Logic)                    │   │   │
│  │  └─────────────────────────────────────────────┘   │   │
│  │              (Use Cases)                            │   │
│  └─────────────────────────────────────────────────────┘   │
│                  (Adapters)                                │
└─────────────────────────────────────────────────────────────┘
```

#### **Domain Layer** (`com.ff8.domain`)
- **Pure business logic** - no external dependencies
- **Entities**: Core business objects (`MagicData`, `StatusEffectSet`)
- **Value Objects**: Immutable data containers
- **Domain Services**: Business rules and calculations
- **Events**: Domain events for loose coupling

#### **Application Layer** (`com.ff8.application`)
- **Use Cases**: Application-specific business rules
- **DTOs**: Data transfer objects for layer boundaries
- **Ports**: Interface definitions (primary/secondary)
- **Services**: Application services orchestrating use cases

#### **Infrastructure Layer** (`com.ff8.infrastructure`)
- **Primary Adapters**: UI controllers, REST endpoints
- **Secondary Adapters**: File I/O, database, external services
- **Configuration**: Dependency injection and setup

### Architecture Rules

1. **Dependency Direction**: Always point inward
   ```
   Infrastructure → Application → Domain
   ```

2. **No Skip Layers**: Don't bypass architectural boundaries
   ```java
   // ❌ Bad - Infrastructure directly accessing Domain
   @Controller
   public class MagicController {
       private MagicData magicData; // Direct domain access
   }
   
   // ✅ Good - Through Application layer
   @Controller
   public class MagicController {
       private MagicEditorUseCase magicEditorUseCase;
   }
   ```

3. **Interface Segregation**: Use focused interfaces
   ```java
   // ✅ Good - Specific interfaces
   public interface MagicRepository {
       Optional<MagicData> findByIndex(int index);
       void save(MagicData magic);
   }
   ```

### **README Updates**
- Update feature lists when adding new functionality
- Include usage examples for new features
- Document breaking changes in upgrade guides
- Keep architecture diagrams current

## 🔄 Development Workflow

### **Branch Strategy**
```bash
# Feature development
git checkout -b feature/magic-status-effects
git checkout -b feature/ui-improvements

# Bug fixes
git checkout -b bugfix/binary-parsing-issue
git checkout -b bugfix/validation-error

# Documentation
git checkout -b docs/contributing-guide
git checkout -b docs/architecture-update
```

### **Commit Messages**
```bash
# ✅ Good commit messages
feat: add support for custom status effect combinations
fix: resolve binary parsing issue with 48-bit status effects
docs: update architecture documentation with new patterns
test: add comprehensive tests for magic validation service
refactor: extract status effect parsing to separate service

# ❌ Bad commit messages
fix stuff
update code
changes
```

### **Pull Request Process**
1. **Create Feature Branch**: Based on `main`
2. **Implement Changes**: Following all guidelines
3. **Add Tests**: Comprehensive test coverage
4. **Update Documentation**: README, JavaDoc, etc.
5. **Self Review**: Check against this guide
6. **Create Pull Request**: With detailed description
7. **Address Feedback**: Respond to review comments
8. **Merge**: After approval and CI passing

## 🚀 Getting Started

### **Development Setup**
```bash
# Clone the repository
git clone https://github.com/your-username/FF8_magic_creator.git
cd FF8_magic_creator

# Build the project
./gradlew build

# Run tests
./gradlew test

# Run the application
./gradlew run
```

### **IDE Configuration**
- **IntelliJ IDEA**: Import as Gradle project
- **Eclipse**: Use Gradle plugin
- **VS Code**: Install Java Extension Pack

### **Lombok Setup**
- Install Lombok plugin for your IDE
- Enable annotation processing
- Add Lombok to build path (handled by Gradle)

## 🎯 Code Review Checklist

### **Architecture**
- [ ] Follows hexagonal architecture principles
- [ ] Maintains proper layer separation
- [ ] Uses appropriate design patterns
- [ ] Implements proper dependency injection

### **Java 21 Features**
- [ ] Uses modern Java constructs where appropriate
- [ ] Leverages pattern matching and switch expressions
- [ ] Implements records for data transfer
- [ ] Uses sealed types for controlled inheritance

### **Lombok Usage**
- [ ] Reduces boilerplate code appropriately
- [ ] Uses correct annotations for the context
- [ ] Maintains code readability
- [ ] Follows established patterns

### **Testing**
- [ ] Comprehensive test coverage
- [ ] Tests both happy path and edge cases
- [ ] Uses appropriate testing frameworks
- [ ] Includes integration tests where needed

### **Documentation**
- [ ] Complete JavaDoc for public APIs
- [ ] Updated README and related docs
- [ ] Clear and concise comments
- [ ] Architecture decisions documented

## 🤝 Community

- **Discussions**: Use GitHub Discussions for questions
- **Issues**: Report bugs and request features via GitHub Issues
- **Code Review**: Participate in pull request reviews
- **Documentation**: Help improve documentation

## 📞 Support

If you need help or have questions:
- Check existing [Issues](https://github.com/Djoe-Denne/FF8ResourceCreator/issues)
- Review [Documentation](./README.md)

---

**Happy coding! 🎮✨** 