/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static com.google.common.collect.Sets.newHashSet;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.type.TypeReference;
import org.rf.ide.core.dryrun.JsonMessageMapper.JsonMessageMapperException;
import org.rf.ide.core.execution.agent.event.LibraryImportEvent;
import org.rf.ide.core.execution.agent.event.MessageEvent;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

/**
 * @author mmarzec
 */
public class RobotDryRunLibraryImportCollector {

    private static final String MESSAGE_KEY = "import_error";

    private static final TypeReference<Map<String, RobotDryRunLibraryError>> MESSAGE_TYPE = new TypeReference<Map<String, RobotDryRunLibraryError>>() {
    };

    private final List<RobotDryRunLibraryImport> importedLibraries = new LinkedList<>();

    private final Set<String> erroneousLibraryNames = new HashSet<>();

    private final Set<String> standardLibraryNames;

    public RobotDryRunLibraryImportCollector(final Set<String> standardLibraryNames) {
        this.standardLibraryNames = standardLibraryNames;
    }

    public void collectFromLibraryImportEvent(final LibraryImportEvent event) {
        if (event.getImporter().isPresent() && !erroneousLibraryNames.contains(event.getName())
                && !standardLibraryNames.contains(event.getName())) {
            final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createKnown(event.getName(),
                    event.getSource().orElse(null), event.getImporter().map(Sets::newHashSet).orElse(newHashSet()),
                    event.getArguments());
            importedLibraries.add(libImport);
        }
    }

    public void collectFromMessageEvent(final MessageEvent event) {
        try {
            JsonMessageMapper.readValue(event, MESSAGE_KEY, MESSAGE_TYPE).ifPresent(importError -> {
                final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createUnknown(importError.getName(),
                        formatAdditionalInfo(importError.getError()));
                importedLibraries.add(libImport);
                erroneousLibraryNames.add(importError.getName());
            });
        } catch (final IOException e) {
            throw new JsonMessageMapperException("Problem with mapping message for key '" + MESSAGE_KEY + "'", e);
        }
    }

    @VisibleForTesting
    static String formatAdditionalInfo(final String error) {
        return error.replaceAll("\\\\n", "\n").replaceAll("\\\\'", "'").replace(
                "<class 'robot.errors.DataError'>, DataError", "");
    }

    public List<RobotDryRunLibraryImport> getImportedLibraries() {
        return importedLibraries;
    }

}
