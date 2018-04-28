package org.monarchinitiative.hpoworkbench.gui;


import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.File;

/**
 * This is used to figure out where VPVGui will store the viewpoint files. For instance, with linux
 * this would be /home/username/.loinc2hpo/...
 */
public class PlatformUtil {

    public static final String HPO_OBO_FILENAME = "hp.obo";

    public static final String MONDO_OBO_FILENAME = "mondo.obo";

    public static final String HPO_ANNOTATIONS_FILENAME = "phenotype.hpoa";

    public static final String HPO_WORKBENCH_SETTINGS_FILENAME = "application.properties";

    /**
     * Get path to directory where HRMD-gui stores global settings.
     * The path depends on underlying operating system. Linux, Windows and OSX
     * currently supported.
     * @return File to directory
     */
    public static File getHpoWorkbenchDir() {
        CurrentPlatform platform = figureOutPlatform();

        File linuxPath = new File(System.getProperty("user.home") + File.separator + ".hpoworkbench");
        File windowsPath = new File(System.getProperty("user.home") + File.separator + "hpoworkbench");
        File osxPath = new File(System.getProperty("user.home") + File.separator + ".hpoworkbench");

        switch (platform) {
            case LINUX: return linuxPath;
            case WINDOWS: return windowsPath;
            case OSX: return osxPath;
            case UNKNOWN: return null;
            default:
                Alert a = new Alert(AlertType.ERROR);
                a.setTitle("Find HPOworkbench config dir");
                a.setHeaderText(null);
                a.setContentText(String.format("Unrecognized platform. %s", platform.toString()));
                a.showAndWait();
                return null;
        }
    }


    public static String getLocalHPOPath() {
        File dir = getHpoWorkbenchDir();
        return dir + File.separator + HPO_OBO_FILENAME;
    }

    /** Return the absolute path to the settings file, which is kept in the .loinc2hpo directory in the
     * user's home directory. For simplicity assume one user per account etc. The file is a simple key:value file.
     * @return path to the local copy of the HPO annotation file
     */
    public static String getLocalPhenotypeAnnotationPath() {
        File dir = getHpoWorkbenchDir();
        return dir + File.separator + HPO_ANNOTATIONS_FILENAME;
    }

    /**
     * Get the absolute path to the log file.
     * @return the absolute path,e.g., /home/user/.vpvgui/vpvgui.log
     */
    public static String getAbsoluteLogPath() {
        File dir = getHpoWorkbenchDir();
        return dir + File.separator + "hpoworkbench.log";
    }

    /** Return the absolute path to the settings file, which is kept in the .loinc2hpo directory in the
     * user's home directory. For simplicity assume one user per account etc. The file is a simple key:value file.
     * @return
     */
//    public static String getPathToSettingsFile() {
//        File dir = getHpoWorkbenchDir();
//        return dir + File.separator + HPO_WORKBENCH_SETTINGS_FILENAME;
//    }

    /* Based on this post: http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/ */
    private static CurrentPlatform figureOutPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("nix") || osName.contains("nux")  || osName.contains("aix") ) {
            return CurrentPlatform.LINUX;
        } else if (osName.contains("win")) {
            return CurrentPlatform.WINDOWS;
        } else if (osName.contains("mac") ) {
            return CurrentPlatform.OSX;
        } else {
            return CurrentPlatform.UNKNOWN;
        }
    }

    public static boolean isMacintosh() {
        return figureOutPlatform().equals(CurrentPlatform.OSX);
    }




    private enum CurrentPlatform {
        LINUX("Linux"),
        WINDOWS("Windows"),
        OSX("Os X"),
        UNKNOWN("Unknown");

        private final String name;

        CurrentPlatform(String n) {this.name = n; }

        @Override
        public String toString() { return this.name; }
    }

}
