/*
 * Copyright (c) 2001-2017, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Additionally licensed with:
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spf4j.io.compress;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.spf4j.io.BufferedInputStream;
import org.spf4j.io.Streams;
import org.spf4j.recyclable.impl.ArraySuppliers;

/**
 * @author zoly
 */
@Beta
@SuppressFBWarnings("AFBR_ABNORMAL_FINALLY_BLOCK_RETURN")
@ParametersAreNonnullByDefault
public final class Compress {

  private Compress() {
  }

  /**
   * Zip a file or folder.
   * @param fileOrFolderToCompress file or folder to compress.
   * @return the Path of the compressed file. It will created in the same folder as the input parent.
   * @throws IOException
   */
  @Nonnull
  public static Path zip(final Path fileOrFolderToCompress) throws IOException {
    Path parent = fileOrFolderToCompress.getParent();
    if (parent == null) {
      throw new IllegalArgumentException("Not a file: " + fileOrFolderToCompress);
    }
    Path destFile = parent.resolve(fileOrFolderToCompress.getFileName() + ".zip");
    zip(fileOrFolderToCompress, destFile);
    return destFile;
  }

  /**
   * Zip a file or folder.
   * @param fileOrFolderToCompress file or folder to compress.
   * @param destFile the destination zip file.
   * @throws IOException
   */
  @Nonnull
  public static void zip(final Path fileOrFolderToCompress,
          final Path destFile) throws IOException {
    zip(fileOrFolderToCompress, destFile, (p) -> true);
  }

