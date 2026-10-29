import { cpSync, mkdirSync, rmSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';
const root = dirname(fileURLToPath(import.meta.url));
const output = join(root, 'dist');
rmSync(output, { recursive: true, force: true });
mkdirSync(output);
for (const file of ['index.html', 'style.css', 'script.js', 'robots.txt', 'sitemap.xml', 'assets']) {
  cpSync(join(root, file), join(output, file), { recursive: true });
}
console.log('Static website built in dist/');
