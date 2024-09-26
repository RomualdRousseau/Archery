package com.github.romualdrousseau.any2json.commons.python;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PythonManager.class);

    public PythonManager(final String moduleName) throws IOException {
        final var prop = new Properties();
        prop.load(this.findPropertiesFile());

        this.modulePath = this.getModulePath(prop.getProperty(moduleName + ".module-path"));
        this.mainEntry = prop.getProperty(moduleName + ".module-main", "main.py");
        this.hasVirtualEnv = prop.getProperty(moduleName + ".virtual-env", "false").equals("true");
        this.virtualEnvPath = this.getVirtualEnvPath(prop.getProperty(moduleName + ".virtual-env-path", ".venv"));
        this.hasDependencies = prop.getProperty(moduleName + ".dependencies", "false").equals("true");
    }

    public PythonManager enableVirtualEnv() throws IOException, InterruptedException {
        if (this.virtualEnvPath.toFile().exists()) {
            return this;
        }

        LOGGER.info("venv: Create a new virtual environment");

        final ProcessBuilder processBuilder = new ProcessBuilder("python", "-m", "venv", this.virtualEnvPath.toString());
        processBuilder.directory(this.modulePath.toFile());
        processBuilder.inheritIO();
        processBuilder.redirectErrorStream(true);
        processBuilder.start().waitFor();
        return this;
    }

    public PythonManager installDependencies() throws IOException, InterruptedException {
        if (this.isRequirementsInstalled()) {
            return this;
        }

        LOGGER.info("pip: Install and update all dependencies");

        final ProcessBuilder processBuilder = new ProcessBuilder(this.getPipScript(), "install", "-r",
                "requirements.txt");
        processBuilder.directory(this.modulePath.toFile());
        processBuilder.inheritIO();
        processBuilder.redirectErrorStream(true);
        processBuilder.start().waitFor();

        final var lockFile = this.modulePath.resolve("requirements.lock").toFile();
        lockFile.createNewFile();

        return this;
    }

    public PythonManager setEnviroment(final Map<String, String> environment) {
        this.environment = environment;
        return this;
    }

    public Process run(final String... args) throws IOException, InterruptedException {
        if (this.hasVirtualEnv) {
            this.enableVirtualEnv();
        }

        if (this.hasDependencies) {
            this.installDependencies();
        }

        final var command = Stream.of(List.of(this.getPythonScript(), this.mainEntry), List.of(args))
                .flatMap(Collection::stream).toList();
        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(this.modulePath.toFile());
        processBuilder.redirectErrorStream(true);

        if (this.environment != null || this.environment.size() > 0) {
            final var env = processBuilder.environment();
            this.environment.forEach((k, v) -> env.put(k, v));
        }

        LOGGER.info("python: Call {} with args: {}", this.mainEntry, args);

        return processBuilder.start();
    }

    private InputStream findPropertiesFile() throws IOException {
        final var userDir = System.getProperty("user.dir");
        return this.getPathIfExists(Path.of(userDir, "python4j.properties"))
                .or(() -> this.getPathIfExists(Path.of(userDir, "classes", "python4j.properties")))
                .flatMap(this::pathToStream)
                .or(() -> this.resolveResourceAsStream("python4j.properties"))
                .orElseThrow(() -> PythonManager.panicAndAbort("python4j.properties"));
    }

    private boolean isRequirementsInstalled() throws IOException {
        final var requireFile = this.modulePath.resolve("requirements.txt").toFile();
        if (!requireFile.exists()) {
            return false;
        }

        final var lockFile = this.modulePath.resolve("requirements.lock").toFile();
        if (lockFile.exists()) {
            if (requireFile.lastModified() < lockFile.lastModified()) {
                return true;
            }
            lockFile.delete();
        }

        return false;
    }

    private String getPythonScript() {
        if (this.hasVirtualEnv) {
            return this.getScriptPath("bin/python")
                    .or(() -> this.getScriptPath("Scripts/python.exe"))
                    .orElseThrow(() -> PythonManager.panicAndAbort("python"))
                    .toString();
        } else {
            return "python";
        }
    }

    private String getPipScript() {
        if (this.hasVirtualEnv) {
            return this.getScriptPath("bin/pip")
                    .or(() -> this.getScriptPath("Scripts/pip.exe"))
                    .orElseThrow(() -> PythonManager.panicAndAbort("pip"))
                    .toString();
        } else {
            return "pip";
        }
    }

    private Optional<Path> getScriptPath(final String pathName) {
        return this.getPathIfExists(this.virtualEnvPath.resolve(pathName));
    }

    private Path getModulePath(final String moduleName) {
        final var userDir = System.getProperty("user.dir");
        return this.getPathIfExists(Path.of(userDir, moduleName))
                .or(() -> this.getPathIfExists(Path.of(userDir, "classes", moduleName)))
                .orElseThrow(() -> PythonManager.panicAndAbort(moduleName));
    }

    private Path getVirtualEnvPath(String virtualEnvPath) {
        if (Path.of(virtualEnvPath).isAbsolute()) {
            return Path.of(virtualEnvPath);
        } else {
            return this.modulePath.resolve(virtualEnvPath);
        }
    }

    private Optional<InputStream> pathToStream(final Path x) {
        try {
            return Optional.of(Files.newInputStream(x));
        } catch (final IOException e) {
            return Optional.empty();
        }
    }

    private Optional<InputStream> resolveResourceAsStream(final String resourceName) {
        final InputStream resource = this.getClass().getClassLoader().getResourceAsStream(resourceName);
        if (resource == null) {
            LOGGER.debug("module: {} not found", resourceName);
            return Optional.empty();
        }
        LOGGER.debug("module: {} found at {}", resourceName, resource);
        return Optional.of(resource);
    }

    private Optional<Path> getPathIfExists(final Path path) {
        if (!path.toFile().exists()) {
            LOGGER.debug("module: {} not found at {}", path.getFileName(), path);
            return Optional.empty();
        }
        LOGGER.debug("module: {} found at {}", path.getFileName(), path);
        return Optional.of(path);
    }

    private static RuntimeException panicAndAbort(final String name) {
        LOGGER.error("module: {} not found, abort ...", name);
        return new RuntimeException(String.format("%s not found, abort ...", name));
    }

    private final Path modulePath;
    private final String mainEntry;
    private final boolean hasVirtualEnv;
    private final Path virtualEnvPath;
    private final boolean hasDependencies;
    private Map<String, String> environment = null;
}
