/*
 * Copyright 2015-2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.navigator.RobotValidationExcludedDecorator;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfigWriter;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.viewers.Selections;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Michal Anglart
 */
abstract class ChangeExclusionHandler {

    public void changeExclusion(final IEventBroker eventBroker, final IStructuredSelection selection) {
        final List<IResource> resourcesToChange = Selections.getAdaptableElements(selection, IResource.class);

        for (final IResource res : resourcesToChange) {
            removeMarkers(res);
        }

        final Map<RobotProject, Collection<IPath>> pathGroupedByProject = groupByProject(resourcesToChange).asMap();
        pathGroupedByProject.forEach((robotProject, paths) -> {
            changeExclusion(robotProject, paths);
            fireEvents(eventBroker, robotProject.getProject(), paths);
        });

        SwtThread.asyncExec(() -> {
            final IDecoratorManager manager = PlatformUI.getWorkbench().getDecoratorManager();
            manager.update(RobotValidationExcludedDecorator.ID);
        });

    }

    private Multimap<RobotProject, IPath> groupByProject(final List<IResource> resourcesToChange) {
        final Multimap<RobotProject, IPath> groupedPaths = LinkedListMultimap.create();
        for (final IResource resource : resourcesToChange) {
            final RobotProject robotProject = RedPlugin.getModelManager().createProject(resource.getProject());
            groupedPaths.put(robotProject, resource.getProjectRelativePath());
        }
        return groupedPaths;
    }

    private void changeExclusion(final RobotProject robotProject, final Collection<IPath> toChange) {
        RobotProjectConfig config = robotProject.getOpenedProjectConfig();

        final boolean inEditor = config != null;
        if (config == null) {
            config = new RedEclipseProjectConfigReader().readConfiguration(robotProject);
        }

        for (final IPath pathToChange : toChange) {
            changeExclusion(config, pathToChange);
        }

        if (!inEditor) {
            new RedEclipseProjectConfigWriter().writeConfiguration(config, robotProject);
        }
        try {
            robotProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (final CoreException e) {
            // nothing to do
        }
    }

    private void fireEvents(final IEventBroker eventBroker, final IProject project, final Collection<IPath> toChange) {
        final RedProjectConfigEventData<Collection<IPath>> eventData = new RedProjectConfigEventData<>(
                project.getFile(RobotProjectConfig.FILENAME), toChange);
        eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_VALIDATION_EXCLUSIONS_STRUCTURE_CHANGED, eventData);
    }

    private void removeMarkers(final IResource resource) {
        try {
            if (resource.exists()) {
                resource.deleteMarkers(null, true, IResource.DEPTH_INFINITE);
            }
        } catch (final CoreException e) {
            e.printStackTrace();
        }

    }

    protected abstract void changeExclusion(RobotProjectConfig config, IPath pathToChange);
}
