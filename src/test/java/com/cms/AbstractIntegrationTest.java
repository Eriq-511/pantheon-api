package com.cms;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for all service-layer integration tests.
 *
 * <p>Tests run against the {@code cms-test-postgres} Docker container
 * (postgres:16-alpine exposed on localhost:5433).  Start the container once
 * before running the test suite – {@code scripts/run-tests.ps1} (or
 * {@code run-tests.bat} on Windows) handles this automatically. The datasource
 * URL is configured in
 * {@code src/test/resources/application.properties}.
 *
 * <p>Each test method is wrapped in a transaction that rolls back
 * automatically, so every test starts with a clean database state.
 */
@SpringBootTest
@Transactional
public abstract class AbstractIntegrationTest {
}
