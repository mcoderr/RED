/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views;

import static com.google.common.collect.Iterables.find;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.DefinitionPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Predicate;

/**
 * @author mmarzec
 */
public class DocumentationView {

    public static final String ID = "org.robotframework.ide.DocumentationView";

    public static final String SHOW_DOC_EVENT_TOPIC = "DocumentationView/Show";

    public static final String REFRESH_DOC_EVENT_TOPIC = "DocumentationView/Refresh";

    private StyledText styledText;

    private DocLoadingJob docLoadingJob;

    private RobotFileInternalElement currentlyDisplayedElement;

    @PostConstruct
    public void postConstruct(final Composite parent, final IViewPart part) {

        parent.setLayout(new FillLayout());

        styledText = new StyledText(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        styledText.setMargins(5, 5, 5, 5);
        styledText.setBackground(ColorsManager.getColor(SWT.COLOR_INFO_BACKGROUND));
        styledText.setFont(JFaceResources.getDefaultFont());
        styledText.setEditable(false);

        docLoadingJob = new DocLoadingJob("Documentation Loading Job");

        createToolbarActions(part.getViewSite().getActionBars().getToolBarManager());
    }

    @Focus
    public void onFocus() {
        styledText.setFocus();
    }

    @Inject
    @Optional
    private void showEvent(@UIEventTopic(SHOW_DOC_EVENT_TOPIC) final RobotFileInternalElement element) {

        if (element == null) {
            resetCurrentlyDisplayedElement();
            styledText.setText("");
            return;
        }

        currentlyDisplayedElement = element;
        docLoadingJob.schedule();
    }

    @Inject
    @Optional
    private void refreshEvent(@UIEventTopic(REFRESH_DOC_EVENT_TOPIC) final RobotFileInternalElement element) {
        resetCurrentlyDisplayedElement();
        showEvent(element);
    }

    private void resetCurrentlyDisplayedElement() {
        currentlyDisplayedElement = null;
    }

    private void createToolbarActions(final IToolBarManager toolBarManager) {
        final ShowInSourceAction showInSourceAction = new ShowInSourceAction();
        showInSourceAction.setText("Show In Source");
        showInSourceAction.setImageDescriptor(RedImages.getGoToImage());
        toolBarManager.add(showInSourceAction);
        final ToggleWordWrapAction toggleWordWrapAction = new ToggleWordWrapAction();
        toggleWordWrapAction.setChecked(false);
        toggleWordWrapAction.setText("Word Wrap");
        toggleWordWrapAction.setImageDescriptor(RedImages.getWordwrapImage());
        toolBarManager.add(toggleWordWrapAction);
        final RefreshDocAction refreshDocAction = new RefreshDocAction();
        refreshDocAction.setText("Refresh");
        refreshDocAction.setImageDescriptor(RedImages.getRefreshImage());
        toolBarManager.add(refreshDocAction);
    }

    class DocLoadingJob extends Job {

        public DocLoadingJob(final String name) {
            super(name);
            setUser(true);
        }

        @Override
        protected IStatus run(final IProgressMonitor monitor) {

            if (currentlyDisplayedElement != null
                    && currentlyDisplayedElement.getLinkedElement() instanceof IDocumentationHolder) {

                final String documentationText = DocumentationServiceHandler
                        .toShowConsolidated((IDocumentationHolder) currentlyDisplayedElement.getLinkedElement());
                final String parentName = currentlyDisplayedElement.getParent().getName();
                final String fileName = currentlyDisplayedElement.getSuiteFile().getName();

                SwtThread.asyncExec(new DocTextSetter(documentationText, parentName, fileName));
            }

            return Status.OK_STATUS;
        }
    }

    class DocTextSetter implements Runnable {

        private String documentationText;

        private String documentationSettingParentName;

        private String fileName;

        public DocTextSetter(final String documentationText, final String documentationSettingParentName,
                final String fileName) {
            this.documentationText = documentationText;
            this.documentationSettingParentName = documentationSettingParentName;
            this.fileName = fileName;
        }

        @Override
        public void run() {
            styledText.setText("");

            styledText.append(documentationSettingParentName + "\n");
            styledText.append(fileName + "\n\n");
            styledText.setStyleRange(new StyleRange(0, documentationSettingParentName.length(), null, null, SWT.BOLD));
            styledText.setStyleRange(new StyleRange(documentationSettingParentName.length() + 1, fileName.length(),
                    null, null, SWT.ITALIC));

            styledText.append(documentationText);
        }

    }

    class ShowInSourceAction extends Action implements IWorkbenchAction {

        private static final String ID = "org.robotframework.action.documentationView.ShowInSourceAction";

        public ShowInSourceAction() {
            setId(ID);
        }

        @Override
        public void run() {
            if (currentlyDisplayedElement != null) {
                IEditorPart activeEditor = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow()
                        .getActivePage()
                        .getActiveEditor();
                if (activeEditor instanceof RobotFormEditor) {
                    RobotFormEditor editor = (RobotFormEditor) activeEditor;
                    final SuiteSourceEditor suiteEditor = editor.activateSourcePage();
                    final ISelectionProvider selectionProvider = suiteEditor.getSite().getSelectionProvider();

                    final DefinitionPosition position = currentlyDisplayedElement.getDefinitionPosition();
                    selectionProvider.setSelection(new TextSelection(position.getOffset(), position.getLength()));
                }
            }
        }

        @Override
        public void dispose() {
        }
    }

    class ToggleWordWrapAction extends Action implements IWorkbenchAction {

        private static final String ID = "org.robotframework.action.documentationView.ToggleWordWrapAction";

        public ToggleWordWrapAction() {
            setId(ID);
        }

        @Override
        public void run() {
            styledText.setWordWrap(!styledText.getWordWrap());
        }

        @Override
        public void dispose() {
        }
    }

    class RefreshDocAction extends Action implements IWorkbenchAction {

        private static final String ID = "org.robotframework.action.documentationView.RefreshDocAction";

        public RefreshDocAction() {
            setId(ID);
        }

        @Override
        public void run() {
            if (currentlyDisplayedElement != null) {
                final RobotSuiteFile suiteFile = currentlyDisplayedElement.getSuiteFile();
                final RobotElement parent = currentlyDisplayedElement.getParent();
                if (suiteFile != null && parent != null) {
                    ModelType modelType = ((AModelElement<?>) currentlyDisplayedElement.getLinkedElement())
                            .getModelType();
                    RobotFileInternalElement docElement = null;
                    if (modelType == ModelType.USER_KEYWORD_DOCUMENTATION) {
                        final RobotKeywordDefinition keywordDefinition = find(suiteFile.getUserDefinedKeywords(),
                                new Predicate<RobotKeywordDefinition>() {

                                    @Override
                                    public boolean apply(final RobotKeywordDefinition def) {
                                        return parent.getName().equals(def.getName());
                                    }
                                }, null);
                        if (keywordDefinition != null) {
                            docElement = keywordDefinition.getDocumentationSetting();
                        }
                    } else if (modelType == ModelType.TEST_CASE_DOCUMENTATION) {
                        final com.google.common.base.Optional<RobotCasesSection> casesSection = suiteFile
                                .findSection(RobotCasesSection.class);
                        if (casesSection.isPresent()) {
                            final RobotCase testCase = find(casesSection.get().getChildren(),
                                    new Predicate<RobotCase>() {

                                        @Override
                                        public boolean apply(final RobotCase robotCase) {
                                            return parent.getName().equals(robotCase.getName());
                                        }
                                    });
                            if (testCase != null) {
                                final List<RobotDefinitionSetting> documentationSetting = testCase
                                        .getDocumentationSetting();
                                if (!documentationSetting.isEmpty()) {
                                    docElement = documentationSetting.get(0);
                                }
                            }
                        }
                    }

                    if (docElement != null) {
                        resetCurrentlyDisplayedElement();
                        showEvent(docElement);
                    } else {
                        showEvent(null);
                    }
                }
            }
        }

        @Override
        public void dispose() {
        }
    }
}
