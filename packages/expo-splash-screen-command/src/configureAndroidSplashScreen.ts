import path from 'path';
import fs from 'fs-extra';
import chalk from 'chalk';
import { projectConfig } from '@react-native-community/cli-platform-android';

import { Mode } from './constants';

const DRAWABLES_CONFIGS = {
  drawable: {
    multiplier: 1,
  },
  'drawable-mdpi': {
    multiplier: 1,
  },
  'drawable-hdpi': {
    multiplier: 1.5,
  },
  'drawable-xhdpi': {
    multiplier: 2,
  },
  'drawable-xxhdpi': {
    multiplier: 3,
  },
  'drawable-xxxhdpi': {
    multiplier: 4,
  },
};
const FILENAMES = {
  SPLASH_SCREEN_DRAWABLE: 'splashscreen_image.png',
  SPLASH_SCREEN_XML: 'splashscreen.xml',
  COLORS: 'colors_splashscreen.xml',
  STYLES: 'styles_splashscreen.xml',
  ANDROID_MANIFEST: 'AndroidManifest.xml',
};
const TEMPLATES_COMMENTS = {
  LINE: `<!-- THIS LINE HANDLED BY 'expo-splash-screen' COMMAND AND IT'S DISCOURAGED TO MODIFY IT MANUALLY -->`,
  TOP: `<!--\n\n    THIS FILE IS CREATED BY 'expo-splash-screen' COMMAND AND IT'S FRAGMENTS ARE HANDLED BY IT\n\n-->`,
  TOP_NO_MANUAL_MODIFY: `<!--\n\n    THIS FILE IS CREATED BY 'expo-splash-screen' COMMAND AND IT'S DISCOURAGED TO MODIFY IT MANUALLY\n\n-->`,
  ANDROID_MANIFEST: `<!-- THIS ACTIVITY'S 'android:theme' ATTRIBUTE IS HANDLED BY 'expo-splash-screen' COMMAND AND IT'S DISCOURAGED TO MODIFY IT MANUALLY -->`,
};

/**
 * Modifies file's content if either `replacePattern` or `insertPattern` matches.
 * If `replacePatten` matches `replaceContent` is used, otherwise if `insertPattern` matches `insertContent` is used.
 * @returns `true` if the file's content is changes, `false` otherwise.
 */
async function replaceOrInsertInFile(
  filePath: string,
  {
    replaceContent,
    replacePattern,
    insertContent,
    insertPattern,
  }: {
    replaceContent: string;
    replacePattern: RegExp | string;
    insertContent: string;
    insertPattern: RegExp | string;
  }
): Promise<boolean> {
  return (
    (await replaceInFile(filePath, { replaceContent, replacePattern })) ||
    (await insertToFile(filePath, { insertContent, insertPattern }))
  );
}

/**
 * Tries to do following actions:
 * - when file doesn't exist or is empty - create it with given fileContent,
 * - when file does exist and contains provided replacePattern - replace replacePattern with replaceContent,
 * - when file does exist and doesn't contain provided replacePattern - insert given insertContent before first match of insertPattern,
 * - when insertPattern does not occur in the file - append insertContent to the end of the file.
 */
async function writeOrReplaceOrInsertInFile(
  filePath: string,
  {
    fileContent,
    replaceContent,
    replacePattern,
    insertContent,
    insertPattern,
  }: {
    fileContent: string;
    replaceContent: string;
    replacePattern: RegExp | string;
    insertContent: string;
    insertPattern: RegExp | string;
  }
) {
  if (!(await fs.pathExists(filePath)) || !/\S/m.test(await fs.readFile(filePath, 'utf8'))) {
    return await writeToFile(filePath, fileContent);
  }

  if (
    await replaceOrInsertInFile(filePath, {
      replaceContent,
      replacePattern,
      insertContent,
      insertPattern,
    })
  ) {
    return;
  }

  const originalFileContent = await fs.readFile(filePath, 'utf8');
  return await fs.writeFile(filePath, `${originalFileContent}${insertPattern}`);
}

/**
 * Overrides or creates file (with possibly missing directories) with given content.
 */
async function writeToFile(filePath: string, fileContent: string) {
  const fileDirnamePath = path.dirname(filePath);
  if (!(await fs.pathExists(fileDirnamePath))) {
    await fs.mkdirp(fileDirnamePath);
  }
  return await fs.writeFile(filePath, fileContent);
}

/**
 * @returns `true` if replacement is successful, `false` otherwise.
 */
async function replaceInFile(
  filePath: string,
  { replaceContent, replacePattern }: { replaceContent: string; replacePattern: string | RegExp }
) {
  const originalFileContent = await fs.readFile(filePath, 'utf8');
  const replacePatternOccurrence = originalFileContent.search(replacePattern);
  if (replacePatternOccurrence !== -1) {
    await fs.writeFile(filePath, originalFileContent.replace(replacePattern, replaceContent));
    return true;
  }
  return false;
}

