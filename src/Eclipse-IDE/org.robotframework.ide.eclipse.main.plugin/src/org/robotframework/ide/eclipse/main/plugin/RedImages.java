/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.IWorkbenchAdapter;

import com.google.common.io.Files;

@SuppressWarnings("PMD.GodClass")
public class RedImages {

    public static final VariableImages VARIABLES = new VariableImages();

    private static final Map<ImageDescriptor, ImageDescriptor> GRAY_IMAGES = new HashMap<>(0);


    public static ImageDescriptor getFilterImage() {
        return RedPlugin.getImageDescriptor("resources/filter.png");
    }

    public static ImageDescriptor getExecuteRunImage() {
        return RedPlugin.getImageDescriptor("resources/exec_run.png");
    }

    public static ImageDescriptor getExecuteDebugImage() {
        return RedPlugin.getImageDescriptor("resources/exec_debug.png");
    }

    public static ImageDescriptor getStopImage() {
        return RedPlugin.getImageDescriptor("resources/stop.png");
    }

    public static ImageDescriptor getInterruptImage() {
        return RedPlugin.getImageDescriptor("resources/stop_soft.png");
    }

    public static ImageDescriptor getDisabledInterruptImage() {
        return RedPlugin.getImageDescriptor("resources/stop_soft_disabled.png");
    }

    public static ImageDescriptor getSuspendImage() {
        return DebugUITools.getImageDescriptor("IMG_ELCL_SUSPEND");
    }

    public static ImageDescriptor getResumeImage() {
        return DebugUITools.getImageDescriptor("IMG_ELCL_RESUME");
    }

    public static ImageDescriptor getDeleteImage() {
        return getWorkbenchSharedImage(ISharedImages.IMG_TOOL_DELETE);
    }

    public static ImageDescriptor getCloseImage() {
        return getWorkbenchSharedImage(ISharedImages.IMG_ELCL_REMOVE);
    }

    public static ImageDescriptor getDisabledCloseImage() {
        return getWorkbenchSharedImage(ISharedImages.IMG_ELCL_REMOVE_DISABLED);
    }

    public static ImageDescriptor getCloseAllImage() {
        return getWorkbenchSharedImage(ISharedImages.IMG_ELCL_REMOVEALL);
    }

    public static ImageDescriptor getDisabledCloseAllImage() {
        return getWorkbenchSharedImage(ISharedImages.IMG_ELCL_REMOVEALL_DISABLED);
    }

    public static ImageDescriptor getSaveImage() {
        return getWorkbenchSharedImage(ISharedImages.IMG_ETOOL_SAVE_EDIT);
    }

    public static ImageDescriptor getFolderImage() {
        return getWorkbenchSharedImage(ISharedImages.IMG_OBJ_FOLDER);
    }

    public static ImageDescriptor getBigErrorImage() {
        return RedPlugin.getImageDescriptor("resources/big_error.png");
    }

    public static Optional<URI> getBigErrorImageUri() {
        return getImageUri("resources", "big_error.png");
    }

    public static ImageDescriptor getRefreshImage() {
        return RedPlugin.getImageDescriptor("resources/refresh.png");
    }

    public static ImageDescriptor getSyncedImage() {
        return getWorkbenchSharedImage(ISharedImages.IMG_ELCL_SYNCED);
    }

    public static ImageDescriptor getBackImage() {
        return getWorkbenchSharedImage(ISharedImages.IMG_TOOL_BACK);
    }

    public static ImageDescriptor getForwardImage() {
        return getWorkbenchSharedImage(ISharedImages.IMG_TOOL_FORWARD);
    }

    public static ImageDescriptor getSyncBrokenImage() {
        return RedPlugin.getImageDescriptor("resources/sync_broken.png");
    }

    public static ImageDescriptor getOpenInBrowserImage() {
        return RedPlugin.getImageDescriptor("resources/open_browser.png");
    }

    public static ImageDescriptor getCutImage() {
        return getWorkbenchSharedImage(ISharedImages.IMG_TOOL_CUT);
    }

