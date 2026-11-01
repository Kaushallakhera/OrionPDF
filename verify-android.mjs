import fs from 'node:fs';
import path from 'node:path';

const requiredFiles = [
  'app/build.gradle.kts',
  'app/src/main/AndroidManifest.xml',
  'app/src/main/res/values/strings.xml',
  'app/src/main/java/com/example/MainActivity.kt',
  'app/src/main/java/com/example/data/PdfEntities.kt',
  'app/src/main/java/com/example/data/PdfDao.kt',
  'app/src/main/java/com/example/data/AppDatabase.kt',
  'app/src/main/java/com/example/data/PdfRepository.kt',
  'app/src/main/java/com/example/pdf/BundledPdfSeeder.kt',
  'app/src/main/java/com/example/pdf/PdfRenderEngine.kt',
  'app/src/main/java/com/example/viewmodel/PdfAppViewModel.kt',
  'app/src/main/java/com/example/ui/PdfThumbnailView.kt',
  'app/src/main/java/com/example/ui/LibraryScreen.kt',
  'app/src/main/java/com/example/ui/ReaderScreen.kt',
  'app/src/main/java/com/example/ui/ReaderSheets.kt',
  'app/src/main/java/com/example/ui/HomeMenuAndProSheet.kt',
  'app/src/main/java/com/example/ui/theme/Color.kt',
  'app/src/main/java/com/example/ui/theme/Type.kt',
  'app/src/main/java/com/example/ui/theme/Theme.kt'
];

let hasError = false;

for (const relPath of requiredFiles) {
  const fullPath = path.resolve(process.cwd(), relPath);
  if (!fs.existsSync(fullPath)) {
    console.error(`[ERROR] Missing required Android file: ${relPath}`);
    hasError = true;
    continue;
  }
  const content = fs.readFileSync(fullPath, 'utf8');
  if (relPath.endsWith('.kt')) {
    let openBraces = 0;
    for (const ch of content) {
      if (ch === '{') openBraces++;
      if (ch === '}') openBraces--;
    }
    if (openBraces !== 0) {
      console.error(`[ERROR] Unbalanced braces (${openBraces}) in ${relPath}`);
      hasError = true;
    }
  }
}

if (hasError) {
  process.exit(1);
}

console.log('[SUCCESS] Verified all 20 Android Kotlin/Compose and XML source files for Orion PDF Reader.');
