import { copyFile, constants } from 'node:fs/promises';
import { resolve } from 'node:path';

const distDir = resolve('dist');
const indexPath = resolve(distDir, 'index.html');
const notFoundPath = resolve(distDir, '404.html');

try {
  await copyFile(indexPath, notFoundPath, constants.COPYFILE_FICLONE);
  console.log('Created dist/404.html for SPA fallback.');
} catch (error) {
  console.warn('Unable to create 404.html fallback:', error);
}
