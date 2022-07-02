#!/usr/bin/env bash

##
# Run this script as `bash package.sh` to build a native app.
# The script works on both UNIX and OSX platforms.


##
# Static variables
SRC_DIR=hpoworkbench-gui
APP_NAME="hpoworkbench"
CMD_NAME="hpoworkbench"
VERSION=2.2.2

BUILD_DIR=${SRC_DIR}/target

JAR_NAME="HpoWorkbench.jar"
VENDOR="The Jackson Laboratory"
DESCRIPTION="HPO Workbench is a Java application for exploring Human Phenotype Ontology (HPO) and HPO annotations."
COPYRIGHT="Copyright 2022, All rights reserved"
ICON="${BUILD_DIR}/classes/img/icon"


function detect_platform() {
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
      echo "Linux"
    elif [[ "$OSTYPE" =~ darwin.* ]]; then
      echo "Osx";
    elif [[ "$OSTYPE" =~ "win64" ]]; then
          echo "Windows";
    else
      # More people use OSX
      echo "Osx"
    fi
}

# The following function not used since HPO Workbench is not yet modular
function build_for_module_path() {
    # copy the JAR and lib (dependencies)
    cp $BUILD_DIR/$JAR_NAME $PACKAGE_DIR
    cp -r $BUILD_DIR/lib $PACKAGE_DIR/lib
    if [[ "$PLATFORM" == "Linux" ]]; then
      # Setup Linux CLI using module path
      MPATH="${PACKAGE_DIR}/lib:${PACKAGE_DIR}/${JAR_NAME}"
      printf "Module path: %s\n" "${MPATH}"
      MODULE="org.jax.isopret.app/org.jax.isopret_gui.App"
      printf "Module %s\n" "${MODULE}"

      DETECTED_MODULES=$(jdeps --multi-release 17 --ignore-missing-deps --print-module-deps --module-path "${MPATH}" ${PACKAGE_DIR}/${JAR_NAME})
      printf "Detected modules: %s\n" "${DETECTED_MODULES}"
      MANUAL_MODULES="jdk.localedata"
      JAVA_RUNTIME=${PACKAGE_DIR}/java-runtime
      printf "Building Java runtime to: %s\n" ${JAVA_RUNTIME}
      #jlink --no-header-files --no-man-pages --compress=2 --strip-debug --module-path ${MPATH} --add-modules "${DETECTED_MODULES},${MANUAL_MODULES}" --include-locales=en --output ${JAVA_RUNTIME}

      # jpackage --name "${APP_NAME}" --module-path "${MPATH}" --module "${MODULE}" --java-options -Xmx2048m --runtime-image ${JAVA_RUNTIME} --linux-menu-group Science --linux-shortcut --linux-package-name "${CMD_NAME}" --icon "${ICON}.png" --app-version "${VERSION}" --description "${DESCRIPTION}" --vendor "${VENDOR}" --license-file LICENSE --copyright "${COPYRIGHT}"
    elif [[ "$PLATFORM" == "Osx" ]]; then
      # setup OSX CLI using module path
      #jpackage --name "${APP_NAME}" --input "${PACKAGE_DIR}" --main-jar "${JAR_NAME}" --mac-package-name "${CMD_NAME}" --icon "${ICON}.icns" --app-version "${VERSION}" --description "${DESCRIPTION}" --vendor "${VENDOR}" --license-file LICENSE --copyright "${COPYRIGHT}"
      echo "Sorry, module path is not yet supported"
    else
      printf "Unknown platform %s\n. Abort." "${PLATFORM}"
      exit
    fi
}

function build_for_class_path() {
  # copy the fat JAR
  cp $BUILD_DIR/$JAR_NAME $PACKAGE_DIR
  if [[ "$PLATFORM" == "Linux" ]]; then
    # setup Linux CLI
    jpackage --name "${APP_NAME}" \
      --input "${PACKAGE_DIR}" \
      --main-jar "${JAR_NAME}" \
      --app-version "${VERSION}" \
      --icon "${ICON}.ico" \
      --description "${DESCRIPTION}" \
      --linux-menu-group Science \
      --linux-shortcut \
      --linux-package-name "${CMD_NAME}" \
      --vendor "${VENDOR}" \
      --license-file LICENSE \
      --copyright "${COPYRIGHT}"
#--main-class "org.monarchinitiative.hpo_case_annotator.app.App" \
  elif [[ "$PLATFORM" == "Osx" ]]; then
    # setup OSX CLI
    jpackage --name "${APP_NAME}" \
    --input "${PACKAGE_DIR}" \
    --main-jar "${JAR_NAME}" \
    --app-version "${VERSION}" \
    --description "${DESCRIPTION}" \
    --mac-package-name "${CMD_NAME}" \
    --vendor "${VENDOR}" \
    --license-file LICENSE \
    --copyright "${COPYRIGHT}"
#    --icon "${ICON}.icns" \
  elif [[ "$PLATFORM" == "Windows" ]]; then
     # setup OSX CLI
        jpackage --name "${APP_NAME}" \
        --input "${PACKAGE_DIR}" \
        --main-jar "${JAR_NAME}" \
        --app-version "${VERSION}" \
        --description "${DESCRIPTION}" \
        --windows-package-name "${CMD_NAME}" \
        --vendor "${VENDOR}" \
        --license-file LICENSE \
        --copyright "${COPYRIGHT}" \
        --win-console
    #    --icon "${ICON}.icns" \
  else
    printf "Unknown platform %s\n. Abort." "${PLATFORM}"
    exit
  fi
}

##
# Dynamic variables
#
PLATFORM=$(detect_platform)
PACKAGE="classpath"
#PACKAGE="modular"


# 1. Build
printf "Building hpoworkbench\n"
./mvnw clean package


# 2. Prepare packaging folder
PACKAGE_DIR=/tmp/hpoworkbench_app
printf "Creating temporary directory at %s\n" ${PACKAGE_DIR}
mkdir -p $PACKAGE_DIR

# 3. Package for platform and package type
if [[ "$PACKAGE" == "modular" ]]; then
  printf "Packaging modular hpoworkbench for %s\n" "${PLATFORM}"
  build_for_module_path

elif [[ "$PACKAGE" == "classpath" ]]; then
  printf "Packaging classpath hpoworkbench for %s\n" "${PLATFORM}"
  build_for_class_path

else
  printf "\nUnknown packaging type '%s'\n" "${PACKAGE}"
fi

# 4. Clean up the packaging folder
printf "Removing the temporary directory %s\n" ${PACKAGE_DIR}
rm -r $PACKAGE_DIR
printf "Done!\n"