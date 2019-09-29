package nl.piter.vterm.sys;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simple URI based FS util.
 * Implementation is using normalized URIs to void filesystem depending paths.
 */
@Slf4j
public class  SysFS {

    public URI resolveFileURI(String subPath) {
        URI uri = Paths.get(subPath).toAbsolutePath().toUri().normalize();
        log.debug("resolveFileURI():'{}'=>'{}'",subPath,uri);
        return uri;
    }

    public Path resolvePath(URI propFileUri) {
        Path path = Paths.get(propFileUri);
        log.debug("resolvePath():{} => '{}'", propFileUri, path);
        return path;
    }

    public void mkdirs(URI pathUri) throws IOException {
        Path paths = resolvePath(pathUri).toAbsolutePath();
        log.debug("mkdirs():{}",paths);
        Files.createDirectories(paths);
    }

    public void mkdir(URI pathUri) throws IOException {
        Path path = resolvePath(pathUri).toAbsolutePath();
        log.debug("mkdir():{}",path);
        Files.createDirectory(path);
    }

    public InputStream newInputStream(URI propFileUri) throws IOException {
        log.debug("newInputStream():{}",propFileUri);
        return Files.newInputStream(this.resolvePath(propFileUri));
    }

    public OutputStream newOutputStream(URI propFileUri) throws IOException {
        log.debug("newOutputStream():{}",propFileUri);
        return Files.newOutputStream(this.resolvePath(propFileUri));
    }

}
