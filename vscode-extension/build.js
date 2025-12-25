const fs = require('fs');
const path = require('path');

const rootDir = path.join(__dirname, '..');
const targetDir = path.join(__dirname, 'python-core', 'src'); // 💡 src 폴더 구조 유지

console.log('🏗️  Starting standardized build process...');

// 1. Clean and Create
if (fs.existsSync(path.join(__dirname, 'python-core'))) {
    fs.rmSync(path.join(__dirname, 'python-core'), { recursive: true });
}
fs.mkdirSync(targetDir, { recursive: true });

// 2. Copy source files into python-core/src
console.log('📂 Copying Python source code into src/...');
const srcDir = path.join(rootDir, 'src');
copyRecursiveSync(srcDir, targetDir);

// 3. Copy requirements to python-core/ (for venv install)
fs.copyFileSync(path.join(rootDir, 'requirements.txt'), path.join(__dirname, 'python-core', 'requirements.txt'));

console.log('✅ Standardized build finished!');

function copyRecursiveSync(src, dest) {
    if (fs.statSync(src).isDirectory()) {
        if (!fs.existsSync(dest)) fs.mkdirSync(dest);
        fs.readdirSync(src).forEach(child => copyRecursiveSync(path.join(src, child), path.join(dest, child)));
    } else {
        fs.copyFileSync(src, dest);
    }
}
