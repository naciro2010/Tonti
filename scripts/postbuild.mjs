import { copyFile, writeFile, constants } from 'node:fs/promises';
import { resolve } from 'node:path';

const distDir = resolve('dist');
const indexPath = resolve(distDir, 'index.html');
const notFoundPath = resolve(distDir, '404.html');
const noJekyllPath = resolve(distDir, '.nojekyll');

try {
  await copyFile(indexPath, notFoundPath, constants.COPYFILE_FICLONE);
  console.log('Created dist/404.html for SPA fallback.');
} catch (error) {
  console.warn('Unable to create 404.html fallback:', error);
}

try {
  await writeFile(noJekyllPath, '');
  console.log('Created dist/.nojekyll for GitHub Pages.');
} catch (error) {
  console.warn('Unable to create .nojekyll:', error);
}
