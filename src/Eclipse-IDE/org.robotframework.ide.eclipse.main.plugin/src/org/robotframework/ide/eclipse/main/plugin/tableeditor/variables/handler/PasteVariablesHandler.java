/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import java.util.Optional;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.InsertVariablesCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.PasteVariablesHandler.E4PasteVariablesHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class PasteVariablesHandler extends DIParameterizedHandler<E4PasteVariablesHandler> {

    public PasteVariablesHandler() {
        super(E4PasteVariablesHandler.class);
    }

    public static class E4PasteVariablesHandler {

        @Execute
        public void pasteVariables(final RobotEditorCommandsStack commandsStack,
                @Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile fileModel,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard) {

            final RobotVariable[] variables = clipboard.getVariables();

            if (variables != null) {
                final Optional<? extends EditorCommand> command = getInsertingCommand(fileModel, selection, variables);
                if (command.isPresent()) {
                    commandsStack.execute(command.get());
                }
            }
        }

        private Optional<? extends EditorCommand> getInsertingCommand(final RobotSuiteFile fileModel,
                final IStructuredSelection selection, final RobotVariable[] variables) {
            final Optional<RobotVariable> firstSelected = Selections.getOptionalFirstElement(selection,
                    RobotVariable.class);

            if (firstSelected.isPresent()) {
                final RobotVariable selectedVar = firstSelected.get();
                final int index = selectedVar.getParent().getChildren().indexOf(selectedVar);
                return Optional.of(new InsertVariablesCommand(selectedVar.getParent(), index, variables));
            } else {
                final RobotVariablesSection section = fileModel.findSection(RobotVariablesSection.class).orElse(null);
                if (section != null) {
                    return Optional.of(new InsertVariablesCommand(section, variables));
                }
            }
            return Optional.empty();
        }
    }
}