    public static ImageDescriptor getCopyImage() {
        return getWorkbenchSharedImage(ISharedImages.IMG_TOOL_COPY);
    }

    public static ImageDescriptor getPasteImage() {
        return getWorkbenchSharedImage(ISharedImages.IMG_TOOL_PASTE);
    }

    public static ImageDescriptor getTaskImage() {
        return getWorkbenchSharedImage(IDE.SharedImages.IMG_OBJS_TASK_TSK);
    }

    public static ImageDescriptor getRobotImage() {
        return RedPlugin.getImageDescriptor("resources/robot.png");
    }

    public static ImageDescriptor getRemoteRobotImage() {
        return RedPlugin.getImageDescriptor("resources/robot_remote.png");
    }

    public static ImageDescriptor getScriptRobotImage() {
        return RedPlugin.getImageDescriptor("resources/robot_script.png");
    }

    public static ImageDescriptor getRobotCasesFileSectionImage() {
        return RedPlugin.getImageDescriptor("resources/section.png");
    }

    public static ImageDescriptor getRobotCasesFileDeleteSectionImage() {
        return RedPlugin.getImageDescriptor("resources/section_delete.png");
    }

    public static ImageDescriptor getRobotVariableImage() {
        return RedPlugin.getImageDescriptor("resources/variable.png");
    }

    public static ImageDescriptor getRobotUnknownVariableImage() {
        return RedPlugin.getImageDescriptor("resources/variable_unknown.png");
    }

    public static ImageDescriptor getRobotWarnedVariableImage() {
        return RedPlugin.getImageDescriptor("resources/variable_warn.png");
    }

    public static ImageDescriptor getRobotScalarVariableImage() {
        return RedPlugin.getImageDescriptor("resources/variable_scalar.png");
    }

    public static ImageDescriptor getRobotListVariableImage() {
        return RedPlugin.getImageDescriptor("resources/variable_list.png");
    }

    public static ImageDescriptor getRobotDictionaryVariableImage() {
        return RedPlugin.getImageDescriptor("resources/variable_dict.png");
    }

    public static ImageDescriptor getRobotSettingImage() {
        return RedPlugin.getImageDescriptor("resources/gear.png");
    }

    public static ImageDescriptor getSourceImage() {
        return RedPlugin.getImageDescriptor("resources/source.png");
    }

    public static ImageDescriptor getRobotFileImage() {
        return RedPlugin.getImageDescriptor("resources/file_robot.png");
    }

    public static Optional<URI> getRobotFileImageUri() {
        return getImageUri("resources", "file_robot.png");
    }

    public static ImageDescriptor getTestCaseImage() {
        return RedPlugin.getImageDescriptor("resources/case.png");
    }

    public static Optional<URI> getTestCaseImageUri() {
        return getImageUri("resources", "case.png");
    }

    public static ImageDescriptor getTemplatedTestCaseImage() {
        return RedPlugin.getImageDescriptor("resources/case_templated.png");
    }

    public static Optional<URI> getTemplatedTestCaseImageUri() {
        return getImageUri("resources", "case_templated.png");
    }

    public static ImageDescriptor getTestCaseSettingImage() {
        return RedPlugin.getImageDescriptor("resources/gear_small.png");
    }

    public static ImageDescriptor getAddImage() {
        return RedPlugin.getImageDescriptor("resources/add.png");
    }

    public static ImageDescriptor getMagnifierImage() {
        return RedPlugin.getImageDescriptor("resources/magnifier.png");
    }

    public static ImageDescriptor getTooltipImage() {
        return RedPlugin.getImageDescriptor("resources/tooltip.png");
    }

    public static ImageDescriptor getTooltipAddImage() {
        return RedPlugin.getImageDescriptor("resources/tooltip_add.png");
    }

    public static ImageDescriptor getTooltipRemoveImage() {
        return RedPlugin.getImageDescriptor("resources/tooltip_remove.png");
    }

