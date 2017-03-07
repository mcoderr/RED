/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.execution.LogLevel;
import org.rf.ide.core.execution.Status;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;

public class ExecutionMessagesTrackerTest {

    @Test
    public void messageStoreIsAdded_whenAgentIsInitializedAndStoreDoesNotExist() {
        final RobotTestsLaunch launchContext = new RobotTestsLaunch();
        final ExecutionMessagesTracker tracker = new ExecutionMessagesTracker(launchContext);

        assertThat(launchContext.getExecutionData(ExecutionMessagesStore.class).isPresent()).isFalse();
        tracker.handleAgentInitializing();

        assertThat(launchContext.getExecutionData(ExecutionMessagesStore.class).isPresent()).isTrue();
    }

    @Test
    public void messageStoreIsNotAdded_whenAgentIsInitializedButStoreAlreadyExist() {
        final RobotTestsLaunch launchContext = new RobotTestsLaunch();
        final ExecutionMessagesStore store = new ExecutionMessagesStore();
        launchContext.getExecutionData(ExecutionMessagesStore.class, () -> store);

        final ExecutionMessagesTracker tracker = new ExecutionMessagesTracker(launchContext);

        tracker.handleAgentInitializing();

        assertThat(launchContext.getExecutionData(ExecutionMessagesStore.class).isPresent()).isTrue();
        assertThat(launchContext.getExecutionData(ExecutionMessagesStore.class).get()).isSameAs(store);
    }

    @Test
    public void testStartMessageIsStored_whenThereIsAStoreDefined() {
        final RobotTestsLaunch launchContext = new RobotTestsLaunch();
        final ExecutionMessagesStore store = launchContext.getExecutionData(ExecutionMessagesStore.class,
                () -> new ExecutionMessagesStore());

        final ExecutionMessagesTracker tracker = new ExecutionMessagesTracker(launchContext);
        tracker.handleTestStarted("tc", "test_case");

        assertThat(store.getMessage()).isEqualTo("Starting test: test_case\n");
    }

    @Test
    public void testStartMessageIsNotStored_whenThereIsNoStoreDefined() {
        final RobotTestsLaunch launchContext = new RobotTestsLaunch();

        final ExecutionMessagesTracker tracker = new ExecutionMessagesTracker(launchContext);
        tracker.handleTestStarted("tc", "test_case");

        final ExecutionMessagesStore store = launchContext.getExecutionData(ExecutionMessagesStore.class,
                () -> new ExecutionMessagesStore());
        assertThat(store.getMessage()).isEmpty();
    }

    @Test
    public void testEndMessageIsStored_whenThereIsAStoreDefined() {
        final RobotTestsLaunch launchContext = new RobotTestsLaunch();
        final ExecutionMessagesStore store = launchContext.getExecutionData(ExecutionMessagesStore.class,
                () -> new ExecutionMessagesStore());

        final ExecutionMessagesTracker tracker = new ExecutionMessagesTracker(launchContext);
        tracker.handleTestEnded("tc", "test_case", 100, Status.PASS, "");

        assertThat(store.getMessage()).isEqualTo("Ending test: test_case\n\n");
    }

    @Test
    public void testEndMessageIsNotStored_whenThereIsNoStoreDefined() {
        final RobotTestsLaunch launchContext = new RobotTestsLaunch();

        final ExecutionMessagesTracker tracker = new ExecutionMessagesTracker(launchContext);
        tracker.handleTestEnded("tc", "test_case", 100, Status.PASS, "");

        final ExecutionMessagesStore store = launchContext.getExecutionData(ExecutionMessagesStore.class,
                () -> new ExecutionMessagesStore());
        assertThat(store.getMessage()).isEmpty();
    }

    @Test
    public void logMessageIsStored_whenThereIsAStoreDefined() {
        final RobotTestsLaunch launchContext = new RobotTestsLaunch();
        final ExecutionMessagesStore store = launchContext.getExecutionData(ExecutionMessagesStore.class,
                () -> new ExecutionMessagesStore());

        final ExecutionMessagesTracker tracker = new ExecutionMessagesTracker(launchContext);
        tracker.handleLogMessage("msg", LogLevel.INFO, "stamp");

        assertThat(store.getMessage()).isEqualTo("stamp : INFO : msg\n");
    }

    @Test
    public void logMessageIsNotStored_whenThereIsNoStoreDefined() {
        final RobotTestsLaunch launchContext = new RobotTestsLaunch();

        final ExecutionMessagesTracker tracker = new ExecutionMessagesTracker(launchContext);
        tracker.handleLogMessage("msg", LogLevel.INFO, "stamp");

        final ExecutionMessagesStore store = launchContext.getExecutionData(ExecutionMessagesStore.class,
                () -> new ExecutionMessagesStore());
        assertThat(store.getMessage()).isEmpty();
    }

}
