/*
 * (C) Piter.NL
 */
//---
package nl.piter.vterm.sys;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.*;

/**
 * Simple URI based FS util.
 * Implementation is using normalized URIs to void filesystem depending paths.
 */
@Slf4j
public class SysFS {

    public URI resolveFileURI(String subPath) {
        URI uri = Paths.get(subPath).toAbsolutePath().toUri().normalize();
        log.debug("resolveFileURI():'{}'=>'{}'", subPath, uri);
        return uri;
    }

    public Path resolvePath(URI pathUri) {
        Path path = Paths.get(pathUri);
        log.debug("resolvePath():{} => '{}'", pathUri, path);
        return path;
    }

    public void mkdirs(URI pathUri) throws IOException {
        Path paths = resolvePath(pathUri).toAbsolutePath();
        log.debug("mkdirs():{}", paths);
        Files.createDirectories(paths);
    }

    public void mkdir(URI pathUri) throws IOException {
        Path path = resolvePath(pathUri).toAbsolutePath();
        log.debug("mkdir():{}", path);
        Files.createDirectory(path);
    }

    public InputStream newInputStream(URI pathUri) throws IOException {
        log.debug("newInputStream():{}", pathUri);
        return Files.newInputStream(this.resolvePath(pathUri), READ);
    }

    public OutputStream newOutputStream(URI pathUri) throws IOException {
        log.debug("newOutputStream():{}", pathUri);
        return Files.newOutputStream(this.resolvePath(pathUri), CREATE, WRITE);
    }

}