    public static ImageDescriptor getTooltipMoreImage() {
        return RedPlugin.getImageDescriptor("resources/tooltip_more.png");
    }

    public static ImageDescriptor getTooltipWarnImage() {
        return RedPlugin.getImageDescriptor("resources/tooltip_warn.png");
    }

    public static ImageDescriptor getTooltipProhibitedImage() {
        return RedPlugin.getImageDescriptor("resources/tooltip_prohibited.png");
    }

    public static ImageDescriptor getSearchMarkerImage() {
        return RedPlugin.getImageDescriptor("resources/red_search_marker.png");
    }

    public static ImageDescriptor getLibraryImage() {
        return RedPlugin.getImageDescriptor("resources/library.png");
    }

    public static ImageDescriptor getJavaLibraryImage() {
        return RedPlugin.getImageDescriptor("resources/library_java.png");
    }

    public static ImageDescriptor getPythonLibraryImage() {
        return RedPlugin.getImageDescriptor("resources/library_python.png");
    }

    public static ImageDescriptor getPythonSitePackagesLibraryImage() {
        return RedPlugin.getImageDescriptor("resources/library_python_site_packages.png");
    }

    public static ImageDescriptor getVirtualLibraryImage() {
        return RedPlugin.getImageDescriptor("resources/library_virtual.png");
    }

    public static ImageDescriptor getRobotLibraryErrorImage() {
        return RedPlugin.getImageDescriptor("resources/library_error.png");
    }

    public static ImageDescriptor getRobotLibraryWarnImage() {
        return RedPlugin.getImageDescriptor("resources/library_warn.png");
    }

    public static ImageDescriptor getJavaClassImage() {
        return RedPlugin.getImageDescriptor("resources/java_class.png");
    }

    public static ImageDescriptor getBookImage() {
        return RedPlugin.getImageDescriptor("resources/book.png");
    }

    public static Optional<URI> getBookImageUri() {
        return getImageUri("resources", "book.png");
    }

    public static ImageDescriptor getBigKeywordImage() {
        return RedPlugin.getImageDescriptor("resources/big_keyword.png");
    }

    public static ImageDescriptor getKeywordImage() {
        return RedPlugin.getImageDescriptor("resources/keyword.png");
    }

    public static Optional<URI> getKeywordImageUri() {
        return getImageUri("resources", "keyword.png");
    }

    public static ImageDescriptor getUserKeywordImage() {
        return RedPlugin.getImageDescriptor("resources/keyword_user.png");
    }

    public static Optional<URI> getUserKeywordImageUri() {
        return getImageUri("resources", "keyword_user.png");
    }

    public static ImageDescriptor getRobotProjectConfigFile() {
        return RedPlugin.getImageDescriptor("resources/file_redxml.png");
    }

    public static ImageDescriptor getFocusSectionImage() {
        return RedPlugin.getImageDescriptor("resources/focus_section.png");
    }

    public static ImageDescriptor getResourceImage() {
        return RedPlugin.getImageDescriptor("resources/resource.png");
    }

    public static ImageDescriptor getCollapseAllImage() {
        return RedPlugin.getImageDescriptor("resources/collapseall.png");
    }

    public static ImageDescriptor getExpandAllImage() {
        return RedPlugin.getImageDescriptor("resources/expandall.png");
    }

    public static ImageDescriptor getRelaunchImage() {
        return RedPlugin.getImageDescriptor("resources/relaunch.png");
    }

    public static ImageDescriptor getRelaunchFailedImage() {
        return RedPlugin.getImageDescriptor("resources/relaunchf.png");
    }

    public static ImageDescriptor getFailuresImage() {
        return RedPlugin.getImageDescriptor("resources/failures.png");
    }

    public static ImageDescriptor getGoToImage() {
        return RedPlugin.getImageDescriptor("resources/goto.png");
    }

    public static ImageDescriptor getTestImage() {
        return RedPlugin.getImageDescriptor("resources/test.png");
    }