  /**
   * Zip a file or folder.
   * @param fileOrFolderToCompress file or folder to compress.
   * @param destFile the destination zip file.
   * @throws IOException
   */
  @Nonnull
  public static void zip(final Path fileOrFolderToCompress,
          final Path destFile, final Predicate<Path> filter) throws IOException {
    Path parent = destFile.getParent();
    if (parent == null) {
      throw new IllegalArgumentException("Parent is null for: " + fileOrFolderToCompress);
    }
    Path tmpFile = Files.createTempFile(parent, ".", "tmp");
    try {
      Path relativePath;
      if (Files.isDirectory(fileOrFolderToCompress)) {
        relativePath = fileOrFolderToCompress;
      } else {
        relativePath = fileOrFolderToCompress.getParent();
      }
      try (BufferedOutputStream fos = new BufferedOutputStream(Files.newOutputStream(tmpFile));
              ZipOutputStream zos = new ZipOutputStream(fos, StandardCharsets.UTF_8)) {
          try (Stream<Path> ws = Files.walk(fileOrFolderToCompress)) {
            ws.forEach((path) -> {
              if (Files.isDirectory(path)) {
                return;
              }
              if (!filter.test(path)) {
                return;
              }
              String fileName = relativePath.relativize(path).toString();
              try (InputStream in = new BufferedInputStream(Files.newInputStream(path),
                      8192, ArraySuppliers.Bytes.TL_SUPPLIER)) {
                ZipEntry ze = new ZipEntry(fileName);
                zos.putNextEntry(ze);
                Streams.copy(in, zos);
              } catch (IOException ex) {
                throw new UncheckedIOException("Error compressing " + path, ex);
              }
            });
          }
      }
      Files.move(tmpFile, destFile,
              StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(tmpFile);
    }
  }

  /**
   * Copy file atomic.
   * file will be written to a tmp file in the destination folder, and atomically renamed (if file system supports)
   * @param source
   * @param destinationFile
   * @throws IOException
   */
  public static void copyFileAtomic(final Path source, final Path destinationFile) throws IOException {
    Path parent = destinationFile.getParent();
    if (parent == null) {
      throw new IllegalArgumentException("Destination " + destinationFile + " is not a file");
    }
    Path tmpFile = Files.createTempFile(parent, ".", null);
    try {
      try (InputStream in = new BufferedInputStream(Files.newInputStream(source),
              8192, ArraySuppliers.Bytes.TL_SUPPLIER);
              OutputStream os = new BufferedOutputStream(Files.newOutputStream(tmpFile))) {
        Streams.copy(in, os);
      }
      Files.move(tmpFile, destinationFile,
              StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(tmpFile);
    }
  }


  /**
   * Unzip a zip archive to same folder.
   * @param zipFile
   * @return list of unzipped files.
   * @throws IOException
   */
  @Nonnull
  public static List<Path> unzip(final Path zipFile) throws IOException {
    Path parent = zipFile.getParent();
    if (parent == null) {
      throw new IllegalArgumentException("File " + zipFile + " cannot be unzipped to null parent folder");
    }
    return unzip(zipFile, parent);
  }

  /**
   * Unzip a zip file to a destination folder.
   * @param zipFile
   * @param destinationDirectory
   * @return the list of files that were extracted.
   * @throws IOException in case extraction fails for whatever reason.
   */
  @Nonnull
  public static List<Path> unzip(final Path zipFile, final Path destinationDirectory) throws IOException {
    return unzip(zipFile, destinationDirectory, (p) -> true);
  }

  /**
   * Unzip a zip file to a destination folder.
   * @param zipFile
   * @param destinationDirectory
   * @return the list of files that were extracted.
   * @throws IOException in case extraction fails for whatever reason.
   */
  @Nonnull
  public static List<Path> unzip(final Path zipFile, final Path destinationDirectory,
          final Predicate<Path> filter) throws IOException {
    if (!Files.exists(destinationDirectory)) {
      Files.createDirectories(destinationDirectory);
    }
    if (!Files.isDirectory(destinationDirectory)) {
      throw new IllegalArgumentException("Destination " + destinationDirectory + " must be a directory");
    }
    final List<Path> response = new ArrayList<>();
    URI zipUri = URI.create("jar:" + zipFile.toUri().toURL());
    synchronized (zipUri.toString().intern()) { // newFileSystem fails if already one there...
      try (FileSystem zipFs = FileSystems.newFileSystem(zipUri, Collections.emptyMap())) {
        for (Path root : zipFs.getRootDirectories()) {
          Path dest =  destinationDirectory.resolve(root.toString().substring(1));
          Files.walkFileTree(root, new SimpleFileVisitor<Path>() {

            private final Set<Path> created = new HashSet<>();

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                    throws IOException {
              if (!filter.test(file)) {
                return FileVisitResult.CONTINUE;
              }
              Path destination = dest.resolve(root.relativize(file).toString());
              Path parent = destination.getParent();
              if (parent != null && created.add(parent)) {
                Files.createDirectories(parent);
              }
              copyFileAtomic(file, destination);
              response.add(destination);
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
              if (!filter.test(dir)) {
                return FileVisitResult.CONTINUE;
              }
              return FileVisitResult.CONTINUE;
            }
          });
        }
      } catch (IOException | RuntimeException ex) {
        for (Path path : response) {
          try {
            Files.delete(path);
          } catch (IOException | RuntimeException ex2) {
            ex.addSuppressed(ex2);
          }
        }
        throw ex;
      }
    }
    return response;
  }

  @Nonnull
  public static List<Path> unzip2(final Path zipFile, final Path destinationDirectory) throws IOException {
    return unzip2(zipFile, destinationDirectory, (p) -> true);
  }

  @Nonnull
  @SuppressFBWarnings("PATH_TRAVERSAL_IN")
  public static List<Path> unzip2(final Path zipFile, final Path destDir,
          final Predicate<Path> filter) throws IOException {
    final List<Path> response = new ArrayList<>();
    final Set<Path> created = new HashSet<>();
    try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null) {
        String fName = zipEntry.getName();
        if (fName.contains("..")) {
          throw new IllegalArgumentException("Backreference " + fName + " not allowed in " + zipFile);
        }
        if (filter.test(Paths.get(fName))) {
          Path newFile = destDir.resolve(fName);
          Path parent = newFile.getParent();
          if (parent != null && created.add(parent)) {
            Files.createDirectories(parent);
          }
          try (OutputStream fos = new BufferedOutputStream(Files.newOutputStream(newFile))) {
            Streams.copy(zis, fos);
          }
          response.add(newFile);
        }
        zipEntry = zis.getNextEntry();
      }
      zis.closeEntry();
    }
    return response;
  }



}
