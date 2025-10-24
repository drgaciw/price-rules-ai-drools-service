# Spring AI M6 Migration Quick Reference

## Quick Fix Checklist

### 1. Import Changes (All Files)

#### EmbeddingClient → EmbeddingModel
```java
// BEFORE
import org.springframework.ai.embedding.EmbeddingClient;

// AFTER
import org.springframework.ai.embedding.EmbeddingModel;
```

#### OpenAiEmbeddingClient → OpenAiEmbeddingModel
```java
// BEFORE
import org.springframework.ai.openai.OpenAiEmbeddingClient;

// AFTER
import org.springframework.ai.openai.OpenAiEmbeddingModel;
```

### 2. Class/Interface Renames

| Old Name | New Name | Package |
|----------|----------|---------|
| `EmbeddingClient` | `EmbeddingModel` | `org.springframework.ai.embedding` |
| `OpenAiEmbeddingClient` | `OpenAiEmbeddingModel` | `org.springframework.ai.openai` |
| `ChatClient` | `ChatModel` | `org.springframework.ai.chat` |
| `OpenAiChatClient` | `OpenAiChatModel` | `org.springframework.ai.openai` |

### 3. Files Requiring Updates

#### Priority 1: Core Embedding Services
- [ ] `src/main/java/com/example/pricerulesaidrools/ai/cache/EmbeddingService.java`
  - Replace `EmbeddingClient` → `EmbeddingModel` (3 occurrences)

- [ ] `src/main/java/com/example/pricerulesaidrools/ai/cache/VectorStoreConfiguration.java`
  - Replace `EmbeddingClient` → `EmbeddingModel` (4 occurrences)
  - Replace `OpenAiEmbeddingClient` → `OpenAiEmbeddingModel` (1 occurrence)
  - Investigate `RedisVectorStore` API changes

#### Priority 2: Parser Implementation
- [ ] `src/main/java/com/example/pricerulesaidrools/ai/parser/StructuredOutputParserImpl.java`
  - Find new parser package location
  - Update import: `org.springframework.ai.parser.*` → `org.springframework.ai.converter.*` (likely)

### 4. Common Code Patterns

#### Bean Configuration
```java
// BEFORE
@Bean
public EmbeddingClient embeddingClient() {
    return new OpenAiEmbeddingClient(apiKey);
}

// AFTER
@Bean
public EmbeddingModel embeddingModel() {
    return new OpenAiEmbeddingModel(apiKey);
}
```

#### Constructor Injection
```java
// BEFORE
@RequiredArgsConstructor
public class EmbeddingService {
    private final EmbeddingClient embeddingClient;
}

// AFTER
@RequiredArgsConstructor
public class EmbeddingService {
    private final EmbeddingModel embeddingModel;
}
```

### 5. Search and Replace Commands

#### Unix/Linux (sed)
```bash
# Backup files first
cp src/main/java/com/example/pricerulesaidrools/ai/cache/EmbeddingService.java src/main/java/com/example/pricerulesaidrools/ai/cache/EmbeddingService.java.backup

# Replace in all Java files
find src -name "*.java" -type f -exec sed -i 's/EmbeddingClient/EmbeddingModel/g' {} \;
find src -name "*.java" -type f -exec sed -i 's/OpenAiEmbeddingClient/OpenAiEmbeddingModel/g' {} \;
find src -name "*.java" -type f -exec sed -i 's/ChatClient/ChatModel/g' {} \;
find src -name "*.java" -type f -exec sed -i 's/OpenAiChatClient/OpenAiChatModel/g' {} \;
```

#### IDE Search/Replace (IntelliJ/VSCode)
- Open "Replace in Path" (Ctrl+Shift+R / Cmd+Shift+R)
- Match case: ON
- File mask: `*.java`

Replacements:
1. `EmbeddingClient` → `EmbeddingModel`
2. `OpenAiEmbeddingClient` → `OpenAiEmbeddingModel`
3. `ChatClient` → `ChatModel`
4. `OpenAiChatClient` → `OpenAiChatModel`

### 6. Validation Steps

After making changes:

```bash
# 1. Compile
mvn clean compile

# 2. If successful, run tests
mvn test

# 3. Full build
mvn clean verify

# 4. Check for warnings
mvn clean compile 2>&1 | grep -i warning
```

### 7. Common Issues and Solutions

#### Issue: "Cannot find symbol: class RedisVectorStore"

**Solution:** Check if the class still exists in `spring-ai-redis-store`

```bash
# Extract and search the JAR
cd /home/username01/.m2/repository/org/springframework/ai/spring-ai-redis-store/1.0.0-M6/
jar -tf spring-ai-redis-store-1.0.0-M6.jar | grep -i redis
```

**Alternative:** Use Spring AI documentation to find the new vector store API

#### Issue: "Package org.springframework.ai.parser does not exist"

**Solution:** Check for new converter/parser locations

```bash
# Search for parser classes in spring-ai-core
cd /home/username01/.m2/repository/org/springframework/ai/spring-ai-core/1.0.0-M6/
jar -tf spring-ai-core-1.0.0-M6.jar | grep -i parser
jar -tf spring-ai-core-1.0.0-M6.jar | grep -i converter
```

#### Issue: Method signatures don't match

**Solution:** Check Spring AI M6 JavaDoc or source code

```bash
# View JavaDoc online
# https://docs.spring.io/spring-ai/docs/1.0.0-M6/api/
```

### 8. Testing Checklist

- [ ] EmbeddingService creates embeddings successfully
- [ ] VectorStore stores and retrieves embeddings
- [ ] StructuredOutputParser parses model outputs
- [ ] No deprecation warnings in logs
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Manual testing of AI features

### 9. Performance Verification

Compare before/after metrics:
- Embedding generation time
- Vector store query latency
- Memory usage during AI operations
- Thread pool utilization

### 10. Additional Resources

- **Spring AI M6 Docs:** https://docs.spring.io/spring-ai/reference/1.0.0-M6/
- **API JavaDoc:** https://docs.spring.io/spring-ai/docs/1.0.0-M6/api/
- **GitHub Issues:** https://github.com/spring-projects/spring-ai/issues
- **Migration Examples:** https://github.com/spring-projects/spring-ai/tree/main/spring-ai-spring-boot-autoconfigure

## Estimated Time

- **Automated replacements:** 5-10 minutes
- **Manual API fixes:** 1-2 hours
- **Testing and validation:** 1-2 hours
- **Total:** 2-4 hours

## Rollback

If issues arise:
```bash
git checkout .
git clean -fd
mvn clean compile -DskipTests
```

This reverts all changes and returns to the pre-upgrade state.