    public static ImageDescriptor getTestPassImage() {
        return RedPlugin.getImageDescriptor("resources/testok.png");
    }

    public static ImageDescriptor getTestFailImage() {
        return RedPlugin.getImageDescriptor("resources/testerr.png");
    }

    public static ImageDescriptor getTestInProgressImage() {
        return RedPlugin.getImageDescriptor("resources/testrun.png");
    }

    public static ImageDescriptor getSuiteImage() {
        return RedPlugin.getImageDescriptor("resources/tsuite.png");
    }

    public static ImageDescriptor getSuitePassImage() {
        return RedPlugin.getImageDescriptor("resources/tsuiteok.png");
    }

    public static ImageDescriptor getSuiteFailImage() {
        return RedPlugin.getImageDescriptor("resources/tsuiteerror.png");
    }

    public static ImageDescriptor getSuiteInProgressImage() {
        return RedPlugin.getImageDescriptor("resources/tsuiterun.png");
    }

    public static ImageDescriptor getSuccessImage() {
        return RedPlugin.getImageDescriptor("resources/success.png");
    }

    public static ImageDescriptor getBigSuccessImage() {
        return RedPlugin.getImageDescriptor("resources/success_big.png");
    }

    public static ImageDescriptor getErrorImage() {
        return RedPlugin.getImageDescriptor("resources/error.png");
    }

    public static ImageDescriptor getFatalErrorImage() {
        return RedPlugin.getImageDescriptor("resources/error_fatal.png");
    }

    public static ImageDescriptor getWarningImage() {
        return RedPlugin.getImageDescriptor("resources/warning.png");
    }

    public static ImageDescriptor getBigWarningImage() {
        return RedPlugin.getImageDescriptor("resources/warning_big.png");
    }

    public static ImageDescriptor getTagImage() {
        return RedPlugin.getImageDescriptor("resources/tag.png");
    }

    public static ImageDescriptor getRemoveTagImage() {
        return RedPlugin.getImageDescriptor("resources/close_tag.png");
    }

    public static ImageDescriptor getChangeImage() {
        return RedPlugin.getImageDescriptor("resources/change.png");
    }

    public static ImageDescriptor getActivateOnStdOutImage() {
        return RedPlugin.getImageDescriptor("resources/activate_stdout.png");
    }

    public static ImageDescriptor getActivateOnStdErrImage() {
        return RedPlugin.getImageDescriptor("resources/activate_stderr.png");
    }

    public static ImageDescriptor getRemoteConnectedImage() {
        return RedPlugin.getImageDescriptor("resources/remote_connected.png");
    }

    public static ImageDescriptor getRemoteDisconnectedImage() {
        return RedPlugin.getImageDescriptor("resources/remote_disconnected.png");
    }

    public static ImageDescriptor getElementImage() {
        return RedPlugin.getImageDescriptor("resources/generic_element.png");
    }

    public static ImageDescriptor getSortImage() {
        return RedPlugin.getImageDescriptor("resources/sort_alphabetically.png");
    }

    public static ImageDescriptor getSortUpImage() {
        return RedPlugin.getImageDescriptor("resources/sort_up.png");
    }

    public static ImageDescriptor getSortDownImage() {
        return RedPlugin.getImageDescriptor("resources/sort_down.png");
    }

    public static ImageDescriptor getAdderStateChangeImage() {
        return RedPlugin.getImageDescriptor("resources/adder_state_change.png");
    }

    public static ImageDescriptor getWordwrapImage() {
        return RedPlugin.getImageDescriptor("resources/wordwrap.png");
    }

    public static ImageDescriptor getBlockSelectionModeImage() {
        return RedPlugin.getImageDescriptor("resources/block_selection_mode.png");
    }

    public static ImageDescriptor getShowWhitespaceCharImage() {
        return RedPlugin.getImageDescriptor("resources/show_whitespace_chars.png");
    }

    public static ImageDescriptor getEditImage() {
        return RedPlugin.getImageDescriptor("resources/edit.png");
    }

