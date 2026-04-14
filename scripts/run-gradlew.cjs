/**
 * 루트에서 OS에 맞는 Gradle Wrapper를 실행하고 인자를 그대로 넘깁니다.
 * 사용: node scripts/run-gradlew.cjs test
 */
const { spawnSync } = require('node:child_process');
const path = require('node:path');

const root = path.join(__dirname, '..');
const wrapper = path.join(
  root,
  process.platform === 'win32' ? 'gradlew.bat' : 'gradlew',
);
const args = process.argv.slice(2);

const result = spawnSync(wrapper, args, {
  cwd: root,
  stdio: 'inherit',
});

const code = result.status;
process.exit(code == null ? 1 : code);