/**
 * @returns `true` if insertion is successful, `false` otherwise.
 */
async function insertToFile(
  filePath: string,
  { insertContent, insertPattern }: { insertContent: string; insertPattern: RegExp | string }
) {
  const originalFileContent = await fs.readFile(filePath, 'utf8');
  const insertPatternOccurrence = originalFileContent.search(insertPattern);
  if (insertPatternOccurrence !== -1) {
    await fs.writeFile(
      filePath,
      `${originalFileContent.slice(
        0,
        insertPatternOccurrence
      )}${insertContent}${originalFileContent.slice(insertPatternOccurrence)}`
    );
    return true;
  }
  return false;
}

/**
 * Deletes all previous splash_screen_images and copies new one to desired drawable directory.
 * @see https://developer.android.com/training/multiscreen/screendensities
 */
async function configureSplashScreenDrawables(
  androidMainResPath: string,
  splashScreenImagePath: string
) {
  await Promise.all(
    Object.keys(DRAWABLES_CONFIGS)
      .map(drawableDirectoryName =>
        path.resolve(androidMainResPath, drawableDirectoryName, FILENAMES.SPLASH_SCREEN_DRAWABLE)
      )
      .map(async drawablePath => {
        if (await fs.pathExists(drawablePath)) {
          await fs.remove(drawablePath);
        }
      })
  );

  if (!(await fs.pathExists(path.resolve(androidMainResPath, 'drawable')))) {
    await fs.mkdir(path.resolve(androidMainResPath, 'drawable'));
  }
  await fs.copyFile(
    splashScreenImagePath,
    path.resolve(androidMainResPath, 'drawable', FILENAMES.SPLASH_SCREEN_DRAWABLE)
  );
}

async function configureColorsXML(androidMainResPath: string, splashScreenBackgroundColor: string) {
  await writeOrReplaceOrInsertInFile(path.resolve(androidMainResPath, 'values', FILENAMES.COLORS), {
    fileContent: `${TEMPLATES_COMMENTS.TOP}
<resources>
  <color name="splashscreen_background">${splashScreenBackgroundColor}</color> ${TEMPLATES_COMMENTS.LINE}
</resources>
`,
    replaceContent: `  <color name="splashscreen_background">${splashScreenBackgroundColor}</color> ${TEMPLATES_COMMENTS.LINE}\n`,
    replacePattern: /(?<=(?<openingTagLine>^.*?<resources>.*?$\n)(?<beforeLines>(?<beforeLine>^.*$\n)*?))(?<colorLine>^.*?(?<color><color name="splashscreen_background">.*<\/color>).*$\n)(?=(?<linesAfter>(?<afterLine>^.*$\n)*?)(?<closingTagLine>^.*?<\/resources>.*?$\n))/m,

    insertContent: `  <color name="splashscreen_background">${splashScreenBackgroundColor}</color> ${TEMPLATES_COMMENTS.LINE}\n`,
    insertPattern: /^(.*?)<\/resources>(.*?)$/m,
  });
}

async function configureDrawableXML(androidMainResPath: string, mode: Mode) {
  const nativeSplashScreen: string =
    mode !== Mode.NATIVE
      ? ''
      : `

  <item>
    <bitmap
      android:gravity="center"
      android:src="@drawable/splashscreen_image"
    />
  </item>`;

  await writeToFile(
    path.resolve(androidMainResPath, 'drawable', FILENAMES.SPLASH_SCREEN_XML),
    `${TEMPLATES_COMMENTS.TOP_NO_MANUAL_MODIFY}
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
  <item android:drawable="@color/splashscreen_background"/>${nativeSplashScreen}
</layer-list>
`
  );
}

async function configureStylesXML(androidMainResPath: string) {
  await writeOrReplaceOrInsertInFile(path.resolve(androidMainResPath, 'values', FILENAMES.STYLES), {
    fileContent: `${TEMPLATES_COMMENTS.TOP}
<resources>
  <style name="Theme.App.SplashScreen" parent="Theme.AppCompat.Light.NoActionBar"> ${TEMPLATES_COMMENTS.LINE}
    <item name="android:windowBackground">@drawable/splashscreen</item>  ${TEMPLATES_COMMENTS.LINE}
  </style>
</resources>
`,
    replaceContent: `    <item name="android:windowBackground">@drawable/splashscreen</item>  ${TEMPLATES_COMMENTS.LINE}\n`,
    replacePattern: /(?<=(?<styleNameLine>^.*?(?<styleName><style name="Theme\.App\.SplashScreen" parent=".*?">).*?$\n)(?<linesBeforeWindowBackgroundLine>(?<singleBeforeLine>^.*$\n)*?))(?<windowBackgroundLine>^.*?(?<windowBackground><item name="android:windowBackground">.*<\/item>).*$\n)(?=(?<linesAfterWindowBackgroundLine>(?<singleAfterLine>^.*$\n)*?)(?<closingTagLine>^.*?<\/style>.*?$\n))/m,

    insertContent: `  <style name="Theme.App.SplashScreen" parent="Theme.AppCompat.Light.NoActionBar">  ${TEMPLATES_COMMENTS.LINE}
    <item name="android:windowBackground">@drawable/splashscreen</item>  ${TEMPLATES_COMMENTS.LINE}
  </style>
`,
    insertPattern: /^(.*?)<\/resources>(.*?)$/m,
  });
}

