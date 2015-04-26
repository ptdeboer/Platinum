///*
// * Copyright 2012-2014 Netherlands eScience Center.
// *
// * Licensed under the Apache License, Version 2.0 (the "License").
// * You may not use this file except in compliance with the License.
// * You may obtain a copy of the License at the following location:
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
// * ---
// */
//// source:
//
//package nl.esciencecenter.vbrowser.vrs.node;
//
//import java.nio.file.attribute.BasicFileAttributes;
//import java.nio.file.attribute.FileTime;
//
//import nl.esciencecenter.ptk.presentation.Presentation;
//import nl.esciencecenter.vbrowser.vrs.io.VFSFileAttributes;
//
///**
// * BasiFileAttributes adapter.
// */
//public class FileAttributes implements VFSFileAttributes {
//  protected BasicFileAttributes attrs;
//
//  protected FileAttributes(BasicFileAttributes attrs) {
//      this.attrs = attrs;
//  }
//
//  @Override
//  public boolean isSymbolicLink() {
//      return attrs.isSymbolicLink();
//  }
//
//  public boolean isHidden() {
//      return false;
//  }
//
//  public java.util.Date getModificationTimeDate() {
//      FileTime time = this.lastModifiedTime();
//      if (time == null) {
//          return null;
//      }
//      return Presentation.createDate(time);
//  }
//
//  public java.util.Date getCreationTimeDate() {
//      FileTime time = this.creationTime();
//      if (time == null) {
//          return null;
//      }
//      return Presentation.createDate(time);
//  }
//
//  public java.util.Date getLastAccessTimeDate() {
//      FileTime time = this.lastAccessTime();
//      if (time == null) {
//          return null;
//      }
//      return Presentation.createDate(time);
//  }
//
//  @Override
//  public boolean isOther() {
//      return !(isRegularFile() || isDirectory() || isSymbolicLink());
//  }
//
//  @Override
//  public Object fileKey() {
//      return attrs.fileKey();
//  }
//
//  @Override
//  public FileTime lastModifiedTime() {
//      return attrs.lastModifiedTime();
//  }
//
//  @Override
//  public FileTime lastAccessTime() {
//      return attrs.lastAccessTime();
//  }
//
//  @Override
//  public FileTime creationTime() {
//      return attrs.creationTime();
//  }
//
//  @Override
//  public boolean isRegularFile() {
//      return attrs.isRegularFile();
//  }
//
//  @Override
//  public boolean isDirectory() {
//      return attrs.isDirectory();
//  }
//
//  @Override
//  public long size() {
//      return attrs.size();
//  }
//
//  @Override
//  public boolean isLocal() {
//      return false;
//  }
//
//  @Override
//  public boolean isRemote() {
//      return false;
//  }
//
// }