    public static ImageDescriptor getStackFrameImage() {
        return RedPlugin.getImageDescriptor("resources/stackframe.png");
    }

    public static ImageDescriptor getImageForResource(final IResource resource) {
        return resource.getAdapter(IWorkbenchAdapter.class).getImageDescriptor(resource);
    }

    public static ImageDescriptor getImageForFileWithExtension(final String extension) {
        final String extWithoutDot = extension.startsWith(".") ? extension.substring(1) : extension;
        return PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor("file." + extWithoutDot);
    }


    /**
     * For given image descriptor the gray version descriptor is created.
     *
     * @param descriptor
     * @return Gray version of image from parameter.
     */
    public static ImageDescriptor getGrayedImage(final ImageDescriptor descriptor) {
        if (descriptor == null) {
            return null;
        }
        ImageDescriptor grayImage = GRAY_IMAGES.get(descriptor);
        if (grayImage == null) {
            grayImage = ImageDescriptor.createWithFlags(descriptor, SWT.IMAGE_GRAY);
            GRAY_IMAGES.put(descriptor, grayImage);
        }
        return grayImage;
    }

    private static ImageDescriptor getWorkbenchSharedImage(final String symbolicName) {
        return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(symbolicName);
    }

    private static Optional<URI> getImageUri(final String parentPath, final String filename) {
        // The file is copied from bundle to our plugin metadata folder, because
        // those images has to be placed somewhere in file system, not in other zips/jars
        // in order to be displayed by the browser
        try {
            final String path = parentPath + "/" + filename;

            final File stateLocation = RedPlugin.getDefault().getStateLocation().toFile();
            final File docsDir = new File(stateLocation, "docs");
            if (!docsDir.exists()) {
                docsDir.mkdir();
            }
            final File targetFile = new File(docsDir, filename);
            if (!targetFile.exists()) {
                final InputStream stream = FileLocator.openStream(RedPlugin.getDefault().getBundle(), new Path(path),
                        false);
                final byte[] buffer = new byte[stream.available()];
                stream.read(buffer);
                Files.write(buffer, targetFile);
            }

            return Optional.of(targetFile.toURI());
        } catch (final IOException e) {
            return Optional.empty();
        }
    }

    public static class VariableImages {

        public ImageDescriptor getDebugScalarVariableImage() {
            return RedPlugin.getImageDescriptor("resources/variable_scalar_debug.png");
        }

        public ImageDescriptor getDebugListVariableImage() {
            return RedPlugin.getImageDescriptor("resources/variable_list_debug.png");
        }

        public ImageDescriptor getDebugDictionaryVariableImage() {
            return RedPlugin.getImageDescriptor("resources/variable_dict_debug.png");
        }

        public ImageDescriptor getDebugGlobalScopeDecorator() {
            return RedPlugin.getImageDescriptor("resources/variable_debug_global.png");
        }

        public ImageDescriptor getDebugSuiteScopeDecorator() {
            return RedPlugin.getImageDescriptor("resources/variable_debug_suite.png");
        }

        public ImageDescriptor getDebugTestScopeDecorator() {
            return RedPlugin.getImageDescriptor("resources/variable_debug_test.png");
        }

        public ImageDescriptor getDebugLocalScopeDecorator() {
            return RedPlugin.getImageDescriptor("resources/variable_debug_local.png");
        }
    }

    public static class Decorators {

        public static ImageDescriptor getRobotDecorator() {
            return RedPlugin.getImageDescriptor("resources/decorator_robot.png");
        }

        public static ImageDescriptor getFolderDecorator() {
            return RedPlugin.getImageDescriptor("resources/decorator_folder.png");
        }

        public static ImageDescriptor getResourceDecorator() {
            return RedPlugin.getImageDescriptor("resources/decorator_resource_file.png");
        }

        public static ImageDescriptor getInitFileDecorator() {
            return RedPlugin.getImageDescriptor("resources/decorator_init_file.png");
        }
    }
}