async function configureAndroidManifestXML(androidMainPath: string) {
  const androidManifestPath = path.resolve(androidMainPath, 'AndroidManifest.xml');
  if (
    !(await replaceOrInsertInFile(androidManifestPath, {
      replaceContent: `android:theme="@style/Theme.App.SplashScreen"`,
      replacePattern: /(?<nameBeforeTheme>(?<=(?<application1>^.*?<application(.*|\n)*?)(?<activity1>^.*?<activity(.|\n)*?android:name="\.MainActivity"(.|\n)*?))(?<androidTheme1>android:theme=".*?"\s*?))|((?<=(?<application2>^.*?<application(.|\n)*?)(?<activity2>^.*?<activity(.|\n)*?))(?<androidTheme2>android:theme=".*?"\s*?)(?=((.|\n)*?android:name="\.MainActivity"(.|\n)*?)))/m,

      insertContent: `\n      android:theme="@style/Theme.App.SplashScreen"`,
      insertPattern: /(?<=(?<application>^.*?<application(.*|\n)*?)(?<activity>^.*?<activity))(?<activityAttributes>(.|\n)*?android:name="\.MainActivity"(.|\n)*?>)/m,
    })) ||
    !(await replaceOrInsertInFile(androidManifestPath, {
      replaceContent: `\n\n    ${TEMPLATES_COMMENTS.ANDROID_MANIFEST}\n`,
      replacePattern: RegExp(
        `(?<=(?<application>^.*?<application(.|\n)*?))([\n\t ])*(?<comment>${TEMPLATES_COMMENTS.ANDROID_MANIFEST.replace(
          /[-/\\^$*+?.()|[\]{}]/g,
          '\\$&'
        )})([\n\t ])*(?=(?<activity>(^.*?<activity)(.|\n)*?android:name="\.MainActivity"(.|\n)*?>))`,
        'm'
      ),

      insertContent: `\n    ${TEMPLATES_COMMENTS.ANDROID_MANIFEST}\n`,
      insertPattern: /(?<=(?<application>^.*?<application(.|\n)*?))(?<activity>(^.*?<activity)(.|\n)*?android:name="\.MainActivity"(.|\n)*?>)/m,
    }))
  ) {
    console.log(
      chalk.yellow(
        `${chalk.magenta(
          'AndroidManifest.xml'
        )} does not contain <activity /> entry for ${chalk.magenta(
          'MainActivity'
        )}. SplashScreen style will not be applied.`
      )
    );
  }
}

/**
 * Configures or creates splash screen's:
 * - background color
 * - xml drawable file
 * - style with theme including 'android:windowBackground'
 * - theme for activity in AndroidManifest.xml
 */
async function configureSplashScreenXMLs(
  androidMainPath: string,
  mode: Mode,
  splashScreenBackgroundColor: string
) {
  const androidMainResPath = path.resolve(androidMainPath, 'res');
  await Promise.all([
    configureColorsXML(androidMainResPath, splashScreenBackgroundColor),
    configureDrawableXML(androidMainResPath, mode),
    configureStylesXML(androidMainResPath),
    configureAndroidManifestXML(androidMainPath),
  ]);
}

/**
 * Injects specific code to MainApplication that would trigger SplashScreen.
 * TODO: make it work
 */
async function configureShowingSplashScreen(projectRootPath: string) {
  // const mainAndroidFilePath = projectConfig(projectRootPath)?.mainFilePath;
}

export default async function configureAndroidSplashScreen(imagePath: string, mode: Mode) {
  const splashScreenBackgroundColor = `#FFFFFF`;
  const projectRootPath = path.resolve();
  const androidMainPath = path.resolve(projectRootPath, 'android/app/src/main');

  return Promise.all([
    await configureSplashScreenDrawables(path.resolve(androidMainPath, 'res'), imagePath),
    await configureSplashScreenXMLs(androidMainPath, mode, splashScreenBackgroundColor),
    await configureShowingSplashScreen(projectRootPath),
  ]).then(() => {});
}
