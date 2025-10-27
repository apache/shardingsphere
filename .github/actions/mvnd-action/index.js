import * as core from '@actions/core';
import path from 'path';
import fs from 'fs/promises';
import os from 'os';
import StreamZip from 'node-stream-zip';
import * as exec from '@actions/exec';

/**
 * Fetches a platform-specific mvnd binary from a URL and saves it locally
 *
 * @param {string} baseUrl - Base URL where the binary can be downloaded from
 * @param {string} version - Version of mvnd to download
 * @param {string} saveDir - Directory path where the binary should be saved
 * @returns {Promise<[string, string]>} Path to the extracted binary directory and the installation directory
 * @throws {Error} If the download or extraction fails
 */
const fetchAndSaveBinary = async (baseUrl, version, saveDir) => {
  try {
    let platformSuffix;

    switch(process.platform) {
      case 'win32':
        platformSuffix = 'windows-amd64';
        break;
      case 'darwin':
        platformSuffix = process.arch === 'arm64' ? 'darwin-aarch64' : 'darwin-amd64';
        break;
      case 'linux':
        platformSuffix = 'linux-amd64';
        break;
      default:
        core.warning(`Unknown platform detected: ${process.platform}. Defaulting to fetching linux variant`)
        platformSuffix = 'linux-amd64';
        break;
    }

    const directoryName = `maven-mvnd-${version}-${platformSuffix}`;
    const url = new URL(baseUrl);
    url.pathname += `${version}/${directoryName}.zip`;

    core.info(`Fetching binary from: ${url}`);
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Failed to fetch: ${response.status} ${response.statusText}`);
    }
    const arrayBuffer = await response.arrayBuffer();
    const buffer = Buffer.from(arrayBuffer);
    core.info(`Writing zip to file: ${saveDir}.zip`);
    await fs.writeFile(`${saveDir}.zip`, buffer);

    core.info(`Extracting from zip file: ${directoryName} -> ${saveDir}`);

    const zip = new StreamZip.async({ file: `${saveDir}.zip` });

    zip.on('extract', (entry, file) => {
      core.debug(`Extracted ${entry.name} to ${file}`);
    });
    const count = await zip.extract(null, saveDir);
    core.debug(`Extracted ${count} entries`);
    await zip.close();

    // Remove the zip file after extraction
    await fs.unlink(`${saveDir}.zip`);
    core.debug('Removed temporary zip file');

    // On macOS, remove quarantine flags from extracted files
    if (process.platform === 'darwin') {
      core.info('Removing quarantine flags from extracted files on macOS');
      const extractedPath = path.resolve(saveDir, directoryName);
      try {
        await exec.exec('xattr', ['-rd', 'com.apple.quarantine', extractedPath]);
        core.debug('Successfully removed quarantine flags');
      } catch (error) {
        core.warning(`Failed to remove quarantine flags: ${error.message}. mvnd migth not work correctly!`);
        // Continue execution since this is not critical
      }
    }

    // Make everything in the binary directory executable
    const binaryDirectoryPath = path.resolve(saveDir, `${directoryName}/bin`)
    const files = await fs.readdir(binaryDirectoryPath);
    for (const file of files) {
      const filePath = path.join(binaryDirectoryPath, file);
      await fs.chmod(filePath, 0o755); // rwxr-xr-x permissions
      core.info(`Made ${filePath} executable`);
    }
    return [binaryDirectoryPath, path.resolve(saveDir, directoryName)];
  } catch (error) {
    core.error(`Failed to fetch and save mvnd binary: ${error.message}`);
    throw error;
  }
};

const fileExistsAndIsAccessible = async (path) => {
  try {
    await fs.access(path, fs.constants.F_OK);
    return true;
  } catch (error) {
    return false;
  }
};

const createDirectoryIfNecessary = async (targetPath) => {
  const directory = path.dirname(targetPath);
  await fs.mkdir(directory, { recursive: true });
};

const addDirectoryToPath = (directoryPath) => {
  const absolutePath = path.resolve(directoryPath);

  // GitHub Actions specific way to modify PATH for all subsequent steps
  // See: https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#adding-a-system-path
  core.addPath(absolutePath);

  core.info(`Added ${absolutePath} to the PATH`);
};

const getTempDirectory = () => {
  // Prefer GitHub Actions temp directory if available, otherwise fallback to OS temp dir
  return process.env.RUNNER_TEMP || os.tmpdir();
};

try {
  const baseUrl = core.getInput('hosted-binary-url');
  const version = core.getInput('version');
  const saveDir = core.getInput('cache-directory-override') ?
    path.resolve(core.getInput('cache-directory-override')) :
    path.resolve(getTempDirectory(), 'mvnd-cache');
  const binaryName = process.platform === 'win32' ? 'mvnd.exe' : 'mvnd';
  const fullSavePath = path.join(saveDir, `/bin/${binaryName}`);

  core.info(`Resolved target location for binary is: ${fullSavePath}`);

  if (await fileExistsAndIsAccessible(fullSavePath)) {
    core.info('File seems to already exist at the target location. Skipping fetch');
    addDirectoryToPath(saveDir);
    core.setOutput('cached-binary-path', fullSavePath);
    // Set the directory path output as well
    core.setOutput('cached-directory-path', path.dirname(path.dirname(fullSavePath)));
  } else {
    core.info(`No existing binary found at ${fullSavePath}`);
    await createDirectoryIfNecessary(saveDir);

    // The binary directory path depends on the architecture and OS
    const [binaryDirectoryPath, installationDirectoryPath] = await fetchAndSaveBinary(baseUrl, version, saveDir);
    core.info(`Binary directory path: ${binaryDirectoryPath}`);
    core.info(`Installation directory path: ${installationDirectoryPath}`);

    addDirectoryToPath(binaryDirectoryPath);
    core.setOutput('cached-binary-path', path.resolve(binaryDirectoryPath, binaryName));
    // Set the directory path output as well
    core.setOutput('cached-directory-path', installationDirectoryPath);
  }
} catch (error) {
  core.setFailed(error.message);
}
