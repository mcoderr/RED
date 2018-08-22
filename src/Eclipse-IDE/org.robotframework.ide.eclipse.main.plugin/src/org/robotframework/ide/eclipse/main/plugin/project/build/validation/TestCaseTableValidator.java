/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.ERowType;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopContinueRowDescriptor;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseSetup;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTags;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTeardown;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTimeout;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.AttributesAugmentingReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.ValidationKeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.testcases.DocumentationTestCaseDeclarationSettingValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.testcases.PostconditionDeclarationExistenceValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.testcases.PreconditionDeclarationExistenceValidator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Range;

class TestCaseTableValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;

    private final Optional<RobotCasesSection> testCaseSection;

    private final ValidationReportingStrategy reporter;

    TestCaseTableValidator(final FileValidationContext validationContext, final Optional<RobotCasesSection> section,
            final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.testCaseSection = section;
        this.reporter = reporter;
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!testCaseSection.isPresent()) {
            return;
        }
        final RobotCasesSection robotCasesSection = testCaseSection.get();
        final TestCaseTable casesTable = robotCasesSection.getLinkedElement();
        final List<TestCase> cases = casesTable.getTestCases();

        validateByExternal(robotCasesSection, monitor);

        reportEmptyNamesOfCases(cases);
        reportEmptyCases(cases);
        reportDuplicatedCases(cases);
        reportSettingsProblems(cases);
        reportKeywordUsageProblems(robotCasesSection.getChildren());
        reportUnknownVariables(cases);
    }

    private void validateByExternal(final RobotCasesSection section, final IProgressMonitor monitor)
            throws CoreException {
        new DocumentationTestCaseDeclarationSettingValidator(validationContext.getFile(), section, reporter)
                .validate(monitor);
        new PreconditionDeclarationExistenceValidator(validationContext.getFile(), reporter, section).validate(monitor);
        new PostconditionDeclarationExistenceValidator(validationContext.getFile(), reporter, section)
                .validate(monitor);
    }

    private void reportEmptyNamesOfCases(final List<TestCase> cases) {
        for (final TestCase testCase : cases) {
            final RobotToken caseName = testCase.getName();
            if (caseName.getText().trim().isEmpty()) {
                final RobotProblem problem = RobotProblem.causedBy(TestCasesProblem.EMPTY_CASE_NAME);
                final int startOffset = caseName.getStartOffset();
                final int endOffset = caseName.getEndOffset();

                final ProblemPosition problemPosition = new ProblemPosition(caseName.getFilePosition().getLine(),
                        Range.closed(startOffset, endOffset));
                reporter.handleProblem(problem, validationContext.getFile(), problemPosition);
            }
        }
    }

    private void reportEmptyCases(final List<TestCase> cases) {
        for (final TestCase testCase : cases) {
            final RobotToken caseName = testCase.getTestName();

            if (!hasAnythingToExecute(testCase)) {
                final String name = caseName.getText();
                final RobotProblem problem = RobotProblem.causedBy(TestCasesProblem.EMPTY_CASE).formatMessageWith(name);
                final Map<String, Object> arguments = ImmutableMap.of(AdditionalMarkerAttributes.NAME, name);
                reporter.handleProblem(problem, validationContext.getFile(), caseName, arguments);
            }
        }
    }

    private boolean hasAnythingToExecute(final TestCase testCase) {
        for (final RobotExecutableRow<?> robotExecutableRow : testCase.getExecutionContext()) {
            if (robotExecutableRow.isExecutable()) {
                return true;
            }
        }
        return false;
    }

    private void reportDuplicatedCases(final List<TestCase> cases) {
        final Set<String> duplicatedNames = newHashSet();

        for (final TestCase case1 : cases) {
            for (final TestCase case2 : cases) {
                if (case1 != case2) {
                    final String case1Name = case1.getTestName().getText();
                    final String case2Name = case2.getTestName().getText();

                    if (case1Name.equalsIgnoreCase(case2Name)) {
                        duplicatedNames.add(case1Name.toLowerCase());
                    }
                }
            }
        }

        for (final TestCase testCase : cases) {
            final RobotToken caseName = testCase.getTestName();
            final String name = caseName.getText();

            if (duplicatedNames.contains(name.toLowerCase())) {
                final RobotProblem problem = RobotProblem.causedBy(TestCasesProblem.DUPLICATED_CASE)
                        .formatMessageWith(name);
                final Map<String, Object> additionalArguments = ImmutableMap.of("name", name);
                reporter.handleProblem(problem, validationContext.getFile(), caseName, additionalArguments);
            }
        }
    }

    private void reportSettingsProblems(final List<TestCase> cases) throws CoreException {
        for (final TestCase testCase : cases) {
            new TestCaseSettingsValidator(validationContext, testCase, reporter).validate(null);
        }
    }

    void reportKeywordUsageProblems(final List<RobotCase> cases) {
        for (final RobotCase testCase : cases) {
            reportKeywordUsageProblemsInTestCaseSettings(testCase);
            reportKeywordUsageProblems(validationContext, reporter, testCase.getLinkedElement().getExecutionContext(),
                    testCase.getTemplateInUse());
        }

    }

    private void reportKeywordUsageProblemsInTestCaseSettings(final RobotCase testCase) {
        final RobotToken templateKeywordToken = testCase.getLinkedElement().getTemplateKeywordLocation();
        if (templateKeywordToken != null && !templateKeywordToken.getFilePosition().isNotSet()
                && isTemplateFromTestCasesTable(testCase)
                && !templateKeywordToken.getText().toLowerCase().equals("none")) {
            validateExistingKeywordCall(validationContext, reporter, templateKeywordToken, Optional.empty());
        }
    }

    private boolean isTemplateFromTestCasesTable(final RobotCase testCase) {
        return testCase.getLinkedElement().getRobotViewAboutTestTemplate() != null;
    }

    static void reportKeywordUsageProblemsInSetupAndTeardownSetting(final FileValidationContext validationContext,
            final ValidationReportingStrategy reporter, final RobotToken keywordNameToken,
            final Optional<List<RobotToken>> arguments) {
        final MappingResult variablesExtraction = new VariableExtractor().extract(keywordNameToken,
                validationContext.getFile().getName());
        final List<VariableDeclaration> variablesDeclarations = variablesExtraction.getCorrectVariables();
        if (variablesExtraction.getMappedElements().size() == 1 && variablesDeclarations.size() == 1) {
            final RobotProblem problem = RobotProblem
                    .causedBy(GeneralSettingsProblem.VARIABLE_AS_KEYWORD_USAGE_IN_SETTING)
                    .formatMessageWith(variablesDeclarations.get(0).getVariableName().getText());
            reporter.handleProblem(problem, validationContext.getFile(), keywordNameToken);
        } else {
            validateExistingKeywordCall(validationContext, reporter, keywordNameToken, arguments);
        }
    }

    static void reportKeywordUsageProblems(final FileValidationContext validationContext,
            final ValidationReportingStrategy reporter, final List<? extends RobotExecutableRow<?>> executables,
            final Optional<String> templateKeyword) {

        for (final RobotExecutableRow<?> executable : executables) {
            if (!executable.isExecutable() || templateKeyword.isPresent()) {
                continue;
            }

            final IExecutableRowDescriptor<?> executableRowDescriptor = executable.buildLineDescription();
            RobotToken keywordName = executableRowDescriptor.getAction().getToken();

            final IFile file = validationContext.getFile();
            if (executableRowDescriptor.getRowType() == ERowType.FOR) {
                final List<BuildMessage> messages = executableRowDescriptor.getMessages();
                for (final BuildMessage buildMessage : messages) {
                    final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.INVALID_FOR_KEYWORD)
                            .formatMessageWith(buildMessage.getMessage());
                    final ProblemPosition position = new ProblemPosition(
                            buildMessage.getFileRegion().getStart().getLine(),
                            Range.closed(buildMessage.getFileRegion().getStart().getOffset(),
                                    buildMessage.getFileRegion().getEnd().getOffset()));
                    reporter.handleProblem(problem, file, position);
                }
                continue;
            }

            if (executableRowDescriptor.getRowType() == ERowType.FOR_CONTINUE) {
                final ForLoopContinueRowDescriptor<?> loopContinueRowDescriptor = (ForLoopContinueRowDescriptor<?>) executable
                        .buildLineDescription();
                keywordName = loopContinueRowDescriptor.getKeywordAction().getToken();
            }

            if (!keywordName.getFilePosition().isNotSet()) {
                validateExistingKeywordCall(validationContext, reporter, keywordName,
                        Optional.of(executableRowDescriptor.getKeywordArguments()));
            } else {
                reporter.handleProblem(RobotProblem.causedBy(KeywordsProblem.MISSING_KEYWORD)
                        .formatMessageWith(executable.getAction().getText()), file, executable.getAction());
            }
        }
    }

    static void validateExistingKeywordCall(final FileValidationContext validationContext,
            final ValidationReportingStrategy reporter, final RobotToken keywordName,
            final Optional<List<RobotToken>> arguments) {
        final ListMultimap<String, KeywordEntity> keywordProposal = validationContext
                .findPossibleKeywords(keywordName.getText());

        final Optional<String> nameToUse = GherkinStyleSupport.firstNameTransformationResult(keywordName.getText(),
                gherkinNameVariant -> validationContext.isKeywordAccessible(keywordProposal, gherkinNameVariant)
                        ? Optional.of(gherkinNameVariant)
                        : Optional.empty());
        final String name = !nameToUse.isPresent() || nameToUse.get().isEmpty() ? keywordName.getText()
                : nameToUse.get();
        final int offset = keywordName.getStartOffset() + (keywordName.getText().length() - name.length());
        final ProblemPosition position = new ProblemPosition(keywordName.getLineNumber(),
                Range.closed(offset, offset + name.length()));

        if (!nameToUse.isPresent()) {
            reporter.handleProblem(RobotProblem.causedBy(KeywordsProblem.UNKNOWN_KEYWORD).formatMessageWith(name),
                    validationContext.getFile(), position, ImmutableMap.of(AdditionalMarkerAttributes.NAME, name,
                            AdditionalMarkerAttributes.ORIGINAL_NAME, keywordName.getText()));
            return;
        }

        final ListMultimap<KeywordScope, KeywordEntity> keywords = validationContext
                .getPossibleKeywords(keywordProposal, name);

        for (final KeywordScope scope : KeywordScope.defaultOrder()) {
            final List<KeywordEntity> keywordEntities = keywords.get(scope);
            if (keywordEntities.size() == 1) {
                final ValidationKeywordEntity keyword = (ValidationKeywordEntity) keywordEntities.get(0);
                validateFoundKeyword(validationContext.getFile(), keywordName, name, arguments, position, keyword,
                        reporter);
                break;
            } else if (keywordEntities.size() > 1) {
                final List<String> sources = keywordEntities.stream().map(KeywordEntity::getSourceNameInUse).collect(
                        toList());
                reporter.handleProblem(
                        RobotProblem.causedBy(KeywordsProblem.AMBIGUOUS_KEYWORD).formatMessageWith(name,
                                "[" + String.join(", ", sources) + "]"),
                        validationContext.getFile(), position,
                        ImmutableMap.of(AdditionalMarkerAttributes.NAME, name, AdditionalMarkerAttributes.ORIGINAL_NAME,
                                keywordName.getText(), AdditionalMarkerAttributes.SOURCES, String.join(";", sources)));
                break;
            }
        }
    }

    private static void validateFoundKeyword(final IFile file, final RobotToken nameToken, final String actualName,
            final Optional<List<RobotToken>> arguments, final ProblemPosition position,
            final ValidationKeywordEntity keyword, final ValidationReportingStrategy reporter) {

        if (keyword.isDeprecated()) {
            reporter.handleProblem(
                    RobotProblem.causedBy(KeywordsProblem.DEPRECATED_KEYWORD).formatMessageWith(actualName), file,
                    position);
        }
        if (keyword.isFromNestedLibrary(file)) {
            reporter.handleProblem(
                    RobotProblem.causedBy(KeywordsProblem.KEYWORD_FROM_NESTED_LIBRARY).formatMessageWith(actualName),
                    file,
                    position);
        }
        if (keyword.hasInconsistentName(actualName)) {
            reporter.handleProblem(
                    RobotProblem.causedBy(KeywordsProblem.KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION)
                            .formatMessageWith(actualName, keyword.getNameFromDefinition()),
                    file, position,
                    ImmutableMap.of(AdditionalMarkerAttributes.NAME, actualName,
                            AdditionalMarkerAttributes.ORIGINAL_NAME,
                            keyword.getNameFromDefinition(), AdditionalMarkerAttributes.SOURCES,
                            keyword.getSourceNameInUse()));
        }
        if (arguments.isPresent()) {
            final KeywordCallArgumentsValidator argsValidator = createKeywordCallArgumentsValidator(file, nameToken,
                    keyword.getArgumentsDescriptor(), arguments.get(), reporter);
            argsValidator.validate(null);

            final List<RobotToken> varsForSyntaxCheck = SpecialKeywords.getArgumentsToValidateForVariablesSyntax(
                    QualifiedKeywordName.create(keyword.getKeywordName(), keyword.getSourceName()), arguments.get());
            for (final RobotToken varToken : varsForSyntaxCheck) {
                if (!VariableNamesSupport.isCleanVariable(varToken.getText())) {
                    final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_VARIABLE_SYNTAX)
                            .formatMessageWith(varToken.getText());
                    reporter.handleProblem(problem, file, varToken);
                }
            }
        }
    }

    private static KeywordCallArgumentsValidator createKeywordCallArgumentsValidator(final IFile file,
            final RobotToken definingToken, final ArgumentsDescriptor argumentsDescriptor,
            final List<RobotToken> arguments, final ValidationReportingStrategy reporter) {
        return new KeywordCallArgumentsValidator(file, definingToken, reporter, argumentsDescriptor, arguments);
    }

    private void reportUnknownVariables(final List<TestCase> cases) {
        final Set<String> variables = validationContext.getAccessibleVariables();

        for (final TestCase testCase : cases) {
            reportUnknownVariablesInTimeoutSetting(testCase, variables);
            reportUnknownVariablesInTags(testCase, variables);
            reportUnknownVariables(validationContext, reporter, collectTestCaseExeRowsForVariablesChecking(testCase),
                    variables);
        }
    }

    private List<? extends RobotExecutableRow<?>> collectTestCaseExeRowsForVariablesChecking(final TestCase testCase) {
        final List<RobotExecutableRow<?>> exeRows = newArrayList();
        final List<TestCaseSetup> setups = testCase.getSetups();
        if (!setups.isEmpty()) {
            exeRows.add(setups.get(0).asExecutableRow());
        }
        exeRows.addAll(testCase.getExecutionContext());
        final List<TestCaseTeardown> teardowns = testCase.getTeardowns();
        if (!teardowns.isEmpty()) {
            exeRows.add(teardowns.get(0).asExecutableRow());
        }
        return exeRows;
    }

    private void reportUnknownVariablesInTimeoutSetting(final TestCase testCase, final Set<String> variables) {
        final List<TestCaseTimeout> timeouts = testCase.getTimeouts();
        for (final TestCaseTimeout testCaseTimeout : timeouts) {
            final RobotToken timeoutToken = testCaseTimeout.getTimeout();
            if (timeoutToken != null) {
                validateTimeoutSetting(validationContext, reporter, variables, timeoutToken);
            }
        }
    }

    static void validateTimeoutSetting(final FileValidationContext validationContext,
            final ValidationReportingStrategy reporter, final Set<String> variables, final RobotToken timeoutToken) {
        final String timeout = timeoutToken.getText();
        if (!RobotTimeFormat.isValidRobotTimeArgument(timeout.trim())) {

            if (containsVariables(validationContext, timeoutToken)) {
                final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext, reporter);
                unknownVarsValidator.reportUnknownVars(variables, newArrayList(timeoutToken));
            } else {
                final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_TIME_FORMAT)
                        .formatMessageWith(timeout);
                reporter.handleProblem(problem, validationContext.getFile(), timeoutToken);
            }
        }
    }

    private static boolean containsVariables(final FileValidationContext validationContext,
            final RobotToken timeoutToken) {
        final String filename = validationContext.getFile().getName();
        return !new VariableExtractor().extract(timeoutToken, filename).getCorrectVariables().isEmpty();
    }

    private void reportUnknownVariablesInTags(final TestCase testCase, final Set<String> variables) {
        final List<TestCaseTags> tags = testCase.getTags();

        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext, reporter);
        for (final TestCaseTags tag : tags) {
            unknownVarsValidator.reportUnknownVars(variables, tag.getTags());
        }
    }

    static void reportUnknownVariables(final FileValidationContext validationContext,
            final ValidationReportingStrategy reporter, final List<? extends RobotExecutableRow<?>> executables,
            final Set<String> variables) {

        final Set<String> definedVariables = newHashSet(variables);

        final Map<String, Object> additionalMarkerAttributes = ImmutableMap
                .of(AdditionalMarkerAttributes.DEFINE_VAR_LOCALLY, Boolean.TRUE);
        final ValidationReportingStrategy reportingStrategy = AttributesAugmentingReportingStrategy.create(reporter,
                additionalMarkerAttributes);

        final UnknownVariables unknownVarsValidator = new UnknownVariables(validationContext, reportingStrategy);
        for (final RobotExecutableRow<?> row : executables) {
            if (row.isExecutable()) {
                final IExecutableRowDescriptor<?> lineDescriptor = row.buildLineDescription();
                final QualifiedKeywordName keywordName = getSpecialKeywordName(validationContext, lineDescriptor);

                final List<VariableDeclaration> variableUsedInCall = SpecialKeywords.getUsedVariables(keywordName,
                        lineDescriptor);
                unknownVarsValidator.reportUnknownVariables(definedVariables, variableUsedInCall);

                SpecialKeywords.getCreatedVariables(keywordName, lineDescriptor)
                        .forEach(var -> definedVariables.add(VariableNamesSupport.extractUnifiedVariableName(var)));
                lineDescriptor.getCreatedVariables()
                        .forEach(var -> definedVariables.add(VariableNamesSupport.extractUnifiedVariableName(var)));
            }
        }
    }

    private static QualifiedKeywordName getSpecialKeywordName(final FileValidationContext validationContext,
            final IExecutableRowDescriptor<?> lineDescriptor) {
        final String originalName = getKeywordToken(lineDescriptor).getText();
        final QualifiedKeywordName keywordName = QualifiedKeywordName.fromOccurrence(originalName);
        if (!keywordName.getKeywordSource().isEmpty()) {
            return keywordName;
        }
        if (SpecialKeywords.mayBeSpecialKeyword(originalName)) {
            for (final KeywordScope scope : KeywordScope.defaultOrder()) {
                final List<KeywordEntity> possible = validationContext.getPossibleKeywords(originalName, true)
                        .get(scope);
                if (possible.isEmpty()) {
                    continue;
                } else if (possible.size() == 1) {
                    return QualifiedKeywordName.create(originalName, possible.get(0).getSourceNameInUse());
                }
            }
        }
        return null;
    }

    private static RobotToken getKeywordToken(final IExecutableRowDescriptor<?> lineDescription) {
        if (lineDescription.getRowType() == ERowType.FOR_CONTINUE) {
            return ((ForLoopContinueRowDescriptor<?>) lineDescription).getKeywordAction().getToken();
        } else {
            return lineDescription.getAction().getToken();
        }
    }
}
